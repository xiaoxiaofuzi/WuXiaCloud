package com.wx.wxsystem.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wx.wxcommonrocketmq.event.StringMsgEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.bus.event.AckRemoteApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListener;

public class StringMsgListener implements ApplicationListener<StringMsgEvent> {

    @Autowired
    private ObjectMapper objectMapper;

    @EventListener
    public void onAckEvent(AckRemoteApplicationEvent event)
            throws JsonProcessingException {
        System.out.printf("Server listeners on %s\n",
                objectMapper.writeValueAsString(event));
    }

    @Override
    public void onApplicationEvent(StringMsgEvent event) {
        System.out.printf("Server [port : %s] listeners on %s\n", event.getOriginService(),
                event.getMsg());
    }
}
