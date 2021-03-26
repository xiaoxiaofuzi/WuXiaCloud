package com.wx.wxcommoncore.api.system;

import com.configs.CeshiConfiguration;
import com.wx.wxcommoncore.api.system.fallback.RemoteSysUserServiceFallBack;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(contextId = "remoteSysUserService", value = "wx-system",fallback = RemoteSysUserServiceFallBack.class,configuration = CeshiConfiguration.class)
public interface RemoteSysUserService {

     @GetMapping("/sys")
     ResponseEntity<ModelMap> getSysUser();

     @GetMapping("/sys/add")
     ResponseEntity<ModelMap> addSysUser();

}
