package com.wx.wxsystem.controller;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.wx.wxcommondatasource.base.BaseController;
import com.wx.wxsystem.entity.WxSysUser;
import com.wx.wxsystem.service.WxSysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.util.Random;

@RestController
@RequestMapping("/sys")
@Slf4j
public class WxSysUserController extends BaseController<WxSysUserService,WxSysUser> {


    @GetMapping
    public ResponseEntity<ModelMap> getUserS(){
        PageHelper.startPage(1,10);
        Page<WxSysUser> users = (Page<WxSysUser>)baseService.list();
        return setSuccessModelMap(users.getResult());
    }


    @GetMapping("/add")
    public ResponseEntity<ModelMap> addUser(){
        WxSysUser user = new WxSysUser();
        user.setName("郭浩"+System.currentTimeMillis());
        user.setLogin("admin"+new Random().nextInt(10));
        user.setPassword("pw"+new Random().nextInt(10));
        user.setCreationTime(new Timestamp(System.currentTimeMillis()));
        baseService.save(user);
        return setSuccessModelMap(JSON.toJSONString(user));
    }

}
