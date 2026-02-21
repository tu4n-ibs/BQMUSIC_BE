package com.example.demo.controller;

import com.example.demo.common.SecurityUtils;
import com.example.demo.entity.UserEntity;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserFollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/follow-user")
@RequiredArgsConstructor
public class UserFollowController {

    private final UserFollowService userFollowService;
    private final UserRepository userRepository;

    @PostMapping("/{targetId}/follow")
    public ResponseEntity<String> followUser(@PathVariable String targetId) {
        String currentUserId = SecurityUtils.getCurrentUserId();

        userFollowService.followUser(currentUserId, targetId);
        return new ResponseEntity<>(targetId, HttpStatus.OK);
    }

    @DeleteMapping("/{targetId}/unfollow")
    public ResponseEntity<String> unfollowUser(@PathVariable String targetId) {
        String currentUserId = SecurityUtils.getCurrentUserId();

        userFollowService.unfollowUser(currentUserId, targetId);
        return new ResponseEntity<>(targetId, HttpStatus.OK);
    }
}