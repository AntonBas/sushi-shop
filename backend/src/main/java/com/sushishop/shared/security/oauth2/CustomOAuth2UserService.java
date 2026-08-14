package com.sushishop.shared.security.oauth2;

import com.sushishop.user.User;
import com.sushishop.shared.enums.UserRole;
import com.sushishop.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) {
        var oauthUser = super.loadUser(request);
        var attributes = oauthUser.getAttributes();
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");

        var user = userRepository.findByEmail(email).orElseGet(() -> {
            var newUser = User.builder()
                    .name(name)
                    .email(email)
                    .phone("")
                    .userRole(UserRole.CUSTOMER)
                    .emailVerified(true)
                    .tokenVersion(0)
                    .build();
            return userRepository.save(newUser);
        });

        if (!user.isEmailVerified()) {
            user.setEmailVerified(true);
            userRepository.save(user);
        }

        return oauthUser;
    }
}