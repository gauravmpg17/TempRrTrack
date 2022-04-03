package asset.trak.views.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import asset.trak.database.entity.Inventorymaster
import asset.trak.modelsrrtrack.AppTimeStamp
import asset.trak.utils.*
import asset.trak.utils.Constants.disableUserInteraction
import asset.trak.utils.Constants.enableUserInteraction
import asset.trak.utils.Constants.firstTimeKey
import asset.trak.views.activity.TestActivity
import asset.trak.views.baseclasses.BaseFragment
import asset.trak.views.inventory.ViewInventoryFragment
import asset.trak.views.module.InventoryViewModel
import com.markss.rfidtemplate.R
import com.markss.rfidtemplate.application.Application
import com.markss.rfidtemplate.application.Application.bookDao
import com.markss.rfidtemplate.application.Application.roomDatabaseBuilder
import com.markss.rfidtemplate.rapidread.GlobalRapidReadFragment
import com.shashank.sony.fancytoastlib.FancyToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_view_inventory.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class HomeFragment : BaseFragment(R.layout.fragment_home) {
    private val inventoryViewModel: InventoryViewModel by activityViewModels()
    var sharedPreference: SharedPreferences? = null
    private var isFirstInstall: Boolean = false
    private var isDataRefreshed=false
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreference =
            requireActivity().getSharedPreferences(Constants.PrefenceFileName, Context.MODE_PRIVATE)
        listeners()
        if (Constants.isInternetAvailable(requireContext())) {
            disableUserInteraction(requireActivity())
            getLastSync()
        }
    }


    private fun listeners() {
        searchLin.setOnClickListener {
            replaceFragment(
                requireActivity().supportFragmentManager, MyLibrarySearchFragment(),
                R.id.content_frame
            )
        }
        globalInventory.setOnClickListener {
            mainCoroutines {
                try {
                    decreaseRangeToThirty(300)
                } catch (e: Exception) {
                    Log.d("decreaseRangeToThirty", e.message.toString())
                }
                //   getLastSync()
                val pendingInventory = CoroutineScope(Dispatchers.IO).async {
                    roomDatabaseBuilder.getBookDao().getGlobalPendingInventoryScan()
                }.await()

                val cnt = CoroutineScope(Dispatchers.IO).async {
                    roomDatabaseBuilder.getBookDao().getInventoryMasterAllCount()
                }.await()
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)
                val cal = Calendar.getInstance()
                val dateFormat = sdf.format(cal.time)
                if (pendingInventory.isEmpty()) {
                    val inventoryMaster = Inventorymaster(
                        scanID = "A" + UUID.randomUUID().toString(),
                        deviceId = sharedPreference?.getString(Constants.DeviceId, "A").toString(),
                        deviceIdCount = ((cnt ?: 0) + 1),
                        status = Constants.InventoryStatus.PENDING,
                        locationId = 0,
                        scanStartDatetime = dateFormat
                    )
                    ioCoroutines {
                        roomDatabaseBuilder.getBookDao().addInventoryItem(inventoryMaster)
                    }
                }
                replaceFragment(
                    requireActivity().supportFragmentManager,
                    GlobalRapidReadFragment.newInstance("global"),
                    R.id.content_frame
                )
            }
        }
        locationInventory.setOnClickListener {
            inventoryViewModel.isFirstTime = true
            startActivityForResult(
                Intent(
                    requireActivity(),
                    TestActivity::class.java
                ).putExtra("type", "1"), 101
            )
        }
        configLin.setOnClickListener {
            replaceFragment(
                requireActivity().supportFragmentManager, MainConfigurationFragment(),
                R.id.content_frame
            )
        }

        linearSync.setOnClickListener {
            //   disconnectRFIDReader()
            inventoryViewModel.isFirstTime = true
            startActivityForResult(
                Intent(
                    requireActivity(),
                    TestActivity::class.java
                ).putExtra("type", "2"), 101
            )
        }

