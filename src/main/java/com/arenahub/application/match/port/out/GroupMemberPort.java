package com.arenahub.application.match.port.out;

import com.arenahub.domain.group.vo.GroupRole;
import com.arenahub.domain.group.vo.PlayerPosition;
import com.arenahub.domain.group.vo.SkillSource;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface GroupMemberPort {

    Optional<GroupMemberView> findMember(UUID groupId, UUID userId);

    Optional<GroupMemberView> findMemberById(UUID memberId);

    void banFromPresence(UUID memberId, String reason, UUID bannedBy, UUID groupId);

    void unbanFromPresence(UUID memberId, UUID groupId, UUID performedBy);

    record GroupMemberView(
            UUID id,
            UUID userId,
            UUID groupId,
            String userName,
            GroupRole role,
            BigDecimal skill,
            SkillSource skillSource,
            PlayerPosition position,
            boolean isSubscriber,
            Instant joinedAt,
            boolean presenceBanned,
            String presenceBanReason
    ) {}
}
