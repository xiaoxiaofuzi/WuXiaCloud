package com.wx.wxcommonlocale.config;

import com.wx.wxcommoncore.utils.I18nUtils;
import com.wx.wxcommonlocale.support.aspect.LocaleInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultBeanFactoryPointcutAdvisor;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class WxLocaleConfigs {

    @Bean
    public LocaleInterceptor localeInterceptor(final MessageSource messageSource) {
        I18nUtils.setMessageSource(messageSource);
        return new LocaleInterceptor();
    }

    @Bean
    public Advisor localeAdvisor(final LocaleInterceptor localeInterceptor) {
        AspectJExpressionPointcut localePointcut = new AspectJExpressionPointcut();
        localePointcut.setExpression(
                "@within(org.springframework.stereotype.Controller) || @within(org.springframework.web.bind.annotation.RestController)");
        DefaultBeanFactoryPointcutAdvisor apiLogAdvisor = new DefaultBeanFactoryPointcutAdvisor();
        apiLogAdvisor.setAdvice(localeInterceptor);
        apiLogAdvisor.setPointcut(localePointcut);
        return apiLogAdvisor;
    }


}
