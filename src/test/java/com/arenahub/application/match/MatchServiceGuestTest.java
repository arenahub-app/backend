package com.arenahub.application.match;

import com.arenahub.application.exception.*;
import com.arenahub.application.match.port.in.AddGuestUseCase;
import com.arenahub.application.match.port.in.RemoveGuestUseCase;
import com.arenahub.application.match.port.out.ChargePort;
import com.arenahub.application.match.port.out.GroupMemberPort;
import com.arenahub.application.match.port.out.GroupMemberPort.GroupMemberView;
import com.arenahub.application.match.port.out.GuestRepository;
import com.arenahub.application.match.port.out.MatchRepository;
import com.arenahub.domain.group.vo.GroupRole;
import com.arenahub.domain.group.vo.GroupStatus;
import com.arenahub.domain.group.vo.PlayerPosition;
import com.arenahub.domain.group.vo.SkillSource;
import com.arenahub.domain.match.Match;
import com.arenahub.domain.match.MatchGuest;
import com.arenahub.domain.match.WaitingEntry;
import com.arenahub.domain.match.vo.GuestStatus;
import com.arenahub.domain.match.vo.Location;
import com.arenahub.domain.payment.vo.ChargeStatus;
import com.arenahub.infrastructure.persistence.group.GroupJpaEntity;
import com.arenahub.infrastructure.persistence.group.GroupJpaRepository;
import com.arenahub.presentation.match.dto.GuestResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchServiceGuestTest {

    @Mock MatchRepository matchRepository;
    @Mock GroupMemberPort groupMemberPort;
    @Mock GroupJpaRepository groupRepo;
    @Mock ChargePort chargePort;
    @Mock GuestRepository guestRepository;

    private MatchService matchService;

    private final UUID groupId = UUID.randomUUID();
    private final UUID actorId = UUID.randomUUID();
    private final UUID memberId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        matchService = new MatchService(matchRepository, groupMemberPort, groupRepo, chargePort, guestRepository);
    }

    private GroupMemberView adminView() {
        return new GroupMemberView(memberId, actorId, groupId, "Admin",
                GroupRole.ADMIN, BigDecimal.valueOf(5.0), SkillSource.MANUAL, null,
                false, Instant.now(), false, null);
    }

    private Match scheduledMatch(int maxPlayers) {
        return Match.create(groupId, Instant.now().plus(Duration.ofHours(2)),
                new Location("Quadra", null), maxPlayers, memberId);
    }

    private GroupJpaEntity groupWithFee(BigDecimal fee) {
        GroupJpaEntity g = new GroupJpaEntity();
        g.setStatus(GroupStatus.ACTIVE);
        g.setMatchFee(fee);
        return g;
    }

    private GroupJpaEntity groupWithoutFee() {
        GroupJpaEntity g = new GroupJpaEntity();
        g.setStatus(GroupStatus.ACTIVE);
        return g;
    }

    @Test
    void addGuest_noSlot_throwsMatchFull() {
        Match match = scheduledMatch(2);
        UUID matchId = match.getId();

        when(groupMemberPort.findMember(groupId, actorId)).thenReturn(Optional.of(adminView()));
        when(matchRepository.findByIdAndGroupId(matchId, groupId)).thenReturn(Optional.of(match));
        when(matchRepository.countOccupiedByMatchId(matchId)).thenReturn(2L);
        when(guestRepository.countOccupiedByMatchId(matchId)).thenReturn(0L);

        assertThatThrownBy(() -> matchService.execute(new AddGuestUseCase.Command(
                groupId, matchId, actorId, "Carlos", new BigDecimal("3.5"), PlayerPosition.MIDFIELDER)))
                .isInstanceOf(MatchFullException.class);
    }

    @Test
    void addGuest_withFee_createsChargeAndPaymentPendingGuest() {
        Match match = scheduledMatch(10);
        UUID matchId = match.getId();

        when(groupMemberPort.findMember(groupId, actorId)).thenReturn(Optional.of(adminView()));
        when(matchRepository.findByIdAndGroupId(matchId, groupId)).thenReturn(Optional.of(match));
        when(matchRepository.countOccupiedByMatchId(matchId)).thenReturn(3L);
        when(guestRepository.countOccupiedByMatchId(matchId)).thenReturn(0L);
        when(groupRepo.findByIdAndDeletedAtIsNull(groupId)).thenReturn(Optional.of(groupWithFee(new BigDecimal("25.00"))));

        UUID guestId = UUID.randomUUID();
        MatchGuest savedGuest = MatchGuest.add(matchId, groupId, "Carlos", new BigDecimal("3.5"),
                PlayerPosition.MIDFIELDER, actorId, true);
        when(guestRepository.save(any(MatchGuest.class))).thenReturn(savedGuest);

        UUID chargeId = UUID.randomUUID();
        ChargePort.ChargeView chargeView = new ChargePort.ChargeView(chargeId, new BigDecimal("25.00"), "pix@key", ChargeStatus.PENDING);
        when(chargePort.createDailyForGuest(eq(groupId), any(UUID.class), eq(new BigDecimal("25.00")), eq(matchId)))
                .thenReturn(chargeView);

        GuestResponse response = matchService.execute(new AddGuestUseCase.Command(
                groupId, matchId, actorId, "Carlos", new BigDecimal("3.5"), PlayerPosition.MIDFIELDER));

        assertThat(response.status()).isEqualTo(GuestStatus.PAYMENT_PENDING);
        assertThat(response.chargeId()).isEqualTo(chargeId);
        verify(chargePort).createDailyForGuest(eq(groupId), any(UUID.class), eq(new BigDecimal("25.00")), eq(matchId));
    }

    @Test
    void addGuest_withoutFee_createsConfirmedGuest() {
        Match match = scheduledMatch(10);
        UUID matchId = match.getId();

        when(groupMemberPort.findMember(groupId, actorId)).thenReturn(Optional.of(adminView()));
        when(matchRepository.findByIdAndGroupId(matchId, groupId)).thenReturn(Optional.of(match));
        when(matchRepository.countOccupiedByMatchId(matchId)).thenReturn(3L);
        when(guestRepository.countOccupiedByMatchId(matchId)).thenReturn(0L);
        when(groupRepo.findByIdAndDeletedAtIsNull(groupId)).thenReturn(Optional.of(groupWithoutFee()));

        MatchGuest savedGuest = MatchGuest.add(matchId, groupId, "Ana", new BigDecimal("4.0"),
                PlayerPosition.FORWARD, actorId, false);
        when(guestRepository.save(any(MatchGuest.class))).thenReturn(savedGuest);

        GuestResponse response = matchService.execute(new AddGuestUseCase.Command(
                groupId, matchId, actorId, "Ana", new BigDecimal("4.0"), PlayerPosition.FORWARD));

        assertThat(response.status()).isEqualTo(GuestStatus.CONFIRMED);
        assertThat(response.chargeId()).isNull();
        verify(chargePort, never()).createDailyForGuest(any(), any(), any(), any());
    }

    @Test
    void removeGuest_confirmedGuest_promotesQueue() {
        Match match = scheduledMatch(5);
        UUID matchId = match.getId();
        UUID guestId = UUID.randomUUID();

        when(groupMemberPort.findMember(groupId, actorId)).thenReturn(Optional.of(adminView()));
        when(matchRepository.findByIdAndGroupId(matchId, groupId)).thenReturn(Optional.of(match));

        MatchGuest guest = MatchGuest.reconstitute(guestId, matchId, groupId, "Carlos",
                new BigDecimal("3.5"), PlayerPosition.MIDFIELDER, GuestStatus.CONFIRMED,
                actorId, Instant.now(), Instant.now());
        when(guestRepository.findByIdAndMatchId(guestId, matchId)).thenReturn(Optional.of(guest));

        UUID waitingMemberId = UUID.randomUUID();
        WaitingEntry waitingEntry = WaitingEntry.create(matchId, groupId, waitingMemberId, 1);
        when(matchRepository.findFirstWaitingEntry(matchId)).thenReturn(Optional.of(waitingEntry));
        when(matchRepository.savePresenceEntry(any())).thenAnswer(inv -> inv.getArgument(0));

        matchService.execute(new RemoveGuestUseCase.Command(groupId, matchId, guestId, actorId));

        verify(guestRepository).delete(guestId);
        verify(chargePort).cancelGuestCharge(matchId, guestId, null);
        verify(matchRepository).deleteWaitingEntry(waitingEntry.getId());
        verify(matchRepository).savePresenceEntry(any());
    }
}
