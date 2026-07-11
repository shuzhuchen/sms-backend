package net.javaguides.sms_backend.security;

import net.javaguides.sms_backend.entity.AppUser;
import net.javaguides.sms_backend.repository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger log = LoggerFactory.getLogger(CustomOAuth2UserService.class);
    private static final String DEFAULT_ROLE = "ROLE_USER";

    private final AppUserRepository appUserRepository;

    public CustomOAuth2UserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = fetchOAuth2User(userRequest);
        String provider = userRequest.getClientRegistration().getRegistrationId();
        String providerId = oauth2User.getName();
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");

        AppUser localUser = appUserRepository.findByProviderAndProviderId(provider, providerId)
                .map(user -> {
                    log.info("Loaded existing OAuth2 user provider={} providerId={}", provider, providerId);
                    return user;
                })
                .orElseGet(() -> {
                    AppUser createdUser = appUserRepository.save(new AppUser(null, provider, providerId, email, null, null, name, DEFAULT_ROLE));
                    log.info("Created OAuth2 user provider={} providerId={}", provider, providerId);
                    return createdUser;
                });

        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority(localUser.getRole())),
                oauth2User.getAttributes(),
                email == null || email.isBlank() ? "sub" : "email"
        );
    }

    protected OAuth2User fetchOAuth2User(OAuth2UserRequest userRequest) {
        return super.loadUser(userRequest);
    }
}
