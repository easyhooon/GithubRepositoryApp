package kr.ac.konkuk.githubrepository.data.response

import kr.ac.konkuk.githubrepository.data.entity.GithubRepoEntity

data class GithubRepoSearchResponse(
    val totalCount: Int,
    val githubRepoList: List<GithubRepoEntity>,
    val items: List<GithubRepoEntity>
)