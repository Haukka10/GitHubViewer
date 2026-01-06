package com.example.githubinfo;


import java.util.List;

record Repository(
        String name,
        Owner owner,
        List<Branch> branches
) {}

record Owner(
        String login
) {}

record Branch(
        String name,
        String lastCommitSha
) {}

record ErrorResponse(
        int status,
        String message
) {}

record GitHubRepository(
        String name,
        Owner owner,
        boolean fork
) {}

record GitHubBranch(
        String name,
        Commit commit
) {}

record Commit(
        String sha
) {}