package com.example.githubinfo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.util.List;


@RestController
class GitHubInfoClient {
    private final RestClient restClient;


    private GitHubInfoClient(@Value("${github.api.url}") String githubApiUrl)
    {
        this.restClient = RestClient.builder().
                baseUrl(githubApiUrl).
                defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public List<GitHubRepository> getUserRepos(String username)
    {
        return restClient.get().uri("/users/{username}/repos", username).retrieve().body(new ParameterizedTypeReference<>() {});
    }

    public List<GitHubBranch> GetRepoBranch(String owner, String repo)
    {
        return restClient.get()
                .uri("/repos/{owner}/{repo}/branches",owner, repo)
                .retrieve().body(new ParameterizedTypeReference<>() {});
    }

}
