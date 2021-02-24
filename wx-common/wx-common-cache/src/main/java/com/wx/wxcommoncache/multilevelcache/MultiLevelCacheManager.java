package com.wx.wxcommoncache.multilevelcache;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.transaction.TransactionAwareCacheDecorator;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 二级缓存 redis+caffeine
 * @author gh
 */
public class MultiLevelCacheManager implements CacheManager, InitializingBean {

    private final ConcurrentMap<String, Cache> cacheMap = new ConcurrentHashMap<>(16);

    private volatile Set<String> cacheNames = Collections.emptySet();
    /**
     * 创建Caffeine缓存---- 配置
     * expireAfterWrite：最后一次写入或访问后经过多久过期
     * initialCapacity：初始的缓存空间大小
     * maximumSize： 缓存的最大条数
     */
    private final Caffeine<Object, Object> cacheBuilder;

    @Nullable
    private CacheLoader<Object, Object> cacheLoader;

    /**
     * 创建redis缓存-----1.连接
     */
    private @Nullable RedisCacheWriter cacheWriter;

    /**
     *创建redis缓存-----2.默认配置
     */
    private final RedisCacheConfiguration defaultCacheConfig;

    /**
     * 一级缓存配置，提前加载
     */
    private final Map<String,CaffeineSpec> caffeineSpecConfigurations;


    /**
     * 二级缓存配置，提前加载
     */
    private final Map<String,RedisCacheConfiguration> redisCacheConfigurations;

    /**
     * 是否允许动态创建缓存，默认是true
     */
    private boolean dynamic = true;

    /**
     * 一级缓存缓存值是否允许为NULL
     */
    private boolean allowNullValues = true;

    /**
     * 缓存是否开启事务
     */
    private boolean transactionAware = false;


    @Override
    public void afterPropertiesSet() {
        initializeCaches();
    }


    /**
     * 初始化加载缓存
     */
    private void initializeCaches() {
        Collection<? extends Cache> caches = loadCaches();
        if(caches == null){
            return;
        }

        synchronized (this.cacheMap) {
            this.cacheNames = Collections.emptySet();
            this.cacheMap.clear();
            Set<String> cacheNames = new LinkedHashSet<>(caches.size());
            for (Cache cache : caches) {
                String name = cache.getName();
                this.cacheMap.put(name, cache);
                cacheNames.add(name);
            }
            this.cacheNames = Collections.unmodifiableSet(cacheNames);
        }
    }

    /**
     * 提前加载一部分缓存
     */
    protected Collection<? extends Cache> loadCaches() {

        if(checkInitCache()){

            List<Cache> caches = new LinkedList<>();
            String cacheName = null;
            for(Map.Entry<String,CaffeineSpec> entry : caffeineSpecConfigurations.entrySet()){
                cacheName = entry.getKey();
                caches.add(createMultiLevelCache(cacheName,entry.getValue(),redisCacheConfigurations.get(cacheName)));
            }
            return caches;
        }
        return null;
    }

    private boolean checkInitCache() {
        boolean needInitCache = !CollectionUtils.isEmpty(caffeineSpecConfigurations) && !CollectionUtils.isEmpty(redisCacheConfigurations)
                && caffeineSpecConfigurations.size() == redisCacheConfigurations.size();
        if(!needInitCache && !CollectionUtils.isEmpty(caffeineSpecConfigurations)){
            throw new RuntimeException("缓存配置出错，初始化缓存时，发现一级缓存和二级缓存的配置不一致!");
        }
        return needInitCache;
    }

