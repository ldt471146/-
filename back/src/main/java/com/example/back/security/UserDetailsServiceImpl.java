package com.example.back.security;

import com.example.back.entity.SysUser;
import com.example.back.mapper.SysRoleMapper;
import com.example.back.mapper.SysUserMapper;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户详情服务
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;

    public UserDetailsServiceImpl(SysUserMapper sysUserMapper, SysRoleMapper sysRoleMapper) {
        this.sysUserMapper = sysUserMapper;
        this.sysRoleMapper = sysRoleMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        SysUser user = sysUserMapper.selectByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        List<String> roleCodes = sysRoleMapper.selectRoleCodesByUserId(user.getId());
        List<SimpleGrantedAuthority> authorities = roleCodes.stream()
                .map(code -> new SimpleGrantedAuthority("ROLE_" + code))
                .collect(Collectors.toList());
        return new UserPrincipal(user, authorities);
    }
}
