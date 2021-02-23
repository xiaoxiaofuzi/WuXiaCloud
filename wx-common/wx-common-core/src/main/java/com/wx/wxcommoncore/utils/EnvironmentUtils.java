package com.wx.wxcommoncore.utils;

import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
  *
  * @ClassName EnvironmentUtils
  * @author gh
  * @Description 上下文变量工具类
  * @Date 2021/2/23 0023 16:45
  * @Version 1.0
  **/
@Component
public class EnvironmentUtils implements EnvironmentAware {

    private static Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        EnvironmentUtils.environment = environment;
    }

    // 获取环境变量中的配置属性
    public static String searchByKey(String key){
        return EnvironmentUtils.environment.getProperty(key);
    }

}
