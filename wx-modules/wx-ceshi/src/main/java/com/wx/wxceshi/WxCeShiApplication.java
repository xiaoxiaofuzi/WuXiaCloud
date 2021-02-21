package com.wx.wxceshi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.client.SpringCloudApplication;

import javax.net.ssl.HttpsURLConnection;

@SpringCloudApplication
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
    }

}
