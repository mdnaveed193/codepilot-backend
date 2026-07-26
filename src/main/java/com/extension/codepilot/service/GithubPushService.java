package com.extension.codepilot.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.extension.codepilot.dto.CreateRepoRequest;
import com.extension.codepilot.dto.GithubRepoResp;
import com.extension.codepilot.dto.PushRequest;
import com.extension.codepilot.dto.RepoCheckResp;
import com.extension.codepilot.entity.CodepilotRepos;
import com.extension.codepilot.entity.User;
import com.extension.codepilot.repository.CodepilotReposRepo;
import com.extension.codepilot.security.CustomUserDetails;
import com.extension.codepilot.security.TokenEncryptionService;

@Service
public class GithubPushService {

	@Autowired
	private TokenEncryptionService encryptionService;

	@Autowired
	private CodepilotReposRepo codepilotReposRepo;

	@Autowired
	private GithubApiService githubApiService;

	@Autowired
	private ReadmeService readmeService;

	private String getDecryptedGithubAccessToken(User user) {
		return encryptionService.decrypt(user.getAccessToken());
	}

	public GithubRepoResp createRepository(CreateRepoRequest request, Authentication authentication) {

		CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

		User user = userDetails.getUser();

		String repositoryName = request.getRepositoryName();

		if (repositoryName == null || repositoryName.isBlank()) {
			throw new RuntimeException("Repository name is required");
		}

		repositoryName = repositoryName.trim();
		if (!isValidRepositoryName(repositoryName)) {
			throw new RuntimeException("Invalid repository name");
		}

		CodepilotRepos existingRepository = codepilotReposRepo.findByUserIdAndRepositoryName(user.getId(),
				repositoryName);

		if (existingRepository != null) {
			throw new RuntimeException("Repository is already registered with CodePilot");
		}

		boolean existsOnGithub = githubApiService.repositoryExists(getDecryptedGithubAccessToken(user),
				user.getGithubUsername(), repositoryName);

		if (existsOnGithub) {
			throw new RuntimeException("Repository already exists on GitHub");
		}

		boolean isPrivate = Boolean.TRUE.equals(request.getIsPrivate());

		Map<String, Object> createdRepository = githubApiService.createRepository(getDecryptedGithubAccessToken(user),
				repositoryName, isPrivate);

		if (createdRepository == null || createdRepository.get("id") == null) {
			throw new RuntimeException("GitHub repository creation failed");
		}

		CodepilotRepos repository = new CodepilotRepos();
		repository.setGithubRepositoryId(Long.valueOf(createdRepository.get("id").toString()));

		repository.setRepositoryName(createdRepository.get("name").toString());
		repository.setIsPrivate(Boolean.valueOf(createdRepository.get("private").toString()));
		repository.setHtmlUrl(createdRepository.get("html_url").toString());

		Object defaultBranch = createdRepository.get("default_branch");

		if (defaultBranch != null) {
			repository.setDefaultBranch(defaultBranch.toString());
		}

		repository.setCreatedAt(LocalDateTime.now());

		repository.setUser(user);

		codepilotReposRepo.save(repository);

		return new GithubRepoResp(repository.getGithubRepositoryId(), repository.getRepositoryName(),
				repository.getIsPrivate(), repository.getHtmlUrl());

	}

	public ResponseEntity<?> pushSolution(PushRequest pushRequest, Authentication authentication) {

		CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

		User user = userDetails.getUser();

		String accessToken = getDecryptedGithubAccessToken(user);

		String githubUsername = user.getGithubUsername();

		Long repositoryId = pushRequest.getRepositoryId();

		if (repositoryId == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("code", "REPOSITORY_ID_REQUIRED",

					"message", "Please configure a repository before pushing.",

					"repositoryConfigurationRequired", true));
		}

		CodepilotRepos repository = codepilotReposRepo.findByUserIdAndGithubRepositoryId(user.getId(), repositoryId);

