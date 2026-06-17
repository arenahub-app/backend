package com.arenahub.presentation.match;

import com.arenahub.application.match.port.in.*;
import com.arenahub.presentation.group.dto.MemberResponse;
import com.arenahub.presentation.match.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/groups/{groupId}")
public class PresenceController {

    private final GetPresenceListUseCase getPresenceListUseCase;
    private final ConfirmPresenceUseCase confirmPresenceUseCase;
    private final CancelPresenceUseCase cancelPresenceUseCase;
    private final AdminForcePresenceUseCase adminForcePresenceUseCase;
    private final AdminRemovePresenceUseCase adminRemovePresenceUseCase;
    private final BanFromPresenceUseCase banFromPresenceUseCase;
    private final UnbanFromPresenceUseCase unbanFromPresenceUseCase;

    public PresenceController(GetPresenceListUseCase getPresenceListUseCase,
                               ConfirmPresenceUseCase confirmPresenceUseCase,
                               CancelPresenceUseCase cancelPresenceUseCase,
                               AdminForcePresenceUseCase adminForcePresenceUseCase,
                               AdminRemovePresenceUseCase adminRemovePresenceUseCase,
                               BanFromPresenceUseCase banFromPresenceUseCase,
                               UnbanFromPresenceUseCase unbanFromPresenceUseCase) {
        this.getPresenceListUseCase = getPresenceListUseCase;
        this.confirmPresenceUseCase = confirmPresenceUseCase;
        this.cancelPresenceUseCase = cancelPresenceUseCase;
        this.adminForcePresenceUseCase = adminForcePresenceUseCase;
        this.adminRemovePresenceUseCase = adminRemovePresenceUseCase;
        this.banFromPresenceUseCase = banFromPresenceUseCase;
        this.unbanFromPresenceUseCase = unbanFromPresenceUseCase;
    }

    @GetMapping("/matches/{matchId}/presence")
    public ResponseEntity<PresenceListResponse> getPresenceList(@PathVariable UUID groupId,
                                                                 @PathVariable UUID matchId,
                                                                 Authentication auth) {
        return ResponseEntity.ok(getPresenceListUseCase.execute(
                new GetPresenceListUseCase.Command(groupId, matchId, currentUserId(auth))));
    }

    @PostMapping("/matches/{matchId}/presence")
    public ResponseEntity<PresenceActionResponse> confirmOrDecline(@PathVariable UUID groupId,
                                                                    @PathVariable UUID matchId,
                                                                    @Valid @RequestBody ConfirmPresenceRequest req,
                                                                    Authentication auth) {
        if ("CONFIRM".equals(req.action())) {
            return ResponseEntity.ok(confirmPresenceUseCase.execute(
                    new ConfirmPresenceUseCase.Command(groupId, matchId, currentUserId(auth))));
        } else {
            cancelPresenceUseCase.execute(
                    new CancelPresenceUseCase.Command(groupId, matchId, currentUserId(auth)));
            return ResponseEntity.ok(new PresenceActionResponse("DECLINED", null, null, null));
        }
    }

    @DeleteMapping("/matches/{matchId}/presence")
    public ResponseEntity<Void> cancelPresence(@PathVariable UUID groupId,
                                                @PathVariable UUID matchId,
                                                Authentication auth) {
        cancelPresenceUseCase.execute(
                new CancelPresenceUseCase.Command(groupId, matchId, currentUserId(auth)));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/matches/{matchId}/presence/{memberId}")
    public ResponseEntity<PresenceEntryResponse> adminForcePresence(@PathVariable UUID groupId,
                                                                     @PathVariable UUID matchId,
                                                                     @PathVariable UUID memberId,
                                                                     Authentication auth) {
        return ResponseEntity.ok(adminForcePresenceUseCase.execute(
                new AdminForcePresenceUseCase.Command(groupId, matchId, memberId, currentUserId(auth))));
    }

    @DeleteMapping("/matches/{matchId}/presence/{memberId}")
    public ResponseEntity<Void> adminRemovePresence(@PathVariable UUID groupId,
                                                     @PathVariable UUID matchId,
                                                     @PathVariable UUID memberId,
                                                     Authentication auth) {
        adminRemovePresenceUseCase.execute(
                new AdminRemovePresenceUseCase.Command(groupId, matchId, memberId, currentUserId(auth)));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/members/{memberId}/presence-ban")
    public ResponseEntity<MemberResponse> banMember(@PathVariable UUID groupId,
                                                     @PathVariable UUID memberId,
                                                     @Valid @RequestBody BanMemberRequest req,
                                                     Authentication auth) {
        return ResponseEntity.ok(banFromPresenceUseCase.execute(
                new BanFromPresenceUseCase.Command(groupId, memberId, currentUserId(auth), req.reason())));
    }

    @DeleteMapping("/members/{memberId}/presence-ban")
    public ResponseEntity<Void> unbanMember(@PathVariable UUID groupId,
                                             @PathVariable UUID memberId,
                                             Authentication auth) {
        unbanFromPresenceUseCase.execute(
                new UnbanFromPresenceUseCase.Command(groupId, memberId, currentUserId(auth)));
        return ResponseEntity.noContent().build();
    }

    private UUID currentUserId(Authentication auth) {
        return UUID.fromString(auth.getName());
    }
}
