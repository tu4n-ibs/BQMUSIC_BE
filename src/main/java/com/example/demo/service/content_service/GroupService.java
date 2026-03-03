package com.example.demo.service.content_service;

import com.example.demo.common.AppException;
import com.example.demo.entity.*;
import com.example.demo.model.content_dto.CreateGroupRequest;
import com.example.demo.model.content_dto.GroupJoinRequestResponse;
import com.example.demo.model.enum_object.GroupJoinStatus;
import com.example.demo.model.enum_object.GroupRole;
import com.example.demo.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public List<GroupJoinRequestResponse> getPendingRequests(String groupId, String currentUserId) {
        GroupMemberEntity admin = groupMemberRepository.findByGroupEntity_IdAndUserEntity_Id(groupId, currentUserId)
                .orElseThrow(() -> new AppException(HttpStatus.FORBIDDEN, "GROUP_FB_001", "User is not a member of this group"));
        groupRepository.findById(groupId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "GROUP_NF_001", "Group not found"));
        if (admin.getGroupRole() != GroupRole.ADMIN) {
            throw new AppException(HttpStatus.FORBIDDEN, "GROUP_FB_002", "Only group admin can perform this action");
        }
         List<GroupJoinRequestEntity> groupJoinRequestEntities= groupJoinRequestRepository.
                  findAllByGroup_IdAndGroupJoinStatus(groupId, GroupJoinStatus.PENDING);

        return groupJoinRequestEntities.stream().map(groupJoinRequestEntity -> {
            UserEntity userEntity = groupJoinRequestEntity.getUser();
            return GroupJoinRequestResponse.builder()
                    .userId(userEntity.getId())
                    .name(userEntity.getName())
                    .imageUrl(userEntity.getImageUrl())
                    .joinDate(groupJoinRequestEntity.getCreatedAt())
                    .build();
        }).toList();
    }
    @Transactional
    public void leaveGroup(String groupId, String currentUserId) {
        GroupMemberEntity member = groupMemberRepository.findByGroupEntity_IdAndUserEntity_Id(groupId, currentUserId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "GROUP_NF_003", "Bạn không ở trong nhóm này"));

        if (member.getGroupRole() == GroupRole.ADMIN) {
            long adminCount = groupMemberRepository.countByGroupEntity_IdAndGroupRole(groupId, GroupRole.ADMIN);
            if (adminCount == 1) {
                throw new AppException(HttpStatus.BAD_REQUEST, "GROUP_BR_004", "Admin duy nhất không thể rời nhóm. Hãy chỉ định admin mới hoặc giải tán nhóm.");
            }
        }
        groupMemberRepository.delete(member);
    }
    // ==================== QUẢN LÝ BAN (CHẶN) ====================

    @Transactional
    public void banUser(String groupId, String targetUserId, String currentUserId) {
        // 1. Kiểm tra quyền của người thực hiện (Chỉ ADMIN mới có quyền ban)
        GroupMemberEntity actor = groupMemberRepository.findByGroupEntity_IdAndUserEntity_Id(groupId, currentUserId)
                .orElseThrow(() -> new AppException(HttpStatus.FORBIDDEN, "GROUP_FB_001", "User is not a member of this group"));

        if (actor.getGroupRole() != GroupRole.ADMIN) {
            throw new AppException(HttpStatus.FORBIDDEN, "GROUP_FB_002", "Only group admin can ban users");
        }

        // 2. Không cho phép tự ban chính mình
        if (currentUserId.equals(targetUserId)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "GROUP_BR_005", "You cannot ban yourself");
        }

        // 3. Kiểm tra user bị ban có tồn tại không
        UserEntity targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "USER_NF_001", "Target user not found"));

        GroupEntity group = actor.getGroupEntity();

        // 4. Kiểm tra xem đã bị ban chưa để tránh trùng lặp
        boolean isAlreadyBanned = groupBanRepository.existsByGroup_IdAndUserBan_Id(groupId, targetUserId);
        if (isAlreadyBanned) {
            throw new AppException(HttpStatus.BAD_REQUEST, "GROUP_BR_006", "User is already banned from this group");
        }

        // 5. Nếu targetUser đang là thành viên, xóa khỏi danh sách thành viên
        groupMemberRepository.findByGroupEntity_IdAndUserEntity_Id(groupId, targetUserId)
                .ifPresent(groupMemberRepository::delete);

        // 6. Xóa các join request đang chờ (nếu có)
        groupJoinRequestRepository.findAllByGroup_IdAndGroupJoinStatus(groupId, GroupJoinStatus.PENDING)
                .stream()
                .filter(req -> req.getUser().getId().equals(targetUserId))
                .forEach(groupJoinRequestRepository::delete);

        // 7. Thực hiện lưu vào bảng Ban
        GroupBanEntity ban = GroupBanEntity.builder()
                .group(group)
                .userBan(targetUser)
                .build();

        groupBanRepository.save(ban);
    }

    @Transactional
    public void unbanUser(String groupId, String targetUserId, String currentUserId) {
        // 1. Kiểm tra quyền admin
        validateAdminRole(groupId, currentUserId);

        // 2. Tìm bản ghi ban
        GroupBanEntity banEntry = groupBanRepository.findByGroup_IdAndUserBan_Id(groupId, targetUserId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "GROUP_NF_004", "User is not in the ban list"));

        // 3. Xóa bản ghi ban
        groupBanRepository.delete(banEntry);
    }

    private void validateAdminRole(String groupId, String userId) {
        GroupMemberEntity member = groupMemberRepository.findByGroupEntity_IdAndUserEntity_Id(groupId, userId)
                .orElseThrow(() -> new AppException(HttpStatus.FORBIDDEN, "GROUP_FB_001", "User is not a member of this group"));
        if (member.getGroupRole() != GroupRole.ADMIN) {
            throw new AppException(HttpStatus.FORBIDDEN, "GROUP_FB_002", "Only group admin can perform this action");
        }
    }
}