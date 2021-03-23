package com.wx.wxcommonrocketmq.event;

import org.springframework.cloud.bus.event.RemoteApplicationEvent;

public class StringMsgEvent extends RemoteApplicationEvent {
    /**
     *
     */
    private String msg;
    private String originService;

    public StringMsgEvent() { // 序列化
    }

    public StringMsgEvent(Object source, String originService, String destinationService, String msg) {
        super(source, originService,destinationService);
        this.msg = msg;
        this.originService = originService;
    }

    public String getMsg() {
        return msg;
    }

    @Override
    public String getOriginService() {
        return originService;
    }
}
