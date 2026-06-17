package com.arenahub.presentation.payment.dto;

import java.util.List;

public record ChargePageResponse(
        List<ChargeResponse> content,
        long totalElements,
        int totalPages
) {}
