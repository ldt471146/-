package com.example.back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 知识点依赖（from -> to）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_knowledge_dependency")
public class EduKnowledgeDependency extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long fromPointId;

    private Long toPointId;
}

