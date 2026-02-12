package com.example.back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名（展示用）
     */
    private String username;

    /**
     * 登录邮箱
     */
    private String email;

    /**
     * 加密密码
     */
    private String password;

    /**
     * 手机号（可选）
     */
    private String phone;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 状态：1-正常，0-禁用
     */
    private Integer status;

    /**
     * 禁言状态：0-正常，1-禁言
     */
    private Integer muteStatus;

    /**
     * 封禁/禁言原因
     */
    private String banReason;
}
