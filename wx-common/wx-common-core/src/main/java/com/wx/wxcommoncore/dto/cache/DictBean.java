package com.wx.wxcommoncore.dto.cache;

import com.alibaba.fastjson.JSONObject;
import lombok.Builder;

import java.io.Serializable;

@Builder
public class DictBean implements Serializable {

    public DictBean(){
    }
    public DictBean(String code, String name, JSONObject children) {
        this.code = code;
        this.name = name;
        this.children = children;
    }
    private String code;

    private String name;

    private JSONObject children;

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

    public JSONObject getChildren() {
        if(children == null){
            children = new JSONObject(16);
        }
        return children;
    }

    public void setChildren(JSONObject children) {
        this.children = children;
    }
}
