package com.arenahub.domain.group;

import com.arenahub.domain.group.vo.*;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class GroupMemberTest {

    @Test
    void create_setsDefaultSkillAndOwnerRole() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        GroupMember member = GroupMember.create(groupId, userId, GroupRole.OWNER);

        assertThat(member.getId()).isNotNull();
        assertThat(member.getGroupId()).isEqualTo(groupId);
        assertThat(member.getUserId()).isEqualTo(userId);
        assertThat(member.getRole()).isEqualTo(GroupRole.OWNER);
        assertThat(member.getSkill()).isEqualTo(Skill.DEFAULT);
        assertThat(member.getSkillSource()).isEqualTo(SkillSource.DEFAULT);
        assertThat(member.isSubscriber()).isFalse();
        assertThat(member.getJoinedAt()).isNotNull();
    }

    @Test
    void changeRole_updatesRole() {
        GroupMember member = GroupMember.create(UUID.randomUUID(), UUID.randomUUID(), GroupRole.PLAYER);

        member.changeRole(GroupRole.ADMIN);

        assertThat(member.getRole()).isEqualTo(GroupRole.ADMIN);
    }

    @Test
    void updateSkill_setsManualSource() {
        GroupMember member = GroupMember.create(UUID.randomUUID(), UUID.randomUUID(), GroupRole.PLAYER);

        member.updateSkill(Skill.of(4.5));

        assertThat(member.getSkill().value()).isEqualByComparingTo("4.5");
        assertThat(member.getSkillSource()).isEqualTo(SkillSource.MANUAL);
    }
}
