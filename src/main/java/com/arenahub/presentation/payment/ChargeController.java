package com.arenahub.presentation.payment;

import com.arenahub.application.payment.port.in.*;
import com.arenahub.domain.payment.vo.ChargeStatus;
import com.arenahub.presentation.payment.dto.*;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/groups/{groupId}/charges")
public class ChargeController {

    private final GetGroupChargesUseCase getGroupChargesUseCase;
    private final GetMatchChargesUseCase getMatchChargesUseCase;
    private final GetMyChargesUseCase getMyChargesUseCase;
    private final GetChargeDetailUseCase getChargeDetailUseCase;
    private final SubmitReceiptUseCase submitReceiptUseCase;
    private final ApproveAttemptUseCase approveAttemptUseCase;
    private final RejectAttemptUseCase rejectAttemptUseCase;
    private final ManualApproveChargeUseCase manualApproveChargeUseCase;

    public ChargeController(GetGroupChargesUseCase getGroupChargesUseCase,
                             GetMatchChargesUseCase getMatchChargesUseCase,
                             GetMyChargesUseCase getMyChargesUseCase,
                             GetChargeDetailUseCase getChargeDetailUseCase,
                             SubmitReceiptUseCase submitReceiptUseCase,
                             ApproveAttemptUseCase approveAttemptUseCase,
                             RejectAttemptUseCase rejectAttemptUseCase,
                             ManualApproveChargeUseCase manualApproveChargeUseCase) {
        this.getGroupChargesUseCase = getGroupChargesUseCase;
        this.getMatchChargesUseCase = getMatchChargesUseCase;
        this.getMyChargesUseCase = getMyChargesUseCase;
        this.getChargeDetailUseCase = getChargeDetailUseCase;
        this.submitReceiptUseCase = submitReceiptUseCase;
        this.approveAttemptUseCase = approveAttemptUseCase;
        this.rejectAttemptUseCase = rejectAttemptUseCase;
        this.manualApproveChargeUseCase = manualApproveChargeUseCase;
    }

    @GetMapping
    public ResponseEntity<ChargePageResponse> listCharges(
            @PathVariable UUID groupId,
            @RequestParam(required = false) ChargeStatus status,
            @RequestParam(required = false) UUID matchId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {
        return ResponseEntity.ok(getGroupChargesUseCase.execute(
                new GetGroupChargesUseCase.Command(groupId, currentUserId(auth),
                        status, matchId, page, size)));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<ChargeResponse>> myCharges(
            @PathVariable UUID groupId, Authentication auth) {
        return ResponseEntity.ok(getMyChargesUseCase.execute(
                new GetMyChargesUseCase.Command(groupId, currentUserId(auth))));
    }

    @GetMapping("/by-match/{matchId}")
    public ResponseEntity<List<ChargeResponse>> byMatch(
            @PathVariable UUID groupId,
            @PathVariable UUID matchId,
            Authentication auth) {
        return ResponseEntity.ok(getMatchChargesUseCase.execute(
                new GetMatchChargesUseCase.Command(groupId, matchId, currentUserId(auth))));
    }

    @GetMapping("/{chargeId}")
    public ResponseEntity<ChargeDetailResponse> detail(
            @PathVariable UUID groupId,
            @PathVariable UUID chargeId,
            Authentication auth) {
        return ResponseEntity.ok(getChargeDetailUseCase.execute(
                new GetChargeDetailUseCase.Command(groupId, chargeId, currentUserId(auth))));
    }

    @PostMapping(value = "/{chargeId}/payment-attempts",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PaymentAttemptResponse> submitReceipt(
            @PathVariable UUID groupId,
            @PathVariable UUID chargeId,
            @RequestParam("file") MultipartFile file,
            Authentication auth) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                submitReceiptUseCase.execute(new SubmitReceiptUseCase.Command(
                        groupId, chargeId, currentUserId(auth),
                        file.getBytes(), file.getOriginalFilename(),
                        file.getContentType())));
    }

    @PostMapping("/{chargeId}/payment-attempts/{attemptId}/approve")
    public ResponseEntity<ChargeStatusResponse> approveAttempt(
            @PathVariable UUID groupId,
            @PathVariable UUID chargeId,
            @PathVariable UUID attemptId,
            @RequestBody(required = false) ReviewAttemptRequest req,
            Authentication auth) {
        String note = req != null ? req.reviewNote() : null;
        return ResponseEntity.ok(approveAttemptUseCase.execute(
                new ApproveAttemptUseCase.Command(groupId, chargeId, attemptId,
                        currentUserId(auth), note)));
    }

    @PostMapping("/{chargeId}/payment-attempts/{attemptId}/reject")
    public ResponseEntity<ChargeStatusResponse> rejectAttempt(
            @PathVariable UUID groupId,
            @PathVariable UUID chargeId,
            @PathVariable UUID attemptId,
            @RequestBody ReviewAttemptRequest req,
            Authentication auth) {
        return ResponseEntity.ok(rejectAttemptUseCase.execute(
                new RejectAttemptUseCase.Command(groupId, chargeId, attemptId,
                        currentUserId(auth), req.reviewNote())));
    }

    @PostMapping("/{chargeId}/approve-manually")
    public ResponseEntity<ChargeStatusResponse> approveManually(
            @PathVariable UUID groupId,
            @PathVariable UUID chargeId,
            @RequestBody(required = false) ManualApprovalRequest req,
            Authentication auth) {
        String note = req != null ? req.note() : null;
        return ResponseEntity.ok(manualApproveChargeUseCase.execute(
                new ManualApproveChargeUseCase.Command(groupId, chargeId, currentUserId(auth), note)));
    }

    private UUID currentUserId(Authentication auth) {
        return UUID.fromString(auth.getName());
    }
}