    private MultiLevelCacheManager(RedisCacheWriter cacheWriter, RedisCacheConfiguration defaultCacheConfiguration,CaffeineSpec defaultCaffeineSpec,
                              boolean dynamic) {

        Assert.notNull(cacheWriter, "CacheWriter must not be null!");
        Assert.notNull(defaultCacheConfiguration, "DefaultCacheConfiguration must not be null!");
        this.cacheWriter = cacheWriter;
        this.caffeineSpecConfigurations = new LinkedHashMap<>();
        this.redisCacheConfigurations = new LinkedHashMap<>();
        this.dynamic = dynamic;
        this.defaultCacheConfig = defaultCacheConfiguration;
        cacheBuilder = Caffeine.from(defaultCaffeineSpec);
    }


    private MultiLevelCacheManager(RedisCacheWriter cacheWriter, RedisCacheConfiguration defaultCacheConfiguration,CaffeineSpec defaultCaffeineSpec,
                                   Map<String,CaffeineSpec> caffeineSpecConfigurations,Map<String,RedisCacheConfiguration> redisCacheConfigurations, boolean dynamic) {

        this(cacheWriter,defaultCacheConfiguration,defaultCaffeineSpec, dynamic);
        this.caffeineSpecConfigurations.putAll(caffeineSpecConfigurations);
        this.redisCacheConfigurations.putAll(redisCacheConfigurations);
    }


    @Override
    public Cache getCache(String name) {

        Cache cache = this.cacheMap.get(name);
        if (cache != null) {
            return cache;
        }
        //创建缓存
        Cache missingCache = getMissingCache(name);
        if (missingCache != null) {
            // Fully synchronize now for missing cache registration
            synchronized (this.cacheMap) {
                cache = this.cacheMap.get(name);
                if (cache == null) {
                    cache = missingCache;
                    this.cacheMap.put(name, cache);
                    updateCacheNames(name);
                }
            }
        }
        return cache;
    }


    @Override
    public Collection<String> getCacheNames() {
        return this.cacheNames;
    }




    /**
     * 修改已知缓存
     * @param allowNullValues 是否为 null
     */
    public void setAllowNullValues(boolean allowNullValues) {
        if (this.allowNullValues != allowNullValues) {
            this.allowNullValues = allowNullValues;
            refreshCommonCaches();
        }
    }


    /**
     * 在初始化CacheManager的时候初始化一组缓存。
     * 使用这个方法会在CacheManager初始化的时候就会将一组缓存初始化好，并且在运行时不会再去创建更多的缓存。
     * 使用空的Collection或者重新在配置里面指定dynamic后，就可重新在运行时动态的来创建缓存。
     *
     * @param cacheNames 缓存名称
     */
    public void setCacheNames(Collection<String> cacheNames) {
        if (cacheNames != null) {
            for (String name : cacheNames) {
                this.cacheMap.put(name, createMultiLevelCache(name));
            }
            this.dynamic = cacheNames.isEmpty();
        }
    }


    /**
     *  设置此缓存器是否应公开可识别事务的缓存对象。
     *  默认是“false”。将此设置为“true”，将缓存put/evic操作与正在进行的spring管理的事务同步，仅在成功事务的提交后阶段执行实际的缓存put/evict操作。
     */
    public void setTransactionAware(boolean transactionAware) {
        this.transactionAware = transactionAware;
    }

    /**
     * 返回此CacheManager是否已配置为可感知事务。
     */
    public boolean isTransactionAware() {
        return this.transactionAware;
    }

    public boolean isAllowNullValues() {
        return this.allowNullValues;
    }


    /**
     * 是否可以创建缓存
     * @param name 缓存名称
     * @return
     */
    private Cache getMissingCache(String name) {
        return dynamic ? createMultiLevelCache(name) : null;
    }

    private void updateCacheNames(String name) {
        Set<String> cacheNames = new LinkedHashSet<>(this.cacheNames.size() + 1);
        cacheNames.addAll(this.cacheNames);
        cacheNames.add(name);
        this.cacheNames = Collections.unmodifiableSet(cacheNames);
    }


    private Cache decorateCache(Cache cache) {
        return (isTransactionAware() ? new TransactionAwareCacheDecorator(cache) : cache);
    }

