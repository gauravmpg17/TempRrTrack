package asset.trak.views.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import asset.trak.database.entity.BookAttributes
import asset.trak.database.entity.Inventorymaster
import asset.trak.utils.Constants
import asset.trak.utils.Constants.disableUserInteraction
import asset.trak.utils.Constants.enableUserInteraction
import asset.trak.views.activity.TestActivity
import asset.trak.views.baseclasses.BaseFragment
import asset.trak.views.inventory.ViewInventoryFragment
import asset.trak.views.module.InventoryViewModel
import com.markss.rfidtemplate.R
import com.markss.rfidtemplate.application.Application
import com.markss.rfidtemplate.application.Application.bookDao
import com.markss.rfidtemplate.rapidread.GlobalRapidReadFragment
import com.markss.rfidtemplate.settings.SettingListFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_home.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


private const val TAG = "HomeFragment"

@AndroidEntryPoint
class HomeFragment : BaseFragment(R.layout.fragment_home) {
    private var isSyncClicked: Boolean = false
    private var badgeBitmap: Bitmap? = null
    private var badgeURI: Uri? = null
    private var isImagedownloaded: Boolean = false
    private var badgeImage: File? = null
    private var listBookAttributes: ArrayList<BookAttributes> = ArrayList()
    private val inventoryViewModel: InventoryViewModel by activityViewModels()
    var sharedPreference: SharedPreferences? = null
    override fun onStart() {
        super.onStart()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // disableUserInteraction(requireActivity())
        sharedPreference =
            requireActivity().getSharedPreferences(Constants.PrefenceFileName, Context.MODE_PRIVATE)
        //  progressBar.visibility=View.VISIBLE
        /*Save Books Data to Database*/
//        saveDataToDataBase()
        listeners()

        if (Constants.isInternetAvailable(requireContext())) {
            if (Application.isFirstTime) {
                Log.e("dhdgdhdh", "getLastSync First")
                disableUserInteraction(requireActivity())
                getLastSync()
            } else {
                Log.e("dhdgdhdh", "getLastSync Not Called")
            }

        }
        Log.e("dhdgdhdh", "djd")
    }


    private fun listeners() {
        searchLin.setOnClickListener {
            replaceFragment(
                requireActivity().supportFragmentManager, MyLibrarySearchFragment(),
                R.id.content_frame
            )
        }
        globalInventory.setOnClickListener {
            //global
            val pendingInventory =
                Application.roomDatabaseBuilder.getBookDao().getGlobalPendingInventoryScan()

            val cnt = Application.roomDatabaseBuilder.getBookDao().getInventoryMasterAllCount()
//
//            val inventoryLastItem: Inventorymaster = if (getListInventoryMaster.isNotEmpty())
//                getListInventoryMaster[getListInventoryMaster.size - 1] else Inventorymaster()
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
                Application.roomDatabaseBuilder.getBookDao().addInventoryItem(inventoryMaster)
            }
            replaceFragment(
                requireActivity().supportFragmentManager,
                GlobalRapidReadFragment.newInstance("global"),
                R.id.content_frame
            )
        }
        locationInventory.setOnClickListener {
//            if (RFIDController.mConnectedReader.isConnected) {
//                RFIDController.is_disconnection_requested = true
//                try {
//                    RFIDController.mConnectedReader.disconnect()
//                } catch (e: InvalidUsageException) {
//                    e.printStackTrace()
//                } catch (e: OperationFailureException) {
//                    e.printStackTrace()
//                }
//            }


//            replaceFragment(
//                requireActivity().supportFragmentManager, InventoryScanFragment(),
//                R.id.content_frame
//            )


            startActivityForResult(
                Intent(
                    requireActivity(),
                    TestActivity::class.java
                ).putExtra("type", "1"), 101
            )
        }
        configLin.setOnClickListener {
            replaceFragment(
                requireActivity().supportFragmentManager, SettingListFragment(),
                R.id.content_frame
            )
        }

        linearSync.setOnClickListener {

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


    }


