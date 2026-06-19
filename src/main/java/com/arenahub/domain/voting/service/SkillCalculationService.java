package com.arenahub.domain.voting.service;

import com.arenahub.application.voting.port.out.GroupMemberSkillPort;
import com.arenahub.application.voting.port.out.SkillVotingRepository;
import com.arenahub.domain.group.vo.Skill;
import com.arenahub.domain.group.vo.SkillSource;
import com.arenahub.domain.voting.SkillVoting;
import com.arenahub.domain.voting.Vote;
import com.arenahub.domain.voting.VotingBan;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SkillCalculationService {

    public Map<UUID, BigDecimal> calculateAndUpdate(SkillVoting voting,
                                                     SkillVotingRepository repo,
                                                     GroupMemberSkillPort memberPort) {
        List<Vote> votes = repo.findVotesByVotingId(voting.getId());
        List<VotingBan> bans = repo.findBansByVotingId(voting.getId());

        Set<UUID> bannedVoterIds = bans.stream()
                .map(VotingBan::getMemberId)
                .collect(Collectors.toSet());

        Map<UUID, List<Integer>> votesByTarget = votes.stream()
                .filter(v -> !bannedVoterIds.contains(v.getVoterId()))
                .collect(Collectors.groupingBy(
                        Vote::getTargetMemberId,
                        Collectors.mapping(v -> v.getStars().value(), Collectors.toList())
                ));

        Map<UUID, BigDecimal> result = new HashMap<>();
        votesByTarget.forEach((memberId, starsList) -> {
            double avg = starsList.stream().mapToInt(i -> i).average().orElse(0.0);
            BigDecimal newSkill = BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP);
            memberPort.updateSkill(memberId, new Skill(newSkill), SkillSource.VOTING);
            result.put(memberId, newSkill);
        });
        return result;
    }
}
