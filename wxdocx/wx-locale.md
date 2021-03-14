# locale 国际化配置（springboot）

## 1. 创建文件

​		创建 bundle,这里实在 resources目录下新建的i18n目录中创建

![1615693477274](E:\WuXiaCloud\wxdocx\locale-images\创建bundle.png)

​        添加文件名：**messages(建议，也可以任意)**，并添加中文（zh_CN）,英文（en_US）两种语言。

![1615693857281](E:\WuXiaCloud\wxdocx\locale-images\名称.png)

​                         ![1615693898713](E:\WuXiaCloud\wxdocx\locale-images\语言.png)

 		添加完成，打开任意一个文件，切换编写模式，进行对应的文本编写。这里我们需要的文件已经创建完毕。

![1615694092529](E:\WuXiaCloud\wxdocx\locale-images\文本编写.png)

## 2. springboot自带配置类解析

springboot 有很多自动装配类:XXXAutoConfiguration,国际化配置->**MessageSourceAutoConfiguration**

```java
@Configuration(proxyBeanMethods = false)
/**
* AbstractApplicationContext.MESSAGE_SOURCE_BEAN_NAME = "messageSource"
* SearchStrategy.CURRENT：只搜索当前上下文。
* 这里注解表示：如果当前上下文环境中不存在 messageSource 类,就加载本配置文件中的 MessageSource。
*/
@ConditionalOnMissingBean(name = AbstractApplicationContext.MESSAGE_SOURCE_BEAN_NAME, search = SearchStrategy.CURRENT)
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
/**
* 满足 当前配置文件中的ResourceBundleCondition的匹配规则才会加载当前配置文件，详情请看下面具体解释
*/
@Conditional(ResourceBundleCondition.class)
@EnableConfigurationProperties
public class MessageSourceAutoConfiguration {

	private static final Resource[] NO_RESOURCES = {};

    /**
    * 属性配置：basename: i18n/messages（文件位置）
    * use-code-as-default-message: true（是否使用消息代码作为默认消息，而不是抛出“NoSuchMessageException”。仅在开发过程中推荐false）	
    * always-use-message-format: false(是否总是应用MessageFormat规则，甚至解析没有参数的消息,当前属性可以看出，我们的国际化文件可以写一些可以被解析的字符串，比如：帐号允许长度范围%s-%s)
    * encoding: UTF-8 （默认就是UTF-8）
    * cache-duration: PT-1S（国际化文件中的内容可以被缓存起来，如果不填写将永久被缓存，可以根据情况设置具体的过期时间，格式为：Duration，具体可以百度如何配置，获取查看源码【实际为正则表达式】）
    */
	@Bean
	@ConfigurationProperties(prefix = "spring.messages")
	public MessageSourceProperties messageSourceProperties() {
		return new MessageSourceProperties();
	}

    /**
    * 这里使用 ResourceBundleMessageSource 作为获取具体值得管理类，如果需要获取外部文件或者更多功能
    * 可以使用 ReloadableResourceBundleMessageSource，但这需要重写 MessageSource，和 类注解@ConditionalOnMissingBean 交相呼应。
    */
	@Bean
	public MessageSource messageSource(MessageSourceProperties properties) {
		ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
		if (StringUtils.hasText(properties.getBasename())) {
			messageSource.setBasenames(StringUtils
					.commaDelimitedListToStringArray(StringUtils.trimAllWhitespace(properties.getBasename())));
		}
		if (properties.getEncoding() != null) {
			messageSource.setDefaultEncoding(properties.getEncoding().name());
		}
		messageSource.setFallbackToSystemLocale(properties.isFallbackToSystemLocale());
		Duration cacheDuration = properties.getCacheDuration();
		if (cacheDuration != null) {
			messageSource.setCacheMillis(cacheDuration.toMillis());
		}
		messageSource.setAlwaysUseMessageFormat(properties.isAlwaysUseMessageFormat());
		messageSource.setUseCodeAsDefaultMessage(properties.isUseCodeAsDefaultMessage());
		return messageSource;
	}

    /**
    * 这里规则为：获取环境变量spring.messages.basename（默认为messages），也就是创建国际化文件的位置
    * 去查看是否存在，如果不存在，则没有匹配成功，配置类不加载（不具有国际化功能），反之加载配置文件。
    */
	protected static class ResourceBundleCondition extends SpringBootCondition {

		private static ConcurrentReferenceHashMap<String, ConditionOutcome> cache = new ConcurrentReferenceHashMap<>();

		@Override
		public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
			String basename = context.getEnvironment().getProperty("spring.messages.basename", "messages");
			ConditionOutcome outcome = cache.get(basename);
			if (outcome == null) {
				outcome = getMatchOutcomeForBasename(context, basename);
				cache.put(basename, outcome);
			}
			return outcome;
		}

		private ConditionOutcome getMatchOutcomeForBasename(ConditionContext context, String basename) {
			ConditionMessage.Builder message = ConditionMessage.forCondition("ResourceBundle");
			for (String name : StringUtils.commaDelimitedListToStringArray(StringUtils.trimAllWhitespace(basename))) {
				for (Resource resource : getResources(context.getClassLoader(), name)) {
					if (resource.exists()) {
						return ConditionOutcome.match(message.found("bundle").items(resource));
					}
				}
			}
			return ConditionOutcome.noMatch(message.didNotFind("bundle with basename " + basename).atAll());
		}

		private Resource[] getResources(ClassLoader classLoader, String name) {
			String target = name.replace('.', '/');
			try {
				return new PathMatchingResourcePatternResolver(classLoader)
						.getResources("classpath*:" + target + ".properties");
			}
			catch (Exception ex) {
				return NO_RESOURCES;
			}
		}

	}

}
```

