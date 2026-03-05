package com.example.demo.service.content_service;

import com.example.demo.entity.AlbumEntity;
import com.example.demo.entity.GroupEntity;
import com.example.demo.entity.PostEntity;
import com.example.demo.entity.SongEntity;
import com.example.demo.mapper.PostMapper;
import com.example.demo.model.content_dto.PostResponsePage;
import com.example.demo.model.enum_object.ContextType;
import com.example.demo.model.enum_object.TargetType;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NewsfeedService  {

    private final PostRepository postRepository;
    private final SongRepository songRepository;
    private final AlbumRepository albumRepository;
    private final GroupRepository groupRepository;
    private final PostReactionRepository postReactionRepository;
    private final PlayHistoryRepository playHistoryRepository;


    public Page<PostResponsePage> getNewsfeed(String currentUserId) {

    }
}