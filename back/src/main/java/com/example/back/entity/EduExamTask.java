package com.example.back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 考试任务
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_exam_task")
public class EduExamTask extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long teacherId;

    private Long courseId;

    private Long chapterId;

    private String title;

    private Integer questionCount;

    private Integer durationMinutes;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    /**
     * 0-未发布 1-已发布 2-已结束
     */
    private Integer status;
}

