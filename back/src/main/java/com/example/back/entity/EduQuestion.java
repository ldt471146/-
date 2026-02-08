package com.example.back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 题库题目
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_question")
public class EduQuestion extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 题目标题
     */
    private String title;

    /**
     * 题型：single/multi/truefalse
     */
    private String type;

    /**
     * 题目解析
     */
    private String analysis;

    /**
     * 难度：1-简单 2-中等 3-困难
     */
    private Integer difficulty;

    /**
     * 课程ID（可为空）
     */
    private Long courseId;

    /**
     * 章节ID（可为空）
     */
    private Long chapterId;
}
