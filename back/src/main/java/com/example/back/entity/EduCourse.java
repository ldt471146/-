package com.example.back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 课程表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_course")
public class EduCourse extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 课程标题
     */
    private String title;

    /**
     * 封面图
     */
    private String cover;

    /**
     * 简介
     */
    private String intro;

    /**
     * 教师ID
     */
    private Long teacherId;

    /**
     * 状态：1-上架，0-下架
     */
    private Integer status;

    /**
     * 完结状态：1-已完结，0-更新中
     */
    private Integer finishStatus;
}
