package com.wx.wxgateway;

import com.wx.wxcommoncore.annotation.WxCloudApplication;
import com.wx.wxcommoncore.utils.EnvironmentUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import javax.net.ssl.HttpsURLConnection;

@WxCloudApplication
@Slf4j
public class WxGatewayApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(WxGatewayApplication.class);
    }

    static {
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
    }

    public static void main(String[] args) {
        SpringApplication.run(WxGatewayApplication.class, args);
        String s1 = EnvironmentUtils.searchByKey("wx.config");
        log.info("wx.config:{}",s1);
        String s2 = EnvironmentUtils.searchByKey("wx.dev.config");
        log.info("wx.dev.config:{}",s2);
        String s = EnvironmentUtils.searchByKey("gateway.config");
        log.info("gateway.config:{}",s);
    }

}
