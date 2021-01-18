package com.wx.wxgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class WxGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(WxGatewayApplication.class, args);
    }

}