    public void setCacheLoader(CacheLoader<Object, Object> cacheLoader) {
        if (!ObjectUtils.nullSafeEquals(this.cacheLoader, cacheLoader)) {
            this.cacheLoader = cacheLoader;
            refreshCommonCaches();
        }
    }


    private void refreshCommonCaches() {
        for (Map.Entry<String, Cache> entry : this.cacheMap.entrySet()) {
            if (!this.cacheNames.contains(entry.getKey())) {
                entry.setValue(createMultiLevelCache(entry.getKey()));
            }
        }
    }

    /**
     * 创建默认配置的 多级 缓存
     * @param name
     * @return
     */
    private Cache createMultiLevelCache(String name) {
        return adaptMultiLevelCache(name, null,createNativeCaffeineCache(null));
    }

    /**
     * 创建已知配置的二级缓存（支持事务）
     * @param cacheName
     * @param caffeineSpec
     * @param redisCacheConfiguration
     * @return
     */
    private Cache createMultiLevelCache(String cacheName, CaffeineSpec caffeineSpec, RedisCacheConfiguration redisCacheConfiguration) {
        return adaptMultiLevelCache(cacheName, redisCacheConfiguration,createNativeCaffeineCache(caffeineSpec));
    }


    /**
     *  缓存适配器（支持事务）
     * @param name
     * @param redisCacheConfiguration
     * @param cache
     * @return
     */
    protected Cache adaptMultiLevelCache(String name, RedisCacheConfiguration redisCacheConfiguration ,com.github.benmanes.caffeine.cache.Cache<Object, Object> cache) {
        redisCacheConfiguration  = redisCacheConfiguration != null ? redisCacheConfiguration : defaultCacheConfig;
        return decorateCache(new MultiLevelCache(name, cacheWriter,redisCacheConfiguration,cache, isAllowNullValues()));
    }

    /**
     * 创建一级本地缓存
     * @param caffeineSpec 配置，如果 null 取默认配置
     * @return
     */
    protected com.github.benmanes.caffeine.cache.Cache<Object, Object> createNativeCaffeineCache(CaffeineSpec caffeineSpec) {
        Caffeine<Object, Object> cacheBuilder = caffeineSpec != null ? Caffeine.from(caffeineSpec) : this.cacheBuilder;
        return (this.cacheLoader != null ? cacheBuilder.build(this.cacheLoader) : cacheBuilder.build());
    }


    public static MultiLevelCacheManagerBuilder builder(RedisTemplate<Serializable, Serializable> redisTemplate) {

        Assert.notNull(redisTemplate, "redisTemplate must not be null!");

        return MultiLevelCacheManagerBuilder.builder(redisTemplate);
    }


    // ============= Builder ==========


    public static class MultiLevelCacheManagerBuilder {

        private @Nullable RedisCacheWriter cacheWriter;
        private MultiLevelCacheConfiguration defaultCacheConfiguration;
        private final Map<String, MultiLevelCacheConfiguration> initialCaches = new LinkedHashMap<>();
        private boolean enableTransactions;
        boolean dynamic = true;

        public static MultiLevelCacheManagerBuilder builder(RedisTemplate<Serializable, Serializable> redisTemplate) {

            Assert.notNull(redisTemplate, "redisTemplate must not be null!");

            return MultiLevelCacheManagerBuilder.fromConnectionFactory(redisTemplate);
        }

        private MultiLevelCacheManagerBuilder(RedisCacheWriter cacheWriter) {
            this.cacheWriter = cacheWriter;
        }

        static MultiLevelCacheManagerBuilder fromConnectionFactory(RedisTemplate<Serializable, Serializable> redisTemplate) {

            Assert.notNull(redisTemplate, "redisTemplate must not be null!");

            return new MultiLevelCacheManagerBuilder(new DefaultRedisCacheWriter(redisTemplate));
        }

