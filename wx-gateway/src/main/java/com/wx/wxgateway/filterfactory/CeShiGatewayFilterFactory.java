package com.wx.wxgateway.filterfactory;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
public class CeShiGatewayFilterFactory extends AbstractGatewayFilterFactory<CeShiGatewayFilterFactory.Config> {


    public CeShiGatewayFilterFactory() {
        super(Config.class);
    }

    /**
     * name key.
     */
    public static final String AGE_KEY = "age";
    /**
     * sex key.
     */
    public static final String SEX_KEY = "sex";


    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList( AGE_KEY, NAME_KEY, SEX_KEY);
    }



    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            log.info(JSON.toJSONString(config));
            return chain.filter(exchange);
        });
    }


    public static class Config {
        private Integer age;

        private String name;

        private String sex;

        public String getSex() {
            return sex;
        }

        public void setSex(String sex) {
            this.sex = sex;
        }

        public Config() {
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
