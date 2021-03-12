package com.wx.wxcommonlocale.support.i18n;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.util.WebUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;
import java.util.TimeZone;


@Slf4j
public class HeaderLocaleChangeInterceptor extends LocaleChangeInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws ServletException {
        Locale locale = null;
        TimeZone timeZone = null;
        //从请求头中获取信息格式：zh_CN Asia/Shanghai      zh_CN GMT+8
        String newLocale = request.getHeader(getParamName());
        if (newLocale != null) {
            //过滤不需要变化的方法
            if (checkHttpMethod(request.getMethod())) {
                String localePart = newLocale;
                String timeZonePart = null;
                int spaceIndex = localePart.indexOf(' ');
                if (spaceIndex != -1) {
                    localePart = newLocale.substring(0, spaceIndex);
                    timeZonePart = newLocale.substring(spaceIndex + 1);
                }
                try {
                    locale = (!"-".equals(localePart) ? parseLocaleValue(localePart) : null);
                    if (!StringUtils.isEmpty(timeZonePart)) {
                        timeZone = StringUtils.parseTimeZoneString(timeZonePart);
                    }
                } catch (IllegalArgumentException ex) {
                    if (request.getAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE) != null) {
                        // Error dispatch: ignore locale/timezone parse exceptions
                        log.info("ignore locale/timezone parse exceptions");
                    } else {
                        throw new IllegalStateException("Invalid locale Header '" + getParamName() +
                                "' with value [" + newLocale + "]: " + ex.getMessage());
                    }
                }

                LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
                if (localeResolver == null) {
                    throw new IllegalStateException(
                            "No LocaleResolver found: not in a DispatcherServlet request?");
                }
                try {
                    //设置地区
                    localeResolver.setLocale(request, response, locale);
                    if(locale != null){
                        request.setAttribute(HeaderLocaleContextResolver.LOCALE_REQUEST_ATTRIBUTE_NAME,locale);
                    }
                    //设置区时
                    if(timeZone != null){
                        request.setAttribute(HeaderLocaleContextResolver.TIME_ZONE_REQUEST_ATTRIBUTE_NAME,timeZone);
                    }
                }
                catch (IllegalArgumentException ex) {
                    if (isIgnoreInvalidLocale()) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Ignoring invalid locale value [" + newLocale + "]: " + ex.getMessage());
                        }
                    }
                    else {
                        throw ex;
                    }
                }
            }
        }
        // Proceed in any case.
        return true;
    }

    private boolean checkHttpMethod(String currentMethod) {
        String[] configuredMethods = getHttpMethods();
        if (ObjectUtils.isEmpty(configuredMethods)) {
            return true;
        }
        for (String configuredMethod : configuredMethods) {
            if (configuredMethod.equalsIgnoreCase(currentMethod)) {
                return true;
            }
        }
        return false;
    }

}
