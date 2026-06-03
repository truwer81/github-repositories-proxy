package pl.jakubheppner.githubrepositoriesproxy;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
class GithubRepositoriesService {

    private final GithubClient githubClient;

    GithubRepositoriesService(GithubClient githubClient) {
        this.githubClient = githubClient;
    }

    List<RepositoryResponseDTO> getRepositories(String username) {
        return githubClient.getRepositories(username).stream()
                .filter(repository -> !repository.fork())
                .map(this::toRepository)
                .map(this::toRepositoryResponse)
                .toList();
    }

    private Repository toRepository(GithubRepositoryDTO githubRepository) {
        List<Branch> branches = githubClient
                .getBranches(githubRepository.owner().login(), githubRepository.name()).stream()
                .map(branch -> new Branch(branch.name(), branch.commit().sha()))
                .toList();

        return new Repository(
                githubRepository.name(),
                githubRepository.owner().login(),
                branches
        );
    }

    private RepositoryResponseDTO toRepositoryResponse(Repository repository) {
        return new RepositoryResponseDTO(
                repository.name(),
                repository.ownerLogin(),
                repository.branches().stream()
                        .map(branch -> new BranchResponseDTO(branch.name(), branch.lastCommitSha()))
                        .toList()
        );
    }
}
