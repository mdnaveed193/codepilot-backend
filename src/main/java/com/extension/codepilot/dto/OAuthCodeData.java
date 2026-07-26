package com.extension.codepilot.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OAuthCodeData {
    private String token;
    private String username;

    private LocalDateTime expiresAt;
}