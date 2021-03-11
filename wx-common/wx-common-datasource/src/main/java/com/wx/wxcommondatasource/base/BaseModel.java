package com.wx.wxcommondatasource.base;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author gh
 */
@Data
public abstract class BaseModel implements Serializable {

    @TableId(value = "id",type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("CREATED_BY")
    private Long createdBy;

    @TableField("CREATION_TIME")
    private Timestamp creationTime;

    @TableField("MODIFIER_BY")
    private Long modifierBy;

    @TableField("MODIFICATION_TIME")
    private Timestamp modificationTime;

    @TableField("DELETE_STATUS")
    private boolean deleteStatus;

    @TableField("REMARK")
    private String remark;
}
