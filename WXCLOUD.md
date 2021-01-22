

## spring cloud alibaba

[组件版本关系](https://github.com/alibaba/spring-cloud-alibaba/wiki/%E7%89%88%E6%9C%AC%E8%AF%B4%E6%98%8E)

当前使用：

| Spring Cloud Alibaba Version | Sentinel Version | Nacos Version | Spring Cloud Version    | Spring Boot Version | Seata Version |
| ---------------------------- | ---------------- | ------------- | ----------------------- | ------------------- | ------------- |
| 2.2.4.RELEASE                | 1.8.0            | 1.4.1         | Spring Cloud Hoxton.SR8 | 2.3.2.RELEASE       | 1.3.0         |

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

