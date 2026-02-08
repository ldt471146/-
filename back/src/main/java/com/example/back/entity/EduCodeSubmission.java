package com.example.back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 编程题提交记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_code_submission")
public class EduCodeSubmission extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long problemId;

    private Integer languageId;

    private String sourceCode;

    /**
     * 结果：AC/WA/TLE/RE/CE
     */
    private String result;

    private Integer passedCount;

    private Integer totalCount;
}
