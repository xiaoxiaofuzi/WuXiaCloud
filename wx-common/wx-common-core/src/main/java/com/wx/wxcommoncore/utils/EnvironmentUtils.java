package com.wx.wxcommoncore.utils;

import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.MissingResourceException;

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

    /**
     * Get a value based on key , if key does not exist , null is returned
     *
     * @param key
     * @return
     */
    public static String getString(String key) {
        return EnvironmentUtils.environment.getProperty(key);
    }

    /**
     * Get a value based on key , if key does not exist , null is returned
     *
     * @param key
     * @return
     */
    public static String getString(String key, String defaultValue) {
        try {
            String value = EnvironmentUtils.environment.getProperty(key);
            if (StringUtils.isEmpty(value)) {
                return defaultValue;
            }
            return value;
        } catch (MissingResourceException e) {
            return defaultValue;
        }
    }

    /**
     * 根据key获取值
     *
     * @param key
     * @return
     */
    public static Integer getInt(String key) {
        return EnvironmentUtils.environment.getProperty(key,Integer.class);
    }

    /**
     * 根据key获取值
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public static Integer getInt(String key, int defaultValue) {
        Integer value = EnvironmentUtils.environment.getProperty(key,Integer.class);
        if (StringUtils.isEmpty(value)) {
            return defaultValue;
        }
        return value;
    }

    /**
     * 从配置文件中取得 long 值
     * @param keyName 属性名
     * @return 属性值
     */
    public static Long getLong(String keyName) {
        return EnvironmentUtils.environment.getProperty(keyName,Long.class);
    }

    /**
     * 从配置文件中取得 long 值，若无（或解析异常）则返回默认值
     * @param keyName 属性名
     * @param defaultValue 默认值
     * @return 属性值
     */
    public static Long getLong(String keyName, long defaultValue) {
        Long value = EnvironmentUtils.environment.getProperty(keyName,Long.class);
        if (StringUtils.isEmpty(value)) {
            return defaultValue;
        }
        return value;
    }

    public static Boolean getBoolean(String key) {
        return EnvironmentUtils.environment.getProperty(key,Boolean.class);
    }

    /**
     * 根据key获取值
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public static Boolean getBoolean(String key, boolean defaultValue) {
        Boolean value = EnvironmentUtils.environment.getProperty(key,Boolean.class);
        if (StringUtils.isEmpty(value)) {
            return defaultValue;
        }
        return value;
    }

}
