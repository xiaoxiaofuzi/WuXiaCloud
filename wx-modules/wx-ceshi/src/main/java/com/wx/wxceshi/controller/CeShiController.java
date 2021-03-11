package com.wx.wxceshi.controller;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.wx.wxceshi.entity.User;
import com.wx.wxceshi.service.UserService;
import com.wx.wxcommoncore.support.http.HttpCode;
import com.wx.wxcommondatasource.base.BaseController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
@RequestMapping("/ceshi")
@Slf4j
public class CeShiController extends BaseController<UserService,User> {

    @Autowired
    private RedisTemplate<String,String> redisTemplate;


    @Value("${server.port}")
    private String port;

//    @SentinelResource(value="getPort",blockHandler="handleException",blockHandlerClass=ExceptionUtil.class)
    @GetMapping("/getPort")
    public String getPort(){
        return "当前微服务端口号："+port;
    }

    @Cacheable(value = "cs")
    @GetMapping("/getRedis")
    public String getRedis(){
        log.info("进入service查询,返回abc");
        return "abc";
    }

    @CacheEvict(value = "cs")
    @GetMapping("/delRedis")
    public String delRedis(){
        return "success";
    }


    @GetMapping("/getUserM")
    public String getUserM(){
        return JSON.toJSONString(baseService.selectLambdaMasterUsers());
    }

    @GetMapping("/getUserS")
    public String getUserS(){
        PageHelper.startPage(1,1);
        Page<User> users = (Page<User>)baseService.selectLambdaSlaveUsers();
        log.info(JSON.toJSONString(users));
        return JSON.toJSONString(users.getResult());
    }


    @GetMapping("/addUser")
    public String addUser(){
        User user = new User();
        user.setName("郭浩"+System.currentTimeMillis());
        user.setAge(new Random().nextInt(30));
        baseService.addUser(user);
        return JSON.toJSONString(user);
    }

    @GetMapping("/sel")
    public ResponseEntity<ModelMap> sel(){
        baseService.selectCeShi();
        return setModelMap(HttpCode.OK);
    }

    @GetMapping("/selE")
    public ResponseEntity<ModelMap> selE(){
        int i = 1/0;
        return setModelMap(HttpCode.OK);
    }



}
