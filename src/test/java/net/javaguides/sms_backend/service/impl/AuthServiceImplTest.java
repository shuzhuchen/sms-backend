package net.javaguides.sms_backend.service.impl;

import net.javaguides.sms_backend.dto.LoginRequest;
import net.javaguides.sms_backend.dto.LoginResponse;
import net.javaguides.sms_backend.entity.AppUser;
import net.javaguides.sms_backend.repository.AppUserRepository;
import net.javaguides.sms_backend.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AppUserRepository appUserRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JwtService jwtService = new JwtService("test-jwt-secret-with-enough-length", 3600);

    @Test
    void loginReturnsBearerTokenForValidCredentials() {
        AppUser user = new AppUser(
                1L,
                "local",
                "admin",
                "admin@local.test",
                "admin",
                passwordEncoder.encode("admin123"),
                "Demo Admin",
                "ROLE_ADMIN"
        );
        when(appUserRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        AuthServiceImpl authService = new AuthServiceImpl(appUserRepository, passwordEncoder, jwtService);

        LoginResponse response = authService.login(new LoginRequest("admin", "admin123"));

        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.username()).isEqualTo("admin");
        assertThat(jwtService.isValid(response.token())).isTrue();
        assertThat(jwtService.getUsername(response.token())).isEqualTo("admin");
        assertThat(jwtService.getRole(response.token())).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void loginRejectsInvalidPassword() {
        AppUser user = new AppUser(
                1L,
                "local",
                "admin",
                "admin@local.test",
                "admin",
                passwordEncoder.encode("admin123"),
                "Demo Admin",
                "ROLE_ADMIN"
        );
        when(appUserRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        AuthServiceImpl authService = new AuthServiceImpl(appUserRepository, passwordEncoder, jwtService);

        assertThatThrownBy(() -> authService.login(new LoginRequest("admin", "wrong")))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid username or password");
    }
}
