package com.sushishop.user;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Nonnull
    public UserDetails loadUserByUsername(@Nonnull String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email).map(CustomUserDetails::new).orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }
}
