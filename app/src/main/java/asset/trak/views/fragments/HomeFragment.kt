package asset.trak.views.fragments

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import asset.trak.database.entity.AssetCatalogue
import asset.trak.database.entity.BookAttributes
import asset.trak.utils.Constants
import asset.trak.utils.Constants.disableUserInteraction
import asset.trak.utils.Constants.enableUserInteraction
import asset.trak.views.baseclasses.BaseFragment
import asset.trak.views.module.InventoryViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.markss.rfidtemplate.R
import com.markss.rfidtemplate.application.Application
import com.markss.rfidtemplate.application.Application.bookDao
import com.markss.rfidtemplate.rapidread.RapidReadFragment
import com.markss.rfidtemplate.settings.AdvancedOptionItemFragment
import com.markss.rfidtemplate.settings.SettingListFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


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
            replaceFragment(
                requireActivity().supportFragmentManager, RapidReadFragment.newInstance("global"),
                R.id.content_frame
            )
        }
        locationInventory.setOnClickListener {
            replaceFragment(
                requireActivity().supportFragmentManager, RapidReadFragment.newInstance("location"),
                R.id.content_frame
            )
        }
        configLin.setOnClickListener {

            replaceFragment(
                requireActivity().supportFragmentManager, SettingListFragment(),
                R.id.content_frame
            )
        }

        linearSync.setOnClickListener {
            if (Constants.isInternetAvailable(requireContext())) {
                isSyncClicked = true
                //    progressBar.visibility=View.VISIBLE
                getLastSync()
            }
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
        var deviceId = "A"
        Toast.makeText(activity, syncTime, Toast.LENGTH_SHORT)
            .show()

        inventoryViewModel.getLastSync(syncTime).observe(viewLifecycleOwner) {

            if (it != null && it.statuscode == 200 && it.data != null)  {
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


        var savePath: String? = null

//    fun downloadImage(imageUrl:String,fileName: String):String{
//        Glide.with(this)
//            .asBitmap()
//            .load(imageUrl)
//            .into(object : CustomTarget<Bitmap>(){
//                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
//                    badgeBitmap=resource
//                    val savedUri = saveToInternalStorage(requireContext(),fileName)
//                    savePath=savedUri.toString()
//                }
//                override fun onLoadCleared(placeholder: Drawable?) {
//
//                }
//            })
//
//        return savePath?:""
//    }

//    private fun DownloadImageFromPath(path: String?,fileName: String) {
//        Log.d("img==",path.toString())
//
//        val urlImage = URL(path.toString())
//        val result: Deferred<Bitmap?> = GlobalScope.async {
//            urlImage.toBitmap()
//        }
//
//        GlobalScope.launch(Dispatchers.Main) {
//            val bitmap: Bitmap? = result.await()
//            bitmap?.apply {
//                badgeBitmap = this
//                val savedUri: Uri? = saveToInternalStorage(requireContext(),fileName)
////                roomDatabaseBuilder.
//                Log.d("uri==", savedUri.toString())
//            }
//        }
//    }
//
//    // extension function to get / download bitmap from url
//    private fun URL.toBitmap(): Bitmap? {
//        return try {
//            BitmapFactory.decodeStream(openStream())
//        } catch (e: IOException) {
//            null
//        }
//    }
//
//    // extension function to save an image to internal storage
//    private fun saveToInternalStorage(context: Context,fileName:String): Uri? {
//        return try {
//            val downLoadfileName: String = Constants.convertBitmapToFile(context,badgeBitmap!!,fileName)!!
//            val file = File(downLoadfileName)
//            badgeImage = file
//            isImagedownloaded = true
//            badgeURI = Uri.parse(file.absolutePath)
//            Uri.parse(file.absolutePath)
//        } catch (e: Exception) { // catch the exception
//            e.printStackTrace()
//            null
//        }
//    }
//    @SuppressLint("CheckResult")
//    private fun downloadImage(url: String, index: Int) {
//        val client = OkHttpClient()
//        val request = Request.Builder()
//            .url(url)
//            .build()
//
//        try {
//            val response = client.newCall(request).execute()
//            val bitmap = BitmapFactory.decodeStream(response.body?.byteStream())
//
//            .saveBitmap(mContext, bitmap, image.title).subscribe({ img ->
//                displayNotification(ProgressUpdateEvent(image.title, 3, index + 1))
//                EventBus.getDefault().post(ImageDownloadedEvent(img, image.title, image.id))
//            }, { error ->
//                error.printStackTrace()
//            })
//
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }


    }
}
