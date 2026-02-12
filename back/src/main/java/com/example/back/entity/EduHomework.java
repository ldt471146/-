package com.example.back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 作业表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_homework")
public class EduHomework extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long courseId;

    private String title;

    private LocalDateTime deadline;
}

