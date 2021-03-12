package com.wx.wxcommonlocale.config;

import com.wx.wxcommoncore.utils.I18nUtils;
import com.wx.wxcommonlocale.support.i18n.HeaderLocaleChangeInterceptor;
import com.wx.wxcommonlocale.support.i18n.HeaderLocaleContextResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Locale;

@Configuration
@Slf4j
public class WxLocaleConfigs {

    private final String LOCAL_HEAD_NAME = "locale";

    @Bean
    public LocaleResolver localeResolver(final MessageSource messageSource) {
        I18nUtils.setMessageSource(messageSource);
        HeaderLocaleContextResolver localeResolver = new HeaderLocaleContextResolver();
        localeResolver.setLocaleHeadName(LOCAL_HEAD_NAME);
        localeResolver.setDefaultLocale(Locale.SIMPLIFIED_CHINESE);
        return localeResolver;
    }

    @Bean
    public WebMvcConfigurer localeInterceptor() {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                HeaderLocaleChangeInterceptor localeInterceptor = new HeaderLocaleChangeInterceptor();
                localeInterceptor.setParamName(LOCAL_HEAD_NAME);
                registry.addInterceptor(localeInterceptor);
            }
        };
    }

}
