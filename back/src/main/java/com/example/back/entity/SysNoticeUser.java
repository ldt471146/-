package com.example.back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户通知阅读状态
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_notice_user")
public class SysNoticeUser extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long noticeId;

    /**
     * 是否已读：1-已读 0-未读
     */
    private Integer isRead;
}
