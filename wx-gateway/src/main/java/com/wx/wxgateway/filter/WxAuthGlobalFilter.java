package com.wx.wxgateway.filter;

import com.alibaba.fastjson.JSON;
import com.wx.wxgateway.routepredicatefactory.RequestBodyRoutePredicateFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.util.Map;

@Slf4j
public class WxAuthGlobalFilter implements GlobalFilter,Ordered {

    @Autowired
    private RedisTemplate<Serializable,Serializable> redisTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        Map<String, Object> attributes = exchange.getAttributes();
        log.info(JSON.toJSONString(attributes));
        log.info(exchange.getAttribute(RequestBodyRoutePredicateFactory.REQUEST_BODY_ATTR));
        ServerHttpRequest request = exchange.getRequest();
        // 获取当前网关访问的URI
        String requestUri = request.getPath().pathWithinApplication().value();
        String path = exchange.getRequest().getURI().getPath();
        log.info("path:"+path);
        log.info("uri:"+requestUri);
        Mono<Void> filter = chain.filter(exchange);
        redisTemplate.opsForValue().set("uri",requestUri);
        return filter;
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
