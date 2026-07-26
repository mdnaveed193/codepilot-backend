package com.extension.codepilot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RepoCheckResp {

	 private String repositoryName;

	    private Boolean available;

	    private String message;
}
