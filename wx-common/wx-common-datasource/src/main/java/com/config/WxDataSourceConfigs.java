package com.config;

import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
import com.github.pagehelper.autoconfigure.PageHelperAutoConfiguration;
import com.wx.wxcommondatasource.interceptor.WxMasterSlaveAutoRoutingPlugin;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * @author gh
 * AutoConfigureAfter 注解必须在 扫描包之外才能生效
 *
 *
 */
@Configuration
@EnableConfigurationProperties(DynamicDataSourceProperties.class)
//@DependsOn("pageHelperProperties")
@AutoConfigureAfter({PageHelperAutoConfiguration.class})
public class WxDataSourceConfigs {

    @Bean
    public WxDatasourceProperties wxDatasourceProperties(){
        return new WxDatasourceProperties();
    }

    @Autowired
    private List<SqlSessionFactory> sqlSessionFactoryList;

    @Autowired
    private WxDatasourceProperties wxDatasourceProperties;

    @Autowired
    private DynamicDataSourceProperties dynamicDataSourceProperties;

    /**
     * 功能描述: 如果设置数据库为主从，则添加 WxMasterSlaveAutoRoutingPlugin 拦截器
     *
     * 由于添加了 PageInterceptor（可以查看 PageHelperAutoConfiguration ），要想当前拦截器先执行，
     * 必须  让PageHelperAutoConfiguration先初始化，之后再执行 当前的拦截器
     *
     * @author gh
     * @Date 2021/3/2 0002 
     * @return void
     **/       
    @PostConstruct
    public void addPageInterceptor() {
        if(wxDatasourceProperties.isMasterSlave()){
            WxMasterSlaveAutoRoutingPlugin wxMasterSlaveAutoRoutingPlugin = new WxMasterSlaveAutoRoutingPlugin(dynamicDataSourceProperties);
            for (SqlSessionFactory sqlSessionFactory : sqlSessionFactoryList) {
                org.apache.ibatis.session.Configuration configuration = sqlSessionFactory.getConfiguration();
                if (!containsInterceptor(configuration, wxMasterSlaveAutoRoutingPlugin)) {
                    configuration.addInterceptor(wxMasterSlaveAutoRoutingPlugin);
                }
            }
        }

    }

    /**
     * 是否已经存在相同的拦截器
     *
     * @param configuration
     * @param interceptor
     * @return
     */
    private boolean containsInterceptor(org.apache.ibatis.session.Configuration configuration, Interceptor interceptor) {
        try {
            // getInterceptors since 3.2.2
            return configuration.getInterceptors().contains(interceptor);
        } catch (Exception e) {
            return false;
        }
    }


}
