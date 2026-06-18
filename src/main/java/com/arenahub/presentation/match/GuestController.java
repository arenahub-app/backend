package com.arenahub.presentation.match;

import com.arenahub.application.match.port.in.AddGuestUseCase;
import com.arenahub.application.match.port.in.RemoveGuestUseCase;
import com.arenahub.presentation.match.dto.AddGuestRequest;
import com.arenahub.presentation.match.dto.GuestResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/groups/{groupId}/matches/{matchId}/guests")
public class GuestController {

    private final AddGuestUseCase addGuestUseCase;
    private final RemoveGuestUseCase removeGuestUseCase;

    public GuestController(AddGuestUseCase addGuestUseCase, RemoveGuestUseCase removeGuestUseCase) {
        this.addGuestUseCase = addGuestUseCase;
        this.removeGuestUseCase = removeGuestUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GuestResponse addGuest(@PathVariable UUID groupId,
                                   @PathVariable UUID matchId,
                                   @Valid @RequestBody AddGuestRequest req,
                                   Authentication auth) {
        UUID userId = UUID.fromString(auth.getName());
        return addGuestUseCase.execute(new AddGuestUseCase.Command(
                groupId, matchId, userId, req.name(), req.skill(), req.position()));
    }

    @DeleteMapping("/{guestId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeGuest(@PathVariable UUID groupId,
                             @PathVariable UUID matchId,
                             @PathVariable UUID guestId,
                             Authentication auth) {
        UUID userId = UUID.fromString(auth.getName());
        removeGuestUseCase.execute(new RemoveGuestUseCase.Command(groupId, matchId, guestId, userId));
    }
}
