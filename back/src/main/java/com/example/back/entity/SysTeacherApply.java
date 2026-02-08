package com.example.back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 教师申请表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_teacher_apply")
public class SysTeacherApply extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /**
     * 状态：0-待审核，1-通过，2-拒绝
     */
    private Integer status;

    /**
     * 审核备注
     */
    private String remark;
}
