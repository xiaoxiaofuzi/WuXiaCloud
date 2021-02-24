package com.wx.wxcommoncache.multilevelcache;

import com.alibaba.fastjson.JSON;
import com.github.benmanes.caffeine.cache.Cache;
import com.wx.wxcommoncache.multilevelcache.cache.CaffeineCacheCopy;
import com.wx.wxcommoncache.multilevelcache.cache.RedisCacheCopy;
import com.wx.wxcommoncache.multilevelcache.enums.ChannelTopicEnum;
import com.wx.wxcommoncache.multilevelcache.pojo.MultiLevelCacheMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.support.AbstractValueAdaptingCache;
import org.springframework.cache.support.NullValue;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.util.Assert;

import java.util.concurrent.Callable;

/**
 * @author gh
 */
@Slf4j
public class MultiLevelCache extends AbstractValueAdaptingCache {

    private final String name;

    private final CaffeineCacheCopy caffeineCache;

    private final RedisCacheCopy redisCache;


    MultiLevelCache(String name, RedisCacheWriter cacheWriter, RedisCacheConfiguration cacheConfig, Cache<Object, Object> cache, boolean allowNullValues) {

        super(cacheConfig.getAllowCacheNullValues());

        Assert.notNull(name, "Name must not be null!");
        Assert.notNull(cacheWriter, "CacheWriter must not be null!");
        Assert.notNull(cacheConfig, "CacheConfig must not be null!");
        Assert.notNull(cache, "cache must not be null!");

        this.name = name;
        this.caffeineCache = new CaffeineCacheCopy(name,cache,allowNullValues);
        this.redisCache = new RedisCacheCopy(name,cacheWriter,cacheConfig);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Object getNativeCache() {
        return this;
    }

    public CaffeineCacheCopy getCaffeineCache(){
        return caffeineCache;
    }

    public RedisCacheCopy getRedisCache(){
        return redisCache;
    }


    /**
     * 获取缓存
     * @param key 缓存名称
     * @return
     */
    @Override
    protected Object lookup(Object key) {
        Object result = null;
        result = caffeineCache.lookup(key);
        log.debug("查询一级缓存。 key={},返回值是:{}", key, JSON.toJSONString(result));
        if(result == null || result instanceof NullValue){
            result = redisCache.lookup(key);
            log.debug("查询二级缓存。 key={},返回值是:{}", key, JSON.toJSONString(result));
            if(result != null){
                log.debug("将数据放到一级缓存");
                caffeineCache.putIfAbsent(key, result);
            }
        }
        return result;
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        T result = caffeineCache.get(key, valueLoader);
        log.debug("查询一级缓存。 key={},返回值是:{}", key, JSON.toJSONString(result));
        if(result instanceof NullValue){
            result = redisCache.get(key,valueLoader);
            log.debug("查询二级缓存。 key={},返回值是:{}", key, JSON.toJSONString(result));
            if(result instanceof NullValue) {
                log.debug("将数据放到一级缓存");
                caffeineCache.putIfAbsent(key, result);
            }
        }
        return result;
    }

    @Override
    public void put(Object key, Object value) {
        redisCache.put(key,value);
        publish(key);

    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        ValueWrapper valueWrapper = redisCache.putIfAbsent(key, value);
        publish(key);
        return valueWrapper;
    }


    @Override
    public void evict(Object key) {
        redisCache.evict(key);
        publish(key);
    }

    @Override
    public void clear() {
        redisCache.clear();
        publish();
    }


    /**
     * 发送 REDIS_CACHE_CLEAR_TOPIC 消息
     */
    private void publish() {
        //获取 redis连接
        publish(ChannelTopicEnum.REDIS_CACHE_CLEAR_TOPIC,null);
    }

    /**
     * 发送 REDIS_CACHE_CLEAR_TOPIC 消息
     */
    private void publish(Object key) {
        //获取 redis连接
        publish(ChannelTopicEnum.REDIS_CACHE_DELETE_TOPIC,key);
    }

    /**
     * 发送 REDIS_CACHE_DELETE_TOPIC 消息
     */
    private void publish(ChannelTopicEnum channelTopicEnum,Object key) {
        //获取 redis连接
        DefaultRedisCacheWriter defaultRedisCacheWriter = (DefaultRedisCacheWriter) redisCache.getNativeCache();
        MultiLevelCacheMessage message = new MultiLevelCacheMessage(getName(),key);
        defaultRedisCacheWriter.publish(channelTopicEnum.getChannelTopicStr(),message);
    }
}
