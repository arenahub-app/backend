package com.arenahub.infrastructure.persistence.match;

import com.arenahub.application.match.port.out.GroupMemberPort;
import com.arenahub.infrastructure.persistence.group.GroupMemberJpaEntity;
import com.arenahub.infrastructure.persistence.group.GroupMemberJpaRepository;
import com.arenahub.infrastructure.persistence.user.UserJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class GroupMemberPortAdapter implements GroupMemberPort {

    private final GroupMemberJpaRepository memberRepo;
    private final UserJpaRepository userRepo;
    private final PresenceBanHistoryJpaRepository banHistoryRepo;

    public GroupMemberPortAdapter(GroupMemberJpaRepository memberRepo,
                                   UserJpaRepository userRepo,
                                   PresenceBanHistoryJpaRepository banHistoryRepo) {
        this.memberRepo = memberRepo;
        this.userRepo = userRepo;
        this.banHistoryRepo = banHistoryRepo;
    }

    @Override
    public Optional<GroupMemberView> findMember(UUID groupId, UUID userId) {
        return memberRepo.findByGroupIdAndUserId(groupId, userId)
                .map(e -> toView(e));
    }

    @Override
    public Optional<GroupMemberView> findMemberById(UUID memberId) {
        return memberRepo.findById(memberId).map(e -> toView(e));
    }

    @Override
    public void banFromPresence(UUID memberId, String reason, UUID bannedBy, UUID groupId) {
        GroupMemberJpaEntity member = memberRepo.findById(memberId)
                .orElseThrow(() -> new IllegalStateException("Member not found: " + memberId));
        member.setPresenceBanned(true);
        member.setPresenceBanReason(reason);
        member.setUpdatedAt(java.time.Instant.now());
        memberRepo.save(member);
        banHistoryRepo.save(PresenceBanHistoryJpaEntity.ban(groupId, memberId, reason, bannedBy));
    }

    @Override
    public void unbanFromPresence(UUID memberId, UUID groupId, UUID performedBy) {
        GroupMemberJpaEntity member = memberRepo.findById(memberId)
                .orElseThrow(() -> new IllegalStateException("Member not found: " + memberId));
        member.setPresenceBanned(false);
        member.setPresenceBanReason(null);
        member.setUpdatedAt(java.time.Instant.now());
        memberRepo.save(member);
        banHistoryRepo.save(PresenceBanHistoryJpaEntity.unban(groupId, memberId, performedBy));
    }

    private GroupMemberView toView(GroupMemberJpaEntity e) {
        String userName = userRepo.findById(e.getUserId())
                .map(u -> u.getName())
                .orElse(null);
        return new GroupMemberView(
                e.getId(), e.getUserId(), e.getGroupId(), userName,
                e.getRole(), e.getSkill(), e.getSkillSource(), e.getPosition(),
                e.isSubscriber(), e.getJoinedAt(),
                e.isPresenceBanned(), e.getPresenceBanReason());
    }
}
