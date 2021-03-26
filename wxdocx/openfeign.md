# Rest客户端：OpenFeign

# 1. 使用

## 1. 启动类添加注解

 @EnableFeignClients

```java
@SpringCloudApplication
@EnableFeignClients(basePackages = { "com.wx"})
public @interface WxCloudApplication {
}
```

## 2. 配置类

OpenFeign有自己默认的配置：org.springframework.cloud.openfeign.FeignClientsConfiguration。

提供了多种默认的bean:



可以自定义全局配置类，覆盖OpenFeign 默认的。



也可以自定义个性配置类，覆盖上面两种。







## 2. 申明接口

@FeignClient

> contextId : 接口标识，用作bean名称而不是名称，但不会被用作服务id。
>
> value： 带有可选协议前缀的服务名称，接口要访问的服务，无论是否提供url，都必须为所有客户端指定名称。可以指定为属性键，例如:${propertyKey}。
>
> fallback： 降级
>
> configuration： 配置类，

```java
@FeignClient(contextId = "remoteSysUserService", value = "wx-system",fallback = RemoteSysUserServiceFallBack.class,configuration = FeignAutoConfiguration.class)
public interface RemoteSysUserService {

     @GetMapping("/sys")
     ResponseEntity<ModelMap> getSysUser();

     @GetMapping("/sys/add")
     ResponseEntity<ModelMap> addSysUser();

}
```





