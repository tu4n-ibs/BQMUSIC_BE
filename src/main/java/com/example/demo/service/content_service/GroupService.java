package com.example.demo.service.content_service;

import com.example.demo.common.AppException;
import com.example.demo.entity.GroupEntity;
import com.example.demo.entity.GroupJoinRequestEntity;
import com.example.demo.entity.GroupMemberEntity;
import com.example.demo.entity.UserEntity;
import com.example.demo.model.content_dto.CreateGroupRequest;
import com.example.demo.model.enum_object.GroupJoinStatus;
import com.example.demo.model.enum_object.GroupRole;
import com.example.demo.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GroupService {
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupBanRepository groupBanRepository;
    private final GroupJoinRequestRepository groupJoinRequestRepository;
    private final UserRepository userRepository;

    // ==================== TẠO GROUP ====================
    @Transactional
    public void createGroup(CreateGroupRequest request, String currentUserId) {
        UserEntity creator = userRepository.findById(currentUserId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "USER_NF_001", "User not found"));

        GroupEntity group = GroupEntity.builder()
                .name(request.getName())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .isPrivate(request.getIsPrivate() != null ? request.getIsPrivate() : false)
                .requirePostApproval(request.getRequirePostApproval() != null ? request.getRequirePostApproval() : false)
                .build();

        groupRepository.save(group);

        // Người tạo group tự động là ADMIN
        GroupMemberEntity adminMember = GroupMemberEntity.builder()
                .groupEntity(group)
                .userEntity(creator)
                .groupRole(GroupRole.ADMIN)
                .build();

        groupMemberRepository.save(adminMember);
    }

    // ==================== BẬT/TẮT DUYỆT BÀI VIẾT ====================
    public void toggleRequirePostApproval(String groupId, String currentUserId) {
        GroupMemberEntity member = groupMemberRepository.findByGroupEntity_IdAndUserEntity_Id(groupId, currentUserId)
                .orElseThrow(() -> new AppException(HttpStatus.FORBIDDEN, "GROUP_FB_001", "User is not a member of this group"));

        if (member.getGroupRole() != GroupRole.ADMIN) {
            throw new AppException(HttpStatus.FORBIDDEN, "GROUP_FB_002", "Only group admin can perform this action");
        }

        GroupEntity group = member.getGroupEntity();
        group.setRequirePostApproval(!group.getRequirePostApproval());
        groupRepository.save(group);
    }

    // ==================== BẬT/TẮT PRIVATE GROUP ====================
    public void togglePrivateGroup(String groupId, String currentUserId) {
        GroupMemberEntity member = groupMemberRepository.findByGroupEntity_IdAndUserEntity_Id(groupId, currentUserId)
                .orElseThrow(() -> new AppException(HttpStatus.FORBIDDEN, "GROUP_FB_001", "User is not a member of this group"));

        if (member.getGroupRole() != GroupRole.ADMIN) {
            throw new AppException(HttpStatus.FORBIDDEN, "GROUP_FB_002", "Only group admin can perform this action");
        }

        GroupEntity group = member.getGroupEntity();
        group.setIsPrivate(!group.getIsPrivate());
        groupRepository.save(group);
    }

    // ==================== GỬI REQUEST VÀO GROUP ====================
    @Transactional
    public void sendJoinRequest(String groupId, String currentUserId) {
        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "GROUP_NF_001", "Group not found"));

        UserEntity user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "USER_NF_001", "User not found"));

        // Kiểm tra đã là thành viên chưa
        boolean isMember = groupMemberRepository.existsByGroupEntity_IdAndUserEntity_Id(groupId, currentUserId);
        if (isMember) {
            throw new AppException(HttpStatus.BAD_REQUEST, "GROUP_BR_001", "User is already a member of this group");
        }

        // Kiểm tra bị ban không
        boolean isBanned = groupBanRepository.existsByGroup_IdAndUserBan_Id(groupId, currentUserId);
        if (isBanned) {
            throw new AppException(HttpStatus.FORBIDDEN, "GROUP_FB_003", "User is banned from this group");
        }

        // Nếu group không private → tham gia luôn
        if (!group.getIsPrivate()) {
            GroupMemberEntity newMember = GroupMemberEntity.builder()
                    .groupEntity(group)
                    .userEntity(user)
                    .groupRole(GroupRole.MEMBER)
                    .build();
            groupMemberRepository.save(newMember);
            return;
        }

        // Kiểm tra đã gửi request chưa
        boolean alreadyRequested = groupJoinRequestRepository
                .existsByGroup_IdAndUser_IdAndGroupJoinStatus(groupId, currentUserId, GroupJoinStatus.PENDING);
        if (alreadyRequested) {
            throw new AppException(HttpStatus.BAD_REQUEST, "GROUP_BR_002", "Join request already sent and is pending");
        }

        // Tạo join request
        GroupJoinRequestEntity joinRequest = GroupJoinRequestEntity.builder()
                .group(group)
                .user(user)
                .groupJoinStatus(GroupJoinStatus.PENDING)
                .build();

        groupJoinRequestRepository.save(joinRequest);
    }

    // ==================== ADMIN DUYỆT / TỪ CHỐI REQUEST ====================
    @Transactional
    public void reviewJoinRequest(String groupId, String requestId, boolean approve, String currentUserId) {
        // Kiểm tra quyền admin
        GroupMemberEntity admin = groupMemberRepository.findByGroupEntity_IdAndUserEntity_Id(groupId, currentUserId)
                .orElseThrow(() -> new AppException(HttpStatus.FORBIDDEN, "GROUP_FB_001", "User is not a member of this group"));

        if (admin.getGroupRole() != GroupRole.ADMIN) {
            throw new AppException(HttpStatus.FORBIDDEN, "GROUP_FB_002", "Only group admin can perform this action");
        }

        // Lấy request
        GroupJoinRequestEntity joinRequest = groupJoinRequestRepository.findById(requestId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "GROUP_NF_002", "Join request not found"));

        if (joinRequest.getGroupJoinStatus() != GroupJoinStatus.PENDING) {
            throw new AppException(HttpStatus.BAD_REQUEST, "GROUP_BR_003", "Join request has already been reviewed");
        }

        if (approve) {
            joinRequest.setGroupJoinStatus(GroupJoinStatus.APPROVED);

            GroupMemberEntity newMember = GroupMemberEntity.builder()
                    .groupEntity(joinRequest.getGroup())
                    .userEntity(joinRequest.getUser())
                    .groupRole(GroupRole.MEMBER)
                    .build();

            groupMemberRepository.save(newMember);
        } else {
            joinRequest.setGroupJoinStatus(GroupJoinStatus.REJECTED);
        }

        groupJoinRequestRepository.save(joinRequest);
    }

//    // ==================== LẤY DANH SÁCH REQUEST PENDING (ADMIN) ====================
//    public List<GroupJoinRequestResponse> getPendingRequests(String groupId, String currentUserId) {
//        GroupMemberEntity admin = groupMemberRepository.findByGroupIdAndUserId(groupId, currentUserId)
//                .orElseThrow(() -> new AppException(HttpStatus.FORBIDDEN, "GROUP_FB_001", "User is not a member of this group"));
//
//        if (admin.getGroupRole() != GroupRole.ADMIN) {
//            throw new AppException(HttpStatus.FORBIDDEN, "GROUP_FB_002", "Only group admin can perform this action");
//        }
//
//        return groupJoinRequestRepository
//                .findAllByGroupIdAndStatus(groupId, GroupJoinStatus.PENDING)
//                .stream()
//                .map(GroupJoinRequestResponse::fromEntity)
//                .toList();
//    }
}