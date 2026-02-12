package com.example.back.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.back.entity.SysNotice;
import com.example.back.vo.NoticeVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysNoticeMapper extends BaseMapper<SysNotice> {

    @Select("""
            SELECT n.id, n.title, n.content, n.type, n.created_at AS createdAt,
                   COALESCE(u.is_read, 0) AS isRead
            FROM sys_notice_user u
            INNER JOIN sys_notice n ON n.id = u.notice_id
            WHERE u.user_id = #{userId}
              AND u.is_deleted = 0
              AND n.status = 1
              AND n.is_deleted = 0
            ORDER BY n.created_at DESC
            LIMIT #{size} OFFSET #{offset}
            """)
    List<NoticeVO> list(@Param("userId") Long userId, @Param("offset") long offset, @Param("size") long size);

    @Select("""
            SELECT COUNT(1)
            FROM sys_notice_user u
            INNER JOIN sys_notice n ON n.id = u.notice_id
            WHERE u.user_id = #{userId}
              AND u.is_deleted = 0
              AND n.status = 1
              AND n.is_deleted = 0
            """)
    Long countAll(@Param("userId") Long userId);

    @Select("""
            SELECT COUNT(1)
            FROM sys_notice_user u
            INNER JOIN sys_notice n ON n.id = u.notice_id
            WHERE u.user_id = #{userId}
              AND u.is_deleted = 0
              AND COALESCE(u.is_read, 0) = 0
              AND n.status = 1
              AND n.is_deleted = 0
            """)
    Long countUnread(@Param("userId") Long userId);
}
