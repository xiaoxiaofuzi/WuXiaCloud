package com.wx.wxceshi.service.impl;


import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.dynamic.datasource.annotation.Slave;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wx.wxceshi.entity.User;
import com.wx.wxceshi.mapper.UserMapper;
import com.wx.wxceshi.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public List<User> selectMasterUsers() {
        return baseMapper.selectList(null);
    }

    @Override
    @DS("slave")
    public List<User> selectSlaveUsers() {
        return baseMapper.selectList(null);
    }

    @Override
    public List<User> selectLambdaMasterUsers() {
        return this.lambdaQuery().list();
    }

    @Override
    @DS("slave")
    public List<User> selectLambdaSlaveUsers() {
        return this.lambdaQuery().list();
    }

    @Override
    @Slave
    public List<User> selectSlaveAnnotationUsers() {
        return this.lambdaQuery().list();
    }

    @Override
    @DS("slave")
    public void addUser(User user) {
        baseMapper.insert(user);
    }

    @Override
    public void deleteUserById(Long id) {
        baseMapper.deleteById(id);
    }
}
