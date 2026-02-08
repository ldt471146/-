package com.example.back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 课时内容
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_lesson")
public class EduLesson extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long chapterId;

    private String title;

    /**
     * 内容类型：video/text/file
     */
    private String contentType;

    private String contentUrl;

    private String contentText;

    private Integer sortNo;
}
