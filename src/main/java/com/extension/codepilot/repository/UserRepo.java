package com.extension.codepilot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.extension.codepilot.entity.User;

public interface UserRepo extends JpaRepository<User, Long> {

	User findByGithubId(Long githubId);

	User findByGithubUsername(String githubUsername);
}
