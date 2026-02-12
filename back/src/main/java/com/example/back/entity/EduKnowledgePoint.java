package com.example.back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 知识点
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_knowledge_point")
public class EduKnowledgePoint extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long courseId;

    private Long chapterId;

    private String title;

    private String description;

    private Integer sortNo;

    /**
     * 1-启用 0-停用
     */
    private Integer status;
}

