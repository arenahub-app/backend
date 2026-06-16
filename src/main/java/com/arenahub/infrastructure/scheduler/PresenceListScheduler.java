package com.arenahub.infrastructure.scheduler;

import com.arenahub.application.match.port.out.MatchRepository;
import com.arenahub.domain.match.Match;
import com.arenahub.domain.match.vo.PresenceListStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
public class PresenceListScheduler {

    private final MatchRepository matchRepository;

    public PresenceListScheduler(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void closeExpiredLists() {
        List<Match> toClose = matchRepository.findByPresenceListStatusAndListClosesAtBefore(
                PresenceListStatus.OPEN, Instant.now());

        for (Match match : toClose) {
            match.closePresenceList();
            matchRepository.save(match);
        }
    }
}
