package com.example.back.service;

import com.example.back.dto.UpdatePasswordRequest;
import com.example.back.dto.UpdateProfileRequest;
import com.example.back.vo.UserVO;

/**
 * 用户中心服务
 */
public interface UserService {
    UserVO updateProfile(UpdateProfileRequest request);

    void changePassword(UpdatePasswordRequest request);
}