    private fun getLastSync() {
        progressBar.visibility = View.VISIBLE
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        var syncTime = sharedPreference?.getString(Constants.LastSyncTs, "2022-02-08")
        var currSyncTime = sdf.format(Date())
        var deviceId =
            Settings.Secure.getString(requireActivity().contentResolver, Settings.Secure.ANDROID_ID)
        Toast.makeText(activity, syncTime, Toast.LENGTH_SHORT)
            .show()

        inventoryViewModel.getLastSync(syncTime).observe(viewLifecycleOwner) {

            if (it != null && it.statuscode == 200 && it.data != null) {
                it.data.let {
                    if (!it.AssetMain.isNullOrEmpty()) {
                        bookDao?.addAssetMain(it.AssetMain)
                    }

                    if (!it.InventoryScan.isNullOrEmpty()) {
                        bookDao?.addInventoryScan(it.InventoryScan)
                    }

                    if (!it.MasterLocation.isNullOrEmpty()) {
                        bookDao?.addMasterLocation(it.MasterLocation)
                    }


                    if (!it.MasterVendor.isNullOrEmpty()) {
                        bookDao?.addMasterVendor(it.MasterVendor)
                    }

                    if (!it.Inventorymaster.isNullOrEmpty()) {
                        bookDao?.addInventoryMaster(it.Inventorymaster)
                    }
                }
                Application.isFirstTime = false

//
//            if (!it.categorySubCategoryMap.isNullOrEmpty()) {
//                bookDao?.addCatSubClassification(it.categorySubCategoryMap!!)
//            }
//
//
//            if (!it.locationMaster.isNullOrEmpty()) {
//                bookDao?.addLocationMasterList(it.locationMaster!!)
//            }
//
//
//            if (!it.assetCatalogue.isNullOrEmpty()) {
//                it.assetCatalogue!!.forEach {
//                    Glide.with(this)
//                        .asBitmap()
//                        .load(it.imageUrl)
//                        .into(object : CustomTarget<Bitmap>(){
//                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
//
//                                badgeBitmap=resource
//                                val savedUri = saveToInternalStorage(requireContext(),it.imageId!!)
//                                savePath=savedUri.toString()
//                                it.imagePathFile= savePath
//                                //   it.imagePathFile= downloadImage("https://images.pexels.com/photos/212286/pexels-photo-212286.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=650&w=940",it.imageId!!)
//                                it.imagePathFile= downloadImage(it.imageUrl!!,it.imageId!!)
//
//                                bookDao?.addAssetCatalogueList(
//                                    AssetCatalogue(
//                                        it.id,
//                                        it.assetName,
//                                        it.imagePath,
//                                        it.imagePathFile,
//                                        it.description,
//                                        it.assetClassId,
//                                        it.categoryId,
//                                        it.subCategoryId,
//                                        it.locationId,
//                                        it.rfidTag,
//                                        it.searchTags,
//                                        it.inventorySyncFlag,
//                                        it.createdOn,
//                                        it.modifiedOn,
//                                        it.inventorySyncOn,
//                                        it.inventoryScanId,
//                                        it.Image,
//                                        it.locationName,
//                                        it.categoryName,
//                                        it.subCategoryName,
//                                        it.isSelected,
//                                        it.imageUrl,
//                                        it.imageId
//                                    ))
//
//
//                            }
//                            override fun onLoadCleared(placeholder: Drawable?) {
//
//                            }
//
//                            override fun onLoadFailed(errorDrawable: Drawable?) {
//                                super.onLoadFailed(errorDrawable)
//                                //    Log.d(TAG, ": ${it.imagePathFile}")
//                                //     bookDao?.addAssetCatalogueList(it.assetCatalogue!!)
//
//
//                                bookDao?.addAssetCatalogueList(
//                                    AssetCatalogue(
//                                        it.id,
//                                        it.assetName,
//                                        it.imagePath,
//                                        it.imagePathFile,
//                                        it.description,
//                                        it.assetClassId,
//                                        it.categoryId,
//                                        it.subCategoryId,
//                                        it.locationId,
//                                        it.rfidTag,
//                                        it.searchTags,
//                                        it.inventorySyncFlag,
//                                        it.createdOn,
//                                        it.modifiedOn,
//                                        it.inventorySyncOn,
//                                        it.inventoryScanId,
//                                        it.Image,
//                                        it.locationName,
//                                        it.categoryName,
//                                        it.subCategoryName,
//                                        it.isSelected,
//                                        it.imageUrl,
//                                        it.imageId
//                                    ))
//                            }
//                        })
//                }
//            }
//
//            if (!it.deviceId.isNullOrEmpty()){
//                deviceId= it.deviceId!!;
//            }
//            if (!it.bookAttributes.isNullOrEmpty()) {
////                bookDao?.addBookAttributeList(it.bookAttributes!!)
//                inventoryViewModel.listBookAttributes.clear()
//                inventoryViewModel.listBookAttributes.addAll(it.bookAttributes!!)
//                //   Log.d(TAG, "getLastSync:${ inventoryViewModel.listBookAttributes.size} ")
//                progressBar.visibility=View.INVISIBLE
//                enableUserInteraction(requireActivity())
//                if(isSyncClicked)
//                {
//                    Toast.makeText(activity, R.string.data_sync_success, Toast.LENGTH_SHORT)
//                        .show()
//                    isSyncClicked=false
//                }
//            }

            }
            //save last sync time in sp
            var editor = sharedPreference?.edit()
            editor?.putString(Constants.LastSyncTs, currSyncTime)
            editor?.putString(Constants.DeviceId, deviceId)
            editor?.commit()
            progressBar.visibility = View.INVISIBLE
            enableUserInteraction(requireActivity())

            Toast.makeText(activity, "Saved SynTime in sp:$currSyncTime", Toast.LENGTH_SHORT)
                .show()
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
                    requireActivity().supportFragmentManager, ViewInventoryFragment("rfidlocation",returnValue),
                    R.id.content_frame
                )
            }
        }
    }
}
