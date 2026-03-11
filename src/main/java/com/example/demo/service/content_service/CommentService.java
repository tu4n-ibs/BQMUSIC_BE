package com.example.demo.service.content_service;

import com.example.demo.common.AppException;
import com.example.demo.common.SecurityUtils;
import com.example.demo.entity.CommentEntity;
import com.example.demo.entity.PostEntity;
import com.example.demo.entity.UserEntity;
import com.example.demo.model.content_dto.CommentResponse;
import com.example.demo.model.content_dto.CreateCommentRequest;
import com.example.demo.model.content_dto.UpdateCommentRequest;
import com.example.demo.model.enum_object.ActionType;
import com.example.demo.model.enum_object.TargetNotiType;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    // 1. Tạo comment / reply
    public CommentResponse createComment(CreateCommentRequest request) {
        String userId = SecurityUtils.getCurrentUserId();

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "USER_NF_001", "User not found"));

        PostEntity post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "POST_NF_001", "Post not found"));

        int depth = 0;
        CommentEntity parentComment = null;

        if (request.getParentCommentId() != null) {
            parentComment = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "CMT_NF_001", "Parent comment not found"));

            if (!parentComment.getPost().getId().equals(request.getPostId())) {
                throw new AppException(HttpStatus.BAD_REQUEST, "CMT_BR_001", "Parent comment does not belong to this post");
            }

            depth = Math.min(parentComment.getDepth() + 1, 3);
        }

        CommentEntity comment = CommentEntity.builder()
                .content(request.getContent())
                .post(post)
                .user(user)
                .parent(parentComment)
                .build();

        CommentEntity savedComment = commentRepository.save(comment);

        notificationService.send(user, post.getUserEntity(),
                ActionType.COMMENT, TargetNotiType.POST, post.getId());

        if (parentComment != null) {
            String compositeId = post.getId() + ":" + savedComment.getId() + ":" + parentComment.getId();
            notificationService.send(user, parentComment.getUser(),
                    ActionType.COMMENT, TargetNotiType.COMMENT, compositeId);
        }
        return toResponse(savedComment);
    }

    public Page<CommentResponse> getRootComments(String postId, Pageable pageable) {
        if (!postRepository.existsById(postId)) {
            throw new AppException(HttpStatus.NOT_FOUND, "POST_NF_001", "Post not found");
        }

        return commentRepository
                .findAllByPost_IdAndParentIsNull(postId, pageable)
                .map(this::toResponse);
    }

    // 3. Lấy replies (Sửa lỗi .stream())
    public Page<CommentResponse> getReplies(String commentId, Pageable pageable) {
        if (!commentRepository.existsById(commentId)) {
            throw new AppException(HttpStatus.NOT_FOUND, "CMT_NF_001", "Comment not found");
        }

        return commentRepository
                .findAllByParent_Id(commentId, pageable)
                .map(this::toResponse); // BỎ .stream(), dùng trực tiếp .map() của Page
    }

    // 4. Sửa comment
    public CommentResponse updateComment(String commentId, UpdateCommentRequest request) {
        String userId = SecurityUtils.getCurrentUserId();

        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "CMT_NF_001", "Comment not found"));

        if (!comment.getUser().getId().equals(userId)) {
            throw new AppException(HttpStatus.FORBIDDEN, "CMT_FB_001", "You don't have permission to edit this comment");
        }

        comment.setContent(request.getContent());
        return toResponse(commentRepository.save(comment));
    }

    // 5. Xóa comment
    public void deleteComment(String commentId) {
        String userId = SecurityUtils.getCurrentUserId();

        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "CMT_NF_001", "Comment not found"));

        if (!comment.getUser().getId().equals(userId)) {
            throw new AppException(HttpStatus.FORBIDDEN, "CMT_FB_002", "You don't have permission to delete this comment");
        }

        // Xóa toàn bộ replies trước rồi mới xóa comment cha
        deleteRepliesRecursive(commentId);
        commentRepository.delete(comment);
    }

    // Đệ quy xóa replies
    private void deleteRepliesRecursive(String commentId) {
        Page<CommentEntity> replies = commentRepository.findAllByParent_Id(commentId,Pageable.ofSize(10000));
        for (CommentEntity reply : replies) {
            deleteRepliesRecursive(reply.getId());
            commentRepository.delete(reply);
        }
    }

    // Mapper
    private CommentResponse toResponse(CommentEntity comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .userId(comment.getUser().getId())
                .userName(comment.getUser().getName())
                .userImageUrl(comment.getUser().getImageUrl())
                .postId(comment.getPost().getId())
                .parentCommentId(comment.getParent() != null
                        ? comment.getParent().getId()
                        : null)
                .depth(comment.getDepth())
                .replyCount(commentRepository.countByParent_Id(comment.getId()))
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
