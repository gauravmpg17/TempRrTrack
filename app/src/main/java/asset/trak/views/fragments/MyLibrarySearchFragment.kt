package asset.trak.views.fragments

import android.content.Context
import android.content.SharedPreferences
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
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MyLibrarySearchFragment : BaseFragment(R.layout.fragment_my_library_search),
    OnResultClickListener {

    private lateinit var resultAdapter: ResultAdapter
    private var listBook = ArrayList<AssetMain>()
    private lateinit var navController: NavController
    var sharedPreference: SharedPreferences? = null
    private val inventoryViewModel: InventoryViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreference =
            requireActivity().getSharedPreferences(Constants.PrefenceFileName, Context.MODE_PRIVATE)

        ivBack.setOnClickListener {
            getBackToPreviousFragment()
        }
        setAdaptor()
        searchView.queryHint = Html.fromHtml("<font color = #D3D3D3>" + getResources().getString(R.string.search) + "</font>");

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {

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
        listBook.clear()
        listBook.addAll(Application.roomDatabaseBuilder?.getBookDao()?.getBooks() ?: emptyList())
        if(listBook.isEmpty())
        {
            getLastSync()
        }


//        Log.e("ss",""+listBook[0].assetCatalogue.imagePathFile)

        resultAdapter = ResultAdapter(requireContext(), this, listBook,true)
        rvResult.adapter = resultAdapter
    }

    private fun getLastSync() {
        progressBar.visibility = View.VISIBLE
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        var syncTime = sharedPreference?.getString(Constants.LastSyncTs, "2022-02-08")
        var currSyncTime = sdf.format(Date())
        var deviceId =
            Settings.Secure.getString(requireActivity().contentResolver, Settings.Secure.ANDROID_ID)
        //     Toast.makeText(activity, syncTime, Toast.LENGTH_SHORT).show()

        inventoryViewModel.getLastSync(syncTime).observe(viewLifecycleOwner) {

            if (it != null && it.statuscode == 200 && it.data != null) {
                it.data.let {
                    if (!it.AssetMain.isNullOrEmpty()) {
                        Application.bookDao?.addAssetMain(it.AssetMain)
                        listBook.addAll(Application.roomDatabaseBuilder?.getBookDao()?.getBooks() ?: emptyList())
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
}


