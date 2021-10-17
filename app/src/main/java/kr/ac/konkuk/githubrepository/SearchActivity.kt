package kr.ac.konkuk.githubrepository

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.view.isGone
import kotlinx.coroutines.*
import kr.ac.konkuk.githubrepository.data.entity.GithubRepoEntity
import kr.ac.konkuk.githubrepository.databinding.ActivitySearchBinding
import kr.ac.konkuk.githubrepository.utility.RetrofitUtil
import kr.ac.konkuk.githubrepository.view.RepositoryRecyclerAdapter
import kotlin.coroutines.CoroutineContext

class SearchActivity : AppCompatActivity(), CoroutineScope {

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private lateinit var binding: ActivitySearchBinding

    private lateinit var adapter: RepositoryRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)

        setContentView(binding.root)

        initAdapter()
        initViews()
        bindViews()
    }

    private fun initAdapter() = with(binding) {
        adapter = RepositoryRecyclerAdapter()
    }

    private fun initViews() = with(binding) {
        emptyResultTextView.isGone = true
        recyclerView.adapter = adapter

    }

    private fun bindViews() = with(binding) {
        searchButton.setOnClickListener {
            searchKeyword(searchBarInputView.text.toString())
        }
    }

    private fun searchKeyword(keywordString: String) = launch {
        withContext(Dispatchers.IO) {
            val response = RetrofitUtil.githubApiService.searchRepositories(keywordString)
            if (response.isSuccessful) {
                val body = response.body()
                withContext(Dispatchers.Main) {
//                    Log.e("response", body.toString() )
                    body?.let { setData(it.items) }
                }
            }
        }
    }

    private fun setData(items: List<GithubRepoEntity>) {
        adapter.setSearchResultList(items) {
            Toast.makeText(this, "entity $it", Toast.LENGTH_SHORT).show()
            startActivity(
                Intent(this@SearchActivity, RepositoryActivity::class.java).apply {
                    putExtra(RepositoryActivity.REPOSITORY_OWNER_KEY, it.owner.login)
                    putExtra(RepositoryActivity.REPOSITORY_NAME_KEY, it.name)
                }
            )
        }

    }
}