package com.arenahub.infrastructure.persistence.user;

import com.arenahub.domain.user.User;
import com.arenahub.domain.user.vo.AuthProvider;
import com.arenahub.domain.user.vo.Email;
import com.arenahub.domain.user.vo.Name;
import com.arenahub.domain.user.vo.Phone;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class UserJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(length = 20)
    private String phone;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Column(name = "auth_provider", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private AuthProvider authProvider;

    @Column(name = "google_id", length = 255)
    private String googleId;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public User toDomain() {
        return User.reconstitute(
                id,
                new Name(name),
                new Email(email),
                passwordHash,
                phone != null ? new Phone(phone) : null,
                birthDate,
                photoUrl,
                authProvider,
                googleId,
                emailVerified,
                active,
                createdAt,
                updatedAt
        );
    }

    public static UserJpaEntity fromDomain(User user) {
        UserJpaEntity e = new UserJpaEntity();
        e.id = user.getId();
        e.name = user.getName().value();
        e.email = user.getEmail().value();
        e.passwordHash = user.getPasswordHash();
        e.phone = user.getPhone() != null ? user.getPhone().value() : null;
        e.birthDate = user.getBirthDate();
        e.photoUrl = user.getPhotoUrl();
        e.authProvider = user.getAuthProvider();
        e.googleId = user.getGoogleId();
        e.emailVerified = user.isEmailVerified();
        e.active = user.isActive();
        e.createdAt = user.getCreatedAt();
        e.updatedAt = user.getUpdatedAt();
        return e;
    }
}
