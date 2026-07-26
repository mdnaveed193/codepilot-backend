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

	/*
	 * AES = encryption algorithm GCM = mode that provides encryption and tamper
	 * detection NoPadding = GCM does not need traditional padding
	 */
	private static final String ALGORITHM = "AES/GCM/NoPadding";

	/*
	 * GCM commonly uses a 12-byte IV. A new IV is generated for every encryption.
	 */
	private static final int IV_LENGTH_BYTES = 12;

	/*
	 * GCM authentication tag length. The tag helps detect if encrypted data was
	 * modified.
	 */
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

		/*
		 * We generated a 32-byte key, so verify that the configured value still decodes
		 * to exactly 32 bytes.
		 */
		if (keyBytes.length != 32) {
			throw new IllegalStateException("GitHub token encryption key must decode to exactly 32 bytes");
		}

		this.secretKey = new SecretKeySpec(keyBytes, "AES");
	}

	/*
	 * Parameter: plainToken = original GitHub access token.
	 *
	 * Return: Base64 text containing IV + encrypted token.
	 */
	public String encrypt(String plainToken) {

		if (plainToken == null || plainToken.isBlank()) {
			throw new IllegalArgumentException("GitHub access token cannot be empty");
		}

		try {
			/*
			 * Generate a different random IV for every token encryption.
			 */
			byte[] iv = new byte[IV_LENGTH_BYTES];
			secureRandom.nextBytes(iv);

			Cipher cipher = Cipher.getInstance(ALGORITHM);

			GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(AUTH_TAG_LENGTH_BITS, iv);

			cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec);

			byte[] encryptedToken = cipher.doFinal(plainToken.getBytes(StandardCharsets.UTF_8));

			/*
			 * Put the IV first and the encrypted token after it.
			 *
			 * Final byte structure:
			 *
			 * [12-byte IV][encrypted token + authentication tag]
			 */
			ByteBuffer combinedBuffer = ByteBuffer.allocate(iv.length + encryptedToken.length);

			combinedBuffer.put(iv);
			combinedBuffer.put(encryptedToken);

			/*
			 * Convert binary bytes into normal text that can be stored safely in a VARCHAR
			 * or TEXT database column.
			 */
			return Base64.getEncoder().encodeToString(combinedBuffer.array());

		} catch (Exception exception) {
			throw new IllegalStateException("Failed to encrypt GitHub access token", exception);
		}
	}

	/*
	 * Parameter: encryptedValue = encrypted text loaded from the database.
	 *
	 * Return: Original GitHub access token.
	 */
	public String decrypt(String encryptedValue) {

		if (encryptedValue == null || encryptedValue.isBlank()) {
			throw new IllegalArgumentException("Encrypted GitHub access token cannot be empty");
		}

		try {
			/*
			 * Convert the Base64 database text back into bytes.
			 */
			byte[] combinedBytes = Base64.getDecoder().decode(encryptedValue);

			if (combinedBytes.length <= IV_LENGTH_BYTES) {
				throw new IllegalArgumentException("Invalid encrypted GitHub access token");
			}

			ByteBuffer combinedBuffer = ByteBuffer.wrap(combinedBytes);

			/*
			 * The first 12 bytes are the IV.
			 */
			byte[] iv = new byte[IV_LENGTH_BYTES];
			combinedBuffer.get(iv);

			/*
			 * Everything after the IV is the encrypted token and its GCM authentication
			 * tag.
			 */
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