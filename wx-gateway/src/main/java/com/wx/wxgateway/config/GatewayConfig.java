package com.wx.wxgateway.config;

import com.alibaba.csp.sentinel.annotation.aspectj.SentinelResourceAspect;
import com.wx.wxgateway.filter.WxAuthGlobalFilter;
import com.wx.wxgateway.filterfactory.CeShiGatewayFilterFactory;
import com.wx.wxgateway.handel.GatewayExceptionHandler;
import com.wx.wxgateway.handel.SentinelFallbackHandler;
import com.wx.wxgateway.routepredicatefactory.RequestBodyRoutePredicateFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 网关限流配置
 * 
 * @author ruoyi
 */
@Configuration
public class GatewayConfig {

    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }


    @Bean
    public SentinelResourceAspect sentinelResourceAspect() {
        return new SentinelResourceAspect();
    }


    //==== 自定义断言

    @Bean
    public RequestBodyRoutePredicateFactory requestBodyRoutePredicateFactory(){
        return new RequestBodyRoutePredicateFactory();
    }


    //===== 自定义服务过滤器

    @Bean
    public CeShiGatewayFilterFactory ceShiGatewayFilterFactory(){
        return new CeShiGatewayFilterFactory();
    }

    //===== 自定义全局过滤器

    @Bean
    public WxAuthGlobalFilter wxAuthGlobalFilter(){
        return new WxAuthGlobalFilter();
    }

/*此类在  SentinelSCGAutoConfiguration 中已经注册，无需再次注册*/
//    @Bean
//    @Order(Ordered.HIGHEST_PRECEDENCE)
//    public GlobalFilter sentinelGatewayFilter() {
//        return new SentinelGatewayFilter();
//    }

    //==== 自定义异常

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SentinelFallbackHandler sentinelGatewayExceptionHandler() {
        return new SentinelFallbackHandler();
    }


    @Bean
    @Order(-1)
    public GatewayExceptionHandler gatewayExceptionHandler() {
        return new GatewayExceptionHandler();
    }
}