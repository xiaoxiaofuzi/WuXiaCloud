package com.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author gh
 */
@Data
//@ConfigurationProperties(prefix = WxConstant.PREFIX + WxConstant.DOT + WxDatasourceProperties.PREFIX)
@ConfigurationProperties(prefix = "wx.datasource")
public class WxDatasourceProperties  {

    public static final String PREFIX = "datasource";

    private boolean masterSlave;

}
