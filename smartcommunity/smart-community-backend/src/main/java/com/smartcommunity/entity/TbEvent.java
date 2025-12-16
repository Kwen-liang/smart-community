package com.smartcommunity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 事件实体类
 * 对应数据库表: tb_event
 */
@Data
@TableName("tb_event")
public class TbEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 事件ID (主键) */
    @TableId(type = IdType.AUTO)
    private Long eventId;

    /** 事件标题 */
    private String title;

    /** 事件类型 */
    private String type;

    /** 紧急程度 */
    private String urgency;

    /** 发生时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime occurTime;

    /** 发生地点描述 */
    private String address;

    /** 详细描述 */
    private String description;

    /** * 图片 URL 数组
     * 数据库类型为 JSONB，为了简单起见，Java 端使用 String 接收。
     * 前端传过来的是 JSON 字符串，取出去直接原样给前端解析即可。
     */
    private String images;

    /** 状态 (pending:待审核, processing:处理中, archived:已归档) */
    private String status;

    /** 上报人ID */
    private Long reporterId;

    /** 处理人ID */
    private Long handlerId;

    /** 所属网格ID */
    private Long gridId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    // --- 空间数据 ---

    /** 用于存储事件点位的 GeoJSON */
    @TableField(exist = false)
    private String geomJson;

    /** 上报人姓名 (扩展字段) */
    @TableField(exist = false)
    private String reporterName;
}