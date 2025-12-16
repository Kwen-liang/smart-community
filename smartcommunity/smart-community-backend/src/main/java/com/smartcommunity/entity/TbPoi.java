package com.smartcommunity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;

/**
 * 资源点位实体 (Point of Interest)
 */
@Data
@TableName("tb_poi")
public class TbPoi implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String type; // camera, trash, service...

    // 空间字段转换
    @TableField(exist = false)
    private String geomJson;
}