package com.example.back.controller;

import com.example.back.common.ApiResponse;
import com.example.back.dto.UpdatePasswordRequest;
import com.example.back.dto.UpdateProfileRequest;
import com.example.back.service.UserService;
import com.example.back.vo.UserVO;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<UserVO> uploadAvatar(@RequestPart("file") MultipartFile file) {
        return ApiResponse.ok(userService.uploadAvatar(file));
    }

    @PutMapping("/password")
    public ApiResponse<Void> changePassword(@RequestBody UpdatePasswordRequest request) {
        userService.changePassword(request);
        return ApiResponse.ok();
    }
}
