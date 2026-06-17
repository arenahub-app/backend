package com.arenahub.application.teamformation.port.out;

import com.arenahub.domain.teamformation.TeamFormation;

import java.util.Optional;
import java.util.UUID;

public interface TeamFormationRepository {

    TeamFormation save(TeamFormation formation);

    Optional<TeamFormation> findByMatchIdAndGroupId(UUID matchId, UUID groupId);

    Optional<TeamFormation> findByIdAndGroupId(UUID id, UUID groupId);

    void deleteById(UUID id);
}
