package com.arenahub.presentation.group;

import com.arenahub.application.group.port.in.*;
import com.arenahub.presentation.group.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/groups")
public class GroupController {

    private final CreateGroupUseCase createGroupUseCase;
    private final GetGroupUseCase getGroupUseCase;
    private final ListGroupsUseCase listGroupsUseCase;
    private final UpdateGroupUseCase updateGroupUseCase;
    private final DeactivateGroupUseCase deactivateGroupUseCase;
    private final ListMembersUseCase listMembersUseCase;
    private final UpdateMemberUseCase updateMemberUseCase;
    private final RemoveMemberUseCase removeMemberUseCase;
    private final GenerateInviteUseCase generateInviteUseCase;
    private final ListInvitesUseCase listInvitesUseCase;
    private final DeactivateInviteUseCase deactivateInviteUseCase;

    public GroupController(CreateGroupUseCase createGroupUseCase,
                           GetGroupUseCase getGroupUseCase,
                           ListGroupsUseCase listGroupsUseCase,
                           UpdateGroupUseCase updateGroupUseCase,
                           DeactivateGroupUseCase deactivateGroupUseCase,
                           ListMembersUseCase listMembersUseCase,
                           UpdateMemberUseCase updateMemberUseCase,
                           RemoveMemberUseCase removeMemberUseCase,
                           GenerateInviteUseCase generateInviteUseCase,
                           ListInvitesUseCase listInvitesUseCase,
                           DeactivateInviteUseCase deactivateInviteUseCase) {
        this.createGroupUseCase = createGroupUseCase;
        this.getGroupUseCase = getGroupUseCase;
        this.listGroupsUseCase = listGroupsUseCase;
        this.updateGroupUseCase = updateGroupUseCase;
        this.deactivateGroupUseCase = deactivateGroupUseCase;
        this.listMembersUseCase = listMembersUseCase;
        this.updateMemberUseCase = updateMemberUseCase;
        this.removeMemberUseCase = removeMemberUseCase;
        this.generateInviteUseCase = generateInviteUseCase;
        this.listInvitesUseCase = listInvitesUseCase;
        this.deactivateInviteUseCase = deactivateInviteUseCase;
    }

    @PostMapping
    public ResponseEntity<GroupResponse> create(@Valid @RequestBody CreateGroupRequest req,
                                                 Authentication auth) {
        UUID userId = currentUserId(auth);
        GroupResponse response = createGroupUseCase.execute(
                new CreateGroupUseCase.Command(userId, req.name(), req.sport(), req.description()));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<GroupSummaryResponse>> list(Authentication auth) {
        return ResponseEntity.ok(listGroupsUseCase.execute(currentUserId(auth)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GroupResponse> get(@PathVariable UUID id, Authentication auth) {
        return ResponseEntity.ok(getGroupUseCase.execute(
                new GetGroupUseCase.Command(id, currentUserId(auth))));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<GroupResponse> update(@PathVariable UUID id,
                                                 @Valid @RequestBody UpdateGroupRequest req,
                                                 Authentication auth) {
        return ResponseEntity.ok(updateGroupUseCase.execute(
                new UpdateGroupUseCase.Command(id, currentUserId(auth),
                        req.name(), req.sport(), req.description(), req.pixKey())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id, Authentication auth) {
        deactivateGroupUseCase.execute(new DeactivateGroupUseCase.Command(id, currentUserId(auth)));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<List<MemberResponse>> listMembers(@PathVariable UUID id,
                                                             Authentication auth) {
        return ResponseEntity.ok(listMembersUseCase.execute(
                new ListMembersUseCase.Command(id, currentUserId(auth))));
    }

    @PatchMapping("/{id}/members/{memberId}")
    public ResponseEntity<MemberResponse> updateMember(@PathVariable UUID id,
                                                        @PathVariable UUID memberId,
                                                        @Valid @RequestBody UpdateMemberRequest req,
                                                        Authentication auth) {
        return ResponseEntity.ok(updateMemberUseCase.execute(
                new UpdateMemberUseCase.Command(id, memberId, currentUserId(auth),
                        req.role(), req.skill(), req.position(), req.subscriptionActive())));
    }

    @DeleteMapping("/{id}/members/{memberId}")
    public ResponseEntity<Void> removeMember(@PathVariable UUID id,
                                              @PathVariable UUID memberId,
                                              Authentication auth) {
        removeMemberUseCase.execute(
                new RemoveMemberUseCase.Command(id, memberId, currentUserId(auth)));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/invites")
    public ResponseEntity<InviteResponse> generateInvite(@PathVariable UUID id,
                                                          Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                generateInviteUseCase.execute(
                        new GenerateInviteUseCase.Command(id, currentUserId(auth))));
    }

    @GetMapping("/{id}/invites")
    public ResponseEntity<List<InviteResponse>> listInvites(@PathVariable UUID id,
                                                             Authentication auth) {
        return ResponseEntity.ok(listInvitesUseCase.execute(
                new ListInvitesUseCase.Command(id, currentUserId(auth))));
    }

    @DeleteMapping("/{id}/invites/{inviteId}")
    public ResponseEntity<Void> deactivateInvite(@PathVariable UUID id,
                                                  @PathVariable UUID inviteId,
                                                  Authentication auth) {
        deactivateInviteUseCase.execute(
                new DeactivateInviteUseCase.Command(id, inviteId, currentUserId(auth)));
        return ResponseEntity.noContent().build();
    }

    private UUID currentUserId(Authentication auth) {
        return UUID.fromString(auth.getName());
    }
}
