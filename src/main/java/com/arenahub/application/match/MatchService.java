package com.arenahub.application.match;

import com.arenahub.application.exception.*;
import com.arenahub.application.match.port.in.*;
import com.arenahub.application.match.port.out.ChargePort;
import com.arenahub.application.match.port.out.GroupMemberPort;
import com.arenahub.application.match.port.out.GroupMemberPort.GroupMemberView;
import com.arenahub.application.match.port.out.GuestRepository;
import com.arenahub.application.match.port.out.MatchRepository;
import com.arenahub.domain.group.vo.GroupRole;
import com.arenahub.domain.group.vo.GroupStatus;
import com.arenahub.domain.match.Match;
import com.arenahub.domain.match.MatchGuest;
import com.arenahub.domain.match.PresenceEntry;
import com.arenahub.domain.match.WaitingEntry;
import com.arenahub.domain.match.vo.GuestStatus;
import com.arenahub.domain.match.vo.Location;
import com.arenahub.domain.match.vo.PresenceStatus;
import com.arenahub.infrastructure.persistence.group.GroupJpaRepository;
import com.arenahub.presentation.group.dto.MemberResponse;
import com.arenahub.presentation.match.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class MatchService implements
        CreateMatchUseCase, GetMatchUseCase, ListMatchesUseCase, UpdateMatchUseCase,
        CancelMatchUseCase, ClosePresenceListUseCase, GetPresenceListUseCase,
        ConfirmPresenceUseCase, CancelPresenceUseCase,
        AdminForcePresenceUseCase, AdminRemovePresenceUseCase,
        BanFromPresenceUseCase, UnbanFromPresenceUseCase,
        AddGuestUseCase, RemoveGuestUseCase {

    private final MatchRepository matchRepository;
    private final GroupMemberPort groupMemberPort;
    private final GroupJpaRepository groupRepo;
    private final ChargePort chargePort;
    private final GuestRepository guestRepository;

    public MatchService(MatchRepository matchRepository, GroupMemberPort groupMemberPort,
                        GroupJpaRepository groupRepo, ChargePort chargePort,
                        GuestRepository guestRepository) {
        this.matchRepository = matchRepository;
        this.groupMemberPort = groupMemberPort;
        this.groupRepo = groupRepo;
        this.chargePort = chargePort;
        this.guestRepository = guestRepository;
    }

    // ── Create Match ──────────────────────────────────────────────────────────

    @Override
    public MatchResponse execute(CreateMatchUseCase.Command cmd) {
        var group = groupRepo.findByIdAndDeletedAtIsNull(cmd.groupId())
                .orElseThrow(GroupNotFoundException::new);
        if (group.getStatus() != GroupStatus.ACTIVE) {
            throw new GroupInactiveException();
        }

        GroupMemberView actor = requireMember(cmd.groupId(), cmd.actorUserId());
        requireRole(actor, GroupRole.ADMIN);

        Match match;
        try {
            match = Match.create(cmd.groupId(), cmd.scheduledAt(),
                    new Location(cmd.locationName(), cmd.locationAddress()),
                    cmd.maxPlayers(), actor.userId());
        } catch (IllegalArgumentException ex) {
            throw new MatchInPastException();
        }
        match = matchRepository.save(match);
        return toMatchResponse(match, null);
    }

    // ── Get Match ─────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public MatchResponse execute(GetMatchUseCase.Command cmd) {
        requireMember(cmd.groupId(), cmd.actorUserId());
        Match match = requireMatch(cmd.groupId(), cmd.matchId());

        GroupMemberView member = groupMemberPort.findMember(cmd.groupId(), cmd.actorUserId())
                .orElseThrow(NotAMemberException::new);
        String myPresenceStatus = resolveMyPresenceStatus(match.getId(), member.id());
        return toMatchResponse(match, myPresenceStatus);
    }

    // ── List Matches ──────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<MatchSummaryResponse> execute(ListMatchesUseCase.Command cmd) {
        requireMember(cmd.groupId(), cmd.actorUserId());

        List<Match> matches = switch (cmd.filter() == null ? "upcoming" : cmd.filter()) {
            case "past" -> matchRepository.findPastByGroupId(cmd.groupId());
            case "all" -> matchRepository.findAllByGroupId(cmd.groupId());
            default -> matchRepository.findUpcomingByGroupId(cmd.groupId());
        };

        return matches.stream().map(this::toSummaryResponse).toList();
    }

    // ── Update Match ──────────────────────────────────────────────────────────

    @Override
    public MatchResponse execute(UpdateMatchUseCase.Command cmd) {
        GroupMemberView actor = requireMember(cmd.groupId(), cmd.actorUserId());
        requireRole(actor, GroupRole.ADMIN);
        Match match = requireMatch(cmd.groupId(), cmd.matchId());

        Location newLocation = cmd.locationName() != null
                ? new Location(cmd.locationName(), cmd.locationAddress())
                : null;
        try {
            match.update(cmd.scheduledAt(), newLocation, cmd.maxPlayers());
        } catch (IllegalStateException ex) {
            throw new MatchNotEditableException();
        } catch (IllegalArgumentException ex) {
            throw new MatchInPastException();
        }
        return toMatchResponse(matchRepository.save(match), null);
    }

    // ── Cancel Match ──────────────────────────────────────────────────────────

    @Override
    public void execute(CancelMatchUseCase.Command cmd) {
        GroupMemberView actor = requireMember(cmd.groupId(), cmd.actorUserId());
        requireRole(actor, GroupRole.ADMIN);
        Match match = requireMatch(cmd.groupId(), cmd.matchId());
        try {
            match.cancel();
        } catch (IllegalStateException ex) {
            throw new MatchAlreadyCancelledException();
        }
        matchRepository.save(match);
    }

    // ── Close Presence List ───────────────────────────────────────────────────

    @Override
    public void execute(ClosePresenceListUseCase.Command cmd) {
        GroupMemberView actor = requireMember(cmd.groupId(), cmd.actorUserId());
        requireRole(actor, GroupRole.ADMIN);
        Match match = requireMatch(cmd.groupId(), cmd.matchId());
        try {
            match.closePresenceList();
        } catch (IllegalStateException ex) {
            throw new ListAlreadyClosedException();
        }
        matchRepository.save(match);
    }

    // ── Get Presence List ─────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PresenceListResponse execute(GetPresenceListUseCase.Command cmd) {
        requireMember(cmd.groupId(), cmd.actorUserId());
        Match match = requireMatch(cmd.groupId(), cmd.matchId());

        List<PresenceEntry> confirmedMembers = matchRepository.findPresenceEntriesByMatchIdAndStatuses(
                match.getId(), List.of(PresenceStatus.CONFIRMED, PresenceStatus.PAYMENT_PENDING));
        List<PresenceEntry> declined = matchRepository.findPresenceEntriesByMatchIdAndStatus(
                match.getId(), PresenceStatus.DECLINED);
        List<WaitingEntry> waiting = matchRepository.findWaitingEntriesByMatchId(match.getId());
        List<MatchGuest> guests = guestRepository.findByMatchId(match.getId());

        List<PresenceEntryResponse> confirmedList = new ArrayList<>();
        confirmedMembers.forEach(e -> confirmedList.add(toPresenceEntryResponse(e)));
        guests.forEach(g -> confirmedList.add(toGuestPresenceEntryResponse(g)));

        return new PresenceListResponse(
                confirmedList,
                declined.stream().map(e -> toPresenceEntryResponse(e)).toList(),
                waiting.stream().map(e -> toWaitingEntryResponse(e)).toList()
        );
    }

    // ── Confirm Presence ──────────────────────────────────────────────────────

    @Override
    public PresenceActionResponse execute(ConfirmPresenceUseCase.Command cmd) {
        GroupMemberView member = requireMember(cmd.groupId(), cmd.actorUserId());
        if (member.role() == GroupRole.REFEREE) throw new RefereeCannotConfirmException();

        Match match = requireMatch(cmd.groupId(), cmd.matchId());
        if (!match.isListOpen()) throw new ListClosedException();

        Optional<PresenceEntry> existing = matchRepository.findPresenceEntry(match.getId(), member.id());
        if (existing.isPresent() && existing.get().getStatus() != PresenceStatus.DECLINED) {
            throw new AlreadyConfirmedException();
        }
        existing.ifPresent(e -> matchRepository.deletePresenceEntry(e.getId()));

        if (member.presenceBanned()) {
            PresenceEntry entry = matchRepository.savePresenceEntry(
                    PresenceEntry.pendingBan(match.getId(), cmd.groupId(), member.id(),
                            member.presenceBanReason()));
            return PresenceActionResponse.presence(toPresenceEntryResponse(entry));
        }

        long memberOccupied = matchRepository.countOccupiedByMatchId(match.getId());
        long guestOccupied = guestRepository.countOccupiedByMatchId(match.getId());
        long occupied = memberOccupied + guestOccupied;
        if (occupied < match.getMaxPlayers()) {
            PendingChargeResponse pendingCharge = createChargeIfNeeded(
                    cmd.groupId(), member.id(), match.getId());
            PresenceEntry entry = pendingCharge != null
                    ? matchRepository.savePresenceEntry(
                            PresenceEntry.awaitingPayment(match.getId(), cmd.groupId(), member.id()))
                    : matchRepository.savePresenceEntry(
                            PresenceEntry.confirm(match.getId(), cmd.groupId(), member.id()));
            return PresenceActionResponse.presence(toPresenceEntryResponse(entry), pendingCharge);
        } else {
            long nextPos = matchRepository.countWaitingByMatchId(match.getId()) + 1;
            WaitingEntry entry = matchRepository.saveWaitingEntry(
                    WaitingEntry.create(match.getId(), cmd.groupId(), member.id(), (int) nextPos));
            return PresenceActionResponse.waiting(toWaitingEntryResponse(entry));
        }
    }

    private PendingChargeResponse createChargeIfNeeded(UUID groupId, UUID memberId, UUID matchId) {
        var group = groupRepo.findByIdAndDeletedAtIsNull(groupId).orElse(null);
        if (group == null || group.getMatchFee() == null) return null;

        if (chargePort.existsPendingOrApproved(matchId, memberId)) return null;

        var view = chargePort.createDaily(groupId, memberId, group.getMatchFee(), matchId);
        return new PendingChargeResponse(view.chargeId(), view.amount(), view.pixKey(), view.status());
    }

    // ── Cancel Own Presence ───────────────────────────────────────────────────

    @Override
    public void execute(CancelPresenceUseCase.Command cmd) {
        GroupMemberView member = requireMember(cmd.groupId(), cmd.actorUserId());
        Match match = requireMatch(cmd.groupId(), cmd.matchId());
        if (!match.isListOpen()) throw new ListClosedException();

        Optional<PresenceEntry> presenceOpt = matchRepository.findPresenceEntry(match.getId(), member.id());
        boolean wasConfirmed = presenceOpt.map(e ->
                e.getStatus() == PresenceStatus.CONFIRMED
                || e.getStatus() == PresenceStatus.PAYMENT_PENDING).orElse(false);

        presenceOpt.ifPresent(e -> matchRepository.deletePresenceEntry(e.getId()));
        matchRepository.findWaitingEntry(match.getId(), member.id())
                .ifPresent(w -> matchRepository.deleteWaitingEntry(w.getId()));

        if (wasConfirmed) {
            promoteFirstFromQueue(match);
        }
    }

    // ── Admin Force Presence ──────────────────────────────────────────────────

    @Override
    public PresenceEntryResponse execute(AdminForcePresenceUseCase.Command cmd) {
        GroupMemberView actor = requireMember(cmd.groupId(), cmd.actorUserId());
        requireRole(actor, GroupRole.ADMIN);

        groupMemberPort.findMemberById(cmd.memberId())
                .orElseThrow(MemberNotFoundException::new);
        Match match = requireMatch(cmd.groupId(), cmd.matchId());

        matchRepository.findWaitingEntry(match.getId(), cmd.memberId())
                .ifPresent(w -> matchRepository.deleteWaitingEntry(w.getId()));

        Optional<PresenceEntry> existing = matchRepository.findPresenceEntry(match.getId(), cmd.memberId());
        existing.ifPresent(e -> matchRepository.deletePresenceEntry(e.getId()));

        PresenceEntry entry = matchRepository.savePresenceEntry(
                PresenceEntry.confirm(match.getId(), cmd.groupId(), cmd.memberId()));
        return toPresenceEntryResponse(entry);
    }

    // ── Admin Remove Presence ─────────────────────────────────────────────────

    @Override
    public void execute(AdminRemovePresenceUseCase.Command cmd) {
        GroupMemberView actor = requireMember(cmd.groupId(), cmd.actorUserId());
        requireRole(actor, GroupRole.ADMIN);
        Match match = requireMatch(cmd.groupId(), cmd.matchId());

        PresenceEntry entry = matchRepository.findPresenceEntry(match.getId(), cmd.memberId())
                .orElseThrow(MemberNotFoundException::new);
        boolean wasConfirmed = entry.getStatus() == PresenceStatus.CONFIRMED
                || entry.getStatus() == PresenceStatus.PAYMENT_PENDING;
        matchRepository.deletePresenceEntry(entry.getId());

        if (wasConfirmed) {
            promoteFirstFromQueue(match);
        }
    }

    // ── Ban From Presence ─────────────────────────────────────────────────────

    @Override
    public MemberResponse execute(BanFromPresenceUseCase.Command cmd) {
        GroupMemberView actor = requireMember(cmd.groupId(), cmd.actorUserId());
        requireRole(actor, GroupRole.ADMIN);

        GroupMemberView target = groupMemberPort.findMemberById(cmd.memberId())
                .orElseThrow(MemberNotFoundException::new);
        if (target.presenceBanned()) throw new MemberAlreadyBannedException();

        groupMemberPort.banFromPresence(cmd.memberId(), cmd.reason(), actor.userId(), cmd.groupId());

        GroupMemberView updated = groupMemberPort.findMemberById(cmd.memberId()).orElseThrow();
        return toMemberResponse(updated);
    }

    // ── Unban From Presence ───────────────────────────────────────────────────

    @Override
    public void execute(UnbanFromPresenceUseCase.Command cmd) {
        GroupMemberView actor = requireMember(cmd.groupId(), cmd.actorUserId());
        requireRole(actor, GroupRole.ADMIN);

        GroupMemberView target = groupMemberPort.findMemberById(cmd.memberId())
                .orElseThrow(MemberNotFoundException::new);
        if (!target.presenceBanned()) throw new MemberNotBannedException();

        groupMemberPort.unbanFromPresence(cmd.memberId(), cmd.groupId(), actor.userId());
    }

    // ── Add Guest ─────────────────────────────────────────────────────────────

    @Override
    public GuestResponse execute(AddGuestUseCase.Command cmd) {
        GroupMemberView actor = requireMember(cmd.groupId(), cmd.actorUserId());
        requireRole(actor, GroupRole.ADMIN);
        Match match = requireMatch(cmd.groupId(), cmd.matchId());

        long memberOccupied = matchRepository.countOccupiedByMatchId(match.getId());
        long guestOccupied = guestRepository.countOccupiedByMatchId(match.getId());
        if (memberOccupied + guestOccupied >= match.getMaxPlayers()) {
            throw new MatchFullException();
        }

        var group = groupRepo.findByIdAndDeletedAtIsNull(cmd.groupId())
                .orElseThrow(GroupNotFoundException::new);
        boolean hasFee = group.getMatchFee() != null;

        MatchGuest guest = MatchGuest.add(match.getId(), cmd.groupId(), cmd.name(),
                cmd.skill(), cmd.position(), actor.userId(), hasFee);
        guest = guestRepository.save(guest);

        UUID chargeId = null;
        if (hasFee) {
            var chargeView = chargePort.createDailyForGuest(cmd.groupId(), guest.getId(),
                    group.getMatchFee(), match.getId());
            chargeId = chargeView.chargeId();
        }

        return new GuestResponse(guest.getId(), guest.getMatchId(), guest.getName(),
                guest.getSkill(), guest.getPosition(), guest.getStatus(),
                chargeId, guest.getConfirmedAt(), guest.getCreatedAt());
    }

    // ── Remove Guest ──────────────────────────────────────────────────────────

    @Override
    public void execute(RemoveGuestUseCase.Command cmd) {
        GroupMemberView actor = requireMember(cmd.groupId(), cmd.actorUserId());
        requireRole(actor, GroupRole.ADMIN);
        Match match = requireMatch(cmd.groupId(), cmd.matchId());

        MatchGuest guest = guestRepository.findByIdAndMatchId(cmd.guestId(), match.getId())
                .orElseThrow(GuestNotFoundException::new);

        boolean wasConfirmed = guest.getStatus() == GuestStatus.CONFIRMED;

        chargePort.cancelGuestCharge(match.getId(), guest.getId(), null);
        guestRepository.delete(guest.getId());

        if (wasConfirmed) {
            promoteFirstFromQueue(match);
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private GroupMemberView requireMember(UUID groupId, UUID userId) {
        return groupMemberPort.findMember(groupId, userId)
                .orElseThrow(NotAMemberException::new);
    }

    private void requireRole(GroupMemberView member, GroupRole required) {
        if (!member.role().isAtLeast(required)) throw new InsufficientRoleException();
    }

    private Match requireMatch(UUID groupId, UUID matchId) {
        return matchRepository.findByIdAndGroupId(matchId, groupId)
                .orElseThrow(MatchNotFoundException::new);
    }

    private void promoteFirstFromQueue(Match match) {
        matchRepository.findFirstWaitingEntry(match.getId()).ifPresent(first -> {
            matchRepository.deleteWaitingEntry(first.getId());
            matchRepository.savePresenceEntry(
                    PresenceEntry.confirm(match.getId(), match.getGroupId(), first.getMemberId()));
        });
    }

    private String resolveMyPresenceStatus(UUID matchId, UUID memberId) {
        Optional<PresenceEntry> pe = matchRepository.findPresenceEntry(matchId, memberId);
        if (pe.isPresent()) return pe.get().getStatus().name();
        Optional<WaitingEntry> we = matchRepository.findWaitingEntry(matchId, memberId);
        if (we.isPresent()) return "WAITING";
        return null;
    }

    private MatchResponse toMatchResponse(Match match, String myPresenceStatus) {
        long confirmedMembers = matchRepository.countOccupiedByMatchId(match.getId());
        long confirmedGuests = guestRepository.countOccupiedByMatchId(match.getId());
        long confirmed = confirmedMembers + confirmedGuests;
        long waiting = matchRepository.countWaitingByMatchId(match.getId());
        return new MatchResponse(
                match.getId(), match.getGroupId(), match.getScheduledAt(), match.getListClosesAt(),
                match.getLocation().name(), match.getLocation().address(),
                match.getMaxPlayers(), match.getStatus(), match.getPresenceListStatus(),
                match.getCreatedBy(), match.getCreatedAt(), myPresenceStatus, confirmed, waiting);
    }

    private MatchSummaryResponse toSummaryResponse(Match match) {
        long confirmedMembers = matchRepository.countOccupiedByMatchId(match.getId());
        long confirmedGuests = guestRepository.countOccupiedByMatchId(match.getId());
        long confirmed = confirmedMembers + confirmedGuests;
        long waiting = matchRepository.countWaitingByMatchId(match.getId());
        return new MatchSummaryResponse(
                match.getId(), match.getScheduledAt(), match.getListClosesAt(),
                match.getLocation().name(), match.getLocation().address(),
                match.getMaxPlayers(), match.getStatus(), match.getPresenceListStatus(),
                confirmed, waiting);
    }

    private PresenceEntryResponse toPresenceEntryResponse(PresenceEntry entry) {
        GroupMemberView member = groupMemberPort.findMemberById(entry.getMemberId()).orElse(null);
        return new PresenceEntryResponse(
                entry.getId(), "MEMBER", entry.getMemberId(), null,
                member != null ? member.userName() : null,
                member != null ? member.role().name() : null,
                member != null ? member.skill() : null,
                member != null ? member.position() : null,
                entry.getStatus(), null, entry.getConfirmedAt());
    }

    private PresenceEntryResponse toGuestPresenceEntryResponse(MatchGuest g) {
        return new PresenceEntryResponse(
                g.getId(), "GUEST", null, g.getId(),
                g.getName(), null, g.getSkill(), g.getPosition(),
                null, g.getStatus(), g.getConfirmedAt());
    }

    private WaitingEntryResponse toWaitingEntryResponse(WaitingEntry entry) {
        GroupMemberView member = groupMemberPort.findMemberById(entry.getMemberId()).orElse(null);
        return new WaitingEntryResponse(
                entry.getId(), entry.getMemberId(),
                member != null ? member.userName() : null,
                member != null ? member.role().name() : null,
                member != null ? member.skill() : null,
                member != null ? member.position() : null,
                entry.getPosition(), entry.getCreatedAt());
    }

    private MemberResponse toMemberResponse(GroupMemberView view) {
        return new MemberResponse(
                view.id(), view.userId(), view.groupId(), view.userName(),
                view.role(), view.skill(), view.skillSource(), view.position(),
                view.isSubscriber(), view.joinedAt());
    }
}
