package com.arenahub.infrastructure.persistence.group;

import com.arenahub.domain.group.*;
import com.arenahub.domain.group.vo.GroupRole;
import com.arenahub.domain.group.vo.GroupStatus;
import com.arenahub.domain.group.vo.Sport;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class GroupPersistenceAdapter implements GroupRepository {

    private final GroupJpaRepository groupJpa;
    private final GroupMemberJpaRepository memberJpa;
    private final GroupInviteJpaRepository inviteJpa;

    public GroupPersistenceAdapter(GroupJpaRepository groupJpa,
                                   GroupMemberJpaRepository memberJpa,
                                   GroupInviteJpaRepository inviteJpa) {
        this.groupJpa = groupJpa;
        this.memberJpa = memberJpa;
        this.inviteJpa = inviteJpa;
    }

    @Override
    public Group save(Group group) {
        return groupJpa.save(GroupJpaEntity.fromDomain(group)).toDomain();
    }

    @Override
    public Optional<Group> findById(UUID id) {
        return groupJpa.findByIdAndDeletedAtIsNull(id).map(GroupJpaEntity::toDomain);
    }

    @Override
    public List<GroupSummary> listByUserId(UUID userId) {
        return groupJpa.findGroupSummariesByUserId(userId).stream()
                .map(p -> new GroupSummary(
                        p.getId(),
                        p.getName(),
                        Sport.valueOf(p.getSport()),
                        p.getPhotoUrl(),
                        GroupStatus.valueOf(p.getStatus()),
                        p.getMemberCount().intValue(),
                        GroupRole.valueOf(p.getMyRole())
                ))
                .toList();
    }

    @Override
    public GroupMember saveMember(GroupMember member) {
        return memberJpa.save(GroupMemberJpaEntity.fromDomain(member)).toDomain();
    }

    @Override
    public List<GroupMember> findMembersByGroupId(UUID groupId) {
        return memberJpa.findByGroupId(groupId).stream()
                .map(GroupMemberJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<GroupMember> findMemberById(UUID memberId) {
        return memberJpa.findById(memberId).map(GroupMemberJpaEntity::toDomain);
    }

    @Override
    public Optional<GroupMember> findMemberByGroupIdAndUserId(UUID groupId, UUID userId) {
        return memberJpa.findByGroupIdAndUserId(groupId, userId).map(GroupMemberJpaEntity::toDomain);
    }

    @Override
    public boolean isMember(UUID groupId, UUID userId) {
        return memberJpa.existsByGroupIdAndUserId(groupId, userId);
    }

    @Override
    public long countOwners(UUID groupId) {
        return memberJpa.countByGroupIdAndRole(groupId, GroupRole.OWNER);
    }

    @Override
    public void deleteMember(UUID memberId) {
        memberJpa.deleteById(memberId);
    }

    @Override
    public GroupInvite saveInvite(GroupInvite invite) {
        return inviteJpa.save(GroupInviteJpaEntity.fromDomain(invite)).toDomain();
    }

    @Override
    public List<GroupInvite> findActiveInvitesByGroupId(UUID groupId) {
        return inviteJpa.findByGroupIdAndActiveTrue(groupId).stream()
                .map(GroupInviteJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<GroupInvite> findInviteById(UUID inviteId) {
        return inviteJpa.findById(inviteId).map(GroupInviteJpaEntity::toDomain);
    }

    @Override
    public Optional<GroupInvite> findInviteByToken(String token) {
        return inviteJpa.findByToken(token).map(GroupInviteJpaEntity::toDomain);
    }

    @Override
    public void deactivateAllInvitesByGroupId(UUID groupId) {
        inviteJpa.deactivateAllByGroupId(groupId);
    }
}
