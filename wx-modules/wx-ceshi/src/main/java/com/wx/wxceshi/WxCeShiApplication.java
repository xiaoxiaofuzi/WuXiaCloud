package com.wx.wxceshi;

import com.wx.wxcommoncore.annotation.WxCloudApplication;
import com.wx.wxcommoncore.utils.EnvironmentUtils;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import javax.net.ssl.HttpsURLConnection;

@WxCloudApplication
@MapperScan("com.wx.wxceshi.mapper")
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
