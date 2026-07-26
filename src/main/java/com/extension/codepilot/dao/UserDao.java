package com.extension.codepilot.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.extension.codepilot.entity.User;
import com.extension.codepilot.repository.UserRepo;

@Repository
public class UserDao {

	@Autowired
	private UserRepo userRepo;

	public User save(User user) {
		return userRepo.save(user);
	}

	public User findByGithubId(Long githubId) {
		return userRepo.findByGithubId(githubId);
	}

	public User findByGithubUsername(String username) {
		return userRepo.findByGithubUsername(username);
	}

	public User findById(Long id) {

		return userRepo.findById(id).orElse(null);

	}
}
