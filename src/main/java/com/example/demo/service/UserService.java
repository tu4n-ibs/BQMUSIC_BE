package com.example.demo.service;

import com.example.demo.common.AppException;
import com.example.demo.common.SecurityUtils;
import com.example.demo.entity.RoleEntity;
import com.example.demo.entity.UserEntity;
import com.example.demo.model.*;
import com.example.demo.model.enum_object.Provider;
import com.example.demo.model.user.CreateRequest;
import com.example.demo.model.user.UserDetailResponse;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.redis.RedisAuthRepository;
import com.example.demo.repository.redis.RedisAuthSchema;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmailService emailService;
    private final RedisAuthRepository redisAuthRepository;
    private final CloudinaryServiceForImage cloudinaryService;

    @Transactional
    public void register(CreateRequest createRequest) {
        if (!Objects.equals(createRequest.getPassword(), createRequest.getRePassword())) {
            throw new AppException(
                    HttpStatus.BAD_REQUEST,
                    "PASSWORD_INVALID",
                    "Passwords do not match"
            );
        }

        String redisKey = RedisAuthSchema.getRegisterKey(createRequest.getEmail());
        String status = redisAuthRepository.getStatus(redisKey);

        if (!"VERIFIED".equals(status)) {
            throw new AppException(
                    HttpStatus.FORBIDDEN,
                    "REGISTER_INVALID",
                    "Email chưa được xác thực hoặc phiên đăng ký đã hết hạn"
            );
        }

        if (userRepository.findByEmail(createRequest.getEmail()).isPresent()) {
            throw new AppException(
                    HttpStatus.CONFLICT,
                    "EMAIL_EXIST_001",
                    "Email already exists"
            );
        }

        UserEntity userEntity = new UserEntity();
        userEntity.setProvider(Provider.LOCAL);
        userEntity.setEmail(createRequest.getEmail());
        userEntity.setName(createRequest.getName());
        userEntity.setIsActive(true);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
        userEntity.setPassword(encoder.encode(createRequest.getPassword()));

        RoleEntity role = roleRepository.findByName("USER")
                .orElseThrow(() -> new AppException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "ROLE_NF_001",
                        "Default role 'USER' not found"
                ));

        userEntity.setRoles(Set.of(role));
        userRepository.save(userEntity);

        redisAuthRepository.delete(redisKey);
    }

    @Transactional
    public void sendSMSForgotPassword(String email) {
        if (!userRepository.existsByEmail(email)) {
            throw new AppException(HttpStatus.NOT_FOUND, "USER_NOT_EXIST", "User not found");
        }

        String key = RedisAuthSchema.getForgotPasswordKey(email);

        if (redisAuthRepository.isExists(key)) {
            String status = redisAuthRepository.getStatus(key);
            if ("PENDING".equals(status)) {
                throw new AppException(HttpStatus.CONFLICT, "OTP_EXIST", "Please wait 90 seconds before requesting another OTP");
            }
        }

        String otpCode = processOtpCode();
        redisAuthRepository.saveOtpSession(key, otpCode, 90);

        try {
            String htmlContent = emailService.buildForgotPasswordEmailTemplate(otpCode, email);
            emailService.sendHtmlMail(email, "Password Reset Verification Code", htmlContent);
        } catch (MessagingException e) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "EMAIL_SEND_FAILED",
                    "Unable to send verification email");
        }
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "PASSWORD_NOT_MATCH",
                    "Mật khẩu nhập lại không khớp");
        }

        String key = RedisAuthSchema.getForgotPasswordKey(request.getEmail());
        String status = redisAuthRepository.getStatus(key);

        if (!"VERIFIED".equals(status)) {
            throw new AppException(HttpStatus.FORBIDDEN, "RESET_INVALID",
                    "Phiên đổi mật khẩu không hợp lệ hoặc đã hết hạn. Vui lòng thử lại.");
        }

        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "USER_NOT_EXIST", "User not found"));

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
        user.setPassword(encoder.encode(request.getNewPassword()));
        userRepository.save(user);

        redisAuthRepository.delete(key);
    }

    public void verifyOTPForgotPassword(String email, String inputCode) {
        String key = RedisAuthSchema.getForgotPasswordKey(email);
        String storedOtp = redisAuthRepository.getOtp(key);

        if (storedOtp == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "OTP_EXPIRED", "Mã xác thực hết hạn hoặc không tồn tại");
        }

        if (!storedOtp.equals(inputCode)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "OTP_INVALID", "Mã xác thực không đúng");
        }

        redisAuthRepository.verifySession(key, 30);
    }

    private String processOtpCode() {
        return String.format("%04d", new SecureRandom().nextInt(10000));
    }

    @Transactional
    public void sendSMS(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new AppException(HttpStatus.CONFLICT, "USER_EXIST_001", "Email has already been used");
        }

        String key = RedisAuthSchema.getRegisterKey(email);

        if (redisAuthRepository.isExists(key)) {
            String status = redisAuthRepository.getStatus(key);
            if ("PENDING".equals(status)) {
                throw new AppException(HttpStatus.CONFLICT, "SMS_EXIST", "can not send another sms until 90s after send");
            }
        }

        String otpCode = processOtpCode();
        redisAuthRepository.saveOtpSession(key, otpCode, 90);

        try {
            String htmlContent = emailService.buildOtpEmailTemplate(otpCode, email);
            emailService.sendHtmlMail(email, "register code", htmlContent);
        } catch (MessagingException e) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "EMAIL_SEND_FAILED",
                    "Không thể gửi email xác thực");
        }
    }

    public void verifyOTP(String email, String inputCode) {
        String key = RedisAuthSchema.getRegisterKey(email);
        String storedOtp = redisAuthRepository.getOtp(key);

        if (storedOtp == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "OTP_EXPIRED", "Mã xác thực hết hạn hoặc không tồn tại");
        }

        if (!storedOtp.equals(inputCode)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "OTP_INVALID", "Mã xác thực không đúng");
        }

        redisAuthRepository.verifySession(key, 30);
    }

    public Page<UserPageResponse> findAll(Pageable pageable) {
        Page<UserEntity> userEntityPage = userRepository.findAll(pageable);

        return userEntityPage.map(userEntity -> {
            UserPageResponse response = new UserPageResponse();
            response.setName(userEntity.getName());
            response.setEmail(userEntity.getEmail());
            response.setImageUrl(userEntity.getImageUrl());

            Set<String> roles = userEntity.getRoles()
                    .stream()
                    .map(RoleEntity::getName)
                    .collect(Collectors.toSet());

            response.setRoles(roles);

            return response;
        });
    }

    public List<UserSuggestResponse> getSuggestions() {
        String id = SecurityUtils.getCurrentUserId();
        UserEntity currentUser = userRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "USER_NF_001", "Current user not found"));

        List<UserEntity> suggestedUsers = userRepository.findSuggestedUsers(
                currentUser.getId(),
                PageRequest.of(0, 7)
        );

        return suggestedUsers.stream().map(user -> {
            UserSuggestResponse response = new UserSuggestResponse();
            response.setUserId(user.getId());
            response.setName(user.getName());
            response.setImageUrl(user.getImageUrl());
            return response;
        }).collect(Collectors.toList());
    }

    public void delete(String id) {
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "USER_NF_001", "User not found"));
        userEntity.setIsActive(false);
        userRepository.save(userEntity);
    }

    public void UpdateImage(MultipartFile file) {
        String id = SecurityUtils.getCurrentUserId();
        UserEntity userEntity = userRepository.findById(id).orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "USER_NF_001", "User not found"));
        String urlImage = cloudinaryService.uploadFile(file);
        userEntity.setImageUrl(urlImage);
        userRepository.save(userEntity);
    }

    public void UpdateName(String name) {
        UserEntity userEntity = userRepository.findById(SecurityUtils.getCurrentUserId()).orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "USER_NF_001", "User not found"));
        userEntity.setName(name);
        userRepository.save(userEntity);
    }

    public UserDetailResponse getUserDetail(String id) {
        UserEntity user = userRepository.findById(id).orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "USER_NF_001", "User not found"));
        return new UserDetailResponse(user.getId(), user.getName(), user.getImageUrl(), user.getEmail());
    }
}