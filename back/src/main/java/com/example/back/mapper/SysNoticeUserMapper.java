package com.example.back.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.back.entity.SysNoticeUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Insert;

@Mapper
public interface SysNoticeUserMapper extends BaseMapper<SysNoticeUser> {

    @Update("""
            UPDATE sys_notice_user
            SET is_read = 1
            WHERE user_id = #{userId}
              AND is_deleted = 0
            """)
    int markAllRead(@Param("userId") Long userId);

    @Insert("""
            INSERT INTO sys_notice_user(user_id, notice_id, is_read, created_at, updated_at, is_deleted)
            SELECT #{userId}, n.id, 1, NOW(), NOW(), 0
            FROM sys_notice n
            WHERE n.status = 1 AND n.is_deleted = 0
              AND NOT EXISTS (
                SELECT 1 FROM sys_notice_user u
                WHERE u.user_id = #{userId} AND u.notice_id = n.id
              )
            """)
    int insertAllReadIfMissing(@Param("userId") Long userId);

    @Update("""
            UPDATE sys_notice_user
            SET is_deleted = 1
            WHERE user_id = #{userId}
              AND notice_id = #{noticeId}
              AND is_deleted = 0
            """)
    int deleteByUser(@Param("userId") Long userId, @Param("noticeId") Long noticeId);
}
