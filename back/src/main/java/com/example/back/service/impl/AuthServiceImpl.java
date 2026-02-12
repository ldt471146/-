package com.example.back.service.impl;

import com.example.back.dto.LoginRequest;
import com.example.back.dto.RegisterRequest;
import com.example.back.entity.SysRole;
import com.example.back.entity.SysUser;
import com.example.back.entity.SysUserRole;
import com.example.back.config.JwtProperties;
import com.example.back.config.VerifyProperties;
import com.example.back.mapper.SysRoleMapper;
import com.example.back.mapper.SysUserMapper;
import com.example.back.mapper.SysUserRoleMapper;
import com.example.back.security.UserPrincipal;
import com.example.back.service.AuthService;
import com.example.back.util.JwtUtil;
import com.example.back.vo.AuthVO;
import com.example.back.vo.UserVO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 认证服务实现
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final String DEFAULT_ROLE_CODE = "STUDENT";
    private static final String DEFAULT_ROLE_NAME = "学生";
    private static final String TEACHER_ROLE_CODE = "TEACHER";
    private static final String TEACHER_ROLE_NAME = "教师";
    private static final String CODE_KEY_PREFIX = "verify:email:code:";
    private static final String COOLDOWN_KEY_PREFIX = "verify:email:cooldown:";

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final com.example.back.mapper.SysTeacherApplyMapper teacherApplyMapper;
    private final com.example.back.mapper.SysNoticeMapper noticeMapper;
    private final com.example.back.mapper.SysNoticeUserMapper noticeUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;
    private final JavaMailSender mailSender;
    private final StringRedisTemplate stringRedisTemplate;
    private final VerifyProperties verifyProperties;
    private final String mailFrom;

    public AuthServiceImpl(SysUserMapper sysUserMapper,
                           SysRoleMapper sysRoleMapper,
                           SysUserRoleMapper sysUserRoleMapper,
                           PasswordEncoder passwordEncoder,
                           JwtUtil jwtUtil,
                           JwtProperties jwtProperties,
                           JavaMailSender mailSender,
                           StringRedisTemplate stringRedisTemplate,
                           VerifyProperties verifyProperties,
                           com.example.back.mapper.SysTeacherApplyMapper teacherApplyMapper,
                           com.example.back.mapper.SysNoticeMapper noticeMapper,
                           com.example.back.mapper.SysNoticeUserMapper noticeUserMapper,
                           @Value("${spring.mail.username}") String mailFrom) {
        this.sysUserMapper = sysUserMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.jwtProperties = jwtProperties;
        this.mailSender = mailSender;
        this.stringRedisTemplate = stringRedisTemplate;
        this.verifyProperties = verifyProperties;
        this.teacherApplyMapper = teacherApplyMapper;
        this.noticeMapper = noticeMapper;
        this.noticeUserMapper = noticeUserMapper;
        this.mailFrom = mailFrom;
    }

    @Override
    @Transactional
    public AuthVO register(RegisterRequest request) {
        String codeKey = CODE_KEY_PREFIX + request.getEmail();
        String cachedCode = stringRedisTemplate.opsForValue().get(codeKey);
        if (cachedCode == null || !cachedCode.equalsIgnoreCase(request.getCode())) {
            throw new IllegalArgumentException("验证码错误或已过期");
        }

        SysUser exists = sysUserMapper.selectByEmail(request.getEmail());
        if (exists != null) {
            throw new IllegalArgumentException("邮箱已被注册");
        }

        String roleCode = request.getRoleCode();
        if (roleCode == null || roleCode.isBlank()) {
            roleCode = DEFAULT_ROLE_CODE;
        }
        if (!DEFAULT_ROLE_CODE.equals(roleCode) && !TEACHER_ROLE_CODE.equals(roleCode)) {
            throw new IllegalArgumentException("角色非法");
        }
        if (DEFAULT_ROLE_CODE.equals(roleCode)) {
            String parentPhone = request.getParentPhone() == null ? "" : request.getParentPhone().trim();
            if (parentPhone.isBlank()) {
                throw new IllegalArgumentException("学生注册需填写家长手机号");
            }
        }

        SysUser user = new SysUser();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(1);
        if (DEFAULT_ROLE_CODE.equals(roleCode)) {
            user.setPhone(request.getParentPhone().trim());
        }
        sysUserMapper.insert(user);

        if (TEACHER_ROLE_CODE.equals(roleCode)) {
            Long count = teacherApplyMapper.selectCount(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.example.back.entity.SysTeacherApply>()
                            .eq(com.example.back.entity.SysTeacherApply::getUserId, user.getId())
                            .ne(com.example.back.entity.SysTeacherApply::getStatus, 2)
            );
            if (count != null && count > 0) {
                throw new IllegalArgumentException("教师申请正在审核中");
            }
        }

        SysRole role = sysRoleMapper.selectByCode(DEFAULT_ROLE_CODE);
        if (role == null) {
            role = new SysRole();
            role.setCode(DEFAULT_ROLE_CODE);
            role.setName(DEFAULT_ROLE_NAME);
            sysRoleMapper.insert(role);
        }

        SysUserRole ur = new SysUserRole();
        ur.setUserId(user.getId());
        ur.setRoleId(role.getId());
        sysUserRoleMapper.insert(ur);

        if (TEACHER_ROLE_CODE.equals(roleCode)) {
            com.example.back.entity.SysTeacherApply apply = new com.example.back.entity.SysTeacherApply();
            apply.setUserId(user.getId());
            apply.setStatus(0);
            apply.setRemark("注册申请教师");
            teacherApplyMapper.insert(apply);
            notifyAdminsForTeacherApply(user);
        }

        // 注册成功后清理验证码
        stringRedisTemplate.delete(codeKey);
        stringRedisTemplate.delete(COOLDOWN_KEY_PREFIX + request.getEmail());

        List<String> roles = Collections.singletonList(DEFAULT_ROLE_CODE);
        String token = jwtUtil.generateToken(user.getEmail(), roles);
        return buildAuthVO(token, user, roles);
    }

    @Override
    public AuthVO login(LoginRequest request) {
        SysUser user = sysUserMapper.selectByEmail(request.getEmail());
        if (user == null) {
            throw new IllegalArgumentException("邮箱或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new IllegalArgumentException("账号已禁用");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("邮箱或密码错误");
        }
        List<String> roles = sysRoleMapper.selectRoleCodesByUserId(user.getId());
        boolean remember = request.getRemember() != null && request.getRemember();
        int baseExpire = jwtProperties.getExpire() == null ? 72 : jwtProperties.getExpire();
        int rememberExpire = jwtProperties.getRememberExpire() == null ? baseExpire : jwtProperties.getRememberExpire();
        long expireMs = (remember ? rememberExpire : baseExpire) * 60L * 60L * 1000L;
        String token = jwtUtil.generateToken(user.getEmail(), roles, expireMs);
        return buildAuthVO(token, user, roles);
    }

    @Override
    public UserVO currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            return null;
        }
        SysUser user = principal.getUser();
        List<String> roles = sysRoleMapper.selectRoleCodesByUserId(user.getId());
        return buildUserVO(user, roles);
    }

    @Override
    public void sendEmailCode(String email) {
        String cooldownKey = COOLDOWN_KEY_PREFIX + email;
        Boolean hasCooldown = stringRedisTemplate.hasKey(cooldownKey);
        if (Boolean.TRUE.equals(hasCooldown)) {
            throw new IllegalArgumentException("发送过于频繁，请稍后再试");
        }

        String code = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
        String codeKey = CODE_KEY_PREFIX + email;

        stringRedisTemplate.opsForValue().set(codeKey, code,
                verifyProperties.getCodeExpire(), TimeUnit.SECONDS);
        stringRedisTemplate.opsForValue().set(cooldownKey, "1",
                verifyProperties.getMailLimit(), TimeUnit.SECONDS);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(email);
        message.setSubject("青少年编程平台验证码");
        message.setText("你的验证码是：" + code + "，有效期 " +
                (verifyProperties.getCodeExpire() / 60) + " 分钟，请勿泄露。");
        mailSender.send(message);
    }

    private AuthVO buildAuthVO(String token, SysUser user, List<String> roles) {
        AuthVO vo = new AuthVO();
        vo.setToken(token);
        vo.setUser(buildUserVO(user, roles));
        return vo;
    }

    private UserVO buildUserVO(SysUser user, List<String> roles) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setAvatar(user.getAvatar());
        vo.setStatus(user.getStatus());
        vo.setRoles(roles);
        return vo;
    }

    private void notifyAdminsForTeacherApply(SysUser user) {
        SysRole adminRole = sysRoleMapper.selectByCode("ADMIN");
        if (adminRole == null) {
            return;
        }
        List<SysUserRole> adminLinks = sysUserRoleMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getRoleId, adminRole.getId())
                        .eq(SysUserRole::getIsDeleted, 0)
        );
        if (adminLinks == null || adminLinks.isEmpty()) {
            return;
        }
        com.example.back.entity.SysNotice notice = new com.example.back.entity.SysNotice();
        notice.setType("system");
        notice.setStatus(1);
        notice.setTitle("新的教师申请待审核");
        notice.setContent("用户：" + user.getUsername() + "（ID：" + user.getId() + "）申请成为教师，请尽快审核。");
        noticeMapper.insert(notice);
        for (SysUserRole link : adminLinks) {
            com.example.back.entity.SysNoticeUser nu = new com.example.back.entity.SysNoticeUser();
            nu.setUserId(link.getUserId());
            nu.setNoticeId(notice.getId());
            nu.setIsRead(0);
            nu.setIsDeleted(0);
            noticeUserMapper.insert(nu);
        }
    }

    // no-op
}
