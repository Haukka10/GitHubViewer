# GitHubViewer
GitHub API proxy that filters out forked repositories and returns detailed branch information including last commit SHA for each branch.

A Spring Boot application that acts as a proxy to the GitHub API, returning a list of user repositories (excluding forks) with branch information.

Requirements

* Java 25
* Gradle 8.x

## Running the Application

```
./gradlew bootRun
```

## API Endpoint

GET /users/{username}/repositories <br>
Returns a list of GitHub user repositories (excluding forks) with branch information.<br>
Example Response (200 OK):
<img width="829" height="814" alt="Screenshot 2026-01-06 012948" src="https://github.com/user-attachments/assets/0e89d55c-1b37-486d-8949-ae9aa0390602" />
<br>
Response for Non-Existing User (404 Not Found): <br>

<img width="274" height="93" alt="Screenshot 2026-01-06 013109" src="https://github.com/user-attachments/assets/2092d8ce-1404-4ad9-829a-4370ed9ba04f" />

Example Usage: <br>
```
curl http://localhost:8080/users/username/repositories <- "username" type user name
```
## Architecture
The application follows a simple three-layer architecture:

* Controller (GitHubInfoController) - Handles HTTP requests and responses
* Service (GitHubInfoService) - Contains business logic (filtering forks, data transformation)
* Client (GitHubInfoClient) - Communicates with the GitHub API

## Technology Stack

* Java 25
* Spring Boot 4.0.1
* Spring Web MVC - REST API framework
* Spring RestClient - HTTP client for external API calls
* WireMock - HTTP server mocking for integration tests
* Gradle (Kotlin DSL) - Build tool

## Features
✅ Lists all non-fork repositories for a given GitHub user <br>
✅ Returns repository name and owner login<br>
✅ Includes all branches with their last commit SHA for each repository<br>
✅ Handles 404 errors for non-existing users<br>
✅ Integration tests with WireMock (no mocks, real HTTP flow)<br>
<br>

## Configuration
Edit src/main/resources/application.properties

## Testing
The application includes integration tests that:

* Test the complete flow from Controller through Service to Client
* Use WireMock to emulate the GitHub API
* Cover main business scenarios:

* Returning repositories with branches
* Filtering out forked repositories
* Handling non-existing users (404)



## Notes

* No pagination support (as per requirements)
* No security, caching, or resilience patterns (as per requirements)
* All classes in a single package with package-private visibility
* Minimal models - no separation into DTO/Domain/etc.

# GitHub API Rate Limits
Without authentication, GitHub API allows: <br>

60 requests per hour (per IP address) <br>

## About This Project
This is a demonstration project showcasing Spring Boot best practices and integration testing with WireMock. The implementation follows specific architectural constraints to maintain simplicity and focus on core functionality. <br>
Future Enhancements <br>
Potential improvements for production use: <br>

Caching - Add Redis/Caffeine to reduce GitHub API calls <br>
Rate Limiting - Implement request throttling<br>
Resilience - Add retry logic and circuit breakers (Resilience4j)<br>
Security - Add API authentication and rate limiting per user<br>
Pagination - Support for large repository lists<br>
Async Processing - Parallel fetching of branch information<br>
Monitoring - Add metrics and health checks<br>
Docker - Containerization for easy deployment<br>
