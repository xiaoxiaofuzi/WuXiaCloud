package com.wx.wxcommoncache.config;

import com.wx.wxcommoncache.service.impl.RedissonHelper;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Objects;

/**
 * @author gh
 */
@Configuration
@ConditionalOnClass(RedissonClient.class)
public class RedissonConfig {

    private static final int DEFAULT_TIME_OUT = 6000;

    @Autowired
    private RedisProperties redisProperties;

    @Bean
    public RedissonClient redissonClient() {

        int timeout = DEFAULT_TIME_OUT;
        Duration duration = redisProperties.getTimeout();
        if(Objects.nonNull(duration)){
            timeout = (int)duration.getSeconds() * 1000;
        }

        Config config = new Config();
        //判断使用了那种redis配置
        RedisProperties.Sentinel sentinel;
        RedisProperties.Cluster cluste;
        if(Objects.nonNull(cluste = isCluster())){
            config.useClusterServers().addNodeAddress(cluste.getNodes().toArray(new String[0]))
                    .setPassword(redisProperties.getPassword())
                    .setTimeout(timeout);
        }else if(Objects.nonNull(sentinel = isSentinel())){
            config.useSentinelServers().addSentinelAddress(sentinel.getNodes().toArray(new String[0]))
                    .setMasterName(sentinel.getMaster())
                    .setDatabase(redisProperties.getDatabase())
                    .setPassword(redisProperties.getPassword())
                    .setTimeout(timeout);
        }else {
            config.useSingleServer().setAddress(String.format("redis://%s:%s"
                    ,redisProperties.getHost()
                    ,redisProperties.getPort()))
                    .setDatabase(redisProperties.getDatabase())
                    .setPassword(redisProperties.getPassword())
                    .setTimeout(timeout);
        }
        return Redisson.create(config);
    }

    private RedisProperties.Sentinel isSentinel() {
        return redisProperties.getSentinel();
    }

    private RedisProperties.Cluster isCluster() {
        return redisProperties.getCluster();
    }



    @Bean
    public RedissonHelper redissonHelper(RedissonClient redissonClient){
        return new RedissonHelper(redissonClient);
    }
}
