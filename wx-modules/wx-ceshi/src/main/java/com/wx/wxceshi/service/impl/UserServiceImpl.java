package com.wx.wxceshi.service.impl;


import com.baomidou.dynamic.datasource.annotation.DS;
import com.wx.wxceshi.entity.User;
import com.wx.wxceshi.mapper.UserMapper;
import com.wx.wxceshi.service.UserService;
import com.wx.wxcommondatasource.base.BaseServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl extends BaseServiceImpl<UserMapper, User> implements UserService {

    @Override
    public List<User> selectMasterUsers() {
        return baseMapper.selectList(null);
    }

    @Override
    public List<User> selectSlaveUsers() {
        return baseMapper.selectList(null);
    }

    @Override
    public List<User> selectLambdaMasterUsers() {
        return this.lambdaQuery().list();
    }

    @Override
    public List<User> selectLambdaSlaveUsers() {
        return this.lambdaQuery().list();
    }

    @Override
    public List<User> selectSlaveAnnotationUsers() {
        return this.lambdaQuery().list();
    }

    @DS("#header:aa")
    @Override
    public void addUser(User user) {
        baseMapper.insert(user);
    }

    @Override
    public void deleteUserById(Long id) {
        baseMapper.deleteById(id);
    }


    @Override
    public void selectCeShi(){
        List<Map<String, Object>> map = baseMapper.selectCeShi();
        System.out.println(map);
    }
}
