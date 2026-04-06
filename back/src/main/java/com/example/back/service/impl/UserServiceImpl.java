package com.example.back.service.impl;

import com.example.back.config.UploadProperties;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

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
    private final UploadProperties uploadProperties;

    public UserServiceImpl(SysUserMapper userMapper,
                           PasswordEncoder passwordEncoder,
                           StringRedisTemplate stringRedisTemplate,
                           UploadProperties uploadProperties) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.stringRedisTemplate = stringRedisTemplate;
        this.uploadProperties = uploadProperties;
    }

    @Override
    public UserVO updateProfile(UpdateProfileRequest request) {
        SysUser user = getCurrentUser();
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
        return toUserVO(user);
    }

    @Override
    public UserVO uploadAvatar(MultipartFile file) {
        SysUser user = getCurrentUser();
        validateAvatarFile(file);

        Path avatarDir = Paths.get(uploadProperties.getAvatarDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(avatarDir);
            deleteOldAvatarIfNeeded(user.getAvatar(), avatarDir);
            String extension = resolveExtension(file.getOriginalFilename());
            String filename = "avatar_" + user.getId() + "_" + UUID.randomUUID().toString().replace("-", "") + extension;
            Path target = avatarDir.resolve(filename).normalize();
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            user.setAvatar(uploadProperties.getAvatarUrlPrefix() + filename);
            userMapper.updateById(user);
            return toUserVO(user);
        } catch (IOException ex) {
            throw new IllegalStateException("头像保存失败，请稍后重试");
        }
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

    private SysUser getCurrentUser() {
        Long userId = SecurityUtil.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("未登录");
        }
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        return user;
    }

    private UserVO toUserVO(SysUser user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setAvatar(user.getAvatar());
        vo.setStatus(user.getStatus());
        return vo;
    }

    private void validateAvatarFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请选择头像文件");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new IllegalArgumentException("仅支持上传图片文件");
        }
        if (file.getSize() > uploadProperties.getAvatarMaxSize()) {
            throw new IllegalArgumentException("头像大小不能超过2MB");
        }
    }

    private String resolveExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return ".png";
        }
        String extension = originalFilename.substring(originalFilename.lastIndexOf('.')).toLowerCase(Locale.ROOT);
        return extension.length() > 10 ? ".png" : extension;
    }

    private void deleteOldAvatarIfNeeded(String currentAvatar, Path avatarDir) throws IOException {
        String prefix = uploadProperties.getAvatarUrlPrefix();
        if (currentAvatar == null || currentAvatar.isBlank() || !currentAvatar.startsWith(prefix)) {
            return;
        }
        String filename = currentAvatar.substring(prefix.length());
        Path oldFile = avatarDir.resolve(filename).normalize();
        if (oldFile.startsWith(avatarDir)) {
            Files.deleteIfExists(oldFile);
        }
    }
}
