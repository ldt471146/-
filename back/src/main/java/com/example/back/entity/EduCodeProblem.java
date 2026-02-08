package com.example.back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 编程题
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_code_problem")
public class EduCodeProblem extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    /**
     * 题目描述（Markdown）
     */
    private String content;

    /**
     * 难度：1-简单 2-中等 3-困难
     */
    private Integer difficulty;

    /**
     * 时间限制(ms)
     */
    private Integer timeLimit;

    /**
     * 内存限制(MB)
     */
    private Integer memoryLimit;

    /**
     * 状态：1-上架，0-下架
     */
    private Integer status;
}
