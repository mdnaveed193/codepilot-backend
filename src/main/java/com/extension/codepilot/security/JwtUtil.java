package com.extension.codepilot.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

	@Value("${jwt.secret}")
	private String secret;

	@Value("${jwt.expiration}")
	private long expiration;

	private SecretKey getSigningKey() {
		return Keys.hmacShaKeyFor(secret.getBytes());
	}

	public String generateToken(Long githubId) {

		Date currentDate = new Date();
		Date expiryDate = new Date(currentDate.getTime() + expiration);

		return Jwts.builder().claim("githubId", githubId).issuedAt(currentDate).expiration(expiryDate)
				.signWith(getSigningKey()) // HS256 is automatically selected
				.compact();
	}

	private Claims extractAllClaims(String token) {

		return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
	}

	public Long extractUserId(String token) {

		Claims claims = extractAllClaims(token);

		return claims.get("githubId", Long.class);
	}

	public boolean validateToken(String token) {

		try {

			Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);

			return true;

		} catch (Exception exception) {

			return false;
		}
	}

}