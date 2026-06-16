package com.arenahub.infrastructure.persistence.group;

import java.util.UUID;

public interface GroupSummaryProjection {
    UUID getId();
    String getName();
    String getSport();
    String getPhotoUrl();
    String getStatus();
    Long getMemberCount();
    String getMyRole();
}
