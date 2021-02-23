package com.wx.wxgateway.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.*;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.ContextStoppedEvent;

@Slf4j
public class WxApplicationListener implements ApplicationListener {
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
         if (event instanceof ApplicationEnvironmentPreparedEvent) { // 初始化环境变量
            log.info("==========初始化环境变量==============");
        }else if (event instanceof ApplicationPreparedEvent) { // 初始化完成
            log.info("==========初始化完成Bean==============");
        }else if (event instanceof ApplicationStartedEvent) { // 应用刷新
            log.info("==========应用刷新完成==============");
        } else if (event instanceof ApplicationReadyEvent) {// 应用已启动完成
            log.info("=================================");
            String server = ((ApplicationReadyEvent)event).getSpringApplication().getAllSources().iterator().next()
                    .toString();
            log.info("系统[{}]启动完成!!!", server.substring(server.lastIndexOf(".") + 1));
            log.info("=================================");
        }
    }
}
