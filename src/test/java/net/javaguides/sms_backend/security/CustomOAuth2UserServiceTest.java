package net.javaguides.sms_backend.security;

import net.javaguides.sms_backend.entity.AppUser;
import net.javaguides.sms_backend.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Test
    void loadUserCreatesLocalUserWhenGoogleUserIsNew() {
        OAuth2User googleUser = googleUser("google-sub", "ada@example.com", "Ada Lovelace");
        TestCustomOAuth2UserService service = new TestCustomOAuth2UserService(appUserRepository, googleUser);
        when(appUserRepository.findByProviderAndProviderId("google", "google-sub")).thenReturn(Optional.empty());
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(invocation -> {
            AppUser user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        OAuth2User result = service.loadUser(userRequest());

        ArgumentCaptor<AppUser> userCaptor = ArgumentCaptor.forClass(AppUser.class);
        verify(appUserRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getProvider()).isEqualTo("google");
        assertThat(userCaptor.getValue().getProviderId()).isEqualTo("google-sub");
        assertThat(userCaptor.getValue().getEmail()).isEqualTo("ada@example.com");
        assertThat(userCaptor.getValue().getName()).isEqualTo("Ada Lovelace");
        assertThat(userCaptor.getValue().getRole()).isEqualTo("ROLE_USER");
        assertThat(result.getAuthorities()).extracting(GrantedAuthority::getAuthority).containsExactly("ROLE_USER");
        assertThat(result.getName()).isEqualTo("ada@example.com");
        assertThat((String) result.getAttribute("name")).isEqualTo("Ada Lovelace");
    }

    @Test
    void loadUserUsesExistingLocalUserRole() {
        OAuth2User googleUser = googleUser("google-sub", "admin@example.com", "Admin User");
        TestCustomOAuth2UserService service = new TestCustomOAuth2UserService(appUserRepository, googleUser);
        AppUser existingUser = new AppUser(1L, "google", "google-sub", "admin@example.com", null, null, "Admin User", "ROLE_ADMIN");
        when(appUserRepository.findByProviderAndProviderId("google", "google-sub")).thenReturn(Optional.of(existingUser));

        OAuth2User result = service.loadUser(userRequest());

        verify(appUserRepository, never()).save(any());
        assertThat(result.getAuthorities()).extracting(GrantedAuthority::getAuthority).containsExactly("ROLE_ADMIN");
        String email = result.getAttribute("email");
        assertThat(email).isEqualTo("admin@example.com");
        assertThat((String) result.getAttribute("name")).isEqualTo("Admin User");
    }

    private static OAuth2User googleUser(String subject, String email, String name) {
        return new DefaultOAuth2User(
                java.util.List.of(),
                Map.of("sub", subject, "email", email, "name", name),
                "sub"
        );
    }

    private static OAuth2UserRequest userRequest() {
        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId("google")
                .clientId("client-id")
                .clientSecret("client-secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://localhost/login/oauth2/code/google")
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .tokenUri("https://www.googleapis.com/oauth2/v4/token")
                .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                .userNameAttributeName("sub")
                .scope("openid", "profile", "email")
                .build();

        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "token",
                Instant.now(),
                Instant.now().plusSeconds(60)
        );

        return new OAuth2UserRequest(clientRegistration, accessToken);
    }

    private static class TestCustomOAuth2UserService extends CustomOAuth2UserService {
        private final OAuth2User oauth2User;

        private TestCustomOAuth2UserService(AppUserRepository appUserRepository, OAuth2User oauth2User) {
            super(appUserRepository);
            this.oauth2User = oauth2User;
        }

        @Override
        protected OAuth2User fetchOAuth2User(OAuth2UserRequest userRequest) {
            return oauth2User;
        }
    }
}
