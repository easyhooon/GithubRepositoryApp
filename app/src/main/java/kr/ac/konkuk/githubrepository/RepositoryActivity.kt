package kr.ac.konkuk.githubrepository

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import kotlinx.coroutines.*
import kr.ac.konkuk.githubrepository.data.database.DatabaseProvider
import kr.ac.konkuk.githubrepository.data.entity.GithubRepoEntity
import kr.ac.konkuk.githubrepository.databinding.ActivityRepositoryBinding
import kr.ac.konkuk.githubrepository.extensions.loadCenterInside
import kr.ac.konkuk.githubrepository.utility.RetrofitUtil
import kotlin.coroutines.CoroutineContext

//레파지토리 상세화면
class RepositoryActivity : AppCompatActivity(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + Job()

    companion object {
        const val REPOSITORY_OWNER_KEY = "repositoryOwner"
        const val REPOSITORY_NAME_KEY = "repositoryName"
    }

    private lateinit var binding: ActivityRepositoryBinding

    private val repositoryDao by lazy { DatabaseProvider.provideDB(applicationContext).repositoryDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRepositoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val repositoryOwner = intent.getStringExtra(REPOSITORY_OWNER_KEY) ?: kotlin.run {
            toast("Repository Owner 이름이 없습니다.")
            finish()
            return
        }
        val repositoryName = intent.getStringExtra(REPOSITORY_NAME_KEY) ?: kotlin.run {
            toast("Repository 이름이 없습니다.")
            finish()
            return
        }

        launch {
            loadRepository(repositoryOwner, repositoryName)?.let {
                setData(it)
            } ?: run {
                toast("Repository 정보가 없습니다.")
                finish()
            }
        }

        showLoading(true)
    }

    private suspend fun loadRepository(
        repositoryOwner: String,
        repositoryName: String
    ): GithubRepoEntity? =
        withContext(coroutineContext) {
            var repository: GithubRepoEntity? = null
            withContext(Dispatchers.IO) {
                val response = RetrofitUtil.githubApiService.getRepository(
                    ownerLogin = repositoryOwner,
                    repoName = repositoryName
                )
                if (response.isSuccessful) {
                    val body = response.body()
                    withContext(Dispatchers.Main) {
                        body?.let { repo ->
                            repository = repo
                        }
                    }
                }
            }
            repository
        }

    @SuppressLint("SetTextI18n")
    private fun setData(githubRepoEntity: GithubRepoEntity) = with(binding) {
        Log.e("data", githubRepoEntity.toString())
        showLoading(false)
        ownerProfileImageView.loadCenterInside(githubRepoEntity.owner.avatarUrl, 42f)
        ownerNameAndRepoNameTextView.text =
            "${githubRepoEntity.owner.login}/${githubRepoEntity.name}"
        stargazersCountText.text = githubRepoEntity.stargazersCount.toString()
        githubRepoEntity.language?.let { language ->
            languageText.isGone = false
            languageText.text = language
        } ?: kotlin.run {
            languageText.isGone = true
            languageText.text = ""
        }
        descriptionTextView.text = githubRepoEntity.description
        updateTimeTextView.text = githubRepoEntity.updatedAt

        setLikeState(githubRepoEntity)
    }

    private fun setLikeState(githubRepoEntity: GithubRepoEntity) = launch {
        withContext(Dispatchers.IO) {
            val repository = repositoryDao.getRepository(githubRepoEntity.fullName)
            //좋아요 버튼에 대한 상태를 구현
            val isLike = repository != null
            withContext(Dispatchers.Main) {
                setLikeImage(isLike)
                binding.likeButton.setOnClickListener {
                    likeGithubRepo(githubRepoEntity, isLike)
                }
            }
        }
    }

    private fun setLikeImage(isLike: Boolean) {
        binding.likeButton.setImageDrawable(
            ContextCompat.getDrawable(
                this@RepositoryActivity,
                if (isLike) {
                    R.drawable.ic_like
                } else {
                    R.drawable.ic_dislike
                }
            )
        )
    }

    private fun likeGithubRepo(githubRepoEntity: GithubRepoEntity, isLike: Boolean) = launch {
        withContext(Dispatchers.IO) {
            if (isLike) {
                repositoryDao.remove(githubRepoEntity.fullName)
            } else {
                repositoryDao.insert(githubRepoEntity)
            }
            withContext(Dispatchers.Main) {
                setLikeImage(isLike.not())
            }
        }
    }

    private fun showLoading(isShown: Boolean) = with(binding) {
        progressBar.isGone = isShown.not()
    }

    //확장함수
    private fun Context.toast(message: String) =
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}