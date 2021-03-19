package com.wx.wxcommoncore.api.system;

import com.wx.wxcommoncore.api.system.fallback.RemoteSysUserServiceFallBack;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(contextId = "remoteSysUserService", value = "wx-system",fallback = RemoteSysUserServiceFallBack.class)
public interface RemoteSysUserService {

     @GetMapping("/sys")
     ResponseEntity<ModelMap> getSysUser();

     @GetMapping("/sys/add")
     ResponseEntity<ModelMap> addSysUser();

}