## 3. 获取值

配置类加载好了，那么接下来如何获取值呢？

spring 提供了两种总方式：

### **LocaleContextHolder.getLocale()**

查看源码：重点看 如何获取 **LocaleContext**

```java
/**
* org.springframework.context.i18n.LocaleContextHolder
*/

private static final ThreadLocal<LocaleContext> localeContextHolder =
			new NamedThreadLocal<>("LocaleContext");

private static final ThreadLocal<LocaleContext> inheritableLocaleContextHolder =
			new NamedInheritableThreadLocal<>("LocaleContext");


public static Locale getLocale() {
	return getLocale(getLocaleContext());
}

public static Locale getLocale(@Nullable LocaleContext localeContext) {
    if (localeContext != null) {
        Locale locale = localeContext.getLocale();
        if (locale != null) {
            return locale;
        }
    }
 	return (defaultLocale != null ? defaultLocale : Locale.getDefault());
}

/**
* 这里看出 实际上是从 ThreadLocal 中获取，那么就需要直到是什么时候放入
* ThreadLocal 中。我们继续查看  setLocaleContext 方法
*/
@Nullable
public static LocaleContext getLocaleContext() {
    LocaleContext localeContext = localeContextHolder.get();
    if (localeContext == null) {
        localeContext = inheritableLocaleContextHolder.get();
    }
    return localeContext;
}

/**
* 那么是什么时候调用次方法得，我们需要使用debug模式验证一下。具体查看下面解析。
*/
public static void setLocaleContext(@Nullable LocaleContext localeContext, boolean inheritable) {
    if (localeContext == null) {
        resetLocaleContext();
    }
    else {
        if (inheritable) {
            inheritableLocaleContextHolder.set(localeContext);
            localeContextHolder.remove();
        }
        else {
            localeContextHolder.set(localeContext);
            inheritableLocaleContextHolder.remove();
        }
    }
}
```

#### debug追踪

1. 进入过滤器RequestContextFilter.initContextHolders()方法，获取request头中的accept-language来初始化 locale(默认是zh_CN),此时为SimpleLocaleContext

![1615726676338](E:\WuXiaCloud\wxdocx\locale-images\debug1.png)

![1615726722768](E:\WuXiaCloud\wxdocx\locale-images\debug2.png)

![1615726794705](E:\WuXiaCloud\wxdocx\locale-images\debug3.png)

