package com.arenahub.application.payment;

import com.arenahub.application.exception.*;
import com.arenahub.application.match.port.out.GroupMemberPort;
import com.arenahub.application.match.port.out.GroupMemberPort.GroupMemberView;
import com.arenahub.application.payment.port.in.*;
import com.arenahub.application.payment.port.out.ChargeRepository;
import com.arenahub.application.payment.port.out.ChargeRepository.ChargeView;
import com.arenahub.application.payment.port.out.FinancialEntryRepository;
import com.arenahub.application.payment.port.out.PresencePort;
import com.arenahub.application.storage.port.out.StoragePort;
import com.arenahub.domain.financial.FinancialEntry;
import com.arenahub.domain.financial.vo.EntryCategory;
import com.arenahub.domain.payment.Charge;
import com.arenahub.domain.payment.PaymentAttempt;
import com.arenahub.infrastructure.persistence.group.GroupJpaRepository;
import com.arenahub.presentation.payment.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PaymentService implements
        GetGroupChargesUseCase, GetMatchChargesUseCase, GetMyChargesUseCase,
        GetChargeDetailUseCase, SubmitReceiptUseCase,
        ApproveAttemptUseCase, RejectAttemptUseCase, ManualApproveChargeUseCase {

    private static final List<String> ALLOWED_CONTENT_TYPES =
            List.of("image/jpeg", "image/png", "application/pdf");
    private static final long MAX_FILE_BYTES = 10L * 1024 * 1024;

    private final ChargeRepository chargeRepository;
    private final FinancialEntryRepository financialEntryRepository;
    private final GroupMemberPort groupMemberPort;
    private final GroupJpaRepository groupRepo;
    private final StoragePort storagePort;
    private final PresencePort presencePort;

    public PaymentService(ChargeRepository chargeRepository,
                          FinancialEntryRepository financialEntryRepository,
                          GroupMemberPort groupMemberPort,
                          GroupJpaRepository groupRepo,
                          StoragePort storagePort,
                          PresencePort presencePort) {
        this.chargeRepository = chargeRepository;
        this.financialEntryRepository = financialEntryRepository;
        this.groupMemberPort = groupMemberPort;
        this.groupRepo = groupRepo;
        this.storagePort = storagePort;
        this.presencePort = presencePort;
    }

    // ── Get Group Charges ─────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public ChargePageResponse execute(GetGroupChargesUseCase.Command cmd) {
        GroupMemberView actor = requireMember(cmd.groupId(), cmd.actorUserId());

        if (!actor.role().isAtLeast(com.arenahub.domain.group.vo.GroupRole.ADMIN)) {
            return myChargesPage(actor.id());
        }

        List<ChargeView> views = chargeRepository.findByGroupId(
                cmd.groupId(), cmd.status(), cmd.matchId(), cmd.page(), cmd.size());
        long total = chargeRepository.countByGroupId(cmd.groupId(), cmd.status(), cmd.matchId());
        int totalPages = (int) Math.ceil((double) total / cmd.size());

        return new ChargePageResponse(
                views.stream().map(this::toChargeResponse).toList(),
                total, totalPages);
    }

    private ChargePageResponse myChargesPage(UUID memberId) {
        List<ChargeView> views = chargeRepository.findByMemberId(memberId);
        return new ChargePageResponse(views.stream().map(this::toChargeResponse).toList(),
                views.size(), 1);
    }

    // ── Get Match Charges ─────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<ChargeResponse> execute(GetMatchChargesUseCase.Command cmd) {
        GroupMemberView actor = requireMember(cmd.groupId(), cmd.actorUserId());
        requireAdmin(actor);
        return chargeRepository.findByMatchId(cmd.matchId()).stream()
                .map(this::toChargeResponse)
                .toList();
    }

    // ── Get My Charges ────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<ChargeResponse> execute(GetMyChargesUseCase.Command cmd) {
        GroupMemberView actor = requireMember(cmd.groupId(), cmd.actorUserId());
        return chargeRepository.findByMemberId(actor.id()).stream()
                .map(this::toChargeResponse)
                .toList();
    }

    // ── Get Charge Detail ─────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public ChargeDetailResponse execute(GetChargeDetailUseCase.Command cmd) {
        GroupMemberView actor = requireMember(cmd.groupId(), cmd.actorUserId());
        Charge charge = requireCharge(cmd.chargeId(), cmd.groupId());

        boolean isAdmin = actor.role().isAtLeast(com.arenahub.domain.group.vo.GroupRole.ADMIN);
        if (!isAdmin && !charge.getMemberId().equals(actor.id())) {
            throw new InsufficientRoleException();
        }

        String pixKey = groupRepo.findByIdAndDeletedAtIsNull(cmd.groupId())
                .map(g -> g.getPixKey())
                .orElse(null);

        List<PaymentAttempt> attempts = chargeRepository.findAttemptsByChargeId(cmd.chargeId());
        String memberName = groupMemberPort.findMemberById(charge.getMemberId())
                .map(GroupMemberView::userName).orElse(null);

        return new ChargeDetailResponse(
                charge.getId(), charge.getMemberId(), memberName,
                charge.getType(), charge.getAmount(), pixKey,
                charge.getReferenceMatchId(), null,
                charge.getStatus(), charge.getCreatedAt(),
                attempts.stream().map(a -> toAttemptResponse(a)).toList());
    }

    // ── Submit Receipt ────────────────────────────────────────────────────────

    @Override
    public PaymentAttemptResponse execute(SubmitReceiptUseCase.Command cmd) {
        GroupMemberView actor = requireMember(cmd.groupId(), cmd.actorUserId());
        Charge charge = requireCharge(cmd.chargeId(), cmd.groupId());

        if (!charge.getMemberId().equals(actor.id())) {
            throw new InsufficientRoleException();
        }
        if (!charge.isPending()) {
            throw new ChargeNotPendingException();
        }
        if (!ALLOWED_CONTENT_TYPES.contains(cmd.contentType())) {
            throw new InvalidFileTypeException();
        }
        if (cmd.fileContent().length > MAX_FILE_BYTES) {
            throw new FileTooLargeException();
        }
        if (chargeRepository.existsPendingAttemptByChargeId(cmd.chargeId())) {
            throw new AttemptPendingReviewException();
        }

        String ext = extensionFor(cmd.contentType());
        String key = "payments/%s/%s/%s.%s"
                .formatted(cmd.groupId(), cmd.chargeId(), UUID.randomUUID(), ext);

        storagePort.upload(key, cmd.fileContent(), cmd.contentType());

        PaymentAttempt attempt = PaymentAttempt.createWithFile(
                cmd.chargeId(), cmd.groupId(), key, cmd.contentType());
        attempt = chargeRepository.saveAttempt(attempt);

        return toAttemptResponse(attempt);
    }

    // ── Approve Attempt ───────────────────────────────────────────────────────

    @Override
    public ChargeStatusResponse execute(ApproveAttemptUseCase.Command cmd) {
        GroupMemberView actor = requireMember(cmd.groupId(), cmd.actorUserId());
        requireAdmin(actor);

        Charge charge = requireCharge(cmd.chargeId(), cmd.groupId());
        PaymentAttempt attempt = chargeRepository.findAttemptById(cmd.attemptId())
                .orElseThrow(AttemptNotFoundException::new);

        attempt.approve(cmd.actorUserId(), cmd.reviewNote());
        chargeRepository.saveAttempt(attempt);

        approveChargeAndCreateEntry(charge, cmd.actorUserId());

        return new ChargeStatusResponse(charge.getId(), charge.getStatus(), charge.getUpdatedAt());
    }

    // ── Reject Attempt ────────────────────────────────────────────────────────

    @Override
    public ChargeStatusResponse execute(RejectAttemptUseCase.Command cmd) {
        GroupMemberView actor = requireMember(cmd.groupId(), cmd.actorUserId());
        requireAdmin(actor);

        Charge charge = requireCharge(cmd.chargeId(), cmd.groupId());
        if (!charge.isPending()) throw new ChargeNotPendingException();

        PaymentAttempt attempt = chargeRepository.findAttemptById(cmd.attemptId())
                .orElseThrow(AttemptNotFoundException::new);

        attempt.reject(cmd.actorUserId(), cmd.reviewNote());
        chargeRepository.saveAttempt(attempt);
        chargeRepository.save(charge);

        return new ChargeStatusResponse(charge.getId(), charge.getStatus(), charge.getUpdatedAt());
    }

    // ── Manual Approve ────────────────────────────────────────────────────────

    @Override
    public ChargeStatusResponse execute(ManualApproveChargeUseCase.Command cmd) {
        GroupMemberView actor = requireMember(cmd.groupId(), cmd.actorUserId());
        requireAdmin(actor);

        Charge charge = requireCharge(cmd.chargeId(), cmd.groupId());
        if (!charge.isPending()) throw new ChargeNotPendingException();

        PaymentAttempt attempt = PaymentAttempt.createManual(
                cmd.chargeId(), cmd.groupId(), cmd.actorUserId(), cmd.note());
        chargeRepository.saveAttempt(attempt);

        approveChargeAndCreateEntry(charge, cmd.actorUserId());

        return new ChargeStatusResponse(charge.getId(), charge.getStatus(), charge.getUpdatedAt());
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void approveChargeAndCreateEntry(Charge charge, UUID adminId) {
        charge.approve();
        chargeRepository.save(charge);

        FinancialEntry entry = FinancialEntry.createRevenue(
                charge.getGroupId(),
                EntryCategory.DAILY,
                charge.getAmount(),
                "Diária aprovada — cobrança " + charge.getId(),
                charge.getReferenceMatchId(),
                charge.getId(),
                adminId);
        financialEntryRepository.save(entry);

        if (charge.getReferenceMatchId() != null) {
            presencePort.confirmAfterPayment(charge.getReferenceMatchId(), charge.getMemberId());
        }
    }

    private GroupMemberView requireMember(UUID groupId, UUID userId) {
        return groupMemberPort.findMember(groupId, userId)
                .orElseThrow(NotAMemberException::new);
    }

    private void requireAdmin(GroupMemberView member) {
        if (!member.role().isAtLeast(com.arenahub.domain.group.vo.GroupRole.ADMIN)) {
            throw new InsufficientRoleException();
        }
    }

    private Charge requireCharge(UUID chargeId, UUID groupId) {
        return chargeRepository.findByIdAndGroupId(chargeId, groupId)
                .orElseThrow(ChargeNotFoundException::new);
    }

    private ChargeResponse toChargeResponse(ChargeView v) {
        return new ChargeResponse(
                v.chargeId(), v.memberId(), v.memberName(), v.type(), v.amount(),
                v.referenceMatchId(), v.matchScheduledAt(), v.status(),
                v.latestAttemptStatus(), v.createdAt());
    }

    private PaymentAttemptResponse toAttemptResponse(PaymentAttempt a) {
        String fileUrl = a.getFileKey() != null ? storagePort.getPublicUrl(a.getFileKey()) : null;
        return new PaymentAttemptResponse(
                a.getId(), a.getChargeId(), fileUrl, a.getContentType(),
                a.getValidationResult(), a.getValidationSource(),
                a.getReviewNote(), a.getSubmittedAt(), a.getReviewedAt());
    }

    private static String extensionFor(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "application/pdf" -> "pdf";
            default -> "bin";
        };
    }

    private BigDecimal toBigDecimal(Object val) {
        if (val == null) return null;
        if (val instanceof BigDecimal bd) return bd;
        return new BigDecimal(val.toString());
    }
}
