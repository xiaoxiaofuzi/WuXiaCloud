package com.wx.wxcommoncache.config;

import com.alibaba.fastjson.JSON;
import com.wx.wxcommoncache.multilevelcache.MultiLevelCacheConfiguration;
import com.wx.wxcommoncache.multilevelcache.MultiLevelCacheManager;
import com.wx.wxcommoncache.multilevelcache.enums.ChannelTopicEnum;
import com.wx.wxcommoncache.multilevelcache.listener.RedisMessageListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.*;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
@ConditionalOnClass(CacheConfig.class)
@Slf4j
public class CacheConfig extends CachingConfigurerSupport {

    private static final String PREFIX = "WX:CACHE:";


    @Bean
    public WxCacheProperties wxCacheProperties(){
        return new WxCacheProperties();
    }

    @Bean
    @ConditionalOnProperty(prefix = "wx.cache",name = "type",havingValue = "multi")
    public MessageListenerAdapter redisMessageListener(){
        return new RedisMessageListener();
    }
    @Autowired
    private WxCacheProperties wxCacheProperties;

    /**
     * 添加自定义缓存异常处理
     * 当缓存读写异常时,忽略异常
     */
    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                log.error(exception.getMessage(), exception);
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                log.error(exception.getMessage(), exception);
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                log.error(exception.getMessage(), exception);
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                log.error(exception.getMessage(), exception);
            }
        };
    }

    @Override
    @Bean
    public KeyGenerator keyGenerator() {
        return new KeyGenerator() {
            /** 重写生成key方法 */
            @Override
            public Object generate(Object o, Method method, Object... objects) {
                StringBuilder sb = new StringBuilder(PREFIX);
                org.springframework.cache.annotation.CacheConfig cacheConfig = o.getClass().getAnnotation(org.springframework.cache.annotation.CacheConfig.class);
                Cacheable cacheable = method.getAnnotation(Cacheable.class);
                CachePut cachePut = method.getAnnotation(CachePut.class);
                CacheEvict cacheEvict = method.getAnnotation(CacheEvict.class);
                if (cacheable != null) {
                    String[] cacheNames = cacheable.value();
                    if (ArrayUtils.isNotEmpty(cacheNames)) {
                        sb.append(cacheNames[0]);
                    }
                } else if (cachePut != null) {
                    String[] cacheNames = cachePut.value();
                    if (ArrayUtils.isNotEmpty(cacheNames)) {
                        sb.append(cacheNames[0]);
                    }
                } else if (cacheEvict != null) {
                    String[] cacheNames = cacheEvict.value();
                    if (ArrayUtils.isNotEmpty(cacheNames)) {
                        sb.append(cacheNames[0]);
                    }
                }
                if (cacheConfig != null && sb.toString().equals(PREFIX)) {
                    String[] cacheNames = cacheConfig.cacheNames();
                    if (ArrayUtils.isNotEmpty(cacheNames)) {
                        sb.append(cacheNames[0]);
                    }
                }
                if (sb.toString().equals(PREFIX)) {
                    sb.append(o.getClass().getName()).append(".").append(method.getName());
                }
                sb.append(":");
                if (objects != null) {
                    for (Object object : objects) {
                        sb.append(JSON.toJSONString(object));
                    }
                }
                return sb.toString();
            }
        };
    }


    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    @ConditionalOnProperty(prefix = "wx.cache",name = "type",havingValue = "single")
    public CacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory) {

        log.info("使用redis缓存");
        //默认半个小时
        RedisCacheConfiguration configuration = getRedisCacheConfiguration(wxCacheProperties.getRedisTtl());

        //对特定缓存名称，创建特定的缓存有效期
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>(4);
//        cacheConfigurations.put("ceshi",getRedisCacheConfiguration(3000L));

        //创建builder
        RedisCacheManager.RedisCacheManagerBuilder builder = RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(configuration)
                .withInitialCacheConfigurations(cacheConfigurations);

        //是否开启事务
        if (wxCacheProperties.isRedisEnableTransaction()) {
            builder.transactionAware();
        }

        //创建
        return builder.build();
    }

    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    @ConditionalOnProperty(prefix = "wx.cache",name = "type",havingValue = "multi")
    public CacheManager multiLevelCache(RedisTemplate<Serializable, Serializable> redisTemplate) {
        log.info("使用二级缓存（caffeine + redis）");
        String firstConf = wxCacheProperties.getCaffeineConfig();
        int ttl = wxCacheProperties.getRedisTtl();
        MultiLevelCacheConfiguration multiLevelCacheConfiguration = getMultiLevelCacheConfiguration(firstConf,ttl);

        //对特定缓存名称，创建特定的缓存有效期
        Map<String, MultiLevelCacheConfiguration> multiLevelCacheConfigurationHashMap = new HashMap<>(4);
//        multiLevelCacheConfigurationHashMap.put("ceshi",getMultiLevelCacheConfiguration("initialCapacity=10,maximumSize=500,expireAfterWrite=3000s",3000L));

        //创建builder
        MultiLevelCacheManager.MultiLevelCacheManagerBuilder builder = MultiLevelCacheManager.builder(redisTemplate)
                .cacheDefaults(multiLevelCacheConfiguration)
                .withInitialCacheConfigurations(multiLevelCacheConfigurationHashMap);

        //是否开启事务
        if (wxCacheProperties.isRedisEnableTransaction()) {
            builder.transactionAware();
        }

        //创建
        return builder.build();
    }

    /**
     *  创建监听器
     * @param redisConnectionFactory
     * @param redisMessageListener
     * @return
     */
    @Bean
    @ConditionalOnProperty(prefix = "wx.cache",name = "type",havingValue = "multi")
    public RedisMessageListenerContainer redisContainer(RedisConnectionFactory redisConnectionFactory, MessageListenerAdapter redisMessageListener) {
        final RedisMessageListenerContainer container = new RedisMessageListenerContainer();

        container.setConnectionFactory(redisConnectionFactory);
        container.addMessageListener(redisMessageListener, ChannelTopicEnum.REDIS_CACHE_DELETE_TOPIC.getChannelTopic());
        container.addMessageListener(redisMessageListener, ChannelTopicEnum.REDIS_CACHE_CLEAR_TOPIC.getChannelTopic());
        return container;
    }


    /**
     * 创建缓存配置
     * @param seconds  缓存存活时间
     * @return
     */
    private MultiLevelCacheConfiguration getMultiLevelCacheConfiguration(String firstConf,int seconds) {
        return MultiLevelCacheConfiguration.defaultCacheConfig(firstConf, getRedisCacheConfiguration(seconds));
    }

    /**
     * 创建缓存配置
     * @param seconds  缓存存活时间
     * @return
     */
    private RedisCacheConfiguration getRedisCacheConfiguration(int seconds) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(seconds));
    }
}
