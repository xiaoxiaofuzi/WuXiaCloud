package com.wx.wxceshi;

import com.wx.wxcommoncore.annotation.WxCloudApplication;
import com.wx.wxcommoncore.utils.EnvironmentUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.client.SpringCloudApplication;

import javax.net.ssl.HttpsURLConnection;

@WxCloudApplication
@Slf4j
public class WxCeShiApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(WxCeShiApplication.class);
    }

    static {
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
    }

    public static void main(String[] args) {
        SpringApplication.run(WxCeShiApplication.class, args);
        String s1 = EnvironmentUtils.searchByKey("wx.config");
        log.info("wx.config:{}",s1);
        String s2 = EnvironmentUtils.searchByKey("wx.dev.config");
        log.info("wx.dev.config:{}",s2);
        String s = EnvironmentUtils.searchByKey("gateway.config");
        log.info("gateway.config:{}",s);
        String s3 = EnvironmentUtils.searchByKey("ceshi.config");
        log.info("ceshi.config:{}",s3);
    }

}
