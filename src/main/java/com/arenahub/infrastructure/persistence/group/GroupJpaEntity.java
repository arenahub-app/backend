package com.arenahub.infrastructure.persistence.group;

import com.arenahub.domain.group.Group;
import com.arenahub.domain.group.vo.GroupName;
import com.arenahub.domain.group.vo.GroupStatus;
import com.arenahub.domain.group.vo.Sport;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "groups")
@Getter
@Setter
@NoArgsConstructor
public class GroupJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, length = 80)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private Sport sport;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Column(name = "pix_key", length = 255)
    private String pixKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private GroupStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public Group toDomain() {
        return Group.reconstitute(id, new GroupName(name), sport, description,
                photoUrl, pixKey, status, createdAt, updatedAt);
    }

    public static GroupJpaEntity fromDomain(Group group) {
        GroupJpaEntity e = new GroupJpaEntity();
        e.id = group.getId();
        e.name = group.getName().value();
        e.sport = group.getSport();
        e.description = group.getDescription();
        e.photoUrl = group.getPhotoUrl();
        e.pixKey = group.getPixKey();
        e.status = group.getStatus();
        e.createdAt = group.getCreatedAt();
        e.updatedAt = group.getUpdatedAt();
        return e;
    }
}
