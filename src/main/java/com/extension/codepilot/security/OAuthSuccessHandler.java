package com.extension.codepilot.security;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.extension.codepilot.dto.GithubUserDto;
import com.extension.codepilot.entity.User;
import com.extension.codepilot.service.AuthService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class OAuthSuccessHandler implements AuthenticationSuccessHandler {

	@Autowired
	private AuthService authService;

	@Autowired
	private OAuth2AuthorizedClientService authorizedClientService;

	@Autowired
	private JwtUtil jwtUtil;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {

		OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;

		OAuth2User oAuth2User = token.getPrincipal();

		Map<String, Object> attributes = oAuth2User.getAttributes();

		OAuth2AuthorizedClient authorizedClient = authorizedClientService
				.loadAuthorizedClient(token.getAuthorizedClientRegistrationId(), token.getName());

		if (authorizedClient == null || authorizedClient.getAccessToken() == null) {

			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "GitHub authentication failed");

			return;
		}

		String accessToken = authorizedClient.getAccessToken().getTokenValue();
		GithubUserDto githubUserDto = new GithubUserDto();

		githubUserDto.setGithubId(Long.parseLong(attributes.get("id").toString()));

		githubUserDto.setGithubUsername(attributes.get("login").toString());

		githubUserDto.setAvatar_url(attributes.get("avatar_url").toString());

		githubUserDto.setAccessToken(accessToken);

		Object name = attributes.get("name");

		if (name == null) {
			githubUserDto.setName(attributes.get("login").toString());
		} else {
			githubUserDto.setName(name.toString());
		}

		Object email = attributes.get("email");

		if (email != null) {
			githubUserDto.setEmail(email.toString());
		}

		User user = authService.saveOrUpdateUser(githubUserDto);
		String jwt = jwtUtil.generateToken(user.getGithubId());
		String oneTimeCode = authService.createOneTimeCode(jwt, user.getGithubUsername());

		String redirectUrl = "http://localhost:5173/#/oauth/success" + "?code="
				+ URLEncoder.encode(oneTimeCode, StandardCharsets.UTF_8);

		response.sendRedirect(redirectUrl);

	}

}
