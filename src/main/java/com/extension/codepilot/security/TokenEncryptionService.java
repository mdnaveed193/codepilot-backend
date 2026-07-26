package com.extension.codepilot.security;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TokenEncryptionService {

	private static final String ALGORITHM = "AES/GCM/NoPadding";

	private static final int IV_LENGTH_BYTES = 12;

	private static final int AUTH_TAG_LENGTH_BITS = 128;

	private final SecretKeySpec secretKey;
	private final SecureRandom secureRandom = new SecureRandom();

	public TokenEncryptionService(@Value("${github.token.encryption-key}") String base64EncryptionKey) {

		byte[] keyBytes;

		try {
			keyBytes = Base64.getDecoder().decode(base64EncryptionKey);
		} catch (IllegalArgumentException exception) {
			throw new IllegalStateException("GitHub token encryption key must be valid Base64", exception);
		}

		if (keyBytes.length != 32) {
			throw new IllegalStateException("GitHub token encryption key must decode to exactly 32 bytes");
		}

		this.secretKey = new SecretKeySpec(keyBytes, "AES");
	}

	public String encrypt(String plainToken) {

		if (plainToken == null || plainToken.isBlank()) {
			throw new IllegalArgumentException("GitHub access token cannot be empty");
		}

		try {

			byte[] iv = new byte[IV_LENGTH_BYTES];
			secureRandom.nextBytes(iv);

			Cipher cipher = Cipher.getInstance(ALGORITHM);

			GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(AUTH_TAG_LENGTH_BITS, iv);

			cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec);

			byte[] encryptedToken = cipher.doFinal(plainToken.getBytes(StandardCharsets.UTF_8));

			ByteBuffer combinedBuffer = ByteBuffer.allocate(iv.length + encryptedToken.length);

			combinedBuffer.put(iv);
			combinedBuffer.put(encryptedToken);

			return Base64.getEncoder().encodeToString(combinedBuffer.array());

		} catch (Exception exception) {
			throw new IllegalStateException("Failed to encrypt GitHub access token", exception);
		}
	}

	public String decrypt(String encryptedValue) {

		if (encryptedValue == null || encryptedValue.isBlank()) {
			throw new IllegalArgumentException("Encrypted GitHub access token cannot be empty");
		}

		try {

			byte[] combinedBytes = Base64.getDecoder().decode(encryptedValue);

			if (combinedBytes.length <= IV_LENGTH_BYTES) {
				throw new IllegalArgumentException("Invalid encrypted GitHub access token");
			}

			ByteBuffer combinedBuffer = ByteBuffer.wrap(combinedBytes);

			byte[] iv = new byte[IV_LENGTH_BYTES];
			combinedBuffer.get(iv);

			byte[] encryptedToken = new byte[combinedBuffer.remaining()];

			combinedBuffer.get(encryptedToken);

			Cipher cipher = Cipher.getInstance(ALGORITHM);

			GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(AUTH_TAG_LENGTH_BITS, iv);

			cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec);

			byte[] decryptedToken = cipher.doFinal(encryptedToken);

			return new String(decryptedToken, StandardCharsets.UTF_8);

		} catch (IllegalArgumentException exception) {
			throw exception;

		} catch (Exception exception) {
			throw new IllegalStateException("Failed to decrypt GitHub access token", exception);
		}
	}
}