package com.arenahub.infrastructure.scheduler;

import com.arenahub.application.voting.port.out.GroupMemberSkillPort;
import com.arenahub.application.voting.port.out.SkillVotingRepository;
import com.arenahub.domain.voting.SkillVoting;
import com.arenahub.domain.voting.service.SkillCalculationService;
import com.arenahub.domain.voting.vo.VotingStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
public class SkillVotingScheduler {

    private final SkillVotingRepository votingRepository;
    private final SkillCalculationService calculationService;
    private final GroupMemberSkillPort memberPort;

    public SkillVotingScheduler(SkillVotingRepository votingRepository,
                                 SkillCalculationService calculationService,
                                 GroupMemberSkillPort memberPort) {
        this.votingRepository = votingRepository;
        this.calculationService = calculationService;
        this.memberPort = memberPort;
    }

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void closeExpiredVotings() {
        List<SkillVoting> toClose = votingRepository
                .findByStatusAndDeadlineBefore(VotingStatus.OPEN, Instant.now());

        for (SkillVoting voting : toClose) {
            voting.close(Instant.now());
            votingRepository.save(voting);
            calculationService.calculateAndUpdate(voting, votingRepository, memberPort);
        }
    }
}
