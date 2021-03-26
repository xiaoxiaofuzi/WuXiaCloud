package com.configs;

import feign.Contract;
import feign.Logger;
import org.springframework.context.annotation.Bean;

/**
 * Feign 自定义配置，覆盖全局配置，不要放到扫描包中
 *
 **/
public class CeshiConfiguration {

    @Bean
    public Logger.Level feignLogger(){
        Contract.Default aDefault = new Contract.Default();
        return Logger.Level.BASIC;
    }
}
