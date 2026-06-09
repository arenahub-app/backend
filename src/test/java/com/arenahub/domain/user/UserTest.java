package com.arenahub.domain.user;

import com.arenahub.domain.user.vo.AuthProvider;
import com.arenahub.domain.user.vo.Email;
import com.arenahub.domain.user.vo.Name;
import com.arenahub.domain.user.vo.Phone;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

class UserTest {

    @Test
    void registerLocal_setsCorrectDefaults() {
        User user = User.registerLocal(
                new Name("João Silva"),
                new Email("joao@example.com"),
                "hashed-password",
                new Phone("11987654321"),
                LocalDate.of(1990, 1, 1)
        );

        assertThat(user.getId()).isNotNull();
        assertThat(user.getEmail().value()).isEqualTo("joao@example.com");
        assertThat(user.getAuthProvider()).isEqualTo(AuthProvider.LOCAL);
        assertThat(user.isEmailVerified()).isFalse();
        assertThat(user.isActive()).isTrue();
        assertThat(user.getGoogleId()).isNull();
        assertThat(user.hasIncompleteProfile()).isFalse();
    }

    @Test
    void fromGoogle_setsEmailVerifiedAndNoPassword() {
        User user = User.fromGoogle("google-123", new Name("Maria"), new Email("maria@gmail.com"));

        assertThat(user.getAuthProvider()).isEqualTo(AuthProvider.GOOGLE);
        assertThat(user.isEmailVerified()).isTrue();
        assertThat(user.getPasswordHash()).isNull();
        assertThat(user.getGoogleId()).isEqualTo("google-123");
    }

    @Test
    void fromGoogle_hasIncompleteProfile_whenPhoneIsNull() {
        User user = User.fromGoogle("google-123", new Name("Maria"), new Email("maria@gmail.com"));
        assertThat(user.hasIncompleteProfile()).isTrue();
    }

    @Test
    void verifyEmail_setsEmailVerifiedTrue() {
        User user = User.registerLocal(
                new Name("João"), new Email("j@example.com"), "hash",
                new Phone("11999999999"), LocalDate.of(2000, 1, 1));

        user.verifyEmail();

        assertThat(user.isEmailVerified()).isTrue();
    }

    @Test
    void setPasswordHash_updatesHash() {
        User user = User.registerLocal(
                new Name("João"), new Email("j@example.com"), "old-hash",
                new Phone("11999999999"), LocalDate.of(2000, 1, 1));

        user.setPasswordHash("new-hash");

        assertThat(user.getPasswordHash()).isEqualTo("new-hash");
    }

    @Test
    void email_normalizesToLowercase() {
        Email email = new Email("Test@Example.COM");
        assertThat(email.value()).isEqualTo("test@example.com");
    }

    @Test
    void email_rejectsInvalidFormat() {
        assertThatThrownBy(() -> new Email("not-an-email"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void phone_removesNonDigits() {
        Phone phone = new Phone("(11) 98765-4321");
        assertThat(phone.value()).isEqualTo("11987654321");
    }

    @Test
    void phone_rejectsTooShort() {
        assertThatThrownBy(() -> new Phone("12345"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void name_rejectsTooShort() {
        assertThatThrownBy(() -> new Name("A"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
