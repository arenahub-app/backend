package com.arenahub.domain.user;

import com.arenahub.domain.user.vo.Email;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(Email email);

    Optional<User> findByGoogleId(String googleId);

    boolean existsByEmail(Email email);

    User save(User user);
}
