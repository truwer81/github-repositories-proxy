package pl.jakubheppner.githubrepositoriesproxy;

import java.util.List;

record RepositoryResponseDTO(
        String repositoryName,
        String ownerLogin,
        List<BranchResponseDTO> branches
) {
}
