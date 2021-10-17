package kr.ac.konkuk.githubrepository

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.view.isGone
import kotlinx.coroutines.*
import kr.ac.konkuk.githubrepository.databinding.ActivitySignInBinding
import kr.ac.konkuk.githubrepository.utility.AuthTokenProvider
import kr.ac.konkuk.githubrepository.utility.RetrofitUtil
import kotlin.coroutines.CoroutineContext

class SignInActivity : AppCompatActivity(), CoroutineScope {

    private lateinit var binding: ActivitySignInBinding

    //scope 를 가져다가 쓸 수 있는 하나의 context
    //builder function Job()
    //화면이 종료 된 이후에도 thread 에서 api 콜을 하고 있으면 안되므로 앱이 종료가 되는 시점에 코루틴 작업을 중지
    var job: Job = Job()

    override val coroutineContext: CoroutineContext
    //MainThread 에서 사용하겠다 선언
    //context 로 job 을 추가
        get() = Dispatchers.Main + job
    private val authTokenProvider by lazy { AuthTokenProvider(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignInBinding.inflate(layoutInflater)

        setContentView(binding.root)

        if (checkAuthCodeExist()) {
            launchMainActivity()
        }
        else {
            initViews()
        }


    }

    private fun initViews() = with(binding) {
        loginButton.setOnClickListener {
            loginGithub()
        }
    }

    private fun launchMainActivity() {
        //메인 액티비티가 실행되면 이 액티비티는 필요가없어짐 clear
        startActivity(Intent(this, MainActivity::class.java).apply{
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })

    }

    private fun checkAuthCodeExist(): Boolean = authTokenProvider.token.isNullOrEmpty().not()



    // https://github.com/login/oauth/authorize?client_id=asdqwesad
    private fun loginGithub() {
        val loginUri = Uri.Builder().scheme("https").authority("github.com")
            .appendPath("login")
            .appendPath("oauth")
            .appendPath("authorize")
            .appendQueryParameter("client_id", BuildConfig.GITHUB_CLIENT_ID)
            .build()

        //browser library 기반
        //현재 화면에서 custom tab 으로 이동할 수 있는 intent 를 실행
        CustomTabsIntent.Builder().build().also {
            it.launchUrl(this, loginUri)
        }

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        //access token 을 받아오는 코드
        intent?.data?.getQueryParameter("code")?.let{
            //getAccessToken
            launch(coroutineContext) {
                showProgress()
//                val getAccessTokenJob = getAccessToken(it)
                getAccessToken(it)
                dismissProgress()
            }
        }
    }

    private suspend fun showProgress() = withContext(coroutineContext) {
        with(binding) {
            loginButton.isGone = true
            progressBar.isGone = false
            progressTextView.isGone = false
        }
    }

    private suspend fun dismissProgress() = withContext(coroutineContext) {
        with(binding) {
            loginButton.isGone = false
            progressBar.isGone = true
            progressTextView.isGone = true
        }
    }

    private suspend fun getAccessToken(code: String) = withContext(Dispatchers.IO) {
        val response = RetrofitUtil.authApiService.getAccessToken(
            clientId = BuildConfig.GITHUB_CLIENT_ID,
            clientSecret = BuildConfig.GITHUB_CLIENT_SECRET,
            code = code
        )
        if(response.isSuccessful) {
            val accessToken = response.body()?.accessToken ?:""
            Log.e("AccessToken: ", accessToken)
            if(accessToken.isNotEmpty()){
                //suspend 가 있기 때문에 token 이 update 할때까지 기다림
                authTokenProvider.updateToken(accessToken)
            } else {
                Toast.makeText(this@SignInActivity, "accessToken 이 존재하지 않습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}