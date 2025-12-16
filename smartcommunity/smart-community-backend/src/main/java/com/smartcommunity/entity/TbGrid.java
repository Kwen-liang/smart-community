package com.smartcommunity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 网格实体类
 * 对应数据库表: tb_grid
 */
@Data
@TableName("tb_grid")
public class TbGrid implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 网格ID (主键) */
    @TableId(type = IdType.AUTO)
    private Long gridId;

    /** 网格名称 (如：文理学院-A01) */
    private String name;

    /** 网格编号 */
    private String code;

    /** 面积 (平方米) */
    private BigDecimal area;

    /** 实有人口 */
    private Integer population;

    /** 绘制颜色 */
    private String color;

    /** 责任网格员ID */
    private Long managerId;

    /** 状态 (normal:正常, alert:预警) */
    private String status;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    // --- 空间数据处理 (核心逻辑) ---

    /**
     * 专门用于接收/发送 GeoJSON 字符串的字段。
     * 数据库里实际字段是 'geom' (Geometry类型)，MyBatis 默认无法自动映射。
     * 我们通过 SQL 函数 ST_AsGeoJSON 将 'geom' 转成字符串放入此字段。
     */
    @TableField(exist = false)
    private String geomJson;

    /** 网格员姓名 (扩展字段，用于列表展示) */
    @TableField(exist = false)
    private String managerName;

    /** 网格员电话 (扩展字段) */
    @TableField(exist = false)
    private String managerPhone;
}