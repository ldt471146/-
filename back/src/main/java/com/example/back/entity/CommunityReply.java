package com.example.back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 社区回复
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("community_reply")
public class CommunityReply extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long postId;

    private Long userId;

    private String content;

    private String codeSnippet;

    /**
     * 1-最佳答案，0-普通
     */
    private Integer isBest;

    /**
     * 1-正常，2-隐藏/删除
     */
    private Integer status;
}
