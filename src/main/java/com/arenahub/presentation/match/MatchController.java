package com.arenahub.presentation.match;

import com.arenahub.application.match.port.in.*;
import com.arenahub.presentation.match.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/groups/{groupId}/matches")
public class MatchController {

    private final CreateMatchUseCase createMatchUseCase;
    private final GetMatchUseCase getMatchUseCase;
    private final ListMatchesUseCase listMatchesUseCase;
    private final UpdateMatchUseCase updateMatchUseCase;
    private final CancelMatchUseCase cancelMatchUseCase;
    private final ClosePresenceListUseCase closePresenceListUseCase;

    public MatchController(CreateMatchUseCase createMatchUseCase,
                            GetMatchUseCase getMatchUseCase,
                            ListMatchesUseCase listMatchesUseCase,
                            UpdateMatchUseCase updateMatchUseCase,
                            CancelMatchUseCase cancelMatchUseCase,
                            ClosePresenceListUseCase closePresenceListUseCase) {
        this.createMatchUseCase = createMatchUseCase;
        this.getMatchUseCase = getMatchUseCase;
        this.listMatchesUseCase = listMatchesUseCase;
        this.updateMatchUseCase = updateMatchUseCase;
        this.cancelMatchUseCase = cancelMatchUseCase;
        this.closePresenceListUseCase = closePresenceListUseCase;
    }

    @PostMapping
    public ResponseEntity<MatchResponse> create(@PathVariable UUID groupId,
                                                 @Valid @RequestBody CreateMatchRequest req,
                                                 Authentication auth) {
        MatchResponse response = createMatchUseCase.execute(
                new CreateMatchUseCase.Command(groupId, currentUserId(auth),
                        req.scheduledAt(), req.locationName(), req.locationAddress(), req.maxPlayers()));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<MatchSummaryResponse>> list(@PathVariable UUID groupId,
                                                            @RequestParam(defaultValue = "upcoming") String filter,
                                                            Authentication auth) {
        return ResponseEntity.ok(listMatchesUseCase.execute(
                new ListMatchesUseCase.Command(groupId, currentUserId(auth), filter)));
    }

    @GetMapping("/{matchId}")
    public ResponseEntity<MatchResponse> get(@PathVariable UUID groupId,
                                              @PathVariable UUID matchId,
                                              Authentication auth) {
        return ResponseEntity.ok(getMatchUseCase.execute(
                new GetMatchUseCase.Command(groupId, matchId, currentUserId(auth))));
    }

    @PatchMapping("/{matchId}")
    public ResponseEntity<MatchResponse> update(@PathVariable UUID groupId,
                                                 @PathVariable UUID matchId,
                                                 @Valid @RequestBody UpdateMatchRequest req,
                                                 Authentication auth) {
        return ResponseEntity.ok(updateMatchUseCase.execute(
                new UpdateMatchUseCase.Command(groupId, matchId, currentUserId(auth),
                        req.scheduledAt(), req.locationName(), req.locationAddress(), req.maxPlayers())));
    }

    @PostMapping("/{matchId}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable UUID groupId,
                                        @PathVariable UUID matchId,
                                        Authentication auth) {
        cancelMatchUseCase.execute(new CancelMatchUseCase.Command(groupId, matchId, currentUserId(auth)));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{matchId}/close-list")
    public ResponseEntity<Void> closeList(@PathVariable UUID groupId,
                                           @PathVariable UUID matchId,
                                           Authentication auth) {
        closePresenceListUseCase.execute(
                new ClosePresenceListUseCase.Command(groupId, matchId, currentUserId(auth)));
        return ResponseEntity.noContent().build();
    }

    private UUID currentUserId(Authentication auth) {
        return UUID.fromString(auth.getName());
    }
}
