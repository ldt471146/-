package com.example.back.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 教师申请展示
 */
@Data
public class TeacherApplyVO {
    private Long id;
    private Long userId;
    private String username;
    private String email;
    private Integer status;
    private String remark;
    private LocalDateTime createdAt;
}
