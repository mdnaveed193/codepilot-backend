package com.extension.codepilot.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.extension.codepilot.dto.CreateRepoRequest;
import com.extension.codepilot.dto.GithubRepoResp;
import com.extension.codepilot.dto.PushRequest;
import com.extension.codepilot.dto.RepoCheckResp;
import com.extension.codepilot.service.GithubPushService;

@RestController
@RequestMapping("/api/github")
@CrossOrigin(origins = { "http://localhost:5173", "chrome-extension://nlgnidgnhegkeekklfcclkoiccafodan" })
public class GithubController {

	@Autowired
	private GithubPushService githubPushService;

	@PostMapping("/push")
	public ResponseEntity<?> pushSolution(@RequestBody PushRequest pushRequest, Authentication authentication) {

		System.out.println("push api is called");

		return githubPushService.pushSolution(pushRequest, authentication);
	}

	@GetMapping("/repos/check")
	public RepoCheckResp checkRepositoryAvailability(@RequestParam String repositoryName,
			Authentication authentication) {

		return githubPushService.checkRepositoryAvailability(repositoryName, authentication);

	}

	@GetMapping("/repos")
	public List<GithubRepoResp> getUserRepositories(Authentication authentication) {

		return githubPushService.getUserRepositories(authentication);
	}

	@PostMapping("/repos")
	public GithubRepoResp createRepository(@RequestBody CreateRepoRequest request, Authentication authentication) {

		return githubPushService.createRepository(request, authentication);

	}
}