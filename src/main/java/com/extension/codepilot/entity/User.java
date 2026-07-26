package com.extension.codepilot.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // GitHub's permanent user ID
    @Column(unique = true)
    private Long githubId;

    @Column(unique = true)
    private String githubUsername;

    private String name;
	private String email;
	private String avatar;
	
	private String avatarUrl;

    @Lob
    private String accessToken;

    
//    private String repositoryName;
//
//    private String defaultBranch;
//
//    private Boolean repositoryCreated;
    
    @OneToMany(mappedBy = "user",cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    private List<CodepilotRepos> repositories = new ArrayList<>();
    
   

//    private String installationType;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
