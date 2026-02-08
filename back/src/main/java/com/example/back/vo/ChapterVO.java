package com.example.back.vo;

import lombok.Data;

import java.util.List;

/**
 * 章节返回
 */
@Data
public class ChapterVO {
    private Long id;
    private String title;
    private Integer sortNo;
    private List<LessonVO> lessons;
}
