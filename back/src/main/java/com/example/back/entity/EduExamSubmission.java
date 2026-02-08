package com.example.back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 考试提交记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_exam_submission")
public class EduExamSubmission extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;

    private Long userId;

    private Integer totalCount;

    private Integer correctCount;

    private Integer score;

    private String detailJson;

    private LocalDateTime submittedAt;
}