//        configLin.setOnClickListener {
//            var fragment = AdvancedOptionItemFragment()
//            // fragment = AdvancedOptionItemFragment.newInstance()
//
//
//            replaceFragment(
//                requireActivity().supportFragmentManager, fragment,
//                R.id.content_frame
//            )
//        }

        deleteTables.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val job = CoroutineScope(Dispatchers.IO).async {
                    roomDatabaseBuilder.getBookDao().deleteAssetMainTable()
                    roomDatabaseBuilder.getBookDao().deleteInventoryScanTable()
                    roomDatabaseBuilder.getBookDao().deleteMapRFIDLocationTable()
                    roomDatabaseBuilder.getBookDao().deleteMasterLocationTable()
                    roomDatabaseBuilder.getBookDao().deleteMasterVendorTable()
                    roomDatabaseBuilder.getBookDao().deleteTblAssetCatalogueTable()
                    roomDatabaseBuilder.getBookDao().deleteTblAssetClassCatMapTable()
                    roomDatabaseBuilder.getBookDao().deleteTblAssetClassificationTable()
                    roomDatabaseBuilder.getBookDao().deleteTblBookAttributesTable()
                    roomDatabaseBuilder.getBookDao().deleteTblCatSubCatMapTable()
                    roomDatabaseBuilder.getBookDao().deleteTblCategoryMasterTable()
                    roomDatabaseBuilder.getBookDao().deleteTblInventoryMasterTable()
                    roomDatabaseBuilder.getBookDao().deleteTblLocationMasterTable()
                    roomDatabaseBuilder.getBookDao().deleteTblScanTagTable()
                    roomDatabaseBuilder.getBookDao().deleteTblSubCategoryMasterTable()
                    roomDatabaseBuilder.getBookDao().deleteOffLocation()
                }
                job.await()
                getLastSync(true)
            }
        }
    }


    private fun getLastSync(isFromDelete: Boolean = false) {
        isDataRefreshed=isFromDelete
        progressBar.visibility = View.VISIBLE
        val sharedPreferenceFirstInstall = Application.context.getSharedPreferences(
            Constants.appIstalledFirstTime,
            Context.MODE_PRIVATE
        )
        isFirstInstall = sharedPreferenceFirstInstall.getBoolean(firstTimeKey, true)
        val deviceId =
            Settings.Secure.getString(requireActivity().contentResolver, Settings.Secure.ANDROID_ID)
        val editor = sharedPreference?.edit()
        editor?.putString(Constants.DeviceId, deviceId)
        editor?.commit()
        if (isFirstInstall || isFromDelete) {
            lifecycleScope.launch {
                bookDao?.saveAppTimeStamp(AppTimeStamp(Date()))
                inventoryViewModel.getLastSync("")
                sharedPreferenceFirstInstall.edit().putBoolean(firstTimeKey, false).commit()
            }
        } else {
            lifecycleScope.launch {
                bookDao?.saveAppTimeStamp(AppTimeStamp(Date()))
                val appTimeStamp = async {
                    bookDao?.retriveTimeStamp()
                }.await()
                inventoryViewModel.dateLastSync = apiDateFormat(appTimeStamp?.syncDate!!)
                inventoryViewModel.getLastSync(
                    inventoryViewModel.dateLastSync
                )
            }
        }
        inventoryViewModel.dataSyncStatus.observe(viewLifecycleOwner) { isDataSynced ->
            progressBar.visibility = View.INVISIBLE
            enableUserInteraction(requireActivity())
            if (isDataSynced) {
                if (isDataRefreshed) {
                    FancyToast.makeText(
                        requireActivity(),
                        "Data Refreshed successfully",
                        FancyToast.LENGTH_SHORT,
                        FancyToast.SUCCESS,
                        false
                    ).show()
                } else {
//                 FancyToast.makeText(
//                        requireActivity(),
//                        "Data Synced Successfully",
//                        FancyToast.LENGTH_SHORT,
//                        FancyToast.SUCCESS,
//                        false
//                    ).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101 && resultCode == Activity.RESULT_OK) {
            val type = data?.getStringExtra("type")

            val returnValue: String = data?.getStringExtra("barCode").toString()

            if (type == "1") {
                replaceFragment(
                    requireActivity().supportFragmentManager,
                    ViewInventoryFragment("location", returnValue),
                    R.id.content_frame
                )
            } else {
                replaceFragment(
                    requireActivity().supportFragmentManager,
                    ViewInventoryFragment("rfidlocation", returnValue),
                    R.id.content_frame
                )
            }
        }
    }
}
