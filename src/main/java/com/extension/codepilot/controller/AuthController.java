package com.extension.codepilot.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.extension.codepilot.dto.OAuthCodeData;
import com.extension.codepilot.service.AuthService;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = { "http://localhost:5173", "chrome-extension://nlgnidgnhegkeekklfcclkoiccafodan" })
public class AuthController {

	@Autowired
	private AuthService authService;

	@PostMapping("/exchange-code")
	public ResponseEntity<Map<String, String>> exchangeOneTimeCode(@RequestBody Map<String, String> request) {

		String code = request.get("code");

		if (code == null || code.isBlank()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "OAuth code is required"));
		}

		OAuthCodeData oauthCodeData = authService.exchangeOneTimeCode(code);

		if (oauthCodeData == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("message", "OAuth code is invalid, expired, or already used"));
		}

		return ResponseEntity.ok(Map.of("token", oauthCodeData.getToken(), "username", oauthCodeData.getUsername()));
	}
}
