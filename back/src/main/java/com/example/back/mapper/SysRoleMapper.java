package com.example.back.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.back.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    @Select("select * from sys_role where code = #{code} and is_deleted = 0 limit 1")
    SysRole selectByCode(String code);

    @Select("select r.code from sys_role r " +
            "inner join sys_user_role ur on ur.role_id = r.id and ur.is_deleted = 0 " +
            "where ur.user_id = #{userId} and r.is_deleted = 0")
    List<String> selectRoleCodesByUserId(Long userId);
}
