package com.wx.wxsystem;

import com.wx.wxcommoncore.annotation.WxCloudApplication;
import com.wx.wxcommoncore.utils.EnvironmentUtils;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import javax.net.ssl.HttpsURLConnection;

@WxCloudApplication
@MapperScan("com.wx.**.mapper")
@Slf4j
public class WxSystemApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(WxSystemApplication.class);
    }

    static {
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
    }

    public static void main(String[] args) {
        SpringApplication.run(WxSystemApplication.class, args);
    }
}
