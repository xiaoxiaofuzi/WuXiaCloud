

# spring cloud Gateway

 [官方文档](https://docs.spring.io/spring-cloud-gateway/docs/2.2.6.RELEASE/reference/html/)

https://www.cnblogs.com/crazymakercircle/p/11704077.html

### 工作原理

![1612257597602](.\img\gateway原理.png)



springcloud-限流（gateway、sentinel）：https://blog.csdn.net/qq_43220949/article/details/113100098





## GatewayFilter工厂

[`GatewayFilter`工厂](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#gatewayfilter-factories)

这里简单将Spring Cloud Gateway内置的所有过滤器工厂整理成了一张表格。如下：https://www.cnblogs.co	m/60kmph/p/14326217.html

| 过滤器工厂                  | 作用                                                         | 参数                                                         |
| :-------------------------- | :----------------------------------------------------------- | :----------------------------------------------------------- |
| AddRequestHeader            | 为原始请求添加Header                                         | Header的名称及值                                             |
| AddRequestParameter         | 为原始请求添加请求参数                                       | 参数名称及值                                                 |
| AddResponseHeader           | 为原始响应添加Header                                         | Header的名称及值                                             |
| DedupeResponseHeader        | 剔除响应头中重复的值                                         | 需要去重的Header名称及去重策略                               |
| Hystrix                     | 为路由引入Hystrix的断路器保护                                | `HystrixCommand`的名称                                       |
| FallbackHeaders             | 为fallbackUri的请求头中添加具体的异常信息                    | Header的名称                                                 |
| PrefixPath                  | 为原始请求路径添加前缀                                       | 前缀路径                                                     |
| PreserveHostHeader          | 为请求添加一个preserveHostHeader=true的属性，路由过滤器会检查该属性以决定是否要发送原始的Host | 无                                                           |
| RequestRateLimiter          | 用于对请求限流，限流算法为令牌桶                             | keyResolver、rateLimiter、statusCode、denyEmptyKey、emptyKeyStatus |
| RedirectTo                  | 将原始请求重定向到指定的URL                                  | http状态码及重定向的url                                      |
| RemoveHopByHopHeadersFilter | 为原始请求删除IETF组织规定的一系列Header                     | 默认就会启用，可以通过配置指定仅删除哪些Header               |
| RemoveRequestHeader         | 为原始请求删除某个Header                                     | Header名称                                                   |
| RemoveResponseHeader        | 为原始响应删除某个Header                                     | Header名称                                                   |
| RewritePath                 | 重写原始的请求路径                                           | 原始路径正则表达式以及重写后路径的正则表达式                 |
| RewriteResponseHeader       | 重写原始响应中的某个Header                                   | Header名称，值的正则表达式，重写后的值                       |
| SaveSession                 | 在转发请求之前，强制执行`WebSession::save`操作               | 无                                                           |
| secureHeaders               | 为原始响应添加一系列起安全作用的响应头                       | 无，支持修改这些安全响应头的值                               |
| SetPath                     | 修改原始的请求路径                                           | 修改后的路径                                                 |
| SetResponseHeader           | 修改原始响应中某个Header的值                                 | Header名称，修改后的值                                       |
| SetStatus                   | 修改原始响应的状态码                                         | HTTP 状态码，可以是数字，也可以是字符串                      |
| StripPrefix                 | 用于截断原始请求的路径                                       | 使用数字表示要截断的路径的数量                               |
| Retry                       | 针对不同的响应进行重试                                       | retries、statuses、methods、series                           |
| RequestSize                 | 设置允许接收最大请求包的大小。如果请求包大小超过设置的值，则返回 `413 Payload Too Large` | 请求包大小，单位为字节，默认值为5M                           |
| ModifyRequestBody           | 在转发请求之前修改原始请求体内容                             | 修改后的请求体内容                                           |
| ModifyResponseBody          | 修改原始响应体的内容                                         | 修改后的响应体内容                                           |
| Default                     | 为所有路由添加过滤器                                         | 过滤器工厂名称及值                                           |

**Tips：**每个过滤器工厂都对应一个实现类，并且这些类的名称必须以`GatewayFilterFactory`结尾，这是Spring Cloud Gateway的一个约定，例如`AddRequestHeader`对应的实现类为`AddRequestHeaderGatewayFilterFactory`。





## 全局过滤器

当请求与路由匹配时，过滤Web处理程序会将`GlobalFilter`的所有实例和所有特定`GatewayFilter`于路由的实例添加到过滤器链中。该组合的过滤器链按`org.springframework.core.Ordered`接口排序，您可以通过实现该`getOrder()`方法进行设置。

由于Spring Cloud Gateway区分了执行过滤器逻辑的“前”阶段和“后”阶段，因此优先级最高的过滤器是“前”阶段的第一个，而“后”阶段的最后一个-相。







下表列出了Spring Cloud Gateway执行器端点（请注意，每个端点都`/actuator/gateway`作为基本路径）：

| ID              | HTTP方法 | 描述                                          |
| :-------------- | :------- | :-------------------------------------------- |
| `globalfilters` | GET      | 显示应用于路由的全局过滤器列表。              |
| `routefilters`  | GET      | 显示`GatewayFilter`应用于特定路线的工厂列表。 |
| `refresh`       | POST     | 清除路由缓存。                                |
| `routes`        | GET      | 显示网关中定义的路由列表。                    |
| `routes/{id}`   | GET      | 显示有关特定路线的信息。                      |
| `routes/{id}`   | POST     | 将新路由添加到网关。                          |
| `routes/{id}`   | DELETE   | 从网关中删除现有路由。                        |

## 开发

### 1.自定义断言

您需要实现`RoutePredicateFactory`。有一个`AbstractRoutePredicateFactory`可以扩展的抽象类。

MyRoutePredicateFactory.java

```java
public class MyRoutePredicateFactory extends AbstractRoutePredicateFactory<HeaderRoutePredicateFactory.Config> {

    public MyRoutePredicateFactory() {
        super(Config.class);
    }

    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        // grab configuration from Config object
        return exchange -> {
            //grab the request
            ServerHttpRequest request = exchange.getRequest();
            //take information from the request to see if it
            //matches configuration.
            return matches(config, request);
        };
    }

    public static class Config {
        //Put the configuration properties for your filter here
    }

}
```





### 2.自定义配置过滤器

您必须实现`GatewayFilterFactory`。您可以扩展名为的抽象类`AbstractGatewayFilterFactory`。以下示例显示了如何执行此操作：

自定义过滤器类名称应以结尾`GatewayFilterFactory`。

例如，要引用`Something`配置文件中命名的过滤器，该过滤器必须位于名为的类中`SomethingGatewayFilterFactory`。

例子72. PreGatewayFilterFactory.java

```java
public class PreGatewayFilterFactory extends AbstractGatewayFilterFactory<PreGatewayFilterFactory.Config> {

    public PreGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        // grab configuration from Config object
        return (exchange, chain) -> {
            //If you want to build a "pre" filter you need to manipulate the
            //request before calling chain.filter
            ServerHttpRequest.Builder builder = exchange.getRequest().mutate();
            //use builder to manipulate the request
            return chain.filter(exchange.mutate().request(builder.build()).build());
        };
    }

    public static class Config {
        //Put the configuration properties for your filter here
    }

}
```

PostGatewayFilterFactory.java

```java
public class PostGatewayFilterFactory extends AbstractGatewayFilterFactory<PostGatewayFilterFactory.Config> {

    public PostGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        // grab configuration from Config object
        return (exchange, chain) -> {
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                ServerHttpResponse response = exchange.getResponse();
                //Manipulate the response in some way
            }));
        };
    }

    public static class Config {
        //Put the configuration properties for your filter here
    }

}
```



### 3.自定义全局顾虑器

必须实现`GlobalFilter`接口。这会将过滤器应用于所有请求。

以下示例说明如何分别设置全局前置和后置过滤器：

```java
@Bean
public GlobalFilter customGlobalFilter() {
    return (exchange, chain) -> exchange.getPrincipal()
        .map(Principal::getName)
        .defaultIfEmpty("Default User")
        .map(userName -> {
          //adds header to proxied request
          exchange.getRequest().mutate().header("CUSTOM-REQUEST-HEADER", userName).build();
          return exchange;
        })
        .flatMap(chain::filter);
}

@Bean
public GlobalFilter customGlobalPostFilter() {
    return (exchange, chain) -> chain.filter(exchange)
        .then(Mono.just(exchange))
        .map(serverWebExchange -> {
          //adds header to response
          serverWebExchange.getResponse().getHeaders().set("CUSTOM-RESPONSE-HEADER",
              HttpStatus.OK.equals(serverWebExchange.getResponse().getStatusCode()) ? "It worked": "It did not work");
          return serverWebExchange;
        })
        .then();
}
```



## gateWay所有配置

https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/appendix.html



# spring cloud alibaba

[组件版本关系](https://github.com/alibaba/spring-cloud-alibaba/wiki/%E7%89%88%E6%9C%AC%E8%AF%B4%E6%98%8E)



## 毕业版本依赖关系(推荐使用)

| Spring Cloud Version | Spring Cloud Alibaba Version | Spring Boot Version |
| -------------------- | ---------------------------- | ------------------- |
| Spring Cloud 2020.0  | 2020.0.RC1                   | 2.4.2.RELEASE       |



当前使用：

| Spring Cloud Alibaba Version | Sentinel Version | Nacos Version | Spring Cloud Version | Spring Boot Version | Seata Version |
| ---------------------------- | ---------------- | ------------- | -------------------- | ------------------- | ------------- |
| 2020.0.RC1                   | 1.8.0            | 1.4.1         | 2020.0.2             | 2.4.2               | 1.4.0         |

## Nacos Config

 [官方文档](https://github.com/alibaba/spring-cloud-alibaba/wiki/Nacos-config )

### pom 配置

```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
</dependency>
```

### yml配置 

必须使用 bootstrap.yml配置文件来配置Nacos Server 地址

```yaml
spring:
    #springboot 2.4 版本 加载配置改革了
    config:
      activate:
        on-profile: ${spring.profiles.active}
    # 环境 dev|test|prod
    profiles:
      active: ${spring.profiles.active}
    application:
      name: wx-gateway
    cloud:
      nacos:
        config:
          server-addr: 127.0.0.1:8848
           # 配置文件格式
          file-extension: yml
          #多个 Data Id 同时配置时，他的优先级关系是 spring.cloud.nacos.config.extension-configs[n].data-id 其中 n 的值越大，优先级越高。
          extension-configs[0]:
           data-id: application-${spring.profiles.active}.${spring.cloud.nacos.config.file-extension}
          shared-configs[0]:
           data-id: application-${spring.profiles.active}.${spring.cloud.nacos.config.file-extension}
```



extension-configs 刷新属性

```properties
spring.application.name=opensource-service-provider
spring.cloud.nacos.config.server-addr=127.0.0.1:8848

# config external configuration
# 1、Data Id 在默认的组 DEFAULT_GROUP,不支持配置的动态刷新
spring.cloud.nacos.config.extension-configs[0].data-id=ext-config-common01.properties

# 2、Data Id 不在默认的组，不支持动态刷新
spring.cloud.nacos.config.extension-configs[1].data-id=ext-config-common02.properties
spring.cloud.nacos.config.extension-configs[1].group=GLOBALE_GROUP

# 3、Data Id 既不在默认的组，也支持动态刷新
spring.cloud.nacos.config.extension-configs[2].data-id=ext-config-common03.properties
spring.cloud.nacos.config.extension-configs[2].group=REFRESH_GROUP
spring.cloud.nacos.config.extension-configs[2].refresh=true
```





### 配置的优先级

Spring Cloud Alibaba Nacos Config 目前提供了三种配置能力从 Nacos 拉取相关的配置。

- A: 通过 `spring.cloud.nacos.config.shared-configs[n].data-id` 支持多个共享 Data Id 的配置
- B: 通过 `spring.cloud.nacos.config.extension-configs[n].data-id` 的方式支持多个扩展 Data Id 的配置
- C: 通过内部相关规则(应用名、应用名+ Profile )自动生成相关的 Data Id 配置，本例为：
  - wx-gateway.yml （${spring.application.name}.${file-extension:properties}）
  - wx-gateway-Profile .yml（${spring.application.name}-${profile}.${file-extension:properties}）

当三种方式共同使用时，他们的一个优先级关系是:A < B < C

### 完全关闭配置

通过设置 spring.cloud.nacos.config.enabled = false 来完全关闭 Spring Cloud Nacos Config	



## Nacos Discovery

 [官方文档](https://github.com/alibaba/spring-cloud-alibaba/wiki/Nacos-discovery)

### pom 配置

```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
</dependency>
```

### yml配置 

```yaml
spring:
    # 环境 dev|test|prod
    profiles:
        active: ${spring.profiles.active}
    application:
      name: wx-gateway
    cloud:
      nacos:
        discovery:
          server-addr: 127.0.0.1:8848
      
management:
  endpoints:
    web:
      exposure:
        include: '*'
        exclude: heapdump,dump,threaddump,configprops,env
```

### 关于 Nacos Starter 更多的配置项信息

更多关于 spring-cloud-starter-alibaba-nacos-discovery 的 starter 配置项如下所示:

| 配置项                | Key                                              | 默认值                       | 说明                                                         |
| --------------------- | :----------------------------------------------- | ---------------------------- | ------------------------------------------------------------ |
| `服务端地址`          | `spring.cloud.nacos.discovery.server-addr`       | `无`                         | `Nacos Server 启动监听的ip地址和端口`                        |
| `服务名`              | `spring.cloud.nacos.discovery.service`           | `${spring.application.name}` | `给当前的服务命名`                                           |
| `服务分组`            | `spring.cloud.nacos.discovery.group`             | `DEFAULT_GROUP`              | `设置服务所处的分组`                                         |
| `权重`                | `spring.cloud.nacos.discovery.weight`            | `1`                          | `取值范围 1 到 100，数值越大，权重越大`                      |
| `网卡名`              | `spring.cloud.nacos.discovery.network-interface` | `无`                         | `当IP未配置时，注册的IP为此网卡所对应的IP地址，如果此项也未配置，则默认取第一块网卡的地址` |
| `注册的IP地址`        | `spring.cloud.nacos.discovery.ip`                | `无`                         | `优先级最高`                                                 |
| `注册的端口`          | `spring.cloud.nacos.discovery.port`              | `-1`                         | `默认情况下不用配置，会自动探测`                             |
| `命名空间`            | `spring.cloud.nacos.discovery.namespace`         | `无`                         | `常用场景之一是不同环境的注册的区分隔离，例如开发测试环境和生产环境的资源（如配置、服务）隔离等。` |
| `AccessKey`           | `spring.cloud.nacos.discovery.access-key`        | `无`                         | `当要上阿里云时，阿里云上面的一个云账号名`                   |
| `SecretKey`           | `spring.cloud.nacos.discovery.secret-key`        | `无`                         | `当要上阿里云时，阿里云上面的一个云账号密码`                 |
| `Metadata`            | `spring.cloud.nacos.discovery.metadata`          | `无`                         | `使用Map格式配置，用户可以根据自己的需要自定义一些和服务相关的元数据信息` |
| `日志文件名`          | `spring.cloud.nacos.discovery.log-name`          | `无`                         |                                                              |
| `集群`                | `spring.cloud.nacos.discovery.cluster-name`      | `DEFAULT`                    | `配置成Nacos集群名称`                                        |
| `接入点`              | `spring.cloud.nacos.discovery.enpoint`           | `UTF-8`                      | `地域的某个服务的入口域名，通过此域名可以动态地拿到服务端地址` |
| `是否集成Ribbon`      | `ribbon.nacos.enabled`                           | `true`                       | `一般都设置成true即可`                                       |
| `是否开启Nacos Watch` | `spring.cloud.nacos.discovery.watch.enabled`     | `true`                       | `可以设置成false来关闭 watch`                                |



# Wx-Cache模块

缓存模块，使用spring缓存，实现方式有两种：

- 单缓存（redis）
- 多级缓存（caffeine+redis）



# Wx-DataSource模块

数据源模块，使用 druid 数据源。插件使用：

1. [mybatis-plus](https://mybatis.plus/guide/)
2. [dynamic-datasource](https://dynamic-datasource.com/guide/)

## pom

```xml
<!--mysql连接-->
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
</dependency>

<!--druid-->
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>druid-spring-boot-starter</artifactId>
</dependency>

<!--多数据源-->
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>dynamic-datasource-spring-boot-starter</artifactId>
</dependency>

<!-- https://mvnrepository.com/artifact/com.baomidou/mybatis-plus-boot-starter -->
<!--mybatis-plus-->
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-boot-starter</artifactId>
</dependency>
```

### 多数据源源码分析

```java
//DynamicDataSourceAutoConfiguration

@Role(value = BeanDefinition.ROLE_INFRASTRUCTURE)
@Bean
@ConditionalOnMissingBean
public DynamicDataSourceAnnotationAdvisor dynamicDatasourceAnnotationAdvisor(DsProcessor dsProcessor) {
                    DynamicDataSourceAnnotationInterceptor interceptor = new DynamicDataSourceAnnotationInterceptor(properties.isAllowedPublicOnly(), dsProcessor);
                    DynamicDataSourceAnnotationAdvisor advisor = new DynamicDataSourceAnnotationAdvisor(interceptor);
                    advisor.setOrder(properties.getOrder());
                    return advisor;
}

/**
* 这个为
*/
@Bean
@ConditionalOnMissingBean
public DsProcessor dsProcessor() {
          DsHeaderProcessor headerProcessor = new DsHeaderProcessor();
          DsSessionProcessor sessionProcessor = new DsSessionProcessor();
          DsSpelExpressionProcessor spelExpressionProcessor = new DsSpelExpressionProcessor();
          headerProcessor.setNextProcessor(sessionProcessor);
          sessionProcessor.setNextProcessor(spelExpressionProcessor);
          return headerProcessor;
}


```

# [WX-LOCALE  模块](./wx-locale.md)

