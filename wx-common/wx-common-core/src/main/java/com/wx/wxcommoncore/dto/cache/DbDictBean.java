package com.wx.wxcommoncore.dto.cache;

import java.io.Serializable;

/**
  *
  * @ClassName DbDictBean
  * @author gh
  * @Description 数据库查询出的字典模型
  * @Date 2020/9/28 0028 15:29
  * @Version 1.0
  **/
public class DbDictBean implements Serializable {

    /**
     * 字典类型，如果没有，可以在查询数据的时候不赋值，会取存储的 缓存key作为字典类型。
     *
     * 字典类型没有的示例：查看 FoodTypeServiceImpl.afterPropertiesSet() 中查询数据库的sql。
     *
     * 字典类型存在的示例: 查看 FoodTypeServiceImpl.afterPropertiesSet() 中查询数据库的sql。
     *
     */
    private String dictId;

    private String code;

    private String name;

    private String parentCode;

    public String getDictId() {
        return dictId;
    }

    public void setDictId(String dictId) {
        this.dictId = dictId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParentCode() {
        return parentCode;
    }

    public void setParentCode(String parentCode) {
        this.parentCode = parentCode;
    }
}
