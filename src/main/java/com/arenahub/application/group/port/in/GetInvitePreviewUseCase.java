package com.arenahub.application.group.port.in;

import com.arenahub.presentation.group.dto.InvitePreviewResponse;

public interface GetInvitePreviewUseCase {

    InvitePreviewResponse execute(String token);
}
