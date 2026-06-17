package com.arenahub.application.payment;

import com.arenahub.application.exception.*;
import com.arenahub.application.match.port.out.GroupMemberPort;
import com.arenahub.application.match.port.out.GroupMemberPort.GroupMemberView;
import com.arenahub.application.payment.port.in.*;
import com.arenahub.application.payment.port.out.ChargeRepository;
import com.arenahub.application.payment.port.out.ChargeRepository.ChargeView;
import com.arenahub.application.payment.port.out.FinancialEntryRepository;
import com.arenahub.application.storage.port.out.StoragePort;
import com.arenahub.domain.financial.FinancialEntry;
import com.arenahub.domain.group.vo.GroupRole;
import com.arenahub.domain.group.vo.SkillSource;
import com.arenahub.domain.payment.Charge;
import com.arenahub.domain.payment.PaymentAttempt;
import com.arenahub.domain.payment.vo.ChargeStatus;
import com.arenahub.domain.payment.vo.ChargeType;
import com.arenahub.infrastructure.persistence.group.GroupJpaEntity;
import com.arenahub.infrastructure.persistence.group.GroupJpaRepository;
import com.arenahub.presentation.payment.dto.ChargeStatusResponse;
import com.arenahub.presentation.payment.dto.PaymentAttemptResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock ChargeRepository chargeRepository;
    @Mock FinancialEntryRepository financialEntryRepository;
    @Mock GroupMemberPort groupMemberPort;
    @Mock GroupJpaRepository groupRepo;
    @Mock StoragePort storagePort;
    @Mock com.arenahub.application.payment.port.out.PresencePort presencePort;

    private PaymentService service;

    private final UUID groupId = UUID.randomUUID();
    private final UUID actorId = UUID.randomUUID();
    private final UUID memberId = UUID.randomUUID();
    private final UUID chargeId = UUID.randomUUID();
    private final UUID attemptId = UUID.randomUUID();
    private final UUID matchId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new PaymentService(chargeRepository, financialEntryRepository,
                groupMemberPort, groupRepo, storagePort, presencePort);
    }

    // ── Submit Receipt ────────────────────────────────────────────────────────

    @Test
    void submitReceipt_uploadsAndCreatesAttempt() {
        when(groupMemberPort.findMember(groupId, actorId)).thenReturn(Optional.of(playerView(actorId, memberId)));
        when(chargeRepository.findByIdAndGroupId(chargeId, groupId))
                .thenReturn(Optional.of(pendingCharge(memberId)));
        when(chargeRepository.existsPendingAttemptByChargeId(chargeId)).thenReturn(false);
        when(storagePort.upload(anyString(), any(), anyString())).thenReturn("key");
        when(storagePort.getPublicUrl(anyString())).thenReturn("https://cdn/key.jpg");

        PaymentAttempt attempt = PaymentAttempt.createWithFile(chargeId, groupId, "key", "image/jpeg");
        when(chargeRepository.saveAttempt(any())).thenReturn(attempt);

        PaymentAttemptResponse resp = service.execute(new SubmitReceiptUseCase.Command(
                groupId, chargeId, actorId, new byte[]{1, 2, 3}, "receipt.jpg", "image/jpeg"));

        assertThat(resp.chargeId()).isEqualTo(chargeId);
        verify(storagePort).upload(anyString(), any(), eq("image/jpeg"));
        verify(chargeRepository).saveAttempt(any());
    }

    @Test
    void submitReceipt_throwsInvalidFileType_whenPdf_notAllowed() {
        when(groupMemberPort.findMember(groupId, actorId)).thenReturn(Optional.of(playerView(actorId, memberId)));
        when(chargeRepository.findByIdAndGroupId(chargeId, groupId))
                .thenReturn(Optional.of(pendingCharge(memberId)));

        assertThatThrownBy(() -> service.execute(new SubmitReceiptUseCase.Command(
                groupId, chargeId, actorId, new byte[]{1}, "doc.txt", "text/plain")))
                .isInstanceOf(InvalidFileTypeException.class);
    }

    @Test
    void submitReceipt_throwsAttemptPendingReview_whenAlreadyPending() {
        when(groupMemberPort.findMember(groupId, actorId)).thenReturn(Optional.of(playerView(actorId, memberId)));
        when(chargeRepository.findByIdAndGroupId(chargeId, groupId))
                .thenReturn(Optional.of(pendingCharge(memberId)));
        when(chargeRepository.existsPendingAttemptByChargeId(chargeId)).thenReturn(true);

        assertThatThrownBy(() -> service.execute(new SubmitReceiptUseCase.Command(
                groupId, chargeId, actorId, new byte[]{1}, "r.jpg", "image/jpeg")))
                .isInstanceOf(AttemptPendingReviewException.class);
    }

    @Test
    void submitReceipt_throwsInsufficientRole_whenOtherMember() {
        UUID otherMemberId = UUID.randomUUID();
        when(groupMemberPort.findMember(groupId, actorId)).thenReturn(Optional.of(playerView(actorId, memberId)));
        when(chargeRepository.findByIdAndGroupId(chargeId, groupId))
                .thenReturn(Optional.of(pendingCharge(otherMemberId)));

        assertThatThrownBy(() -> service.execute(new SubmitReceiptUseCase.Command(
                groupId, chargeId, actorId, new byte[]{1}, "r.jpg", "image/jpeg")))
                .isInstanceOf(InsufficientRoleException.class);
    }

    // ── Approve Attempt ───────────────────────────────────────────────────────

    @Test
    void approveAttempt_approvesChargeAndCreatesFinancialEntry() {
        when(groupMemberPort.findMember(groupId, actorId)).thenReturn(Optional.of(adminView()));
        Charge charge = pendingCharge(memberId);
        when(chargeRepository.findByIdAndGroupId(chargeId, groupId)).thenReturn(Optional.of(charge));
        PaymentAttempt attempt = PaymentAttempt.createWithFile(chargeId, groupId, "key", "image/jpeg");
        when(chargeRepository.findAttemptById(attemptId)).thenReturn(Optional.of(attempt));
        when(chargeRepository.saveAttempt(any())).thenReturn(attempt);
        when(chargeRepository.save(any())).thenReturn(charge);
        when(financialEntryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ChargeStatusResponse resp = service.execute(new ApproveAttemptUseCase.Command(
                groupId, chargeId, attemptId, actorId, "OK"));

        assertThat(resp.status()).isEqualTo(ChargeStatus.APPROVED);
        verify(financialEntryRepository).save(any(FinancialEntry.class));
    }

    @Test
    void approveAttempt_throwsInsufficientRole_whenPlayer() {
        when(groupMemberPort.findMember(groupId, actorId)).thenReturn(Optional.of(playerView(actorId, memberId)));

        assertThatThrownBy(() -> service.execute(new ApproveAttemptUseCase.Command(
                groupId, chargeId, attemptId, actorId, null)))
                .isInstanceOf(InsufficientRoleException.class);
    }

    // ── Manual Approve ────────────────────────────────────────────────────────

    @Test
    void manualApprove_createsAttemptAndFinancialEntry() {
        when(groupMemberPort.findMember(groupId, actorId)).thenReturn(Optional.of(adminView()));
        Charge charge = pendingCharge(memberId);
        when(chargeRepository.findByIdAndGroupId(chargeId, groupId)).thenReturn(Optional.of(charge));
        when(chargeRepository.saveAttempt(any())).thenAnswer(inv -> inv.getArgument(0));
        when(chargeRepository.save(any())).thenReturn(charge);
        when(financialEntryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ChargeStatusResponse resp = service.execute(new ManualApproveChargeUseCase.Command(
                groupId, chargeId, actorId, "Pago em dinheiro"));

        assertThat(resp.status()).isEqualTo(ChargeStatus.APPROVED);
        verify(chargeRepository).saveAttempt(argThat(a ->
                a.getValidationSource() == com.arenahub.domain.payment.vo.ValidationSource.MANUAL));
        verify(financialEntryRepository).save(any(FinancialEntry.class));
    }

    @Test
    void manualApprove_throwsChargeNotPending_whenAlreadyApproved() {
        when(groupMemberPort.findMember(groupId, actorId)).thenReturn(Optional.of(adminView()));
        Charge charge = pendingCharge(memberId);
        charge.approve();
        when(chargeRepository.findByIdAndGroupId(chargeId, groupId)).thenReturn(Optional.of(charge));

        assertThatThrownBy(() -> service.execute(new ManualApproveChargeUseCase.Command(
                groupId, chargeId, actorId, null)))
                .isInstanceOf(ChargeNotPendingException.class);
    }

    // ── Reject Attempt ────────────────────────────────────────────────────────

    @Test
    void rejectAttempt_keepChargeAsPending() {
        when(groupMemberPort.findMember(groupId, actorId)).thenReturn(Optional.of(adminView()));
        Charge charge = pendingCharge(memberId);
        when(chargeRepository.findByIdAndGroupId(chargeId, groupId)).thenReturn(Optional.of(charge));
        PaymentAttempt attempt = PaymentAttempt.createWithFile(chargeId, groupId, "key", "image/jpeg");
        when(chargeRepository.findAttemptById(attemptId)).thenReturn(Optional.of(attempt));
        when(chargeRepository.saveAttempt(any())).thenReturn(attempt);
        when(chargeRepository.save(any())).thenReturn(charge);

        ChargeStatusResponse resp = service.execute(new RejectAttemptUseCase.Command(
                groupId, chargeId, attemptId, actorId, "Valor errado"));

        assertThat(resp.status()).isEqualTo(ChargeStatus.PENDING);
        verify(financialEntryRepository, never()).save(any());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Charge pendingCharge(UUID memberId) {
        return Charge.createDaily(groupId, memberId, new BigDecimal("25.00"), matchId);
    }

    private GroupMemberView adminView() {
        return new GroupMemberView(memberId, actorId, groupId, "Admin User",
                GroupRole.ADMIN, BigDecimal.ZERO, SkillSource.DEFAULT, null,
                false, Instant.now(), false, null);
    }

    private GroupMemberView playerView(UUID userId, UUID id) {
        return new GroupMemberView(id, userId, groupId, "Player",
                GroupRole.PLAYER, BigDecimal.ZERO, SkillSource.DEFAULT, null,
                false, Instant.now(), false, null);
    }
}
