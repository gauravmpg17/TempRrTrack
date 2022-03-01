package asset.trak.views.activity

import android.content.Intent
import android.os.Bundle
import asset.trak.views.baseclasses.BaseActivity
import com.markss.rfidtemplate.R
import com.markss.rfidtemplate.home.MainActivity
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class SplashAssetTrak() : BaseActivity(), CoroutineScope {
    private var job: Job = Job()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_assettrak)
    }

    override fun onStart() {
        super.onStart()
        launch {
            getNavigateToNext()
        }
    }

    override fun onRestart() {
        super.onRestart()
        launch {
            getNavigateToNext()
        }
    }

    private suspend fun getNavigateToNext() {
        delay(2000)
        val mainIntent = Intent(this, MainActivity::class.java)
        startActivity(mainIntent)
        finish()
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}