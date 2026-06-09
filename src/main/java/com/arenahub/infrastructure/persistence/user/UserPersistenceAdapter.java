package com.arenahub.infrastructure.persistence.user;

import com.arenahub.domain.user.User;
import com.arenahub.domain.user.UserRepository;
import com.arenahub.domain.user.vo.Email;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class UserPersistenceAdapter implements UserRepository {

    private final UserJpaRepository jpa;

    public UserPersistenceAdapter(UserJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpa.findById(id).map(UserJpaEntity::toDomain);
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return jpa.findByEmailAndDeletedAtIsNull(email.value()).map(UserJpaEntity::toDomain);
    }

    @Override
    public Optional<User> findByGoogleId(String googleId) {
        return jpa.findByGoogleIdAndDeletedAtIsNull(googleId).map(UserJpaEntity::toDomain);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return jpa.existsByEmailAndDeletedAtIsNull(email.value());
    }

    @Override
    public User save(User user) {
        return jpa.save(UserJpaEntity.fromDomain(user)).toDomain();
    }
}
