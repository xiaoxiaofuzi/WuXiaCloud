package com.wx.wxgateway.filter;

import com.alibaba.fastjson.JSON;
import com.wx.wxgateway.routepredicatefactory.RequestBodyRoutePredicateFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
public class WxAuthGlobalFilter implements GlobalFilter,Ordered {


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        Map<String, Object> attributes = exchange.getAttributes();
        log.info(JSON.toJSONString(attributes));
        log.info(exchange.getAttribute(RequestBodyRoutePredicateFactory.REQUEST_BODY_ATTR));

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ServerHttpRequest request = exchange.getRequest();
        // 获取当前网关访问的URI
        String requestUri = request.getPath().pathWithinApplication().value();

        String path = exchange.getRequest().getURI().getPath();

        log.info(path);
        log.info(requestUri);

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
