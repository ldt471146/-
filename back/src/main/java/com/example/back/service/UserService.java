package com.example.back.service;

import com.example.back.dto.UpdatePasswordRequest;
import com.example.back.dto.UpdateProfileRequest;
import com.example.back.vo.UserVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户中心服务
 */
public interface UserService {
    UserVO updateProfile(UpdateProfileRequest request);

    UserVO uploadAvatar(MultipartFile file);

    void changePassword(UpdatePasswordRequest request);
}
