package com.arenahub.infrastructure.persistence.voting;

import com.arenahub.application.voting.port.out.SkillVotingRepository;
import com.arenahub.domain.voting.SkillVoting;
import com.arenahub.domain.voting.Vote;
import com.arenahub.domain.voting.VotingBan;
import com.arenahub.domain.voting.vo.VotingStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class SkillVotingRepositoryAdapter implements SkillVotingRepository {

    private final SkillVotingJpaRepository votingRepo;
    private final VoteJpaRepository voteRepo;
    private final VotingBanJpaRepository banRepo;

    public SkillVotingRepositoryAdapter(SkillVotingJpaRepository votingRepo,
                                        VoteJpaRepository voteRepo,
                                        VotingBanJpaRepository banRepo) {
        this.votingRepo = votingRepo;
        this.voteRepo = voteRepo;
        this.banRepo = banRepo;
    }

    @Override
    public SkillVoting save(SkillVoting voting) {
        return votingRepo.save(SkillVotingJpaEntity.fromDomain(voting)).toDomain();
    }

    @Override
    public Optional<SkillVoting> findById(UUID id) {
        return votingRepo.findById(id).map(SkillVotingJpaEntity::toDomain);
    }

    @Override
    public Optional<SkillVoting> findByIdAndGroupId(UUID id, UUID groupId) {
        return votingRepo.findByIdAndGroupId(id, groupId).map(SkillVotingJpaEntity::toDomain);
    }

    @Override
    public Optional<SkillVoting> findActiveByGroupId(UUID groupId) {
        return votingRepo.findByGroupIdAndStatus(groupId, VotingStatus.OPEN)
                .map(SkillVotingJpaEntity::toDomain);
    }

    @Override
    public List<SkillVoting> findByGroupId(UUID groupId) {
        return votingRepo.findByGroupIdOrderByOpenedAtDesc(groupId).stream()
                .map(SkillVotingJpaEntity::toDomain).toList();
    }

    @Override
    public List<SkillVoting> findByStatusAndDeadlineBefore(VotingStatus status, Instant cutoff) {
        return votingRepo.findByStatusAndDeadlineBefore(status, cutoff).stream()
                .map(SkillVotingJpaEntity::toDomain).toList();
    }

    @Override
    public Vote saveVote(Vote vote) {
        return voteRepo.save(VoteJpaEntity.fromDomain(vote)).toDomain();
    }

    @Override
    public Optional<Vote> findVote(UUID votingId, UUID voterId, UUID targetMemberId) {
        return voteRepo.findByVotingIdAndVoterIdAndTargetMemberId(votingId, voterId, targetMemberId)
                .map(VoteJpaEntity::toDomain);
    }

    @Override
    public List<Vote> findVotesByVotingId(UUID votingId) {
        return voteRepo.findByVotingId(votingId).stream()
                .map(VoteJpaEntity::toDomain).toList();
    }

    @Override
    public List<Vote> findVotesByVotingIdAndVoterId(UUID votingId, UUID voterId) {
        return voteRepo.findByVotingIdAndVoterId(votingId, voterId).stream()
                .map(VoteJpaEntity::toDomain).toList();
    }

    @Override
    public VotingBan saveBan(VotingBan ban) {
        return banRepo.save(VotingBanJpaEntity.fromDomain(ban)).toDomain();
    }

    @Override
    public Optional<VotingBan> findBan(UUID votingId, UUID memberId) {
        return banRepo.findByVotingIdAndMemberId(votingId, memberId)
                .map(VotingBanJpaEntity::toDomain);
    }

    @Override
    public List<VotingBan> findBansByVotingId(UUID votingId) {
        return banRepo.findByVotingId(votingId).stream()
                .map(VotingBanJpaEntity::toDomain).toList();
    }

    @Override
    public void deleteBan(UUID banId) {
        banRepo.deleteById(banId);
    }
}
