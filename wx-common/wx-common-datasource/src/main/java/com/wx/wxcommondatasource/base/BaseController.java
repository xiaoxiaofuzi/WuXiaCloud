package com.wx.wxcommondatasource.base;

import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseController<S extends BaseService, T extends BaseModel> extends AbstractController{

    @Autowired
    protected S baseService;

    public S getBaseService() {
        return baseService;
    }





}
