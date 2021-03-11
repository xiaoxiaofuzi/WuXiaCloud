package com.wx.wxceshi.entity;

import com.wx.wxcommondatasource.base.BaseModel;
import lombok.Data;

@Data
public class User extends BaseModel {

    private String name;

    private Integer age;
}
