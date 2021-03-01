package com.wx.wxcommoncore.configs;

import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigFileApplicationListener;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
  *
  * @ClassName WxEnvironmentPostProcessor
  * @author gh
  * @Description 自定义配置文件加载到上下文环境中
  * @Date 2021/2/23 0023 16:14
  * @Version 1.0
  **/
public class WxEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String DEFAULT_SEARCH_PREFIXES = "classpath*:config/";
    private static final String DEFAULT_SEARCH_SUFFIX = "wx*.properties";
    private static final String YML_SEARCH_SUFFIX = "wx*.yml";

    private int order = ConfigFileApplicationListener.DEFAULT_ORDER + 2;

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        //获取所有的加载属性
        MutablePropertySources propertySources = environment.getPropertySources();
        //获取激活的版本
        String[] profiles = environment.getActiveProfiles();
        Properties props = getConfig(profiles);
        Properties ymlProps = getConfigByYml(profiles);
        props.putAll(ymlProps);
        propertySources.addLast(new PropertiesPropertySource("wxEnvironment", props));
    }

    private Properties getConfigByYml(String[] profiles) {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        List<Resource> resourceList = new ArrayList<>();
        addResources(resolver, resourceList, DEFAULT_SEARCH_PREFIXES + YML_SEARCH_SUFFIX);
        getConfigBySuffix(profiles, resolver, resourceList, YML_SEARCH_SUFFIX);
        YamlPropertiesFactoryBean config = new YamlPropertiesFactoryBean();
        config.setResources(resourceList.toArray(new Resource[]{}));
        config.afterPropertiesSet();
        return config.getObject();

    }

    private void getConfigBySuffix(String[] profiles, PathMatchingResourcePatternResolver resolver, List<Resource> resourceList, String ymlSearchSuffix) {
        if (profiles != null) {
            for (String p : profiles) {
                if (!StringUtils.isEmpty(p)) {
                    p = p + "/";
                }
                addResources(resolver, resourceList, DEFAULT_SEARCH_PREFIXES + p + ymlSearchSuffix);
            }
        }
    }


    private Properties getConfig(String[] profiles) {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        List<Resource> resourceList = new ArrayList<>();
        addResources(resolver, resourceList, DEFAULT_SEARCH_PREFIXES + DEFAULT_SEARCH_SUFFIX);
        getConfigBySuffix(profiles, resolver, resourceList, DEFAULT_SEARCH_SUFFIX);
        try {
            PropertiesFactoryBean config = new PropertiesFactoryBean();
            config.setLocations(resourceList.toArray(new Resource[]{}));
            config.afterPropertiesSet();
            return config.getObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



    /**
     * 加载配置文件
     */
    private void addResources(PathMatchingResourcePatternResolver resolver, List<Resource> resourceList, String path) {
        try {
            Resource[] resources = resolver.getResources(path);
            resourceList.addAll(Arrays.asList(resources));
        } catch (Exception e) {

        }
    }

    @Override
    public int getOrder() {
        return this.order;
    }
}
