package com.wx.wxceshi.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.wx.wxceshi.ExceptionUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ceshi")
public class CeShiController {


    @Value("${server.port}")
    private String port;

    @SentinelResource(value="getPort",blockHandler="handleException",blockHandlerClass=ExceptionUtil.class)
    @GetMapping("/getPort")
    public String getPort(){
        return "当前微服务端口号："+port;
    }

}
