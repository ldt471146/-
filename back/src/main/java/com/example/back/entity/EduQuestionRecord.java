package com.example.back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 题目作答记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_question_record")
public class EduQuestionRecord extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long questionId;

    /**
     * 用户答案（多选用逗号分隔）
     */
    private String answer;

    /**
     * 是否正确：1-正确 0-错误
     */
    private Integer isCorrect;
}