实际就是将**国际化上下文（localeContext）**放入 ThreadLocal,再使用的时候取出来。

2. 进入DispatcherServlet.buildLocaleContext()方法，此时注意到LocaleContextResolver 进入我们的视野

![1615726876383](E:\WuXiaCloud\wxdocx\locale-images\debug4.png)

##### **解析器（LocaleResolver）的配置**

查看源码得知：localeResolver 来自项目初始化，会获取LocaleResolver 实例注入。此时我们就需要创建一个属于我们自己的解析器。**HeaderLocaleContextResolver**

```java

public static final String LOCALE_RESOLVER_BEAN_NAME = "localeResolver";

private void initLocaleResolver(ApplicationContext context) {
    try {
        this.localeResolver = context.getBean(LOCALE_RESOLVER_BEAN_NAME, LocaleResolver.class);
        if (logger.isTraceEnabled()) {
            logger.trace("Detected " + this.localeResolver);
        }
        else if (logger.isDebugEnabled()) {
            logger.debug("Detected " + this.localeResolver.getClass().getSimpleName());
        }
    }
    catch (NoSuchBeanDefinitionException ex) {
        // We need to use the default.
        this.localeResolver = getDefaultStrategy(context, LocaleResolver.class);
        if (logger.isTraceEnabled()) {
            logger.trace("No LocaleResolver '" + LOCALE_RESOLVER_BEAN_NAME +
                         "': using default [" + this.localeResolver.getClass().getSimpleName() + "]");
        }
    }
}
```

**HeaderLocaleContextResolver**.resolveLocaleContext() 设置我们自定义的**LocaleContext**放入ThreadLocal中。

```java
package com.wx.wxcommonlocale.support.i18n;

/**
* HeaderLocaleContextResolver
* 通过给定的请求解析当前地区上下文。这主要用于框架级处理;
* 考虑使用{@link org.springframework.web.servlet.support.RequestContextUtils}
* 或{@link org.springframework.web.servlet.support.RequestContext}
* 用于应用程序级访问当前地区和/或时区。返回的上下文可能是
* {@link org.springframework.context.i18n。TimeZoneAwareLocaleContext}，
* 包含一个地区和相关的时区信息。简单地应用{@code instanceof}检查并相应地进行向下转换。
* 自定义解析器实现还可以在返回的上下文中返回额外的设置，这些设置同样可以通过向下类型转换访问。
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
```

接下来继续追踪，进入FrameworkServlet.processRequest()方法的initContextHolders()将上一步自定义的国际化上下文放入ThreadLocal中。【DispatcherServlet 继承了 FrameworkServlet，在processRequest方法中，调用上一步buildLocaleContext()方法】

![1615727783676](E:\WuXiaCloud\wxdocx\locale-images\debug5.png)

```java
/**
* 将自定的 上下文  放入 当前线程中。
*/
private void initContextHolders(HttpServletRequest request,
			@Nullable LocaleContext localeContext, @Nullable RequestAttributes requestAttributes) {

		if (localeContext != null) {
			LocaleContextHolder.setLocaleContext(localeContext, this.threadContextInheritable);
		}
		if (requestAttributes != null) {
			RequestContextHolder.setRequestAttributes(requestAttributes, this.threadContextInheritable);
		}
	}
```

此时在看如何**LocaleContextHolder.getLocale()**获取值：实际上是从 自定义的方法中  获取。

```java
package com.wx.wxcommonlocale.support.i18n;

@Override
public LocaleContext resolveLocaleContext(final HttpServletRequest request) {
    return new TimeZoneAwareLocaleContext() {
        @Override
        @Nullable
        public Locale getLocale() {
            /**
            * 这里怎么来的？为什么要这样获取呢？请看下面解析。【使用拦截器实现】
            */
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
```

##### 拦截器的配置

当过滤器执行完成，就要执行拦截器的代码，这里可以看到，是获取自定义的头getParamName（）【locale】，来判断是那种语言。

