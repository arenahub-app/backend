package com.arenahub.domain.group;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroupRepository {

    // Group
    Group save(Group group);
    Optional<Group> findById(UUID id);
    List<GroupSummary> listByUserId(UUID userId);

    // Members
    GroupMember saveMember(GroupMember member);
    List<GroupMember> findMembersByGroupId(UUID groupId);
    Optional<GroupMember> findMemberById(UUID memberId);
    Optional<GroupMember> findMemberByGroupIdAndUserId(UUID groupId, UUID userId);
    boolean isMember(UUID groupId, UUID userId);
    long countOwners(UUID groupId);
    void deleteMember(UUID memberId);

    // Invites
    GroupInvite saveInvite(GroupInvite invite);
    List<GroupInvite> findActiveInvitesByGroupId(UUID groupId);
    Optional<GroupInvite> findInviteById(UUID inviteId);
    Optional<GroupInvite> findInviteByToken(String token);
    void deactivateAllInvitesByGroupId(UUID groupId);
}
