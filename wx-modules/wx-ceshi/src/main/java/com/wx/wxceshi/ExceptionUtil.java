package com.wx.wxceshi;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class ExceptionUtil implements ApplicationContextAware {


    private static String port;

    public static String handleException(BlockException ex) {
        return String.format("服务器[%s]扛不住了啊....",port);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        String property = applicationContext.getEnvironment().getProperty("server.port");
        ExceptionUtil.port = property;
    }
}
