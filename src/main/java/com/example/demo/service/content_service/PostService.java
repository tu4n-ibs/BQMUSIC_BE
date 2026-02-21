package com.example.demo.service.content_service;

import com.example.demo.common.AppException;
import com.example.demo.common.SecurityUtils;
import com.example.demo.entity.PostEntity;
import com.example.demo.entity.UserEntity;
import com.example.demo.model.enum_object.ContextType;
import com.example.demo.model.enum_object.PostType;
import com.example.demo.model.enum_object.TargetType;
import com.example.demo.model.enum_object.Visibility;
import com.example.demo.model.content_dto.CreatePostRequest;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.SongRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final SongRepository songRepository;

    public void userCreateNewPost(CreatePostRequest createPostRequest) {
        String userId = SecurityUtils.getCurrentUserId();
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(()->new AppException(HttpStatus.NOT_FOUND,"USER_NF_001","User not found"));
        ContextType contextType = ContextType.PROFILE;
        PostType postType = PostType.OWNER;
        String content = createPostRequest.getContent();
        Visibility visibility = createPostRequest.getVisibility();
        TargetType targetType = createPostRequest.getTargetType();
        String targetId = createPostRequest.getTargetId();
        PostEntity postEntity = new PostEntity(userEntity,contextType,null,postType,null,content,visibility,targetType,targetId);
        postRepository.save(postEntity);
    }

}