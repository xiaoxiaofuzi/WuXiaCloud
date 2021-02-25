package com.wx.wxcommoncache.service.impl;

import com.wx.wxcommoncache.service.CacheManager;
import com.wx.wxcommoncache.utils.CacheUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * Redisson缓存辅助类,用来控制并发和使用布隆过滤器
 *
 * @author wx-
 */
@Slf4j
public class RedissonHelper implements CacheManager {

    private RedissonClient redissonClient;

    public RedissonHelper(RedissonClient redissonClient){
        this.redissonClient = redissonClient;
        CacheUtil.setLockManager(this);
    }

    @Override
    public Object get(String key) {
        throw new RuntimeException("不用作存储");
    }

    @Override
    public Set<Object> getAll(String pattern) {
        return null;
    }

    @Override
    public void set(String key, Serializable value, int seconds) {

    }

    @Override
    public void set(String key, Serializable value) {

    }

    @Override
    public void mSet(Map<? extends Serializable, ? extends Serializable> map) {

    }

    @Override
    public Boolean exists(String key) {
        return null;
    }

    @Override
    public void del(String key) {

    }

    @Override
    public void delAll(String pattern) {

    }

    @Override
    public String type(String key) {
        return null;
    }

    @Override
    public Boolean expire(String key, int seconds) {
        return null;
    }

    @Override
    public Boolean expireAt(String key, long unixTime) {
        return null;
    }

    @Override
    public Long ttl(String key) {
        return null;
    }

    @Override
    public Object getSet(String key, Serializable value) {
        return null;
    }

    @Override
    public boolean lock(String key, String requestId, long seconds) {
        return false;
    }

    @Override
    public boolean unlock(String key, String requestId) {
        return false;
    }

    @Override
    public void hset(String key, Serializable field, Serializable value) {

    }

    @Override
    public Object hget(String key, Serializable field) {
        return null;
    }

    @Override
    public void hdel(String key, Serializable field) {

    }

    @Override
    public boolean setnx(String key, Serializable value) {
        return false;
    }

    @Override
    public Long incr(String key) {
        return null;
    }

    @Override
    public void setrange(String key, long offset, String value) {

    }

    @Override
    public String getrange(String key, long startOffset, long endOffset) {
        return null;
    }

    @Override
    public Object get(String key, Integer expire) {
        return null;
    }

    @Override
    public Object getFire(String key) {
        return null;
    }

    @Override
    public Set<Object> getAll(String pattern, Integer expire) {
        return null;
    }

    @Override
    public <T> T get(String lua, List<Object> ts, Class<T> zclass, Object... objects) {
        return null;
    }

    @Override
    public RLock getLock(String lockKey) {
        return null;
    }

    @Override
    public void hmset(String s, Map<?, ?> mapCache) {

    }

    @Override
    public Map<Object, Object> hget(String s) {
        return null;
    }

    @Override
    public void publisher(String topic, Serializable message) {

    }

    @Override
    public <T> RBloomFilter<T> getBloomFilter(String s) {
        return null;
    }
}
