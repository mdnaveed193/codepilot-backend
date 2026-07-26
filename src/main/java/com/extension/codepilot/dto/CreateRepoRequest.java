package com.extension.codepilot.dto;

import lombok.Data;

@Data
public class CreateRepoRequest {
	
	private String repositoryName;
	
	private Boolean isPrivate;

}
