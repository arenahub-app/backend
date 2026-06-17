package com.arenahub.application.match;

import com.arenahub.application.exception.*;
import com.arenahub.application.match.port.in.*;
import com.arenahub.application.match.port.out.ChargePort;
import com.arenahub.application.match.port.out.GroupMemberPort;
import com.arenahub.application.match.port.out.GroupMemberPort.GroupMemberView;
import com.arenahub.application.match.port.out.MatchRepository;
import com.arenahub.domain.group.vo.GroupRole;
import com.arenahub.domain.group.vo.GroupStatus;
import com.arenahub.domain.match.Match;
import com.arenahub.domain.match.PresenceEntry;
import com.arenahub.domain.match.WaitingEntry;
import com.arenahub.domain.match.vo.Location;
import com.arenahub.domain.match.vo.PresenceStatus;
import com.arenahub.infrastructure.persistence.group.GroupJpaRepository;
import com.arenahub.presentation.group.dto.MemberResponse;
import com.arenahub.presentation.match.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        BanFromPresenceUseCase, UnbanFromPresenceUseCase {

    private final MatchRepository matchRepository;
    private final GroupMemberPort groupMemberPort;
    private final GroupJpaRepository groupRepo;
    private final ChargePort chargePort;

    public MatchService(MatchRepository matchRepository, GroupMemberPort groupMemberPort,
                        GroupJpaRepository groupRepo, ChargePort chargePort) {
        this.matchRepository = matchRepository;
        this.groupMemberPort = groupMemberPort;
        this.groupRepo = groupRepo;
        this.chargePort = chargePort;
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

        List<PresenceEntry> confirmed = matchRepository.findPresenceEntriesByMatchIdAndStatus(
                match.getId(), PresenceStatus.CONFIRMED);
        List<PresenceEntry> declined = matchRepository.findPresenceEntriesByMatchIdAndStatus(
                match.getId(), PresenceStatus.DECLINED);
        List<WaitingEntry> waiting = matchRepository.findWaitingEntriesByMatchId(match.getId());

        return new PresenceListResponse(
                confirmed.stream().map(e -> toPresenceEntryResponse(e)).toList(),
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
        if (existing.isPresent() && existing.get().getStatus() == PresenceStatus.CONFIRMED) {
            throw new AlreadyConfirmedException();
        }
        if (existing.isPresent() && existing.get().getStatus() == PresenceStatus.BANNED_PENDING) {
            throw new AlreadyConfirmedException();
        }

        if (member.presenceBanned()) {
            PresenceEntry entry = matchRepository.savePresenceEntry(
                    PresenceEntry.pendingBan(match.getId(), cmd.groupId(), member.id(),
                            member.presenceBanReason()));
            return PresenceActionResponse.presence(toPresenceEntryResponse(entry));
        }

        long confirmed = matchRepository.countConfirmedByMatchId(match.getId());
        if (confirmed < match.getMaxPlayers()) {
            PresenceEntry entry = matchRepository.savePresenceEntry(
                    PresenceEntry.confirm(match.getId(), cmd.groupId(), member.id()));
            PendingChargeResponse pendingCharge = createChargeIfNeeded(
                    cmd.groupId(), member.id(), match.getId());
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
        boolean wasConfirmed = presenceOpt.map(e -> e.getStatus() == PresenceStatus.CONFIRMED).orElse(false);

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

        GroupMemberView target = groupMemberPort.findMemberById(cmd.memberId())
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
        boolean wasConfirmed = entry.getStatus() == PresenceStatus.CONFIRMED;
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
        long confirmed = matchRepository.countConfirmedByMatchId(match.getId());
        long waiting = matchRepository.countWaitingByMatchId(match.getId());
        return new MatchResponse(
                match.getId(), match.getGroupId(), match.getScheduledAt(), match.getListClosesAt(),
                match.getLocation().name(), match.getLocation().address(),
                match.getMaxPlayers(), match.getStatus(), match.getPresenceListStatus(),
                match.getCreatedBy(), match.getCreatedAt(), myPresenceStatus, confirmed, waiting);
    }

    private MatchSummaryResponse toSummaryResponse(Match match) {
        long confirmed = matchRepository.countConfirmedByMatchId(match.getId());
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
                entry.getId(), entry.getMemberId(),
                member != null ? member.userName() : null,
                member != null ? member.role().name() : null,
                member != null ? member.skill() : null,
                member != null ? member.position() : null,
                entry.getStatus(), entry.getConfirmedAt());
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