```java
package com.wx.wxcommonlocale.support.i18n;

@Slf4j
public class HeaderLocaleChangeInterceptor extends LocaleChangeInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws ServletException {
        Locale locale = null;
        TimeZone timeZone = null;
        //从请求头中获取信息格式：zh_CN Asia/Shanghai      zh_CN GMT+8
        String newLocale = request.getHeader(getParamName());
        ...
            //****
                LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
                if (localeResolver == null) {
                    throw new IllegalStateException(
                            "No LocaleResolver found: not in a DispatcherServlet request?");
                }
                try {
                    //设置地区****
                    localeResolver.setLocale(request, response, locale);
                    if(locale != null){
                        request.setAttribute(HeaderLocaleContextResolver.LOCALE_REQUEST_ATTRIBUTE_NAME,locale);
                    }
                    //设置区时
                    if(timeZone != null){
                        request.setAttribute(HeaderLocaleContextResolver.TIME_ZONE_REQUEST_ATTRIBUTE_NAME,timeZone);
...
        // Proceed in any case.
        return true;
    }

}

```

其中**RequestContextUtils.getLocaleResolver(request);** 从截图中可以看到，是在请求时，将我们自定义的上下文也放入了 request 域中。

```java
@Nullable
public static LocaleResolver getLocaleResolver(HttpServletRequest request) {
    return (LocaleResolver) request.getAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE);
}
```

![1615728700118](E:\WuXiaCloud\wxdocx\locale-images\debug6.png)

**localeResolver.setLocale(request, response, locale);** 具体查看自定义上下文（**HeaderLocaleContextResolver**）的实现。

```java
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
        //*****
        request.setAttribute(localeAttributeName,
                (locale != null ? locale : getDefaultLocale()));
        //****
        request.setAttribute(timeZoneAttributeName,
                (timeZone != null ? timeZone : getDefaultTimeZone()));
    }


    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
        TimeZone timeZone = (TimeZone) request.getAttribute(HeaderLocaleContextResolver.TIME_ZONE_REQUEST_ATTRIBUTE_NAME);
        setLocaleContext(request, response, (locale != null ? timeZone != null ? new SimpleTimeZoneAwareLocaleContext(locale, timeZone) : new SimpleLocaleContext(locale) : null));
    }
```

其中**request.setAttribute(localeAttributeName,(locale != null ? locale : getDefaultLocale()));**正好解释了这里的问题。

![1615728924295](E:\WuXiaCloud\wxdocx\locale-images\wen1.png)

==这个介绍完了==





### RequestContextUtils.getLocale(request)

还是获取自定以的 国际化上下文，调用resolveLocale()方法获取Locale。

![1615729093050](E:\WuXiaCloud\wxdocx\locale-images\debug7.png)

继承关系【**HeaderLocaleContextResolver extends AbstractLocaleContextResolver**】

具体查看HeaderLocaleContextResolver .resolveLocaleContext()方法，上面有介绍

![1615729166434](E:\WuXiaCloud\wxdocx\locale-images\debug8.png)

实则还是从 request 域中获取值。

说白了就是 从 拦截器中 赋值。

### 总结

从上面介绍可以知道：我们需要一个拦截器：用来获取请求头中的语言，放入request 域中。

还需要一个国际化上下文解析器：将上下文放入 ThreadLocal 中。





## 4. 应用

### 4.1 配置文件

![1615694233065](E:\WuXiaCloud\wxdocx\locale-images\配置文件.png)

```yaml
spring:
    messages:
      # 默认 messages, 这里我们多了一层目录i18n
      basename: i18n/messages
      # 如果默认 false, 则会出现匹配不到就会跑异常（NoSuchMessageException）的情况
      use-code-as-default-message: true
      # 是否总是应用MessageFormat规则，即使是没有参数的消息也要解析, 默认 false
      always-use-message-format: false
```

### 4.2 WxLocaleConfigs

```java
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
                registry.addInterceptor(localeInterceptor).order(-1);
            }
        };
    }

}
```

## 附录：

### **HeaderLocaleContextResolver**

```java
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

```

### **HeaderLocaleChangeInterceptor**

```java
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
```