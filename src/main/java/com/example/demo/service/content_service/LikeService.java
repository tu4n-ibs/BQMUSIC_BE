package com.example.demo.service.content_service;

import com.example.demo.common.AppException;
import com.example.demo.common.SecurityUtils;
import com.example.demo.entity.LikeEntity;
import com.example.demo.model.content_dto.LikeResponse;
import com.example.demo.repository.LikeRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public LikeResponse toggleLike(String postId) {
        String userId = SecurityUtils.getCurrentUserId();

        userRepository.findById(userId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "USER_NF_001", "User not found"));

        postRepository.findById(postId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "POST_NF_001", "Post not found"));

        boolean alreadyLiked = likeRepository.existsByPost_IdAndUser_Id(postId, userId);

        if (alreadyLiked) {
            likeRepository.deleteByPost_IdAndUser_Id(postId, userId);
        } else {
            LikeEntity like = LikeEntity.builder()
                    .post(postRepository.getReferenceById(postId))
                    .user(userRepository.getReferenceById(userId))
                    .build();
            likeRepository.save(like);
        }

        long likeCount = likeRepository.countByPost_Id(postId);

        return LikeResponse.builder()
                .postId(postId)
                .isLiked(!alreadyLiked)
                .likeCount(likeCount)
                .build();
    }

    public LikeResponse getLikeStatus(String postId) {
        String userId = SecurityUtils.getCurrentUserId();

        postRepository.findById(postId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "POST_NF_001", "Post not found"));

        boolean isLiked = likeRepository.existsByPost_IdAndUser_Id(postId, userId);
        long likeCount = likeRepository.countByPost_Id(postId);

        return LikeResponse.builder()
                .postId(postId)
                .isLiked(isLiked)
                .likeCount(likeCount)
                .build();
    }
}