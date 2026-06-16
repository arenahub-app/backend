package com.arenahub.presentation.group;

import com.arenahub.application.group.port.in.GetInvitePreviewUseCase;
import com.arenahub.application.group.port.in.JoinByInviteUseCase;
import com.arenahub.presentation.group.dto.InvitePreviewResponse;
import com.arenahub.presentation.group.dto.JoinGroupResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/invites")
public class InviteController {

    private final GetInvitePreviewUseCase getInvitePreviewUseCase;
    private final JoinByInviteUseCase joinByInviteUseCase;

    public InviteController(GetInvitePreviewUseCase getInvitePreviewUseCase,
                             JoinByInviteUseCase joinByInviteUseCase) {
        this.getInvitePreviewUseCase = getInvitePreviewUseCase;
        this.joinByInviteUseCase = joinByInviteUseCase;
    }

    @GetMapping("/{token}")
    public ResponseEntity<InvitePreviewResponse> preview(@PathVariable String token) {
        return ResponseEntity.ok(getInvitePreviewUseCase.execute(token));
    }

    @PostMapping("/{token}/join")
    public ResponseEntity<JoinGroupResponse> join(@PathVariable String token,
                                                   Authentication auth) {
        return ResponseEntity.ok(joinByInviteUseCase.execute(
                new JoinByInviteUseCase.Command(token, UUID.fromString(auth.getName()))));
    }
}
