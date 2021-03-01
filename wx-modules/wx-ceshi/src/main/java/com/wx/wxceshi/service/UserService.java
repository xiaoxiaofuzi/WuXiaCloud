package com.wx.wxceshi.service;



import com.baomidou.mybatisplus.extension.service.IService;
import com.wx.wxceshi.entity.User;

import java.util.List;

public interface UserService extends IService<User> {

    List<User> selectMasterUsers();

    List<User> selectSlaveUsers();

    List<User> selectLambdaMasterUsers();

    List<User> selectLambdaSlaveUsers();

    List<User> selectSlaveAnnotationUsers();

    void addUser(User user);

    void deleteUserById(Long id);
}
