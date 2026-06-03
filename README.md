# GitHub Repositories Proxy

## Overview

GitHub Repositories Proxy is a Spring Boot REST API that acts as a simple proxy to the GitHub REST API.

The application returns public GitHub repositories for a given user, excluding repositories that are forks. For each returned repository, the response includes the repository name, owner login, branch names, and the SHA of the last commit on each branch.

## Tech Stack

- Java 25
- Spring Boot 4.0.6
- Gradle Kotlin DSL
- Spring Web MVC
- RestClient
- JUnit 5 / Spring Boot Test
- WireMock

## Requirements

- JDK 25
- Git
- No external database required

## Running the Application

Run the application with Gradle:

```bash
./gradlew bootRun
```

On Windows:

```bash
gradlew.bat bootRun
```

The application starts on the default Spring Boot port:

```text
8080
```

The application does not require a GitHub token for public repositories.

## Running Tests

Run a full build:

```bash
./gradlew clean build
```

Run the test suite with Gradle:

```bash
./gradlew test
```

On Windows:

```bash
gradlew.bat test
```

The integration tests use WireMock to emulate the GitHub API. They do not send real requests to GitHub.

## API

```http
GET /api/users/{username}/repositories
```

Path parameters:

- `username` - GitHub username.

## Successful Response

```http
200 OK
```

Example response:

```json
[
  {
    "repositoryName": "weather2.0",
    "ownerLogin": "truwer81",
    "branches": [
      {
        "name": "main",
        "lastCommitSha": "abc123"
      }
    ]
  }
]
```

## Error Response

For a GitHub user that does not exist:

```http
404 Not Found
```

Example response:

```json
{
  "status": 404,
  "message": "GitHub user not found"
}
```

## External API

The application uses the following GitHub REST API endpoints:

```http
GET /users/{username}/repos
GET /repos/{owner}/{repository}/branches
```

The GitHub API base URL is configured with:

```properties
github.api.base-url=https://api.github.com
```

It can be overridden when running the application:

```bash
./gradlew bootRun --args='--github.api.base-url=https://api.github.com'
```

The GitHub client uses `Accept: application/vnd.github+json` and `X-GitHub-Api-Version: 2022-11-28`.

## Scope Decisions

- No pagination.
- No security.
- No WebFlux.
- No database.
- Simple Controller / Service / Client structure.
- All production classes are in one package: `pl.jakubheppner.githubrepositoriesproxy`.
- Integration tests use WireMock.

## Project Structure

```text
src/main/java/pl/jakubheppner/githubrepositoriesproxy
src/main/resources/application.yml
src/test/java/pl/jakubheppner/githubrepositoriesproxy
build.gradle.kts
settings.gradle.kts
```

## Example Usage

```bash
curl http://localhost:8080/api/users/truwer81/repositories
```
