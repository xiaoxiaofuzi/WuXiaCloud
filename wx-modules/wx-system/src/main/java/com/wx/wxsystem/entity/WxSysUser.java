package com.wx.wxsystem.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.wx.wxcommondatasource.base.BaseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class WxSysUser extends BaseModel {

    @TableField("login")
    private String login;

    @TableField("password")
    private String password;

    @TableField("name")
    private String name;

}
