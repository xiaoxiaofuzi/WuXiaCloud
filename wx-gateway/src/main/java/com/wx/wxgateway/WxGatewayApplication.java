package com.wx.wxgateway;

import com.wx.wxcommoncore.annotation.WxCloudApplication;
import com.wx.wxcommoncore.utils.EnvironmentUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

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

    public static void main(String[] args) throws Exception {
        SpringApplication.run(WxGatewayApplication.class, args);
        String s1 = EnvironmentUtils.getString("wx.config");
        log.info("wx.config:{}",s1);
        String s2 = EnvironmentUtils.getString("wx.dev.config");
        log.info("wx.dev.config:{}",s2);
        String s = EnvironmentUtils.getString("gateway.config");
        log.info("gateway.config:{}",s);
        String s3 = EnvironmentUtils.getString("ceshi.config");
        log.info("ceshi.config:{}",s3);
    }

}
