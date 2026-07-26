package com.extension.codepilot.service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import com.extension.codepilot.dto.GithubRepoResp;

@Service
public class GithubApiService {

	@Autowired
	private RestClient restClient;

	// Check whether the repository exists
	public boolean repositoryExists(String accessToken, String githubUsername, String repositoryName) {
		String url = "/repos/" + githubUsername + "/" + repositoryName;

		try {

			restClient.get().uri(url).header("Authorization", "Bearer " + accessToken)
					.header("Accept", "application/vnd.github+json").retrieve().toBodilessEntity();
			return true;

		} catch (HttpClientErrorException.NotFound e) {
			return false;
		} catch (Exception e) {
			throw new RuntimeException("Error while checking repository", e);
		}
	}

	// Create a new repository
	public Map<String, Object> createRepository(String accessToken, String repositoryName, boolean isPrivate) {

		String url = "/user/repos";

		Map<String, Object> body = new HashMap<>();

		body.put("name", repositoryName);
		body.put("private", isPrivate);
		body.put("auto_init", true);

		try {

//			restClient.post().uri(url).contentType(MediaType.APPLICATION_JSON)
//					.header("Authorization", "Bearer " + accessToken).header("Accept", "application/vnd.github+json")
//					.body(body).retrieve().toBodilessEntity();

			System.out.println("Repository created successfully.");
			return restClient.post().uri(url).contentType(MediaType.APPLICATION_JSON)
					.header("Authorization", "Bearer " + accessToken).header("Accept", "application/vnd.github+json")
					.body(body).retrieve().body(Map.class);

		} catch (Exception e) {
			throw new RuntimeException("Repository creation failed.", e);
		}
	}

	public String getFileSha(String accessToken, String githubUsername, String repositoryName, String filePath) {

		String url = "/repos/" + githubUsername + "/" + repositoryName + "/contents/" + filePath;

		try {

			Map<String, Object> response = restClient.get().uri(url).header("Authorization", "Bearer " + accessToken)
					.header("Accept", "application/vnd.github+json").retrieve().body(Map.class);

			if (response != null && response.containsKey("sha")) {
				return response.get("sha").toString();
			}
			throw new RuntimeException("SHA not found.");

		} catch (Exception e) {

			throw new RuntimeException("Unable to fetch file SHA.", e);

		}

	}

	public void updateFile(String accessToken, String githubUsername, String repositoryName, String filePath,
			String content, String commitMessage) {

		String sha = getFileSha(accessToken, githubUsername, repositoryName, filePath);

		String url = "/repos/" + githubUsername + "/" + repositoryName + "/contents/" + filePath;

		String encodedContent = Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8));

		Map<String, Object> body = new HashMap<>();

		body.put("message", commitMessage);
		body.put("content", encodedContent);
		body.put("sha", sha);

		restClient.put().uri(url).contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer " + accessToken).header("Accept", "application/vnd.github+json")
				.body(body).retrieve().toBodilessEntity();

		System.out.println("File Updated Successfully");
	}

	public void createFile(String accessToken, String githubUsername, String repositoryName, String filePath,
			String content, String commitMessage) {

		String url = "/repos/" + githubUsername + "/" + repositoryName + "/contents/" + filePath;

		String encodedContent = Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8));

		Map<String, Object> body = new HashMap<>();

		body.put("message", commitMessage);
		body.put("content", encodedContent);

		restClient.put().uri(url).contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer " + accessToken).header("Accept", "application/vnd.github+json")
				.body(body).retrieve().toBodilessEntity();

		System.out.println("File Created Successfully");
	}

	public boolean fileExists(String accessToken, String githubUsername, String repositoryName, String filePath) {

		String url = "/repos/" + githubUsername + "/" + repositoryName + "/contents/" + filePath;

		try {

			restClient.get().uri(url).header("Authorization", "Bearer " + accessToken)
					.header("Accept", "application/vnd.github+json").retrieve().toBodilessEntity();

			return true;

		} catch (HttpClientErrorException.NotFound e) {

			return false;

		}
	}

	public List<GithubRepoResp> getUserRepositories(String accessToken) {

		String url = "/user/repos?per_page=100&sort=updated&type=owner";

		try {
			List<Map<String, Object>> response = restClient.get().uri(url)
					.header("Authorization", "Bearer " + accessToken).header("Accept", "application/vnd.github+json")
					.retrieve().body(List.class);

			List<GithubRepoResp> repos = new ArrayList<>();

			if (response == null) {
				return repos;
			}

			for (Map<String, Object> repo : response) {

				String name = repo.get("name").toString();

				Boolean isPrivate = Boolean.valueOf(repo.get("private").toString());

				Long repositoryId = Long.valueOf(repo.get("id").toString());

				String htmlUrl = repo.get("html_url").toString();

				repos.add(new GithubRepoResp(repositoryId, name, isPrivate, htmlUrl));
			}

			return repos;

		} catch (Exception e) {
			throw new RuntimeException("Unable to fetch GitHub repositories.", e);
		}
	}

	public Long getRepositoryId(String accessToken, String githubUsername, String repositoryName) {

		String url = "/repos/" + githubUsername + "/" + repositoryName;
		Map<String, Object> response = restClient.get().uri(url).header("Authorization", "Bearer " + accessToken)
				.header("Accept", "application/vnd.github+json").retrieve().body(Map.class);

		if (response == null || response.get("id") == null) {
			throw new RuntimeException("Unable to verify repository");
		}

		return Long.valueOf(response.get("id").toString());

	}
}
