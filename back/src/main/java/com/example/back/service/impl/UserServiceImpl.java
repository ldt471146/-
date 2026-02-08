package com.example.back.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.back.dto.UpdatePasswordRequest;
import com.example.back.dto.UpdateProfileRequest;
import com.example.back.entity.SysUser;
import com.example.back.mapper.SysUserMapper;
import com.example.back.service.UserService;
import com.example.back.util.SecurityUtil;
import com.example.back.vo.UserVO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户中心服务实现
 */
@Service
public class UserServiceImpl implements UserService {

    private static final String CODE_KEY_PREFIX = "verify:email:code:";
    private static final String COOLDOWN_KEY_PREFIX = "verify:email:cooldown:";

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate stringRedisTemplate;

    public UserServiceImpl(SysUserMapper userMapper,
                           PasswordEncoder passwordEncoder,
                           StringRedisTemplate stringRedisTemplate) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public UserVO updateProfile(UpdateProfileRequest request) {
        Long userId = SecurityUtil.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("未登录");
        }
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        if (request.getUsername() != null) {
            user.setUsername(request.getUsername().trim());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar().trim());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone().trim());
        }
        userMapper.updateById(user);

        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setAvatar(user.getAvatar());
        vo.setStatus(user.getStatus());
        return vo;
    }

    @Override
    public void changePassword(UpdatePasswordRequest request) {
        Long userId = SecurityUtil.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("未登录");
        }
        if (request.getOldPassword() == null || request.getNewPassword() == null) {
            throw new IllegalArgumentException("请输入原密码和新密码");
        }
        if (request.getCode() == null || request.getCode().isBlank()) {
            throw new IllegalArgumentException("请输入邮箱验证码");
        }
        if (request.getNewPassword().length() < 6 || request.getNewPassword().length() > 32) {
            throw new IllegalArgumentException("新密码长度为6-32位");
        }
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        String codeKey = CODE_KEY_PREFIX + user.getEmail();
        String cachedCode = stringRedisTemplate.opsForValue().get(codeKey);
        if (cachedCode == null || !cachedCode.equalsIgnoreCase(request.getCode())) {
            throw new IllegalArgumentException("验证码错误或已过期");
        }
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("原密码不正确");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);

        stringRedisTemplate.delete(codeKey);
        stringRedisTemplate.delete(COOLDOWN_KEY_PREFIX + user.getEmail());
    }
}
