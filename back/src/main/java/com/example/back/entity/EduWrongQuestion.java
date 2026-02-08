package com.example.back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 错题本
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_wrong_question")
public class EduWrongQuestion extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long questionId;

    /**
     * 错误次数
     */
    private Integer wrongCount;
}
