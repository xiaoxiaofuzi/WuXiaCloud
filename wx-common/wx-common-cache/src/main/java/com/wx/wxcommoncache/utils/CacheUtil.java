package com.wx.wxcommoncache.utils;

import com.wx.wxcommoncache.service.CacheManager;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;

/**
 * @author mysd
 * @since 2018年5月24日 下午6:37:31
 */
@Slf4j
public final class CacheUtil {

    private static CacheManager cacheManager;
    private static CacheManager lockManager;

    public static void setCacheManager(CacheManager cacheManager) {
        CacheUtil.cacheManager = cacheManager;
    }

    public static void setLockManager(CacheManager cacheManager) {
        CacheUtil.lockManager = cacheManager;
    }

    public static CacheManager getCache() {
        return cacheManager;
    }

    public static CacheManager getLockManager() {
        return lockManager;
    }

    /**
     * 获取分布式锁
     * @param key key
     */
    public static RLock getLock(String key) {
        log.debug("TOLOCK : " + key);
        return lockManager.getLock(key);
    }

    public static void unLock(RLock lock) {
        log.debug("UNLOCK : " + lock.getName());
        lock.unlock();
    }


}
