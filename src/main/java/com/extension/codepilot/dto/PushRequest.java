package com.extension.codepilot.dto;

import lombok.Data;

@Data
public class PushRequest {
	private String platform;
	private String filename;
	private String problemTitle;
	private String problemSlug;
	private String difficulty;
	private String description;
	private String language;
	private String code;
	private String submittedAt;
	private Long repositoryId;
	private String repositoryName;
	private Boolean isPrivate;
	private String problemUrl;
}
