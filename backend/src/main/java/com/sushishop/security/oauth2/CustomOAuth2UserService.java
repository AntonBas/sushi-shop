package com.sushishop.security.oauth2;

import com.sushishop.domain.User;
import com.sushishop.domain.enums.UserRole;
import com.sushishop.repository.UserRepository;
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
            var newUser = User.builder().name(name).email(email).userRole(UserRole.CUSTOMER).build();
            return userRepository.save(newUser);
        });

        return oauthUser;
    }

}
