package net.javaguides.sms_backend.config;

import net.javaguides.sms_backend.entity.AppUser;
import net.javaguides.sms_backend.repository.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DemoUserConfig {

    @Bean
    public CommandLineRunner demoUserInitializer(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.demo-user.username:admin}") String username,
            @Value("${app.demo-user.password:admin123}") String password
    ) {
        return args -> appUserRepository.findByUsername(username)
                .orElseGet(() -> appUserRepository.save(new AppUser(
                        null,
                        "local",
                        username,
                        username + "@local.test",
                        username,
                        passwordEncoder.encode(password),
                        "Demo Admin",
                        "ROLE_ADMIN"
                )));
    }
}
