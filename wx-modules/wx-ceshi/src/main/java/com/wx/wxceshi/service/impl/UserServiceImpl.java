package com.wx.wxceshi.service.impl;


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

    @Override
    public void addUser(User user) {
        baseMapper.insert(user);
    }

    @Override
    public void deleteUserById(Long id) {
        baseMapper.deleteById(id);
    }
}