        public MultiLevelCacheManagerBuilder cacheDefaults(MultiLevelCacheConfiguration defaultCacheConfiguration) {

            Assert.notNull(defaultCacheConfiguration, "DefaultCacheConfiguration must not be null!");

            this.defaultCacheConfiguration = defaultCacheConfiguration;

            return this;
        }

        public MultiLevelCacheManagerBuilder transactionAware() {

            this.enableTransactions = true;

            return this;
        }

        public MultiLevelCacheManagerBuilder initialCacheNames(Set<String> cacheNames) {

            Assert.notNull(cacheNames, "CacheNames must not be null!");

            cacheNames.forEach(it -> withCacheConfiguration(it, defaultCacheConfiguration));
            return this;
        }

        public MultiLevelCacheManagerBuilder withInitialCacheConfigurations(
                Map<String, MultiLevelCacheConfiguration> cacheConfigurations) {

            Assert.notNull(cacheConfigurations, "CacheConfigurations must not be null!");
            cacheConfigurations.forEach((cacheName, configuration) -> Assert.notNull(configuration,
                    String.format("RedisCacheConfiguration for cache %s must not be null!", cacheName)));

            this.initialCaches.putAll(cacheConfigurations);
            return this;
        }

        public MultiLevelCacheManagerBuilder withCacheConfiguration(String cacheName,
                                                                    MultiLevelCacheConfiguration cacheConfiguration) {

            Assert.notNull(cacheName, "CacheName must not be null!");
            Assert.notNull(cacheConfiguration, "CacheConfiguration must not be null!");

            this.initialCaches.put(cacheName, cacheConfiguration);
            return this;
        }

        public MultiLevelCacheManagerBuilder disableCreateOnMissingCache() {

            this.dynamic = false;
            return this;
        }

        public Set<String> getConfiguredCaches() {
            return Collections.unmodifiableSet(this.initialCaches.keySet());
        }

        public Optional<MultiLevelCacheConfiguration> getCacheConfigurationFor(String cacheName) {
            return Optional.ofNullable(this.initialCaches.get(cacheName));
        }

        public MultiLevelCacheManager build() {

            Assert.state(cacheWriter != null, "CacheWriter must not be null! You can provide one via 'MultiLevelCacheManagerBuilder#cacheWriter(RedisCacheWriter)'.");
            //redis 缓存配置
            RedisCacheConfiguration redisCacheConfiguration = defaultCacheConfiguration.getRedisCacheConfiguration();
            //caffeine 缓存配置
            @NonNull CaffeineSpec parse = CaffeineSpec.parse(defaultCacheConfiguration.getCacheSpecification());
            MultiLevelCacheManager cm = null;
            //判断是否存在需要初始化配置的缓存
            if(!CollectionUtils.isEmpty(initialCaches)){
                //caffeine 提前加载
                Map<String,CaffeineSpec> caffeineSpecConfigurations = new LinkedHashMap<>(16);
                //redis 提前加载
                Map<String,RedisCacheConfiguration> redisCacheConfigurations = new LinkedHashMap<>(16);

                for(Map.Entry<String, MultiLevelCacheConfiguration> entry : initialCaches.entrySet()){
                    caffeineSpecConfigurations.put(entry.getKey(),CaffeineSpec.parse(entry.getValue().getCacheSpecification()));
                    redisCacheConfigurations.put(entry.getKey(),entry.getValue().getRedisCacheConfiguration());
                }

                cm = new MultiLevelCacheManager(cacheWriter,redisCacheConfiguration ,parse ,caffeineSpecConfigurations,
                        redisCacheConfigurations, dynamic);
            }else {
                cm = new MultiLevelCacheManager(cacheWriter,redisCacheConfiguration,parse,dynamic);
            }
            cm.setTransactionAware(enableTransactions);
            cm.setAllowNullValues(defaultCacheConfiguration.isAllowNullValues());
            return cm;
        }
    }



}
