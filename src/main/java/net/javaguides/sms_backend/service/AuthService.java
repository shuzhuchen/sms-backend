package net.javaguides.sms_backend.service;

import net.javaguides.sms_backend.dto.LoginRequest;
import net.javaguides.sms_backend.dto.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
}
