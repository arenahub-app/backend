package com.arenahub.application.group;

import com.arenahub.application.exception.*;
import com.arenahub.application.group.port.in.*;
import com.arenahub.domain.group.*;
import com.arenahub.domain.group.vo.*;
import com.arenahub.domain.user.UserRepository;
import com.arenahub.presentation.group.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @Mock
    GroupRepository groupRepository;

    @Mock
    UserRepository userRepository;

    private GroupService groupService;

    @BeforeEach
    void setUp() {
        groupService = new GroupService(groupRepository, userRepository);
    }

    @Test
    void createGroup_savesGroupAndOwnerMember() {
        UUID userId = UUID.randomUUID();
        when(groupRepository.save(any(Group.class))).thenAnswer(inv -> inv.getArgument(0));
        when(groupRepository.saveMember(any(GroupMember.class))).thenAnswer(inv -> inv.getArgument(0));

        GroupResponse response = groupService.execute(new CreateGroupUseCase.Command(
                userId, "Arena FC", Sport.FOOTBALL, "Futebol semanal"));

        assertThat(response.name()).isEqualTo("Arena FC");
        assertThat(response.sport()).isEqualTo(Sport.FOOTBALL);
        assertThat(response.memberCount()).isEqualTo(1);
        assertThat(response.myRole()).isEqualTo(GroupRole.OWNER);
        verify(groupRepository).save(any(Group.class));
        verify(groupRepository).saveMember(argThat(m -> m.getRole() == GroupRole.OWNER));
    }

    @Test
    void getGroup_throwsGroupNotFound_whenGroupAbsent() {
        UUID groupId = UUID.randomUUID();
        when(groupRepository.findById(groupId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> groupService.execute(
                new GetGroupUseCase.Command(groupId, UUID.randomUUID())))
                .isInstanceOf(GroupNotFoundException.class);
    }

    @Test
    void getGroup_throwsNotAMember_whenUserNotInGroup() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Group group = Group.create(new GroupName("Arena FC"), Sport.FOOTBALL, null);

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(groupRepository.findMemberByGroupIdAndUserId(groupId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> groupService.execute(
                new GetGroupUseCase.Command(groupId, userId)))
                .isInstanceOf(NotAMemberException.class);
    }

    @Test
    void updateGroup_throwsInsufficientRole_whenCallerIsPlayer() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Group group = Group.create(new GroupName("Arena FC"), Sport.FOOTBALL, null);
        GroupMember player = GroupMember.create(groupId, userId, GroupRole.PLAYER);

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(groupRepository.findMemberByGroupIdAndUserId(groupId, userId)).thenReturn(Optional.of(player));

        assertThatThrownBy(() -> groupService.execute(
                new UpdateGroupUseCase.Command(groupId, userId, "New Name", null, null, null)))
                .isInstanceOf(InsufficientRoleException.class);
    }

    @Test
    void deactivateGroup_throwsInsufficientRole_whenCallerIsAdmin() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Group group = Group.create(new GroupName("Arena FC"), Sport.FOOTBALL, null);
        GroupMember admin = GroupMember.create(groupId, userId, GroupRole.ADMIN);

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(groupRepository.findMemberByGroupIdAndUserId(groupId, userId)).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> groupService.execute(
                new DeactivateGroupUseCase.Command(groupId, userId)))
                .isInstanceOf(InsufficientRoleException.class);
    }

    @Test
    void removeMember_throwsCannotRemoveLastOwner() {
        UUID groupId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        Group group = Group.create(new GroupName("Arena FC"), Sport.FOOTBALL, null);
        GroupMember requester = GroupMember.create(groupId, requesterId, GroupRole.OWNER);
        GroupMember target = GroupMember.create(groupId, UUID.randomUUID(), GroupRole.OWNER);
        target = GroupMember.reconstitute(targetId, groupId, UUID.randomUUID(), GroupRole.OWNER,
                Skill.DEFAULT, SkillSource.DEFAULT, null, false, null, false, null,
                java.time.Instant.now(), java.time.Instant.now());

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(groupRepository.findMemberByGroupIdAndUserId(groupId, requesterId)).thenReturn(Optional.of(requester));
        when(groupRepository.findMemberById(targetId)).thenReturn(Optional.of(target));
        when(groupRepository.countOwners(groupId)).thenReturn(1L);

        UUID finalTargetId = targetId;
        assertThatThrownBy(() -> groupService.execute(
                new RemoveMemberUseCase.Command(groupId, finalTargetId, requesterId)))
                .isInstanceOf(CannotRemoveLastOwnerException.class);
    }

    @Test
    void joinByInvite_throwsUserAlreadyMember_whenAlreadyInGroup() {
        UUID userId = UUID.randomUUID();
        GroupInvite invite = GroupInvite.create(UUID.randomUUID(), UUID.randomUUID());
        Group group = Group.create(new GroupName("Arena FC"), Sport.FOOTBALL, null);

        when(groupRepository.findInviteByToken(invite.getToken())).thenReturn(Optional.of(invite));
        when(groupRepository.findById(invite.getGroupId())).thenReturn(Optional.of(group));
        when(groupRepository.isMember(group.getId(), userId)).thenReturn(true);

        assertThatThrownBy(() -> groupService.execute(
                new JoinByInviteUseCase.Command(invite.getToken(), userId)))
                .isInstanceOf(UserAlreadyMemberException.class);
    }

    @Test
    void joinByInvite_throwsInviteExpired_whenInviteInactive() {
        UUID userId = UUID.randomUUID();
        GroupInvite expiredInvite = GroupInvite.reconstitute(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "expired-token", 0, 50, false,
                java.time.Instant.now().minus(1, java.time.temporal.ChronoUnit.HOURS),
                java.time.Instant.now().minus(8, java.time.temporal.ChronoUnit.DAYS));

        when(groupRepository.findInviteByToken("expired-token")).thenReturn(Optional.of(expiredInvite));

        assertThatThrownBy(() -> groupService.execute(
                new JoinByInviteUseCase.Command("expired-token", userId)))
                .isInstanceOf(InviteExpiredException.class);
    }

    @Test
    void listGroups_returnsMappedSummaries() {
        UUID userId = UUID.randomUUID();
        GroupSummary summary = new GroupSummary(UUID.randomUUID(), "Arena FC", Sport.FOOTBALL,
                null, GroupStatus.ACTIVE, 5, GroupRole.OWNER);

        when(groupRepository.listByUserId(userId)).thenReturn(List.of(summary));

        List<GroupSummaryResponse> result = groupService.execute(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Arena FC");
        assertThat(result.get(0).memberCount()).isEqualTo(5);
    }

    @Test
    void updateMember_throwsCannotDemoteOwner_whenTargetIsOwner() {
        UUID groupId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        Group group = Group.create(new GroupName("Arena FC"), Sport.FOOTBALL, null);
        GroupMember requester = GroupMember.create(groupId, requesterId, GroupRole.OWNER);
        GroupMember target = GroupMember.reconstitute(memberId, groupId, UUID.randomUUID(),
                GroupRole.OWNER, Skill.DEFAULT, SkillSource.DEFAULT, null, false, null,
                false, null, java.time.Instant.now(), java.time.Instant.now());

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(groupRepository.findMemberByGroupIdAndUserId(groupId, requesterId)).thenReturn(Optional.of(requester));
        when(groupRepository.findMemberById(memberId)).thenReturn(Optional.of(target));

        assertThatThrownBy(() -> groupService.execute(
                new UpdateMemberUseCase.Command(groupId, memberId, requesterId,
                        GroupRole.PLAYER, null, null, null)))
                .isInstanceOf(CannotDemoteOwnerException.class);
    }
}
