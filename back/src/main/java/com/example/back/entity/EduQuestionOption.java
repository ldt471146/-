package com.example.back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 题目选项
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_question_option")
public class EduQuestionOption extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long questionId;

    /**
     * 选项标识：A/B/C/D
     */
    private String label;

    private String content;

    /**
     * 是否正确：1-正确 0-错误
     */
    private Integer isCorrect;
}
