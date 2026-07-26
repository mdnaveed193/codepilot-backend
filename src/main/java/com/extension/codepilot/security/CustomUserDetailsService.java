package com.extension.codepilot.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.extension.codepilot.dao.UserDao;
import com.extension.codepilot.entity.User;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	@Autowired
	private UserDao userDao;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userDao.findByGithubUsername(username);

		if (user == null) {
			throw new UsernameNotFoundException("User Not Found");
		}

		return new CustomUserDetails(user);
	}

	public UserDetails loadUserByGithubId(Long githubId) {

		User user = userDao.findByGithubId(githubId);

		if (user == null) {
			throw new UsernameNotFoundException("User Not Found");
		}

		return new CustomUserDetails(user);
	}

}
