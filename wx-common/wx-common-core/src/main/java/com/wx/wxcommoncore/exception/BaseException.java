/**
 *
 */
package com.wx.wxcommoncore.exception;

import com.wx.wxcommoncore.support.WxConstant;
import com.wx.wxcommoncore.support.http.HttpCode;
import com.wx.wxcommoncore.utils.I18nUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ui.ModelMap;


@SuppressWarnings("serial")
public abstract class BaseException extends RuntimeException {
    public BaseException() {
    }

    public BaseException(Throwable ex) {
        super(ex);
    }

    public BaseException(String message) {
        super(message);
    }

    public BaseException(String message, Throwable ex) {
        super(message, ex);
    }

    public void handler(ModelMap modelMap) {
        modelMap.put(WxConstant.ModeMap.CODE, getCode().value());
        if (StringUtils.isNotBlank(getMessage())) {
            modelMap.put(WxConstant.ModeMap.MSG, getMessage());
        } else {
            modelMap.put(WxConstant.ModeMap.MSG, I18nUtils.getMessage("HTTPCODE_"+getCode().value().toString()));
        }
        modelMap.put(WxConstant.ModeMap.TIMESTAMP, System.currentTimeMillis());
    }

    protected abstract HttpCode getCode();
}
