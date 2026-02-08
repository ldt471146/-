package com.example.back.vo;

import lombok.Data;

import java.util.List;

/**
 * 分页返回
 */
@Data
public class PageResultVO<T> {
    private long page;
    private long size;
    private long total;
    private List<T> records;
}
