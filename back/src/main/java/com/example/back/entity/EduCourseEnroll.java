package com.example.back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 课程报名/加入记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_course_enroll")
public class EduCourseEnroll extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long courseId;

    /**
     * 状态：1-正常，0-取消
     */
    private Integer status;
}
