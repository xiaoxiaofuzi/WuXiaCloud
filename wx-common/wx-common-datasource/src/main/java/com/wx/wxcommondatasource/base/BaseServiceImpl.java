package com.wx.wxcommondatasource.base;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

public class BaseServiceImpl<M extends BaseMapper<T>,T extends BaseModel> extends ServiceImpl<M, T> implements BaseService<T> {


}
