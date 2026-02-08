package com.example.back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 课程章节
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_chapter")
public class EduChapter extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long courseId;

    private String title;

    private Integer sortNo;
}