		if (repository == null) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("code", "REPOSITORY_CONFIGURATION_REQUIRED",

					"message",
					"The configured repository is no longer available. "
							+ "Please select another repository or create a new repository.",

					"repositoryConfigurationRequired", true));
		}

		String repositoryName = repository.getRepositoryName();

		boolean repositoryExists = githubApiService.repositoryExists(accessToken, githubUsername, repositoryName);

		if (!repositoryExists) {

			codepilotReposRepo.delete(repository);

			System.out.println("Deleted stale repository record: " + repositoryName);

			return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("code", "REPOSITORY_CONFIGURATION_REQUIRED",

					"message",
					"The selected repository no longer exists on GitHub. "
							+ "Please select another repository or create a new repository.",

					"repositoryConfigurationRequired", true));
		}

		Long currentRepositoryId = githubApiService.getRepositoryId(accessToken, githubUsername, repositoryName);

		if (!currentRepositoryId.equals(repository.getGithubRepositoryId())) {

			codepilotReposRepo.delete(repository);

			return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("code", "REPOSITORY_CONFIGURATION_REQUIRED",

					"message",
					"The selected repository was replaced or recreated on GitHub. "
							+ "Please configure the repository again.",

					"repositoryConfigurationRequired", true));
		}

		String solutionPath = getFilePath(pushRequest);

		String readmePath = getReadmePath(pushRequest);

		String readme = readmeService.generateReadme(pushRequest);

		boolean readmeExists = githubApiService.fileExists(accessToken, githubUsername, repositoryName, readmePath);

		if (readmeExists) {
			githubApiService.updateFile(accessToken, githubUsername, repositoryName, readmePath, readme,
					readmeService.generateReadmeCommitMessage(pushRequest));
		} else {
			githubApiService.createFile(accessToken, githubUsername, repositoryName, readmePath, readme,
					readmeService.generateReadmeCommitMessage(pushRequest));
		}

		boolean solutionExists = githubApiService.fileExists(accessToken, githubUsername, repositoryName, solutionPath);

		if (solutionExists) {
			githubApiService.updateFile(accessToken, githubUsername, repositoryName, solutionPath,
					pushRequest.getCode(), readmeService.generateSolutionCommitMessage(pushRequest));
		} else {
			githubApiService.createFile(accessToken, githubUsername, repositoryName, solutionPath,
					pushRequest.getCode(), readmeService.generateSolutionCommitMessage(pushRequest));
		}

		return ResponseEntity.ok(Map.of("code", "SOLUTION_PUSHED",

				"message", "Solution pushed successfully."));
	}

	private String getFilePath(PushRequest pushRequest) {

		String fileName = getFileName(pushRequest.getLanguage());
		return pushRequest.getPlatform() + "/" + pushRequest.getDifficulty() + "/" + pushRequest.getProblemSlug() + "/"
				+ fileName;
	}

	private String getReadmePath(PushRequest pushRequest) {

		return pushRequest.getPlatform() + "/" + pushRequest.getDifficulty() + "/" + pushRequest.getProblemSlug()
				+ "/README.md";
	}

	private String getFileName(String language) {

		if (language == null || language.trim().isEmpty()) {
			throw new RuntimeException("Language is required");
		}

		if ("Java".equalsIgnoreCase(language) || "java".equalsIgnoreCase(language))
			return "Solution.java";
		if ("C++".equalsIgnoreCase(language) || "cpp".equalsIgnoreCase(language))
			return "Solution.cpp";
		if ("Python".equalsIgnoreCase(language) || "python3".equalsIgnoreCase(language))
			return "Solution.py";
		if ("JavaScript".equalsIgnoreCase(language) || "js".equalsIgnoreCase(language))
			return "Solution.js";
		if ("C".equalsIgnoreCase(language) || "c".equalsIgnoreCase(language))
			return "Solution.c";
		if ("C#".equalsIgnoreCase(language) || "csharp".equalsIgnoreCase(language))
			return "Solution.cs";
		if ("Go".equalsIgnoreCase(language) || "golang".equalsIgnoreCase(language))
			return "Solution.go";
		if ("Kotlin".equalsIgnoreCase(language) || "kt".equalsIgnoreCase(language))
			return "Solution.kt";
		if ("Rust".equalsIgnoreCase(language) || "rs".equalsIgnoreCase(language))
			return "Solution.rs";

		throw new RuntimeException("Unsupported Language: " + language);
	}

	public RepoCheckResp checkRepositoryAvailability(String repositoryName, Authentication authentication) {

		if (authentication == null || authentication.getPrincipal() == null) {
			return new RepoCheckResp(repositoryName, false, "User is not authenticated.");
		}

		if (repositoryName == null || repositoryName.trim().isEmpty()) {
			return new RepoCheckResp(repositoryName, false, "Repository name is required.");
		}

		repositoryName = repositoryName.trim();

		if (!isValidRepositoryName(repositoryName)) {
			return new RepoCheckResp(repositoryName, false,
					"Invalid repository name. Use letters, numbers, hyphen, underscore, or dot only.");
		}

		CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

		User user = userDetails.getUser();

		String accessToken = getDecryptedGithubAccessToken(user);
		String githubUsername = user.getGithubUsername();

		boolean repositoryExists = githubApiService.repositoryExists(accessToken, githubUsername, repositoryName);

		if (repositoryExists) {
			return new RepoCheckResp(repositoryName, false, "Repository already exists in your GitHub account.");
		}

		return new RepoCheckResp(repositoryName, true, "Repository name is available.");
	}

	private boolean isValidRepositoryName(String repositoryName) {

		return repositoryName.matches("^[a-zA-Z0-9._-]+$");
	}

	public List<GithubRepoResp> getUserRepositories(Authentication authentication) {

		CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

		User user = userDetails.getUser();

		return codepilotReposRepo.findByUserId(user.getId()).stream()
				.map(repository -> new GithubRepoResp(repository.getGithubRepositoryId(),
						repository.getRepositoryName(), repository.getIsPrivate(), repository.getHtmlUrl()))
				.toList();
	}
}