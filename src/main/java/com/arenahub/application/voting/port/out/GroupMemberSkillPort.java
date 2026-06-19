package com.arenahub.application.voting.port.out;

import com.arenahub.domain.group.vo.GroupRole;
import com.arenahub.domain.group.vo.Skill;
import com.arenahub.domain.group.vo.SkillSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroupMemberSkillPort {

    List<GroupMemberView> findVotableMembers(UUID groupId);

    Optional<GroupMemberView> findMemberById(UUID memberId);

    Optional<GroupMemberView> findMemberByGroupAndUser(UUID groupId, UUID userId);

    void updateSkill(UUID memberId, Skill newSkill, SkillSource source);

    record GroupMemberView(
            UUID id,
            UUID userId,
            String userName,
            String photoUrl,
            GroupRole role,
            BigDecimal currentSkill
    ) {}
}
