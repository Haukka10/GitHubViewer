package com.example.githubinfo;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class GitHubInfoService {
    private final GitHubInfoClient gitHubClient;

    private GitHubInfoService(GitHubInfoClient gitHubClient)
    {
        this.gitHubClient = gitHubClient;
    }

    public List<Repository> GetUserRepo(String UserName)
    {
        List<GitHubRepository> gitHubRepositories = gitHubClient.getUserRepos(UserName);

        return gitHubRepositories.stream().filter(r -> !r.fork()).map(this::toRepository).toList();
    }

    private @NonNull Repository toRepository(@NonNull GitHubRepository gitHubRepository) {

        List<GitHubBranch> gitHubBranch = gitHubClient.GetRepoBranch(gitHubRepository.owner().login(),gitHubRepository.name());
        List<Branch> branches = gitHubBranch.stream().map(b -> new Branch(b.name(),b.commit().sha())).toList();

        return new Repository(gitHubRepository.name(), gitHubRepository.owner(), branches);
    }
}
