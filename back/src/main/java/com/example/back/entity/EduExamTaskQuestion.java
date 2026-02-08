package com.example.back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 考试任务题目
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_exam_task_question")
public class EduExamTaskQuestion extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;

    private Long questionId;
}

