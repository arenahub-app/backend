package com.arenahub.application.group;

import com.arenahub.application.exception.*;
import com.arenahub.application.group.port.in.*;
import com.arenahub.domain.group.*;
import com.arenahub.domain.group.vo.*;
import com.arenahub.domain.user.UserRepository;
import com.arenahub.presentation.group.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class GroupService implements
        CreateGroupUseCase, GetGroupUseCase, ListGroupsUseCase,
        UpdateGroupUseCase, DeactivateGroupUseCase,
        ListMembersUseCase, UpdateMemberUseCase, RemoveMemberUseCase,
        GenerateInviteUseCase, ListInvitesUseCase, DeactivateInviteUseCase,
        GetInvitePreviewUseCase, JoinByInviteUseCase {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    public GroupService(GroupRepository groupRepository, UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    @Override
    public GroupResponse execute(CreateGroupUseCase.Command cmd) {
        Group group = Group.create(new GroupName(cmd.name()), cmd.sport(), cmd.description());
        group = groupRepository.save(group);

        GroupMember owner = GroupMember.create(group.getId(), cmd.userId(), GroupRole.OWNER);
        groupRepository.saveMember(owner);

        return toGroupResponse(group, 1, GroupRole.OWNER);
    }

    @Override
    @Transactional(readOnly = true)
    public GroupResponse execute(GetGroupUseCase.Command cmd) {
        Group group = requireGroup(cmd.groupId());
        GroupMember myMember = requireMember(cmd.groupId(), cmd.userId());
        int count = groupRepository.findMembersByGroupId(cmd.groupId()).size();
        return toGroupResponse(group, count, myMember.getRole());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupSummaryResponse> execute(UUID userId) {
        return groupRepository.listByUserId(userId).stream()
                .map(s -> new GroupSummaryResponse(
                        s.id(), s.name(), s.sport(), s.photoUrl(), s.status(),
                        s.memberCount(), s.myRole()))
                .toList();
    }

    @Override
    public GroupResponse execute(UpdateGroupUseCase.Command cmd) {
        Group group = requireGroup(cmd.groupId());
        GroupMember myMember = requireMember(cmd.groupId(), cmd.userId());
        requireRole(myMember, GroupRole.ADMIN);
        requireActive(group);

        GroupName name = cmd.name() != null ? new GroupName(cmd.name()) : null;
        group.update(name, cmd.sport(), cmd.description(), cmd.pixKey());
        group = groupRepository.save(group);

        int count = groupRepository.findMembersByGroupId(cmd.groupId()).size();
        return toGroupResponse(group, count, myMember.getRole());
    }

    @Override
    public void execute(DeactivateGroupUseCase.Command cmd) {
        Group group = requireGroup(cmd.groupId());
        GroupMember myMember = requireMember(cmd.groupId(), cmd.userId());
        requireRole(myMember, GroupRole.OWNER);

        group.deactivate();
        groupRepository.save(group);
        groupRepository.deactivateAllInvitesByGroupId(cmd.groupId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemberResponse> execute(ListMembersUseCase.Command cmd) {
        requireGroup(cmd.groupId());
        requireMember(cmd.groupId(), cmd.userId());
        return groupRepository.findMembersByGroupId(cmd.groupId()).stream()
                .map(this::toMemberResponse)
                .toList();
    }

    @Override
    public MemberResponse execute(UpdateMemberUseCase.Command cmd) {
        requireGroup(cmd.groupId());
        GroupMember requester = requireMember(cmd.groupId(), cmd.requesterId());

        GroupMember target = groupRepository.findMemberById(cmd.memberId())
                .filter(m -> m.getGroupId().equals(cmd.groupId()))
                .orElseThrow(MemberNotFoundException::new);

        GroupRole newRole = cmd.role();
        if (newRole != null) {
            if (target.getRole() == GroupRole.OWNER) {
                throw new CannotDemoteOwnerException();
            }
            if (newRole == GroupRole.OWNER || newRole == GroupRole.ADMIN) {
                requireRole(requester, GroupRole.OWNER);
            } else {
                requireRole(requester, GroupRole.ADMIN);
            }
            target.changeRole(newRole);
        } else {
            boolean isSelf = requester.getId().equals(target.getId());
            if (isSelf) {
                if (cmd.skill() != null || cmd.subscriptionActive() != null) {
                    throw new InsufficientRoleException();
                }
            } else {
                requireRole(requester, GroupRole.ADMIN);
            }
        }

        if (cmd.skill() != null) {
            target.updateSkill(Skill.of(cmd.skill()));
        }
        if (cmd.position() != null) {
            target.updatePosition(cmd.position());
        }
        if (cmd.subscriptionActive() != null) {
            target.updateSubscription(cmd.subscriptionActive(), target.getSubscriptionAmount());
        }

        return toMemberResponse(groupRepository.saveMember(target));
    }

    @Override
    public void execute(RemoveMemberUseCase.Command cmd) {
        requireGroup(cmd.groupId());
        GroupMember requester = requireMember(cmd.groupId(), cmd.requesterId());
        requireRole(requester, GroupRole.ADMIN);

        GroupMember target = groupRepository.findMemberById(cmd.memberId())
                .filter(m -> m.getGroupId().equals(cmd.groupId()))
                .orElseThrow(MemberNotFoundException::new);

        if (target.getRole() == GroupRole.OWNER && groupRepository.countOwners(cmd.groupId()) <= 1) {
            throw new CannotRemoveLastOwnerException();
        }

        groupRepository.deleteMember(cmd.memberId());
    }

    @Override
    public InviteResponse execute(GenerateInviteUseCase.Command cmd) {
        Group group = requireGroup(cmd.groupId());
        GroupMember myMember = requireMember(cmd.groupId(), cmd.userId());
        requireRole(myMember, GroupRole.ADMIN);
        requireActive(group);

        GroupInvite invite = GroupInvite.create(cmd.groupId(), cmd.userId());
        return toInviteResponse(groupRepository.saveInvite(invite));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InviteResponse> execute(ListInvitesUseCase.Command cmd) {
        requireGroup(cmd.groupId());
        GroupMember myMember = requireMember(cmd.groupId(), cmd.userId());
        requireRole(myMember, GroupRole.ADMIN);

        return groupRepository.findActiveInvitesByGroupId(cmd.groupId()).stream()
                .map(this::toInviteResponse)
                .toList();
    }

    @Override
    public void execute(DeactivateInviteUseCase.Command cmd) {
        requireGroup(cmd.groupId());
        GroupMember myMember = requireMember(cmd.groupId(), cmd.userId());
        requireRole(myMember, GroupRole.ADMIN);

        GroupInvite invite = groupRepository.findInviteById(cmd.inviteId())
                .filter(i -> i.getGroupId().equals(cmd.groupId()))
                .orElseThrow(InviteNotFoundException::new);

        invite.deactivate();
        groupRepository.saveInvite(invite);
    }

    @Override
    @Transactional(readOnly = true)
    public InvitePreviewResponse execute(String token) {
        GroupInvite invite = groupRepository.findInviteByToken(token)
                .orElseThrow(InviteNotFoundException::new);
        if (!invite.isValid()) throw new InviteExpiredException();

        Group group = requireGroup(invite.getGroupId());
        int memberCount = groupRepository.findMembersByGroupId(invite.getGroupId()).size();

        return new InvitePreviewResponse(
                group.getId(), group.getName().value(), group.getSport(),
                group.getPhotoUrl(), memberCount, invite.getExpiresAt());
    }

    @Override
    public JoinGroupResponse execute(JoinByInviteUseCase.Command cmd) {
        GroupInvite invite = groupRepository.findInviteByToken(cmd.token())
                .orElseThrow(InviteNotFoundException::new);
        if (!invite.isValid()) throw new InviteExpiredException();

        Group group = requireGroup(invite.getGroupId());
        requireActive(group);

        if (groupRepository.isMember(group.getId(), cmd.userId())) {
            throw new UserAlreadyMemberException();
        }

        invite.incrementUsage();
        groupRepository.saveInvite(invite);

        GroupMember member = GroupMember.create(group.getId(), cmd.userId(), GroupRole.PLAYER);
        member = groupRepository.saveMember(member);

        return new JoinGroupResponse(group.getId(), member.getId(), member.getRole());
    }

    private Group requireGroup(UUID groupId) {
        return groupRepository.findById(groupId).orElseThrow(GroupNotFoundException::new);
    }

    private GroupMember requireMember(UUID groupId, UUID userId) {
        return groupRepository.findMemberByGroupIdAndUserId(groupId, userId)
                .orElseThrow(NotAMemberException::new);
    }

    private void requireRole(GroupMember member, GroupRole required) {
        if (!member.getRole().isAtLeast(required)) throw new InsufficientRoleException();
    }

    private void requireActive(Group group) {
        if (!group.isActive()) throw new GroupInactiveException();
    }

    private GroupResponse toGroupResponse(Group group, int memberCount, GroupRole myRole) {
        return new GroupResponse(
                group.getId(), group.getName().value(), group.getSport(),
                group.getDescription(), group.getPhotoUrl(), group.getPixKey(),
                group.getStatus(), memberCount, myRole,
                group.getCreatedAt(), group.getUpdatedAt());
    }

    private MemberResponse toMemberResponse(GroupMember member) {
        String userName = userRepository.findById(member.getUserId())
                .map(u -> u.getName().value())
                .orElse(null);
        return new MemberResponse(
                member.getId(), member.getUserId(), member.getGroupId(),
                userName, member.getRole(), member.getSkill().value(), member.getSkillSource(),
                member.getPosition(), member.isSubscriber(), member.getJoinedAt());
    }

    private InviteResponse toInviteResponse(GroupInvite invite) {
        return new InviteResponse(
                invite.getId(), invite.getGroupId(), invite.getToken(),
                invite.getUsageCount(), invite.getMaxUsages(),
                invite.isActive(), invite.getExpiresAt(), invite.getCreatedAt());
    }
}
