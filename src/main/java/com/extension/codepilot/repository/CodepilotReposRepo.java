package com.extension.codepilot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.extension.codepilot.entity.CodepilotRepos;

public interface CodepilotReposRepo extends JpaRepository<CodepilotRepos, Long> {
	
	List<CodepilotRepos> findByUserId(Long userId);

    CodepilotRepos findByUserIdAndGithubRepositoryId(Long userId,Long githubRepositoryId);

    CodepilotRepos findByUserIdAndRepositoryName(Long userId,String repositoryName);

}
