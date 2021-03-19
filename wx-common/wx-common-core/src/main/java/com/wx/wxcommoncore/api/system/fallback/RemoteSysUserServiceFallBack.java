package com.wx.wxcommoncore.api.system.fallback;

import com.wx.wxcommoncore.api.system.RemoteSysUserService;
import com.wx.wxcommoncore.support.WxConstant;
import com.wx.wxcommoncore.support.http.HttpCode;
import com.wx.wxcommoncore.utils.I18nUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

@Component
public class RemoteSysUserServiceFallBack implements RemoteSysUserService {
    @Override
    public ResponseEntity<ModelMap> getSysUser() {
        return getModelMapResponseEntity();
    }

    @Override
    public ResponseEntity<ModelMap> addSysUser() {
        return getModelMapResponseEntity();
    }

    private ResponseEntity<ModelMap> getModelMapResponseEntity() {
        ModelMap modelMap = new ModelMap();
        modelMap.put(WxConstant.ModeMap.CODE, HttpCode.SERVICE_UNAVAILABLE.value().toString());
        String system = I18nUtils.getMessage("HTTPCODE_"+HttpCode.SERVICE_UNAVAILABLE.value().toString(), "system");
        modelMap.put(WxConstant.ModeMap.MSG, system);
        modelMap.put(WxConstant.ModeMap.TIMESTAMP, System.currentTimeMillis());
        return ResponseEntity.ok(modelMap);
    }
}
