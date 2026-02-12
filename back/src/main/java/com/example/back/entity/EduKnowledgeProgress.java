package com.example.back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户知识点进度
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_knowledge_progress")
public class EduKnowledgeProgress extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long pointId;

    /**
     * 0-未掌握 1-已掌握
     */
    private Integer status;

    private Integer score;
}

