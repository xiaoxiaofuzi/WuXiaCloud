package com.wx.wxcommoncache.service;

import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
  *
  * @ClassName CacheManager
  * @author gh
  * @Description 其中RedisHelper 用来存储   RedissonHelper 用来控制并发和使用布隆过滤器
  * @Date 2021/2/25 0025 11:32
  * @Version 1.0
  **/
public interface CacheManager {

    Object get(final String key);

    Set<Object> getAll(final String pattern);

    void set(final String key, final Serializable value, int seconds);

    void set(final String key, final Serializable value);

    void mSet(Map<? extends Serializable, ? extends Serializable> map);

    Boolean exists(final String key);

    void del(final String key);

    void delAll(final String pattern);

    String type(final String key);

    Boolean expire(final String key, final int seconds);

    Boolean expireAt(final String key, final long unixTime);

    Long ttl(final String key);

    Object getSet(final String key, final Serializable value);

    boolean lock(String key, String requestId, long seconds);

    boolean unlock(String key, String requestId);

    void hset(String key, Serializable field, Serializable value);

    Object hget(String key, Serializable field);

    void hdel(String key, Serializable field);

    boolean setnx(String key, Serializable value);

    Long incr(String key);

    void setrange(String key, long offset, String value);

    String getrange(String key, long startOffset, long endOffset);

    Object get(String key, Integer expire);

    Object getFire(String key);

    Set<Object> getAll(String pattern, Integer expire);

    <T> T get(String lua, List<Object> ts, Class<T> zclass, Object... objects);

    RLock getLock(String lockKey);

    void hmset(String s, Map<?, ?> mapCache);

    Map<Object,Object> hget(String s);

    /**
     * redis 发布订阅消息
     * @param topic 主题
     * @param message 消息
     */
     void publisher(String topic, Serializable message);

    <T> RBloomFilter<T> getBloomFilter(String s);
}
