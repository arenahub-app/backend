package com.arenahub.presentation.teamformation;

import com.arenahub.application.teamformation.port.in.*;
import com.arenahub.presentation.teamformation.dto.GenerateFormationRequest;
import com.arenahub.presentation.teamformation.dto.MovePlayerRequest;
import com.arenahub.presentation.teamformation.dto.TeamFormationResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/groups/{groupId}/matches/{matchId}/team-formations")
public class TeamFormationController {

    private final GenerateTeamFormationUseCase generateTeamFormationUseCase;
    private final GetCurrentFormationUseCase getCurrentFormationUseCase;
    private final MovePlayerUseCase movePlayerUseCase;
    private final DeleteFormationUseCase deleteFormationUseCase;

    public TeamFormationController(GenerateTeamFormationUseCase generateTeamFormationUseCase,
                                   GetCurrentFormationUseCase getCurrentFormationUseCase,
                                   MovePlayerUseCase movePlayerUseCase,
                                   DeleteFormationUseCase deleteFormationUseCase) {
        this.generateTeamFormationUseCase = generateTeamFormationUseCase;
        this.getCurrentFormationUseCase = getCurrentFormationUseCase;
        this.movePlayerUseCase = movePlayerUseCase;
        this.deleteFormationUseCase = deleteFormationUseCase;
    }

    @PostMapping
    public ResponseEntity<TeamFormationResponse> generate(@PathVariable UUID groupId,
                                                           @PathVariable UUID matchId,
                                                           @Valid @RequestBody GenerateFormationRequest req,
                                                           Authentication auth) {
        TeamFormationResponse response = generateTeamFormationUseCase.execute(
                new GenerateTeamFormationUseCase.Command(groupId, matchId, currentUserId(auth), req.numberOfTeams()));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/current")
    public ResponseEntity<TeamFormationResponse> getCurrent(@PathVariable UUID groupId,
                                                             @PathVariable UUID matchId,
                                                             Authentication auth) {
        TeamFormationResponse response = getCurrentFormationUseCase.execute(
                new GetCurrentFormationUseCase.Command(groupId, matchId, currentUserId(auth)));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{formationId}/move")
    public ResponseEntity<TeamFormationResponse> movePlayer(@PathVariable UUID groupId,
                                                             @PathVariable UUID matchId,
                                                             @PathVariable UUID formationId,
                                                             @Valid @RequestBody MovePlayerRequest req,
                                                             Authentication auth) {
        TeamFormationResponse response = movePlayerUseCase.execute(
                new MovePlayerUseCase.Command(groupId, matchId, formationId, currentUserId(auth),
                        req.memberId(), req.toTeamId()));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{formationId}")
    public ResponseEntity<Void> delete(@PathVariable UUID groupId,
                                        @PathVariable UUID matchId,
                                        @PathVariable UUID formationId,
                                        Authentication auth) {
        deleteFormationUseCase.execute(
                new DeleteFormationUseCase.Command(groupId, matchId, formationId, currentUserId(auth)));
        return ResponseEntity.noContent().build();
    }

    private UUID currentUserId(Authentication auth) {
        return UUID.fromString(auth.getName());
    }
}
