package com.wx.wxcommoncache.multilevelcache.cache;

import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCache;

public class CaffeineCacheCopy extends CaffeineCache {

    public CaffeineCacheCopy(String name, Cache<Object, Object> cache) {
        super(name, cache);
    }

    public CaffeineCacheCopy(String name, Cache<Object, Object> cache, boolean allowNullValues) {
        super(name, cache, allowNullValues);
    }


    @Override
    public Object lookup(Object key) {
        return super.lookup(key);
    }
}
