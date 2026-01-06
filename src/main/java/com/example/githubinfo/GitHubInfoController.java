package com.example.githubinfo;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

@Controller
class GitHubInfoController {

    private final GitHubInfoService gitHubService;

    private GitHubInfoController(GitHubInfoService gitHubService) {
        this.gitHubService = gitHubService;
    }

    @GetMapping("/users/{username}/repositories")
    ResponseEntity<List<Repository>> GetAllRepos(@PathVariable String username)
    {
        List<Repository> repositories = gitHubService.GetUserRepo(username);

        return ResponseEntity.ok(repositories);
    }

    @ExceptionHandler(HttpClientErrorException.NotFound.class)
    ResponseEntity<ErrorResponse> handleNotFound(HttpClientErrorException.NotFound ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "User not found"
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
}
