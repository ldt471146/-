package com.example.back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 社区审核操作记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("community_moderation")
public class CommunityModeration extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * POST / REPLY
     */
    private String targetType;

    private Long targetId;

    /**
     * APPROVE / DELETE / MUTE_USER
     */
    private String action;

    private String reason;

    private Long operatorId;
}
