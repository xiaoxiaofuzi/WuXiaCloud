package com.wx.wxceshi.service.impl;


import com.baomidou.dynamic.datasource.annotation.DS;
import com.wx.wxceshi.entity.User;
import com.wx.wxceshi.mapper.UserMapper;
import com.wx.wxceshi.service.UserService;
import com.wx.wxcommoncore.api.system.RemoteSysUserService;
import com.wx.wxcommondatasource.base.BaseServiceImpl;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;

import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl extends BaseServiceImpl<UserMapper, User> implements UserService,ApplicationContextAware {

    private UserServiceImpl _this;

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


    /*分布式事务*/

    @Autowired
    private RemoteSysUserService remoteSysUserService;

    @DS("#header:aa")
    @GlobalTransactional(propagation = io.seata.tm.api.transaction.Propagation.REQUIRED,rollbackFor = Exception.class)
    @Override
    public User addSysUser(User user) {
        _this.saveOrUpdate(user);
        ResponseEntity<ModelMap> responseEntity = remoteSysUserService.addSysUser();
        System.out.println("system系统返回:"+responseEntity);
        int i = 1/0;
        return user;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        UserServiceImpl bean = applicationContext.getBean(UserServiceImpl.class);
        this._this = bean;
    }
}
