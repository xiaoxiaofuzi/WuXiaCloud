/**
 *
 */
package com.wx.wxcommondatasource.base;

import com.github.pagehelper.Page;
import com.wx.wxcommoncore.support.WxConstant;
import com.wx.wxcommoncore.support.http.HttpCode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 控制器基类

 */
public abstract class AbstractController {
    protected Logger logger = LogManager.getLogger();


    /** 设置成功响应代码 */
    protected ResponseEntity<ModelMap> setSuccessModelMap() {
        return setSuccessModelMap(new ModelMap(), null);
    }

    /** 设置成功响应代码 */
    protected ResponseEntity<ModelMap> setSuccessModelMap(ModelMap modelMap) {
        return setSuccessModelMap(modelMap, null);
    }

    /** 设置成功响应代码 */
    protected ResponseEntity<ModelMap> setSuccessModelMap(ModelMap modelMap, Object data) {
        return setModelMap(modelMap, HttpCode.OK, data);
    }

    /** 设置成功响应代码 */
    protected ResponseEntity<ModelMap> setSuccessModelMap(Object data) {
        return setModelMap(new ModelMap(), HttpCode.OK, data);
    }

    /** 设置响应代码 */
    protected ResponseEntity<ModelMap> setModelMap(HttpCode code) {
        return setModelMap(new ModelMap(), code, null);
    }

    /** 设置响应代码 */
    protected ResponseEntity<ModelMap> setModelMap(String code, String msg) {
        return setModelMap(new ModelMap(), code, msg, null);
    }

    /** 设置响应代码 */
    protected ResponseEntity<ModelMap> setModelMap(ModelMap modelMap, HttpCode code) {
        return setModelMap(modelMap, code, null);
    }

    /** 设置响应代码 */
    protected ResponseEntity<ModelMap> setModelMap(HttpCode code, Object data) {
        return setModelMap(new ModelMap(), code, data);
    }

    /** 设置响应代码 */
    protected ResponseEntity<ModelMap> setModelMap(String code, String msg, Object data) {
        return setModelMap(new ModelMap(), code, msg, data);
    }

    /** 设置响应代码 */
    protected ResponseEntity<ModelMap> setModelMap(ModelMap modelMap, HttpCode code, Object data) {
        return setModelMap(modelMap, code.value().toString(), code.msg(), data);
    }

    /** 设置响应代码 */
    protected ResponseEntity<ModelMap> setModelMap(ModelMap modelMap, String code, String msg, Object data) {
        if (!modelMap.isEmpty()) {
            Map<String, Object> map = new HashMap<>(modelMap);
            modelMap.clear();
            for (String key : map.keySet()) {
                if (!key.startsWith("org.springframework.validation.BindingResult") && !"void".equals(key)) {
                    modelMap.put(key, map.get(key));
                }
            }
        }
        if (data != null) {
            if (data instanceof Page<?>) {
                Page<?> page = (Page<?>)data;
                modelMap.put(WxConstant.ModeMap.PAGE_ROWS, page.getResult());
                modelMap.put(WxConstant.ModeMap.PAGE_CURRENT, page.getStartRow());
                modelMap.put(WxConstant.ModeMap.PAGE_SIZE, page.getPageSize());
                modelMap.put(WxConstant.ModeMap.PAGE_PAGES, page.getPages());
                modelMap.put(WxConstant.ModeMap.PAGE_TOTAL, page.getTotal());
            } else if (data instanceof List<?>) {
                modelMap.put(WxConstant.ModeMap.LIST_ROWS, data);
                modelMap.put(WxConstant.ModeMap.LIST_TOTLAL, ((List<?>)data).size());
            } else {
                modelMap.put(WxConstant.ModeMap.DATA, data);
            }
        }
        modelMap.put(WxConstant.ModeMap.CODE, code);
        modelMap.put(WxConstant.ModeMap.MSG, msg);
        modelMap.put(WxConstant.ModeMap.TIMESTAMP, System.currentTimeMillis());
        return ResponseEntity.ok(modelMap);
    }
}
