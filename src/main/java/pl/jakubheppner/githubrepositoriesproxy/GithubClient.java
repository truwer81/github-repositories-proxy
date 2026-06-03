package pl.jakubheppner.githubrepositoriesproxy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
class GithubClient {

    private static final String GITHUB_ACCEPT_HEADER = "application/vnd.github+json";
    private static final String GITHUB_API_VERSION_HEADER = "X-GitHub-Api-Version";
    private static final String GITHUB_API_VERSION = "2022-11-28";

    private final RestClient restClient;

    GithubClient(@Value("${github.api.base-url}") String githubApiBaseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(githubApiBaseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, GITHUB_ACCEPT_HEADER)
                .defaultHeader(GITHUB_API_VERSION_HEADER, GITHUB_API_VERSION)
                .build();
    }

    List<GithubRepositoryDTO> getRepositories(String username) {
        List<GithubRepositoryDTO> repositories = restClient.get()
                .uri("/users/{username}/repos", username)
                .retrieve()
                .onStatus(this::isNotFound, this::handleUserNotFound)
                .body(new ParameterizedTypeReference<List<GithubRepositoryDTO>>() {
                });

        return repositories == null ? List.of() : repositories;
    }

    List<GithubBranchDTO> getBranches(String owner, String repositoryName) {
        List<GithubBranchDTO> branches = restClient.get()
                .uri("/repos/{owner}/{repositoryName}/branches", owner, repositoryName)
                .retrieve()
                .body(new ParameterizedTypeReference<List<GithubBranchDTO>>() {
                });

        return branches == null ? List.of() : branches;
    }

    private boolean isNotFound(HttpStatusCode status) {
        return status.value() == 404;
    }

    private void handleUserNotFound(HttpRequest request, ClientHttpResponse response) {
        throw new GithubUserNotFoundException();
    }
}
