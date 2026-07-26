package com.extension.codepilot.util;

import java.security.SecureRandom;
import java.util.Base64;

public class EncryptionKeyGenerator {

	public static void main(String[] args) {

		// 32 bytes = 256 bits for AES-256.
		byte[] keyBytes = new byte[32];

		SecureRandom secureRandom = new SecureRandom();
		secureRandom.nextBytes(keyBytes);

		String base64Key = Base64.getEncoder().encodeToString(keyBytes);

	}
}
