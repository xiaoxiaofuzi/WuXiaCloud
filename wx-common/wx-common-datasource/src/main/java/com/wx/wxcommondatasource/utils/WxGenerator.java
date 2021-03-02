package com.wx.wxcommondatasource.utils;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;

import java.util.HashMap;
import java.util.Map;

public class WxGenerator {



    /**
     * 测试 run 执行 注意：不生成service接口
     */
    public static void main(String[] args) {
        AutoGenerator mpg = new AutoGenerator();
        // 全局配置
        GlobalConfig gc = new GlobalConfig();
        gc.setOutputDir("F://tmp/");
        gc.setFileOverride(true);
        gc.setActiveRecord(false);
        // XML 二级缓存
        gc.setEnableCache(true);
        // XML ResultMap
        gc.setBaseResultMap(false);
        // XML columList
        gc.setBaseColumnList(false);
        gc.setOpen(false);
        gc.setAuthor("mysd");
        mpg.setGlobalConfig(gc);
        // 数据源配置
        DataSourceConfig dsc = new DataSourceConfig();
        dsc.setDbType(DbType.MYSQL);
        dsc.setDriverName("com.mysql.cj.jdbc.Driver");
        dsc.setUsername("root");
        dsc.setPassword("root");
        dsc.setUrl("jdbc:mysql://localhost:3306/demo?useUnicode=true&characterEncoding=utf8&allowMultiQueries=true&serverTimezone=PRC&useSSL=false");
        mpg.setDataSource(dsc);
        // 策略配置
        StrategyConfig strategy = new StrategyConfig();
        strategy.setRestControllerStyle(true);
        //strategy.setTablePrefix("wx_");// 此处可以修改为您的表前缀
        strategy.setNaming(NamingStrategy.underline_to_camel);// 表名生成策略
        strategy.setInclude("user"); // 需要生成的表
        // strategy.setExclude("test"); // 排除生成的表
        // 自定义实体父类
        strategy.setSuperEntityClass("com.wx.wxcommoncore.base.");
        // 自定义实体，公共字段
//        strategy.setSuperEntityColumns(
//                new String[]{"id", "enable", "remark", "create_by", "create_time", "update_by", "update_time"});
        // 自定义 mapper 父类
        strategy.setSuperMapperClass("com.wx.wxcommoncore.base.BaseMapper");
        // 自定义 service 父类
        strategy.setSuperServiceClass("com.wx.wxcommoncore.base.BaseService");
        // 自定义 service 实现类父类
        strategy.setSuperServiceImplClass("com.wx.wxcommoncore.base.BaseServiceImpl");
        // 自定义 controller 父类
        strategy.setSuperControllerClass("com.wx.wxcommoncore.base.BaseController");
        // 【实体】是否生成字段常量（默认 false）
        // public static final String ID = "test_id";
        // strategy.setEntityColumnConstant(true);
        // 【实体】是否为构建者模型（默认 false）
        // public User setName(String name) {this.name = name; return this;}
        // strategy.setEntityBuliderModel(true);
        strategy.setLogicDeleteFieldName("enable");
        mpg.setStrategy(strategy);
        // 注入自定义配置，可以在 VM 中使用 cfg.abc 设置的值
        InjectionConfig cfg = new InjectionConfig() {
            @Override
            public void initMap() {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("aa", true);
                this.setMap(map);
            }
        };
        mpg.setCfg(cfg);
        // 包配置
        PackageConfig pc = new PackageConfig();
        // 模块前缀
//        String prefix = "cmd";
        pc.setParent("com.wx");
        pc.setEntity("");
        pc.setMapper("" + ".mapper");
        pc.setXml("" + ".xml");
        pc.setService("" );
        pc.setServiceImpl("" + "service");
        pc.setController("" );
        mpg.setPackageInfo(pc);
        // 放置自己项目的 src/main/resources/template 目录下, 默认名称一下可以不配置，也可以自定义模板名称
        TemplateConfig tc = new TemplateConfig();
        tc.setEntity("tpl/entity.java.vm");
        tc.setMapper("tpl/mapper.java.vm");
        tc.setXml("tpl/mapper.xml.vm");
        tc.setService("tpl/iservice.java.vm");
        tc.setServiceImpl("tpl/serviceImpl.java.vm");
        tc.setController("tpl/controller.java.vm");
        mpg.setTemplate(tc);
        // 执行生成
        mpg.execute();
    }


}
