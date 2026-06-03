package pl.jakubheppner.githubrepositoriesproxy;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
class GithubRepositoriesController {

    private final GithubRepositoriesService githubRepositoriesService;

    GithubRepositoriesController(GithubRepositoriesService githubRepositoriesService) {
        this.githubRepositoriesService = githubRepositoriesService;
    }

    @GetMapping("/api/users/{username}/repositories")
    List<RepositoryResponseDTO> getRepositories(@PathVariable String username) {
        return githubRepositoriesService.getRepositories(username);
    }
}
