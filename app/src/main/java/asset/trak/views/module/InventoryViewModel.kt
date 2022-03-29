package asset.trak.views.module

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import asset.trak.database.entity.BookAttributes
import asset.trak.modelsrrtrack.LastSyncData
import asset.trak.modelsrrtrack.LastSyncResponse
import asset.trak.repository.BookRepository
import asset.trak.utils.getFormattedDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.RequestBody
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@HiltViewModel
class InventoryViewModel @Inject constructor(private val bookRepository: BookRepository) :
    ViewModel() {
     var mLastSyncData = MutableLiveData<LastSyncResponse>()
    private var mAssetSyncData = MutableLiveData<Int>()
    var listBookAttributes: ArrayList<BookAttributes> = ArrayList()
    var isFirstTime: Boolean = false
    var isSearchClicked:Boolean=false
    private val _barCode = MutableLiveData<String>()
    val barCode: LiveData<String> get() = _barCode

    private val _timerVal = MutableLiveData("00:00")
    val timerVal: LiveData<String> get() = _timerVal

    private val _timerValTemp = MutableLiveData("0")

    private var timer = Handler(Looper.getMainLooper())

    private val _isStart = MutableLiveData(false)
    val isStart: LiveData<Boolean> get() = _isStart


    fun updateBarCode(barCode: String) {
        _barCode.value = barCode
    }

    fun updateTime() {
        timer.postDelayed(t, 1000)
        _isStart.value = true
    }

    val t = Runnable {
        CoroutineScope(Dispatchers.Main).launch {
            val lastVal = _timerValTemp.value.toString().toInt() + 1
            val min = lastVal / 60
            val sec = lastVal % 60
            _timerValTemp.value = lastVal.toString()
            _timerVal.value = getFormattedDate(
                SimpleDateFormat("hh:mm"),
                SimpleDateFormat("HH:mm"), "$min:$sec"
            )
            startAgain()
        }
    }

    fun startAgain() {
        timer.postDelayed(t, 1000)
    }

    fun resetTimer() {
        timer.removeCallbacks(t)
        _isStart.value = false
        _timerVal.value = "00:00"
        _timerValTemp.value="0"
    }

    fun stopTime() {
        _isStart.value = false
        timer.removeCallbacks(t)
    }


    fun getLastSync(syncTime: String?): LiveData<LastSyncResponse> {
        viewModelScope.launch {
            val data = bookRepository.getLastSync(syncTime)
            data?.apply {
                mLastSyncData.value = this.value
            }
        }
        return mLastSyncData
    }

    fun postAssetSync(body: RequestBody): LiveData<Int> {
        viewModelScope.launch {
            val data = bookRepository.postAssetSync(body)
            data?.apply {
                mAssetSyncData.value = this.value
            }
        }
        return mAssetSyncData
    }


    fun updateMapLocation(body: RequestBody): LiveData<Int> {
        viewModelScope.launch {
            val data = bookRepository.updateMapLocation(body)
            data?.apply {
                mAssetSyncData.value = this.value
            }
        }
        return mAssetSyncData
    }
}