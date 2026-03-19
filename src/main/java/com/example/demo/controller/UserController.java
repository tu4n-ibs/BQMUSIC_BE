package com.example.demo.controller;

import com.example.demo.model.*;
import com.example.demo.model.user.CreateRequest;
import com.example.demo.model.user.UserDetailResponse;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(
            @RequestBody CreateRequest user
    ) {
        userService.register(user);

        return ResponseEntity.ok(
                ApiResponse.success(null, "Register successfully")
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("{id}")
    public void delete (@PathVariable String id) {
       userService.delete(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public Page<UserPageResponse> findAll(@ParameterObject Pageable pageable) {
        return userService.findAll(pageable);
    }


    @GetMapping("/suggestions")
    public ResponseEntity<List<UserSuggestResponse>> getSuggestions() {
        List<UserSuggestResponse> suggestions = userService.getSuggestions();
        return ResponseEntity.ok(suggestions);
    }
    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<Void>> sendOtp(@RequestParam String email) {
        userService.sendSMS(email);

        return ResponseEntity.ok(
                ApiResponse.success(null, "OTP sent successfully")
        );
    }

    @PostMapping("/verifi")
    public ResponseEntity<ApiResponse<Void>> verifyOtp(
            @RequestParam String email,
            @RequestParam String otp
    ) {
        userService.verifyOTP(email, otp);
        return ResponseEntity.ok(
                ApiResponse.success(null, "OTP verified successfully")
        );
    }
    @PostMapping("/send-otp-fp")
    public ApiResponse<Void> forgotPassword(
            @RequestParam String email
    ){
        userService.sendSMSForgotPassword(email);
        return ApiResponse.<Void>builder()
                .statusCode(HttpStatus.OK.value())
                .message("send OTP successfully")
                .build();
    }
    @PostMapping("/confirm-otp-fp")
    public ApiResponse<Void> sendOtpFp(@RequestParam String email,@RequestParam String otp) {
        userService.verifyOTPForgotPassword(email, otp);
        return ApiResponse.<Void>builder()
                .statusCode(HttpStatus.OK.value())
                .message("OTP verified successfully")
                .build();
    }
    @PostMapping("/new-password-fp")
    public ApiResponse<Void> newPasswordF(@RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request);
        return ApiResponse.<Void>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Password reset successfully")
                .build();
    }
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/update-name")
    public ApiResponse<Void> updateName(@RequestParam String name){
        userService.UpdateName(name);
        return ApiResponse.success(null, "Name updated successfully");
    }
    @PreAuthorize("isAuthenticated()")
    @PostMapping("update-image")
    public ApiResponse<Void> updateImage(@RequestParam MultipartFile file){
        userService.UpdateImage(file);
        return ApiResponse.success(null, "image updated successfully");
    }
    @PreAuthorize("isAuthenticated()")
    @GetMapping("userId/{id}")
    public ApiResponse<UserDetailResponse> findById(@PathVariable String id) {
        return ApiResponse.success(userService.getUserDetail(id),"success");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("userId/{id}")
    public ApiResponse<Void> updateStatus(@PathVariable String id, @RequestParam Boolean isActive) {
        userService.updateStatus(id, isActive);
        return ApiResponse.success(null, "User status updated successfully");
    }
}