package com.arenahub.application.voting.port.out;

import com.arenahub.domain.voting.SkillVoting;
import com.arenahub.domain.voting.Vote;
import com.arenahub.domain.voting.VotingBan;
import com.arenahub.domain.voting.vo.VotingStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SkillVotingRepository {

    SkillVoting save(SkillVoting voting);

    Optional<SkillVoting> findById(UUID id);

    Optional<SkillVoting> findByIdAndGroupId(UUID id, UUID groupId);

    Optional<SkillVoting> findActiveByGroupId(UUID groupId);

    List<SkillVoting> findByGroupId(UUID groupId);

    List<SkillVoting> findByStatusAndDeadlineBefore(VotingStatus status, Instant cutoff);

    Vote saveVote(Vote vote);

    Optional<Vote> findVote(UUID votingId, UUID voterId, UUID targetMemberId);

    List<Vote> findVotesByVotingId(UUID votingId);

    List<Vote> findVotesByVotingIdAndVoterId(UUID votingId, UUID voterId);

    VotingBan saveBan(VotingBan ban);

    Optional<VotingBan> findBan(UUID votingId, UUID memberId);

    List<VotingBan> findBansByVotingId(UUID votingId);

    void deleteBan(UUID banId);
}
