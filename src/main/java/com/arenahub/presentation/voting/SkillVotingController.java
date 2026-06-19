package com.arenahub.presentation.voting;

import com.arenahub.application.voting.port.in.*;
import com.arenahub.presentation.voting.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/groups/{groupId}/votings")
public class SkillVotingController {

    private final OpenVotingUseCase openVotingUseCase;
    private final GetActiveVotingUseCase getActiveVotingUseCase;
    private final ListVotingsUseCase listVotingsUseCase;
    private final CloseVotingUseCase closeVotingUseCase;
    private final CastVoteUseCase castVoteUseCase;
    private final GetMyVotesUseCase getMyVotesUseCase;
    private final GetVotingResultsUseCase getVotingResultsUseCase;
    private final BanFromVotingUseCase banFromVotingUseCase;
    private final UnbanFromVotingUseCase unbanFromVotingUseCase;

    public SkillVotingController(OpenVotingUseCase openVotingUseCase,
                                  GetActiveVotingUseCase getActiveVotingUseCase,
                                  ListVotingsUseCase listVotingsUseCase,
                                  CloseVotingUseCase closeVotingUseCase,
                                  CastVoteUseCase castVoteUseCase,
                                  GetMyVotesUseCase getMyVotesUseCase,
                                  GetVotingResultsUseCase getVotingResultsUseCase,
                                  BanFromVotingUseCase banFromVotingUseCase,
                                  UnbanFromVotingUseCase unbanFromVotingUseCase) {
        this.openVotingUseCase = openVotingUseCase;
        this.getActiveVotingUseCase = getActiveVotingUseCase;
        this.listVotingsUseCase = listVotingsUseCase;
        this.closeVotingUseCase = closeVotingUseCase;
        this.castVoteUseCase = castVoteUseCase;
        this.getMyVotesUseCase = getMyVotesUseCase;
        this.getVotingResultsUseCase = getVotingResultsUseCase;
        this.banFromVotingUseCase = banFromVotingUseCase;
        this.unbanFromVotingUseCase = unbanFromVotingUseCase;
    }

    @PostMapping
    public ResponseEntity<VotingResponse> open(@PathVariable UUID groupId,
                                                @Valid @RequestBody OpenVotingRequest req,
                                                Authentication auth) {
        VotingResponse response = openVotingUseCase.execute(
                new OpenVotingUseCase.Command(groupId, currentUserId(auth), req.deadline()));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<VotingSummaryResponse>> list(@PathVariable UUID groupId,
                                                             Authentication auth) {
        return ResponseEntity.ok(listVotingsUseCase.execute(
                new ListVotingsUseCase.Command(groupId, currentUserId(auth))));
    }

    @GetMapping("/active")
    public ResponseEntity<VotingResponse> getActive(@PathVariable UUID groupId,
                                                     Authentication auth) {
        return ResponseEntity.ok(getActiveVotingUseCase.execute(
                new GetActiveVotingUseCase.Command(groupId, currentUserId(auth))));
    }

    @PostMapping("/{votingId}/close")
    public ResponseEntity<VotingResultsResponse> close(@PathVariable UUID groupId,
                                                        @PathVariable UUID votingId,
                                                        Authentication auth) {
        return ResponseEntity.ok(closeVotingUseCase.execute(
                new CloseVotingUseCase.Command(groupId, votingId, currentUserId(auth))));
    }

    @PutMapping("/{votingId}/votes/{targetMemberId}")
    public ResponseEntity<VoteResponse> castVote(@PathVariable UUID groupId,
                                                  @PathVariable UUID votingId,
                                                  @PathVariable UUID targetMemberId,
                                                  @Valid @RequestBody CastVoteRequest req,
                                                  Authentication auth) {
        return ResponseEntity.ok(castVoteUseCase.execute(
                new CastVoteUseCase.Command(groupId, votingId, targetMemberId,
                        currentUserId(auth), req.stars())));
    }

    @GetMapping("/{votingId}/votes/my")
    public ResponseEntity<MyVotesResponse> getMyVotes(@PathVariable UUID groupId,
                                                       @PathVariable UUID votingId,
                                                       Authentication auth) {
        return ResponseEntity.ok(getMyVotesUseCase.execute(
                new GetMyVotesUseCase.Command(groupId, votingId, currentUserId(auth))));
    }

    @GetMapping("/{votingId}/results")
    public ResponseEntity<VotingResultsResponse> getResults(@PathVariable UUID groupId,
                                                             @PathVariable UUID votingId,
                                                             Authentication auth) {
        return ResponseEntity.ok(getVotingResultsUseCase.execute(
                new GetVotingResultsUseCase.Command(groupId, votingId, currentUserId(auth))));
    }

    @PostMapping("/{votingId}/bans")
    public ResponseEntity<Void> ban(@PathVariable UUID groupId,
                                     @PathVariable UUID votingId,
                                     @Valid @RequestBody BanMemberFromVotingRequest req,
                                     Authentication auth) {
        banFromVotingUseCase.execute(new BanFromVotingUseCase.Command(
                groupId, votingId, req.memberId(), req.reason(), currentUserId(auth)));
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{votingId}/bans/{memberId}")
    public ResponseEntity<Void> unban(@PathVariable UUID groupId,
                                       @PathVariable UUID votingId,
                                       @PathVariable UUID memberId,
                                       Authentication auth) {
        unbanFromVotingUseCase.execute(new UnbanFromVotingUseCase.Command(
                groupId, votingId, memberId, currentUserId(auth)));
        return ResponseEntity.noContent().build();
    }

    private UUID currentUserId(Authentication auth) {
        return UUID.fromString(auth.getName());
    }
}
