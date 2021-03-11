package com.wx.wxcommoncore.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

@Slf4j
public class WxApplicationListener implements ApplicationListener {
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ApplicationReadyEvent) {// 应用已启动完成
            log.info("=================================");
            String server = ((ApplicationReadyEvent)event).getSpringApplication().getAllSources().iterator().next()
                    .toString();
            log.info("系统[{}]启动完成!!!", server.substring(server.lastIndexOf(".") + 1));
            log.info("=================================");
        }
    }
}
