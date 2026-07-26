package com.extension.codepilot.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.extension.codepilot.dao.UserDao;
import com.extension.codepilot.dto.GithubUserDto;
import com.extension.codepilot.dto.OAuthCodeData;
import com.extension.codepilot.entity.User;
import com.extension.codepilot.security.TokenEncryptionService;

import java.security.SecureRandom;

@Service
public class AuthService {

	@Autowired
	private TokenEncryptionService encryptionService;

	@Autowired
	private UserDao userDao;

	@Value("${oauth.code.expiration}")
	private long oauthCodeExpiration;

	private final Map<String, OAuthCodeData> oneTimeCodeStore = new ConcurrentHashMap<>();

	private final SecureRandom secureRandom = new SecureRandom();

	public User saveOrUpdateUser(GithubUserDto githubUserDto) {

		User user = userDao.findByGithubId(githubUserDto.getGithubId());

		if (user == null) {
			user = new User();
			user.setGithubId(githubUserDto.getGithubId());
			user.setCreatedAt(LocalDateTime.now());
		}

		user.setGithubUsername(githubUserDto.getGithubUsername());
		user.setName(githubUserDto.getName());
		user.setEmail(githubUserDto.getEmail());
		user.setAvatarUrl(githubUserDto.getAvatar_url());
		String encryptedAccessToken = encryptionService.encrypt(githubUserDto.getAccessToken());

		user.setAccessToken(encryptedAccessToken);
		user.setUpdatedAt(LocalDateTime.now());

		return userDao.save(user);

	}

	public OAuthCodeData exchangeOneTimeCode(String code) {

		if (code == null || code.isBlank()) {
			return null;
		}

		OAuthCodeData oauthCodeData = oneTimeCodeStore.remove(code);

		if (oauthCodeData == null) {
			return null;
		}

		if (LocalDateTime.now().isAfter(oauthCodeData.getExpiresAt())) {

			return null;
		}

		return oauthCodeData;

	}

	public String createOneTimeCode(String jwt, String username) {
		String code = UUID.randomUUID().toString();

		LocalDateTime expiresAt = LocalDateTime.now().plusNanos(oauthCodeExpiration * 1_000_000);

		oneTimeCodeStore.put(code, new OAuthCodeData(jwt, username, expiresAt));

		return code;
	}

}
