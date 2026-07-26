package com.extension.codepilot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GithubRepoResp {

	private Long githubRepositoryId;

	private String repositoryName;

	private Boolean isPrivate;

	private String htmlUrl;
}
