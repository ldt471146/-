package com.example.back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 编程题测试用例
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_code_testcase")
public class EduCodeTestcase extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long problemId;

    /**
     * 输入
     */
    private String inputData;

    /**
     * 输出
     */
    private String outputData;

    /**
     * 是否样例：1-是，0-否
     */
    private Integer isSample;
}
