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
import androidx.navigation.NavController
import asset.trak.modelsrrtrack.AssetMain
import asset.trak.utils.Constants
import asset.trak.utils.decreaseRangeToThirty
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MyLibrarySearchFragment : BaseFragment(R.layout.fragment_my_library_search),
    OnResultClickListener {

    private lateinit var resultAdapter: ResultAdapter
    private var listBook = ArrayList<AssetMain>()
    var sharedPreference: SharedPreferences? = null
    private val inventoryViewModel: InventoryViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getLastSync()
    }

    val receiver=object : BroadcastReceiver(){
        override fun onReceive(p0: Context?, intent: Intent?) {
            tvResult.text="${getString(R.string.lblResults)} (${intent?.getIntExtra("searchCount",0)})"
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().registerReceiver(receiver, IntentFilter("COUNT_UPDATE_SEARCH"))
        progressBar.visibility = View.VISIBLE
        ivBack.setOnClickListener {
            getBackToPreviousFragment()
        }
        resultAdapter = ResultAdapter(requireContext(), this, listBook,true)
        rvResult.adapter = resultAdapter
        setAdaptor()

        searchView.queryHint = Html.fromHtml("<font color = #D3D3D3>" + getResources().getString(R.string.search) + "</font>");

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                if(!newText.isNullOrEmpty())
                {
                    resultAdapter.filter.filter(newText)
                }
                else
                {
                    listBook.clear()
                    listBook.addAll(Application.roomDatabaseBuilder?.getBookDao()?.getBooks() ?: emptyList())
                    resultAdapter = ResultAdapter(requireContext(), this@MyLibrarySearchFragment, listBook,true)
                    rvResult.adapter = resultAdapter
                    tvResult.text="${getString(R.string.lblResults)} (${resultAdapter.itemCount})"
                }

                return true
            }
        })

    }

    override fun onGoalClick(bookAttributes: AssetMain) {
//        try {
//            decreaseRangeToThirty(300)
//        }
//        catch (e: Exception){
//            Log.d("decreaseRangeToThirty", e.message.toString())
//        }

        Log.d("tag123", "onGoalClick: ${bookAttributes.AssetRFID}")
        Application.locateTag = bookAttributes.AssetRFID
        RFIDController.accessControlTag =bookAttributes.AssetRFID
        Application.PreFilterTag =bookAttributes.AssetRFID
        Application.comefrom ="show"



        replaceFragment(requireActivity().supportFragmentManager, LocateOperationsFragment(), R.id.content_frame)
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
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        var syncTime = sharedPreference?.getString(Constants.LastSyncTs, "2022-02-08")
        var currSyncTime = sdf.format(Date())
        var deviceId =
            Settings.Secure.getString(requireActivity().contentResolver, Settings.Secure.ANDROID_ID)
        //     Toast.makeText(activity, syncTime, Toast.LENGTH_SHORT).show()

        inventoryViewModel.mLastSyncData.observe(viewLifecycleOwner) {

            if (it != null && it.statuscode == 200 && it.data != null) {
                it.data.let {
                    if (!it.AssetMain.isNullOrEmpty()) {
                        listBook.clear()
                        Application.bookDao?.addAssetMain(it.AssetMain)
                        listBook.addAll(Application.roomDatabaseBuilder?.getBookDao()?.getBooks() ?: emptyList())
                        tvResult.text="${getString(R.string.lblResults)} (${listBook.size})"
                        resultAdapter.notifyDataSetChanged()
                        Log.d("tag1212121", "setAdaptor: ${listBook.size} ")
                    }
                }
            }
            //save last sync time in sp
            var editor = sharedPreference?.edit()
            editor?.putString(Constants.LastSyncTs, currSyncTime)
            editor?.putString(Constants.DeviceId, deviceId)
            editor?.commit()
            progressBar.visibility = View.INVISIBLE
            Constants.enableUserInteraction(requireActivity())
        }
    }

    private fun getLastSync() {
        sharedPreference =
            requireActivity().getSharedPreferences(Constants.PrefenceFileName, Context.MODE_PRIVATE)
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        var syncTime = sharedPreference?.getString(Constants.LastSyncTs, "2022-02-08")
        inventoryViewModel.getLastSync(syncTime)
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().unregisterReceiver(receiver)
    }
}


