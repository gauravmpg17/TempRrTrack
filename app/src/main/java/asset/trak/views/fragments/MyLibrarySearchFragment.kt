package asset.trak.views.fragments

import android.content.*
import android.os.Bundle
import android.provider.Settings
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import asset.trak.modelsrrtrack.AppTimeStamp
import asset.trak.modelsrrtrack.AssetMain
import asset.trak.utils.Constants
import asset.trak.utils.apiDateFormat
import asset.trak.utils.decreaseRangeToThirty
import asset.trak.utils.mainCoroutines
import asset.trak.views.adapter.OnResultClickListener
import asset.trak.views.adapter.ResultAdapter
import asset.trak.views.baseclasses.BaseFragment
import asset.trak.views.module.InventoryViewModel
import com.markss.rfidtemplate.R
import com.markss.rfidtemplate.application.Application
import com.markss.rfidtemplate.locate_tag.LocateOperationsFragment
import com.markss.rfidtemplate.rfid.RFIDController
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_my_library_search.*
import kotlinx.android.synthetic.main.fragment_my_library_search.progressBar
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MyLibrarySearchFragment : BaseFragment(R.layout.fragment_my_library_search),
    OnResultClickListener {

    private lateinit var resultAdapter: ResultAdapter
    private var listBook = ArrayList<AssetMain>()
    var sharedPreference: SharedPreferences? = null
    private val inventoryViewModel: InventoryViewModel by activityViewModels()

    private var apiHit = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
                Application.bookDao?.saveAppTimeStamp(AppTimeStamp(Date()))
                val appTimeStamp = async {
                    Application.bookDao?.retriveTimeStamp()
                }.await()
            inventoryViewModel.dateLastSync = apiDateFormat(appTimeStamp?.syncDate!!)
            Log.e("dhdgdhdh", "getLastSync First11 ${ inventoryViewModel.dateLastSync}")
            inventoryViewModel.getLastSync(inventoryViewModel.dateLastSync)
       }
    }

    val receiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, intent: Intent?) {
            tvResult.text =
                "${getString(R.string.lblResults)} (${intent?.getIntExtra("searchCount", 0)})"
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (apiHit) {
            progressBar.visibility = View.VISIBLE
        }
        requireActivity().registerReceiver(receiver, IntentFilter("COUNT_UPDATE_SEARCH"))
        ivBack.setOnClickListener {
            getBackToPreviousFragment()
        }
        resultAdapter = ResultAdapter(requireContext(), this, listBook, true)
        rvResult.adapter = resultAdapter
        setAdaptor()

        searchView.queryHint =
            Html.fromHtml("<font color = #D3D3D3>" + getResources().getString(R.string.search) + "</font>");

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                resultAdapter.filter.filter(newText)
                return true
            }
        })



    }

    override fun onGoalClick(bookAttributes: AssetMain) {
        Application.locateTag = bookAttributes.AssetRFID
        RFIDController.accessControlTag = bookAttributes.AssetRFID
        Application.PreFilterTag = bookAttributes.AssetRFID
        Application.comefrom = "show"



        replaceFragment(
            requireActivity().supportFragmentManager,
            LocateOperationsFragment(),
            R.id.content_frame
        )
    }

//    override fun onDestroy() {
//        super.onDestroy()
//        try {
//          //  decreaseRangeToThirty(300)
//        } catch (e: Exception) {
//            Log.d("decreaseRangeToThirty", e.message!!)
//        }
//    }

    private fun setAdaptor() {
   //     val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
   //     var syncTime = sharedPreference?.getString(Constants.LastSyncTs, "2022-02-08")
     //   var currSyncTime = sdf.format(Date())
       // var deviceId = Settings.Secure.getString(requireActivity().contentResolver, Settings.Secure.ANDROID_ID)
        //     Toast.makeText(activity, syncTime, Toast.LENGTH_SHORT).show()


        inventoryViewModel.dataSyncStatus.observe(viewLifecycleOwner) {isDataSynced->
            Log.e("DATA", "OBSERVE_DATA")
            apiHit=false
            if (isDataSynced) {
                        listBook.clear()
                        CoroutineScope(Dispatchers.IO).launch {
                            listBook.addAll(
                                Application.roomDatabaseBuilder?.getBookDao()?.getBooks()
                                    ?: emptyList()
                            )
                            withContext(Dispatchers.Main) {
                                tvResult.text =
                                    "${getString(R.string.lblResults)} (${listBook.size})"
                                resultAdapter.notifyDataSetChanged()
                            }
                        }

            }
            progressBar.visibility = View.INVISIBLE
            Constants.enableUserInteraction(requireActivity())
           inventoryViewModel.dataSyncStatus.removeObservers(viewLifecycleOwner)
        }
    }

//    private fun getLastSync() {
//        Log.e("DATA", "CALL API")
//        sharedPreference =
//            requireActivity().getSharedPreferences(Constants.PrefenceFileName, Context.MODE_PRIVATE)
//        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
//        var syncTime = sharedPreference?.getString(Constants.LastSyncTs, "2022-02-08")
//        inventoryViewModel.getLastSyncSearch(syncTime)
//    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().unregisterReceiver(receiver)
    }
}


