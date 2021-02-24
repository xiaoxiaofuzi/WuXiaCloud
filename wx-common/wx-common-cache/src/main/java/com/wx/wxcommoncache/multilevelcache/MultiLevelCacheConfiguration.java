package com.wx.wxcommoncache.multilevelcache;

import org.springframework.data.redis.cache.RedisCacheConfiguration;


public class MultiLevelCacheConfiguration {

    /**
     *  一级缓存配置
     */
    private final String cacheSpecification;
    /**
     *  一级缓存配置--是否能空值，可以
     */
    private final boolean allowNullValues;

    /**
     * 二级缓存配置
     */
    private final RedisCacheConfiguration redisCacheConfiguration;


    private MultiLevelCacheConfiguration(String cacheSpecification,boolean allowNullValues,RedisCacheConfiguration redisCacheConfiguration) {

        this.cacheSpecification = cacheSpecification;
        this.allowNullValues = allowNullValues;
        this.redisCacheConfiguration = redisCacheConfiguration;

    }

    public static MultiLevelCacheConfiguration defaultCacheConfig() {
        return defaultCacheConfig("",RedisCacheConfiguration.defaultCacheConfig());
    }

    public static MultiLevelCacheConfiguration defaultCacheConfig(RedisCacheConfiguration redisCacheConfiguration) {
        return defaultCacheConfig("",redisCacheConfiguration);
    }

    public static MultiLevelCacheConfiguration defaultCacheConfig(String cacheSpecification,RedisCacheConfiguration redisCacheConfiguration) {
        return new MultiLevelCacheConfiguration(cacheSpecification,true,redisCacheConfiguration);
    }

    public String getCacheSpecification() {
        return cacheSpecification;
    }

    public RedisCacheConfiguration getRedisCacheConfiguration() {
        return redisCacheConfiguration;
    }

    public boolean isAllowNullValues() {
        return allowNullValues;
    }
}
