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
 * 用户实体类
 * 对应数据库表: sys_user
 */
@Data // Lombok 注解，自动生成 getter/setter/toString 方法
@TableName("sys_user") // 指定对应的数据库表名
public class SysUser implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 用户ID (主键) */
    @TableId(type = IdType.AUTO) // 数据库自增主键
    private Long userId;

    /** 归属部门ID */
    private Long deptId;

    /** 登录账号 */
    private String username;

    /** 密码 */
    private String password;

    /** 真实姓名 */
    private String nickname;

    /** 手机号 */
    private String phone;

    /** 头像地址 */
    private String avatar;

    /** 性别 (0:男 1:女) */
    private String sex;

    /** 状态 (0:停用 1:启用) */
    private String status;

    /** 角色标识 (admin/user) */
    private String roleKey;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") // 格式化时间，方便前端显示
    private LocalDateTime createTime;

    // --- 业务扩展字段 (数据库表中不存在，但前端展示需要) ---

    /** 部门名称 */
    @TableField(exist = false) // 告诉 MyBatis-Plus 忽略此字段，不要去数据库找对应的列
    private String deptName;
}