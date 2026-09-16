package com.sushishop.security.oauth2;

import com.sushishop.user.User;
import com.sushishop.user.UserRepository;
import com.sushishop.user.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final String UNVERIFIED_EMAIL_ERROR_CODE = "unverified_email";
    private static final String GOOGLE_SIGN_IN_DISABLED_ERROR_CODE = "google_sign_in_disabled";

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) {
        var oauthUser = super.loadUser(request);
        linkOrCreateUser(oauthUser);
        return oauthUser;
    }

    /**
     * A user row can already exist under this email with emailVerified=false —
     * e.g. someone registered with this address via the password flow but never
     * owned the inbox (attacker pre-registration), or the account is mid
     * email-verification. Google just proved whoever is signing in now DOES own
     * this inbox, so this claims that row. Since the row's existing password
     * may have been set by someone else entirely, it's cleared and tokenVersion
     * bumped (same as password reset) to invalidate it and any of its sessions —
     * otherwise whoever set that password keeps standing access to this account
     * after the real owner starts using Google sign-in.
     */
    void linkOrCreateUser(OAuth2User oauthUser) {
        var attributes = oauthUser.getAttributes();
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");

        if (!Boolean.TRUE.equals(attributes.get("email_verified"))) {
            OAuth2Error error = new OAuth2Error(UNVERIFIED_EMAIL_ERROR_CODE, "Google account email is not verified", null);
            throw new OAuth2AuthenticationException(error, error.toString());
        }

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

        if (!user.isGoogleSignInEnabled()) {
            OAuth2Error error = new OAuth2Error(GOOGLE_SIGN_IN_DISABLED_ERROR_CODE,
                    "Google sign-in is disabled for this account. Please log in with your password.", null);
            throw new OAuth2AuthenticationException(error, error.toString());
        }

        if (!user.isEmailVerified()) {
            user.setEmailVerified(true);
            user.setPassword(null);
            user.setTokenVersion(user.getTokenVersion() + 1);
            userRepository.save(user);
        }
    }
}