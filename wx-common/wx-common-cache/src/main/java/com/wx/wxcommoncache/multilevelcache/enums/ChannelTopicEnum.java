package com.wx.wxcommoncache.multilevelcache.enums;

import org.springframework.data.redis.listener.ChannelTopic;

/**
 * @author Administrator
 */

public enum ChannelTopicEnum {

    /**
     * 删除redis缓存消息频道
     */
    REDIS_CACHE_DELETE_TOPIC("redis:cache:delete:topic", "删除redis缓存消息频道"),
    /**
     * 清空redis缓存消息频道
     */
    REDIS_CACHE_CLEAR_TOPIC("redis:cache:clear:topic", "清空redis缓存消息频道");

    String channelTopic;
    String label;

    ChannelTopicEnum(String channelTopic, String label) {
        this.channelTopic = channelTopic;
        this.label = label;
    }

    public ChannelTopic getChannelTopic() {
        return ChannelTopic.of(channelTopic);
    }

    public static ChannelTopicEnum getChannelTopicEnum(String channelTopic) {
        for (ChannelTopicEnum awardTypeEnum : ChannelTopicEnum.values()) {
            if (awardTypeEnum.getChannelTopicStr().equals(channelTopic)) {
                return awardTypeEnum;
            }
        }
        return null;
    }

    public String getChannelTopicStr() {
        return channelTopic;
    }

}
