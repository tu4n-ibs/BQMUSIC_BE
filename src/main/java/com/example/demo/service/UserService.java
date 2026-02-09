package com.example.demo.service;

import com.example.demo.common.AppException;
import com.example.demo.common.HashUtil;
import com.example.demo.entity.RoleEntity;
import com.example.demo.entity.UserEntity;
import com.example.demo.model.*;
import com.example.demo.model.enum_object.Provider;
import com.example.demo.model.user.CreateRequest;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private static final String IMAGE_PATH = "C:/image-for-porject/";
    private final StringRedisTemplate redisTemplate;
    private final EmailService emailService;

    @Transactional
    public void register(CreateRequest createRequest) {
        if (!Objects.equals(createRequest.getPassword(), createRequest.getRePassword())){
            throw new AppException(
                    HttpStatus.BAD_REQUEST,
                    "PASSWORD_INVALID",
                    "Passwords do not match"
            );
        }
        String redisKey = "register_session:" + createRequest.getEmail();
        String status = (String) redisTemplate.opsForHash().get(redisKey, "status");

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

        redisTemplate.delete(redisKey);
    }

    @Transactional
    public void sendSMSForgotPassword(String email) {
        if (!userRepository.existsByEmail(email)) {
            throw new AppException(HttpStatus.NOT_FOUND, "USER_NOT_EXIST", "User not found");
        }

        String key = "forgot_password:" + email;

        if (redisTemplate.hasKey(key)) {
            String status = (String) redisTemplate.opsForHash().get(key, "status");
            if ("PENDING".equals(status)) {
                throw new AppException(HttpStatus.CONFLICT, "OTP_EXIST", "Please wait 90 seconds before requesting another OTP");
            }
        }

        String otpCode = processOtpCode();

        Map<String, String> data = new HashMap<>();
        data.put("otp", otpCode);
        data.put("status", "PENDING");

        redisTemplate.opsForHash().putAll(key, data);
        redisTemplate.expire(key, 90, TimeUnit.SECONDS);

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

        String key = "forgot_password:" + request.getEmail();
        String status = (String) redisTemplate.opsForHash().get(key, "status");

        if (!"VERIFIED".equals(status)) {
            throw new AppException(HttpStatus.FORBIDDEN, "RESET_INVALID",
                    "Phiên đổi mật khẩu không hợp lệ hoặc đã hết hạn. Vui lòng thử lại.");
        }

        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "USER_NOT_EXIST", "User not found"));

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);

        user.setPassword(encoder.encode(request.getNewPassword()));

        userRepository.save(user);

        redisTemplate.delete(key);
    }
    public void verifyOTPForgotPassword(String email, String inputCode) {
        String key = "forgot_password:" + email;

        String storedOtp = (String) redisTemplate.opsForHash().get(key, "otp");

        if (storedOtp == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "OTP_EXPIRED", "Mã xác thực hết hạn hoặc không tồn tại");
        }

        if (!storedOtp.equals(inputCode)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "OTP_INVALID", "Mã xác thực không đúng");
        }

        redisTemplate.opsForHash().put(key, "status", "VERIFIED");

        redisTemplate.opsForHash().delete(key, "otp");

        redisTemplate.expire(key, 30, TimeUnit.MINUTES);
    }
    private String processOtpCode( ) {
        return String.format("%04d", new SecureRandom().nextInt(10000));

    }
    @Transactional
    public void sendSMS(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new AppException(HttpStatus.CONFLICT, "USER_EXIST_001", "Email has already been used");
        }

        String key = "register_session:" + email;

        if (redisTemplate.hasKey(key)) {
            String status = (String) redisTemplate.opsForHash().get(key, "status");
            if ("PENDING".equals(status)) {
                throw new AppException(HttpStatus.CONFLICT, "SMS_EXIST", "can not send another sms until 90s after send");
            }
        }

        String otpCode = processOtpCode();

        Map<String, String> data = new HashMap<>();
        data.put("otp", otpCode);
        data.put("status", "PENDING");

        redisTemplate.opsForHash().putAll(key, data);
        redisTemplate.expire(key, 90, TimeUnit.SECONDS);

        // Gửi email OTP
        try {
            String htmlContent = emailService.buildOtpEmailTemplate(otpCode, email);
            emailService.sendHtmlMail(email, "register code", htmlContent);
        } catch (MessagingException e) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "EMAIL_SEND_FAILED",
                    "Không thể gửi email xác thực");
        }
    }


    public void verifyOTP(String email, String inputCode) {
        String key = "register_session:" + email;

        String storedOtp = (String) redisTemplate.opsForHash().get(key, "otp");

        if (storedOtp == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "OTP_EXPIRED", "Mã xác thực hết hạn hoặc không tồn tại");
        }

        if (!storedOtp.equals(inputCode)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "OTP_INVALID", "Mã xác thực không đúng");
        }

        redisTemplate.opsForHash().put(key, "status", "VERIFIED");

        redisTemplate.opsForHash().delete(key, "otp");

        redisTemplate.expire(key, 30, TimeUnit.MINUTES);
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

    public UserDetailResponse findByEmail(String email) {
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "USER_NF_001", "User not found"));

        // Kiểm tra tính toàn vẹn của ảnh
        validateImageIntegrity(userEntity);

        Set<String> roles = userEntity.getRoles().stream()
                .map(RoleEntity::getName)
                .collect(Collectors.toSet());

        return new UserDetailResponse(
                userEntity.getId(),
                userEntity.getName(),
                userEntity.getEmail(),
                userEntity.getImageUrl(),
                roles
        );
    }

    public void update(String email, UserUpdateRequest request, MultipartFile newImage) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "USER_NF_001", "User not found"));

        existingUser.setName(request.getName());
        existingUser.setEmail(request.getEmail());
        existingUser.setIsActive(request.getIsActive());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
            existingUser.setPassword(encoder.encode(request.getPassword()));
        }

        if (newImage != null && !newImage.isEmpty()) {
            // Trường hợp 1: Có upload ảnh mới -> Lưu ảnh mới
            try {
                String fileName = System.currentTimeMillis() + "_" + newImage.getOriginalFilename();
                File dir = new File(IMAGE_PATH);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File dest = new File(dir, fileName);

                newImage.transferTo(dest);
                String hash = HashUtil.sha256Hex(dest);

                existingUser.setImageUrl("/public/" + fileName);
                existingUser.setImageHash(hash);
            } catch (Exception e) {
                throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_UPLOAD_ERR", "Error saving new image: " + e.getMessage());
            }
        } else {
            // Trường hợp 2: Không upload ảnh mới -> Kiểm tra ảnh cũ còn nguyên vẹn không
            validateImageIntegrity(existingUser);
        }

        userRepository.save(existingUser);
    }

    public List<UserSuggestResponse> getSuggestions(String email) {
        UserEntity currentUser = userRepository.findByEmail(email)
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
        if (!userRepository.existsById(id)) {
            throw new AppException(HttpStatus.NOT_FOUND, "USER_NF_002", "User ID to delete not found");
        }
        userRepository.deleteById(id);
    }

    // --- HELPER METHOD ---
    private void validateImageIntegrity(UserEntity user) {
        if (user.getImageUrl() != null && user.getImageHash() != null) {
            String fileName = user.getImageUrl().replace("/public/", "");
            File file = new File(IMAGE_PATH + fileName);

            if (file.exists()) {
                try {
                    String currentHash = HashUtil.sha256Hex(file);
                    if (!currentHash.equals(user.getImageHash())) {
                        throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_INTEGRITY_ERR", "Image integrity check failed (Hash mismatch)");
                    }
                } catch (Exception e) {
                    if (e instanceof AppException) throw (AppException) e;
                    throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_HASH_ERR", "Error calculating image hash: " + e.getMessage());
                }
            } else {
                throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_NF_ERR", "Physical image file missing on server");
            }
        }
    }
}