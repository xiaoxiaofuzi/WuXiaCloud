package com.wx.wxcommoncore.configs;

import com.wx.wxcommoncore.support.interceptor.FeignRequestInterceptor;
import feign.Logger;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign 全局配置，覆盖 {@link org.springframework.cloud.openfeign.FeignClientsConfiguration}
 * 相同的覆盖，不同的添加
 **/
@Configuration
public class FeignAutoConfiguration {
    @Bean
    public RequestInterceptor requestInterceptor() {
        return new FeignRequestInterceptor();
    }


    @Bean
    public Logger.Level feignLogger(){
        return Logger.Level.FULL;
    }
}
