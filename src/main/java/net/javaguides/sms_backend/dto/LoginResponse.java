package net.javaguides.sms_backend.dto;

public record LoginResponse(String token, String tokenType, String username) {
}
