package kr.ac.konkuk.githubrepository

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.view.isGone
import kotlinx.coroutines.*
import kr.ac.konkuk.githubrepository.data.database.DatabaseProvider
import kr.ac.konkuk.githubrepository.data.entity.GithubOwner
import kr.ac.konkuk.githubrepository.data.entity.GithubRepoEntity
import kr.ac.konkuk.githubrepository.databinding.ActivityMainBinding
import kr.ac.konkuk.githubrepository.view.RepositoryRecyclerAdapter
import java.util.*
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(), CoroutineScope {

    val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private val repositoryDao by lazy { DatabaseProvider.provideDB(applicationContext).repositoryDao()}

    private lateinit var adapter: RepositoryRecyclerAdapter

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initAdapter()
        initViews()

        //mocking Data를 넣어주는 코드
//        launch {
//            addMockData()
//            val githubRepositories = loadGithubRepositories()
//            withContext(coroutineContext) {
//                Log.e("repositories", githubRepositories.toString())
//            }
//        }
    }

    private fun initViews() = with(binding) {
        searchButton.setOnClickListener {
            startActivity(
//                Intent(this, SearchActivity::class.java) 라고 작성하면 에러 뜸
                Intent(this@MainActivity, SearchActivity::class.java)
            )
        }
    }

    private fun initAdapter() {
        adapter = RepositoryRecyclerAdapter()
    }

    override fun onResume() {
        super.onResume()
        launch(coroutineContext) {
            loadLikedRepositoryList()
        }
    }

    private suspend fun loadLikedRepositoryList() = withContext(Dispatchers.IO) {
//        val repoList = DatabaseProvider.provideDB(this@MainActivity).repositoryDao().getHistory()
        val repoList = repositoryDao.getHistory()
        withContext(Dispatchers.Main) {
            setData(repoList)
        }
    }

    private fun setData(githubRepositoryList: List<GithubRepoEntity>) = with(binding) {
        if(githubRepositoryList.isEmpty()) {
            emptyResultTextView.isGone = false
            recyclerView.isGone = true
        } else {
            emptyResultTextView.isGone = true
            recyclerView.isGone = false
            adapter.setSearchResultList(githubRepositoryList) {
                startActivity(
                    Intent(this@MainActivity, RepositoryActivity::class.java).apply {
                        putExtra(RepositoryActivity.REPOSITORY_OWNER_KEY, it.owner.login)
                        putExtra(RepositoryActivity.REPOSITORY_NAME_KEY, it.name)
                    }
                )
            }

        }
    }

//    private suspend fun addMockData() = withContext(Dispatchers.IO) {
//        val mockData = (0 until 10).map {
//            GithubRepoEntity(
//                name = "repo $it",
//                fullName = "name $it",
//                owner = GithubOwner(
//                    "login",
//                    "avatarUrl"
//                ),
//                description = null,
//                language = null,
//                updatedAt = Date().toString(),
//                stargazersCount = it
//            )
//        }
//        repositoryDao.insertAll(mockData)
//    }


//    loadLikedRepositoryList() 으로 대체됨
//    private suspend fun loadGithubRepositories() = withContext(Dispatchers.IO) {
//        val repositories = repositoryDao.getHistory()
//        return@withContext repositories
//    }
}