package com.example.back.controller;

import com.example.back.common.ApiResponse;
import com.example.back.dto.UpdatePasswordRequest;
import com.example.back.dto.UpdateProfileRequest;
import com.example.back.service.UserService;
import com.example.back.vo.UserVO;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户中心接口
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping("/profile")
    public ApiResponse<UserVO> updateProfile(@RequestBody UpdateProfileRequest request) {
        return ApiResponse.ok(userService.updateProfile(request));
    }

    @PutMapping("/password")
    public ApiResponse<Void> changePassword(@RequestBody UpdatePasswordRequest request) {
        userService.changePassword(request);
        return ApiResponse.ok();
    }
}
