package com.wx.wxcommoncache.config;

import com.wx.wxcommoncache.cacheenum.CacheTypeEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author gh
 */
@Data
@ConfigurationProperties(prefix = "wx.cache")
//@ConfigurationProperties(prefix = WxConstant.PREFIX + WxConstant.DOT + WxCacheProperties.PREFIX)
public class WxCacheProperties {

    public static final String PREFIX = "cache";

    private CacheTypeEnum type = CacheTypeEnum.SINGLE;

    private String caffeineConfig;

    private boolean redisEnableTransaction;

    private Integer redisTtl;


}
