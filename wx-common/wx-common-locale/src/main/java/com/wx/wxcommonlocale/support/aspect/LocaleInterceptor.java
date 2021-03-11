package com.wx.wxcommonlocale.support.aspect;

import com.alibaba.fastjson.JSON;
import com.wx.wxcommoncore.support.WxConstant;
import com.wx.wxcommoncore.utils.I18nUtils;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.Ordered;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.Locale;

@Slf4j
public class LocaleInterceptor implements MethodInterceptor, Ordered {

    @Override
    public int getOrder() {
        return WxConstant.LOCALE;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String locale = request.getHeader("locale");
            String[] split = locale.split("_");
            if (split.length == 2) {
                LocaleContextHolder.setLocale(new Locale(split[0], split[1]));
            }
        }
        Object result = invocation.proceed();
        try {
            if (result instanceof ResponseEntity) {
                ResponseEntity<?> resp = (ResponseEntity<?>) result;
                ModelMap body = null;
                if (resp.getBody() instanceof ModelMap) {
                    body = (ModelMap) resp.getBody();
                }
                if (body == null) {
                    return result;
                }
                String attribute = String.valueOf(body.getAttribute(WxConstant.ModeMap.MSG));
                String msg = StringUtils.isNotBlank(attribute) ? attribute : String.valueOf(body.getAttribute(WxConstant.ModeMap.CODE));
                String localMessage = I18nUtils.getMessage(msg, new Object() {
                });
                if (StringUtils.isNotBlank(localMessage)) {
                    body.addAttribute(WxConstant.ModeMap.MSG, localMessage);
                }
                log.info("response===>{}" , JSON.toJSONString(body));
            }
        } finally

        {
            LocaleContextHolder.resetLocaleContext();
        }

        return result;
    }

}