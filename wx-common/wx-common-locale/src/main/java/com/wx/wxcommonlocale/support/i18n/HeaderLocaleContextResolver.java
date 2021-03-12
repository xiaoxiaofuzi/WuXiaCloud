package com.wx.wxcommonlocale.support.i18n;

import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.SimpleLocaleContext;
import org.springframework.context.i18n.SimpleTimeZoneAwareLocaleContext;
import org.springframework.context.i18n.TimeZoneAwareLocaleContext;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.i18n.AbstractLocaleContextResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;
import java.util.TimeZone;

public class HeaderLocaleContextResolver extends AbstractLocaleContextResolver {

    /**
     * 保存区域设置的会话属性的名称。
     * 仅在内部使用此实现。
     * 使用{@code RequestContext(Utils).getLocale()}
     * 在控制器或视图中检索当前区域设置。
     *
     * @see org.springframework.web.servlet.support.RequestContext#getLocale
     * @see org.springframework.web.servlet.support.RequestContextUtils#getLocale
     */
    public static final String LOCALE_REQUEST_ATTRIBUTE_NAME = HeaderLocaleContextResolver.class.getName() + ".LOCALE";

    public static final String TIME_ZONE_REQUEST_ATTRIBUTE_NAME = HeaderLocaleContextResolver.class.getName() + ".TIME_ZONE";

    private String localeAttributeName = LOCALE_REQUEST_ATTRIBUTE_NAME;

    private String timeZoneAttributeName = TIME_ZONE_REQUEST_ATTRIBUTE_NAME;


    private String localeHeadName;

    public String getLocaleHeadName() {
        return localeHeadName;
    }

    public void setLocaleHeadName(String localeHeadName) {
        this.localeHeadName = localeHeadName;
    }

    /**
     * 通过给定的请求解析当前地区上下文。这主要用于框架级处理;
     * 考虑使用{@link org.springframework.web.servlet.support.RequestContextUtils}
     * 或{@link org.springframework.web.servlet.support.RequestContext}
     * 用于应用程序级访问当前地区和/或时区。返回的上下文可能是
     * {@link org.springframework.context.i18n。TimeZoneAwareLocaleContext}，
     * 包含一个地区和相关的时区信息。简单地应用{@code instanceof}检查并相应地进行向下转换。
     * 自定义解析器实现还可以在返回的上下文中返回额外的设置，这些设置同样可以通过向下类型转换访问。
     *
     * @param request 解析地区上下文的请求
     * @see #resolveLocale(HttpServletRequest)
     * @see org.springframework.web.servlet.support.RequestContextUtils#getLocale
     * @see org.springframework.web.servlet.support.RequestContextUtils#getTimeZone
     */
    @Override
    public LocaleContext resolveLocaleContext(final HttpServletRequest request) {
        return new TimeZoneAwareLocaleContext() {
            @Override
            @Nullable
            public Locale getLocale() {
                Object attribute = request.getAttribute(localeAttributeName);
                return attribute != null ? (Locale) attribute : getDefaultLocale();
            }

            @Override
            @Nullable
            public TimeZone getTimeZone() {
                Object attribute = request.getAttribute(timeZoneAttributeName);
                return attribute != null ? (TimeZone) attribute : getDefaultTimeZone();
            }
        };
    }


    /**
     * 设置当前地区上下文为给定的一个，可能包括一个地区和相关的时区信息。
     *
     * @param request       用于修改区域设置的请求
     * @param response      用于区域设置修改的响应
     * @param localeContext 新的语言环境上下文，或{@code null}来清除语言环境
     * @throws UnsupportedOperationException，如果LocaleResolver实现不支持动态更改地区或时区
     * @see #setLocale(HttpServletRequest, HttpServletResponse, Locale)
     * @see org.springframework.context.i18n.SimpleLocaleContext
     * @see org.springframework.context.i18n.SimpleTimeZoneAwareLocaleContext
     */
    @Override
    public void setLocaleContext(HttpServletRequest request, HttpServletResponse response, LocaleContext localeContext) {
        Locale locale = null;
        TimeZone timeZone = null;
        if (localeContext != null) {
            locale = localeContext.getLocale();
            if (localeContext instanceof TimeZoneAwareLocaleContext) {
                timeZone = ((TimeZoneAwareLocaleContext) localeContext).getTimeZone();
            }
            //设置响应头，微服务之间传递
            response.setHeader(getLocaleHeadName(),
                    (locale != null ? locale.toString() : "-") + (timeZone != null ? ' ' + timeZone.getID() : ""));
        }
        request.setAttribute(localeAttributeName,
                (locale != null ? locale : getDefaultLocale()));
        request.setAttribute(timeZoneAttributeName,
                (timeZone != null ? timeZone : getDefaultTimeZone()));

    }


    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
        TimeZone timeZone = (TimeZone) request.getAttribute(HeaderLocaleContextResolver.TIME_ZONE_REQUEST_ATTRIBUTE_NAME);
        setLocaleContext(request, response, (locale != null ? timeZone != null ? new SimpleTimeZoneAwareLocaleContext(locale, timeZone) : new SimpleLocaleContext(locale) : null));
    }
}
