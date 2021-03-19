package com.wx.wxcommoncore.annotation;

import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

import java.lang.annotation.*;

/**
 * @author gh
 * 修改扫描包
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@SpringCloudApplication
@ComponentScan(basePackages = { "com.wx"})
@EnableFeignClients(basePackages = { "com.wx"})
public @interface WxCloudApplication {
}
