package net.javaguides.sms_backend.service.impl;

import lombok.AllArgsConstructor;
import net.javaguides.sms_backend.dto.LoginRequest;
import net.javaguides.sms_backend.dto.LoginResponse;
import net.javaguides.sms_backend.entity.AppUser;
import net.javaguides.sms_backend.repository.AppUserRepository;
import net.javaguides.sms_backend.security.JwtService;
import net.javaguides.sms_backend.service.AuthService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public LoginResponse login(LoginRequest request) {
        AppUser user = appUserRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        return new LoginResponse(jwtService.generateToken(user.getUsername(), user.getRole()), "Bearer", user.getUsername());
    }
}
