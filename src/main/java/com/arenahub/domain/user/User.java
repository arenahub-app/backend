package com.arenahub.domain.user;

import com.arenahub.domain.user.vo.AuthProvider;
import com.arenahub.domain.user.vo.Email;
import com.arenahub.domain.user.vo.Name;
import com.arenahub.domain.user.vo.Phone;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class User {

    private UUID id;
    private Name name;
    private Email email;
    private String passwordHash;
    private Phone phone;
    private LocalDate birthDate;
    private String photoUrl;
    private AuthProvider authProvider;
    private String googleId;
    private boolean emailVerified;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    private User() {}

    public static User registerLocal(Name name, Email email, String passwordHash,
                                     Phone phone, LocalDate birthDate) {
        User user = new User();
        user.id = UUID.randomUUID();
        user.name = name;
        user.email = email;
        user.passwordHash = passwordHash;
        user.phone = phone;
        user.birthDate = birthDate;
        user.authProvider = AuthProvider.LOCAL;
        user.emailVerified = false;
        user.active = true;
        user.createdAt = Instant.now();
        user.updatedAt = Instant.now();
        return user;
    }

    public static User fromGoogle(String googleId, Name name, Email email) {
        User user = new User();
        user.id = UUID.randomUUID();
        user.googleId = googleId;
        user.name = name;
        user.email = email;
        user.authProvider = AuthProvider.GOOGLE;
        user.emailVerified = true;
        user.active = true;
        user.createdAt = Instant.now();
        user.updatedAt = Instant.now();
        return user;
    }

    public static User reconstitute(UUID id, Name name, Email email, String passwordHash,
                                    Phone phone, LocalDate birthDate, String photoUrl,
                                    AuthProvider authProvider, String googleId,
                                    boolean emailVerified, boolean active,
                                    Instant createdAt, Instant updatedAt) {
        User user = new User();
        user.id = id;
        user.name = name;
        user.email = email;
        user.passwordHash = passwordHash;
        user.phone = phone;
        user.birthDate = birthDate;
        user.photoUrl = photoUrl;
        user.authProvider = authProvider;
        user.googleId = googleId;
        user.emailVerified = emailVerified;
        user.active = active;
        user.createdAt = createdAt;
        user.updatedAt = updatedAt;
        return user;
    }

    public void verifyEmail() {
        this.emailVerified = true;
        this.updatedAt = Instant.now();
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
        this.updatedAt = Instant.now();
    }

    public void linkGoogleAccount(String googleId) {
        this.googleId = googleId;
        this.updatedAt = Instant.now();
    }

    public boolean hasIncompleteProfile() {
        return phone == null;
    }

    public UUID getId() { return id; }
    public Name getName() { return name; }
    public Email getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public Phone getPhone() { return phone; }
    public LocalDate getBirthDate() { return birthDate; }
    public String getPhotoUrl() { return photoUrl; }
    public AuthProvider getAuthProvider() { return authProvider; }
    public String getGoogleId() { return googleId; }
    public boolean isEmailVerified() { return emailVerified; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
