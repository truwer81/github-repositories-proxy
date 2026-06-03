package pl.jakubheppner.githubrepositoriesproxy;

record GithubRepositoryDTO(
        String name,
        boolean fork,
        GithubOwnerDTO owner
) {
}
