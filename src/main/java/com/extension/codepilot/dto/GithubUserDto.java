package com.extension.codepilot.dto;

import lombok.Data;

@Data
public class GithubUserDto {
	 private Long githubId;

	    private String githubUsername;

	    private String name;

	    private String email;

	    private String avatar_url;

	    private String accessToken;
}
