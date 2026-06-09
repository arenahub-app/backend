package com.arenahub.infrastructure.security;

import com.arenahub.domain.user.User;
import com.arenahub.domain.user.UserRepository;
import com.arenahub.domain.user.vo.Email;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(new Email(email))
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));

        return new org.springframework.security.core.userdetails.User(
                user.getId().toString(),
                user.getPasswordHash() != null ? user.getPasswordHash() : "",
                user.isActive(),
                true, true,
                !user.isActive(),
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
