package com.wx.wxcommoncache.multilevelcache.listener;

import com.alibaba.fastjson.JSON;
import com.wx.wxcommoncache.multilevelcache.MultiLevelCache;
import com.wx.wxcommoncache.multilevelcache.enums.ChannelTopicEnum;
import com.wx.wxcommoncache.multilevelcache.pojo.MultiLevelCacheMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.transaction.TransactionAwareCacheDecorator;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

/**
 * redis消息的订阅者
 *
 * @author yuhao.wang
 */

@Slf4j
public class RedisMessageListener extends MessageListenerAdapter {

    @Autowired
    CacheManager multiLevelCache;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        super.onMessage(message, pattern);
        ChannelTopicEnum channelTopic = ChannelTopicEnum.getChannelTopicEnum(new String(message.getChannel()));
        if(channelTopic == null){
            return;
        }
        // 解析订阅发布的信息，获取缓存的名称和缓存的key
        String ms = new String(message.getBody());
        log.debug("redis消息订阅者接收到频道【{}】发布的消息。消息内容：{}", channelTopic.getChannelTopicStr(), message.toString());
        @SuppressWarnings("unchecked")
        MultiLevelCacheMessage multiLevelCacheMessage = JSON.parseObject(ms, MultiLevelCacheMessage.class);
        String cacheName = multiLevelCacheMessage.getCacheName();
        Object key = multiLevelCacheMessage.getKey();


        // 根据缓存名称获取多级缓存
        Cache cache = multiLevelCache.getCache(cacheName);

        if(cache instanceof TransactionAwareCacheDecorator){
            cache = ((TransactionAwareCacheDecorator) cache).getTargetCache();
        }

        // 判断缓存是否是多级缓存
        if (cache instanceof MultiLevelCache) {
            switch (channelTopic) {
                case REDIS_CACHE_DELETE_TOPIC:
                    // 获取一级缓存，并删除一级缓存数据
                    ((MultiLevelCache) cache).getCaffeineCache().evict(key);
                    log.debug("删除一级缓存{}数据,key:{}", cacheName, key.toString());
                    break;

                case REDIS_CACHE_CLEAR_TOPIC:
                    // 获取一级缓存，并删除一级缓存数据
                    ((MultiLevelCache) cache).getCaffeineCache().clear();
                    log.debug("清除一级缓存{}数据", cacheName);
                    break;

                default:
                    log.debug("接收到没有定义的订阅消息频道数据");
                    break;
            }
        }
    }



}
