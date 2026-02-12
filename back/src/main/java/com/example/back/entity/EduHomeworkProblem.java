package com.example.back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 作业题目关联表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_homework_problem")
public class EduHomeworkProblem extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long homeworkId;

    private Long problemId;

    private Integer score;
}

