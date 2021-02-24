package com.wx.wxcommoncache.multilevelcache.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Administrator
 */
@Data
@NoArgsConstructor
public class MultiLevelCacheMessage implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String cacheName;
    private Object key;

    public MultiLevelCacheMessage(String cacheName, Object key) {
        super();
        this.cacheName = cacheName;
        this.key = key;
    }

    public String getCacheName() {
        return cacheName;
    }

    public Object getKey() {
        return key;
    }
}
