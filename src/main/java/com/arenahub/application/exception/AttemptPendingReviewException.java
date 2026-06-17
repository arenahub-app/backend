package com.arenahub.application.exception;

import org.springframework.http.HttpStatus;

public class AttemptPendingReviewException extends DomainException {
    public AttemptPendingReviewException() {
        super("Já existe um comprovante aguardando revisão para esta cobrança",
                HttpStatus.CONFLICT, "attempt-pending-review");
    }
}
