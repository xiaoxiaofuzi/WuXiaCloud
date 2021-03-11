package com.wx.wxceshi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wx.wxceshi.entity.User;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

public interface UserMapper extends BaseMapper<User> {
    List<Map<String, Object>> selectCeShi();
}
