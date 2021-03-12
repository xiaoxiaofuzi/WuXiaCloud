package com.wx.wxcommoncore.api.system;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

@Component
@FeignClient(contextId = "remoteSysUserService", value = "wx-system")
public interface RemoteSysUserService {

    @GetMapping("/sys")
     ResponseEntity<ModelMap> getSysUser();

}
