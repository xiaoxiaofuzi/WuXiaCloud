package com.wx.wxcommoncore.utils;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

/**
  *
  * @ClassName I18nUtils
  * @author gh
  * @Description:  springboot自动配置国际化 {@link org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration}
 *      可以使用 ResourceBundleMessageSource 来进行获取具体指
 *
 *
  * @Date 2021/3/11 0011 11:35
  * @Version 1.0
  **/
public class I18nUtils{

    private static MessageSource messageSource;

    public static void setMessageSource(MessageSource messageSource) {
        I18nUtils.messageSource = messageSource;
    }

    /** 国际化信息 */
    public static String getMessage(String key, Object... params) {
        if(messageSource == null){
            return null;
        }
        return messageSource.getMessage(key,params,LocaleContextHolder.getLocale());
    }


    public static String getMessage(String key) {
        return getMessage(key,new Object(){});
    }

}
