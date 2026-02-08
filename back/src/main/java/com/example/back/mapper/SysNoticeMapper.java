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
            FROM sys_notice n
            LEFT JOIN sys_notice_user u
              ON u.notice_id = n.id AND u.user_id = #{userId} AND u.is_deleted = 0
            WHERE n.status = 1 AND n.is_deleted = 0
              AND NOT EXISTS (
                SELECT 1 FROM sys_notice_user ux
                WHERE ux.notice_id = n.id AND ux.user_id = #{userId} AND ux.is_deleted = 1
              )
            ORDER BY n.created_at DESC
            LIMIT #{size} OFFSET #{offset}
            """)
    List<NoticeVO> list(@Param("userId") Long userId, @Param("offset") long offset, @Param("size") long size);

    @Select("""
            SELECT COUNT(1)
            FROM sys_notice
            WHERE status = 1 AND is_deleted = 0
              AND NOT EXISTS (
                SELECT 1 FROM sys_notice_user ux
                WHERE ux.notice_id = sys_notice.id AND ux.user_id = #{userId} AND ux.is_deleted = 1
              )
            """)
    Long countAll(@Param("userId") Long userId);

    @Select("""
            SELECT COUNT(1)
            FROM sys_notice n
            LEFT JOIN sys_notice_user u
              ON u.notice_id = n.id AND u.user_id = #{userId} AND u.is_deleted = 0
            WHERE n.status = 1 AND n.is_deleted = 0
              AND COALESCE(u.is_read, 0) = 0
              AND NOT EXISTS (
                SELECT 1 FROM sys_notice_user ux
                WHERE ux.notice_id = n.id AND ux.user_id = #{userId} AND ux.is_deleted = 1
              )
            """)
    Long countUnread(@Param("userId") Long userId);
}
