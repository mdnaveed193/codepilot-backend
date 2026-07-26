package com.extension.codepilot.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "codepilot_repositories")
@Data
public class CodepilotRepos {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private Long githubRepositoryId;

	@Column(nullable = false)
	private String repositoryName;

	@Column(nullable = false)
	private Boolean isPrivate;

	private String htmlUrl;

	private String defaultBranch;
	

    private LocalDateTime createdAt;

    
    // Many repositories can belong to one user.
     
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
