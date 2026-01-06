package com.example.githubinfo;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GitHubInfoIntegrationTest {

    private static WireMockServer wireMockServer;

    @LocalServerPort
    private int port;

    private RestClient restClient;

    @BeforeAll
    static void setupWireMock() {
        wireMockServer = new WireMockServer(options().dynamicPort());
        wireMockServer.start();
    }

    @AfterAll
    static void teardownWireMock() {
        wireMockServer.stop();
    }

    @BeforeEach
    void setup() {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();
        wireMockServer.resetAll();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("github.api.url", wireMockServer::baseUrl);
    }

    @Test
    void shouldReturnNonForkRepositoriesWithBranches() {
        // Given - GitHub returns 2 repositories (none are forks)
        wireMockServer.stubFor(get(urlEqualTo("/users/testuser/repos"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                    [
                        {
                            "name": "repo1",
                            "owner": {"login": "testuser"},
                            "fork": false
                        },
                        {
                            "name": "repo2",
                            "owner": {"login": "testuser"},
                            "fork": false
                        }
                    ]
                    """)));

        // GitHub returns branches for repo1
        wireMockServer.stubFor(get(urlEqualTo("/repos/testuser/repo1/branches"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                    [
                        {
                            "name": "main",
                            "commit": {"sha": "abc123"}
                        },
                        {
                            "name": "develop",
                            "commit": {"sha": "def456"}
                        }
                    ]
                    """)));

        // GitHub returns branches for repo2
        wireMockServer.stubFor(get(urlEqualTo("/repos/testuser/repo2/branches"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                    [
                        {
                            "name": "master",
                            "commit": {"sha": "xyz789"}
                        }
                    ]
                    """)));

        // When - we call our endpoint
        Repository[] repositories = restClient.get()
                .uri("/users/testuser/repositories")
                .retrieve()
                .body(Repository[].class);

        // Then - we verify the response
        assertNotNull(repositories);
        assertEquals(2, repositories.length);

        // Verify repo1
        Repository repo1 = repositories[0];
        assertEquals("repo1", repo1.name());
        assertEquals("testuser", repo1.owner().login());
        assertEquals(2, repo1.branches().size());
        assertEquals("main", repo1.branches().get(0).name());
        assertEquals("abc123", repo1.branches().get(0).lastCommitSha());
        assertEquals("develop", repo1.branches().get(1).name());
        assertEquals("def456", repo1.branches().get(1).lastCommitSha());

        // Verify repo2
        Repository repo2 = repositories[1];
        assertEquals("repo2", repo2.name());
        assertEquals("testuser", repo2.owner().login());
        assertEquals(1, repo2.branches().size());
        assertEquals("master", repo2.branches().get(0).name());
        assertEquals("xyz789", repo2.branches().get(0).lastCommitSha());
    }

    @Test
    void shouldFilterOutForkRepositories() {
        // Given - GitHub returns 1 original repo and 1 fork
        wireMockServer.stubFor(get(urlEqualTo("/users/anotheruser/repos"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                    [
                        {
                            "name": "original-repo",
                            "owner": {"login": "anotheruser"},
                            "fork": false
                        },
                        {
                            "name": "forked-repo",
                            "owner": {"login": "anotheruser"},
                            "fork": true
                        }
                    ]
                    """)));

        // GitHub returns branches only for the original repo
        wireMockServer.stubFor(get(urlEqualTo("/repos/anotheruser/original-repo/branches"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                    [
                        {
                            "name": "main",
                            "commit": {"sha": "commit123"}
                        }
                    ]
                    """)));

        // When - we call our endpoint
        Repository[] repositories = restClient.get()
                .uri("/users/anotheruser/repositories")
                .retrieve()
                .body(Repository[].class);

        // Then - only the original repo, the fork was filtered out
        assertNotNull(repositories);
        assertEquals(1, repositories.length);
        assertEquals("original-repo", repositories[0].name());

        // Make sure the fork is not in the list
        assertFalse(List.of(repositories).stream()
                        .anyMatch(repo -> repo.name().equals("forked-repo")),
                "Forked repository should be filtered out");
    }

    @Test
    void shouldReturn404ForNonExistingUser() {
        // Given - GitHub returns 404 for non-existent user
        wireMockServer.stubFor(get(urlEqualTo("/users/nonexistent/repos"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                    {
                        "message": "Not Found",
                        "documentation_url": "https://docs.github.com/rest"
                    }
                    """)));

        // When & Then - we call our endpoint and expect 404
        HttpClientErrorException.NotFound exception = assertThrows(
                HttpClientErrorException.NotFound.class,
                () -> restClient.get()
                        .uri("/users/nonexistent/repositories")
                        .retrieve()
                        .body(ErrorResponse.class)
        );

        // Verify we got 404
        assertEquals(404, exception.getStatusCode().value());
    }
}