package com.example.back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 学习记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_learn_record")
public class EduLearnRecord extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long lessonId;

    /**
     * 学习进度（0-100）
     */
    private Integer progress;

    /**
     * 是否完成：1-是，0-否
     */
    private Integer isFinished;

    /**
     * 学习时长（秒）
     */
    private Integer learnSeconds;
}
