package com.wx.wxcommoncore.exception;

import com.alibaba.fastjson.JSON;
import com.wx.wxcommoncore.support.WxConstant;
import com.wx.wxcommoncore.support.http.HttpCode;
import com.wx.wxcommoncore.utils.I18nUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestControllerAdvice
@Configuration
@Slf4j
public class WxGlobalExceptionAdvice {

    /** 异常处理 */
    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ModelMap> exceptionHandler(HttpServletRequest request, HttpServletResponse response,
                                                     Throwable ex) {
        log.error(WxConstant.EXCEPTION_HEAD, ex);
        ModelMap modelMap = new ModelMap();
        if (ex instanceof BaseException) {
            ((BaseException)ex).handler(modelMap);
        } else if (ex instanceof IllegalArgumentException) {
            String code = HttpCode.BAD_REQUEST.value().toString();
            modelMap.put(WxConstant.ModeMap.CODE, code);
            modelMap.put(WxConstant.ModeMap.MSG, I18nUtils.getMessage("HTTPCODE_"+code));
        } else {
            String code = HttpCode.INTERNAL_SERVER_ERROR.value().toString();
            modelMap.put(WxConstant.ModeMap.CODE, code);
            String message = I18nUtils.getMessage("HTTPCODE_" + code);
            String msg = StringUtils.defaultIfBlank(ex.getMessage(), message);
            modelMap.put(WxConstant.ModeMap.MSG, msg.length() > 100 ? message : msg);
        }
        modelMap.put(WxConstant.ModeMap.TIMESTAMP, System.currentTimeMillis());
        return ResponseEntity.ok(modelMap);
    }

}
