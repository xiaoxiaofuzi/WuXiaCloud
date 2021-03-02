package com.wx.wxcommoncache.cacheenum;


public enum CacheTypeEnum {

    /**
     *  单redis
     */
    SINGLE("reids缓存"),
    /**
     *  redis + caffeine
     */
    MULTI("caffeine+redis多级缓存");

    CacheTypeEnum(String desc){
        this.desc = desc;
    }

    private String desc;

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
