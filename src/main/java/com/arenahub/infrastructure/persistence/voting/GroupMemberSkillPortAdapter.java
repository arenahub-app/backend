package com.arenahub.infrastructure.persistence.voting;

import com.arenahub.application.voting.port.out.GroupMemberSkillPort;
import com.arenahub.domain.group.vo.GroupRole;
import com.arenahub.domain.group.vo.Skill;
import com.arenahub.domain.group.vo.SkillSource;
import com.arenahub.infrastructure.persistence.group.GroupMemberJpaEntity;
import com.arenahub.infrastructure.persistence.group.GroupMemberJpaRepository;
import com.arenahub.infrastructure.persistence.user.UserJpaRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class GroupMemberSkillPortAdapter implements GroupMemberSkillPort {

    private final GroupMemberJpaRepository memberRepo;
    private final UserJpaRepository userRepo;

    public GroupMemberSkillPortAdapter(GroupMemberJpaRepository memberRepo,
                                       UserJpaRepository userRepo) {
        this.memberRepo = memberRepo;
        this.userRepo = userRepo;
    }

    @Override
    public List<GroupMemberView> findVotableMembers(UUID groupId) {
        return memberRepo.findByGroupId(groupId).stream()
                .filter(m -> m.getRole() != GroupRole.REFEREE)
                .map(this::toView)
                .toList();
    }

    @Override
    public Optional<GroupMemberView> findMemberById(UUID memberId) {
        return memberRepo.findById(memberId).map(this::toView);
    }

    @Override
    public Optional<GroupMemberView> findMemberByGroupAndUser(UUID groupId, UUID userId) {
        return memberRepo.findByGroupIdAndUserId(groupId, userId).map(this::toView);
    }

    @Override
    public void updateSkill(UUID memberId, Skill newSkill, SkillSource source) {
        GroupMemberJpaEntity member = memberRepo.findById(memberId)
                .orElseThrow(() -> new IllegalStateException("Member not found: " + memberId));
        member.setSkill(newSkill.value());
        member.setSkillSource(source);
        member.setUpdatedAt(Instant.now());
        memberRepo.save(member);
    }

    private GroupMemberView toView(GroupMemberJpaEntity e) {
        var user = userRepo.findById(e.getUserId()).orElse(null);
        String userName = user != null ? user.getName() : null;
        String photoUrl = user != null ? user.getPhotoUrl() : null;
        return new GroupMemberView(e.getId(), e.getUserId(), userName, photoUrl,
                e.getRole(), e.getSkill());
    }
}
