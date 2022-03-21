package asset.trak.views.inventory

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import asset.trak.database.entity.Inventorymaster
import asset.trak.database.entity.LocationMaster
import asset.trak.modelsrrtrack.MasterLocation
import asset.trak.scannercode.DWInterface
import asset.trak.scannercode.DWReceiver
import asset.trak.utils.Constants
import asset.trak.utils.Constants.disableUserInteraction
import asset.trak.views.baseclasses.BaseFragment
import asset.trak.views.fragments.InventoryScanFragment.Companion.PROFILE_INTENT_ACTION
import asset.trak.views.fragments.InventoryScanFragment.Companion.PROFILE_INTENT_START_ACTIVITY
import asset.trak.views.fragments.InventoryScanFragment.Companion.PROFILE_NAME
import asset.trak.views.module.InventoryViewModel
import cafe.adriel.kbus.KBus
import cafe.adriel.kbus.KBus.post
import com.darryncampbell.datawedgekotlin.ObservableObject
import com.markss.rfidtemplate.R
import com.markss.rfidtemplate.application.Application
import com.markss.rfidtemplate.application.Application.roomDatabaseBuilder
import com.markss.rfidtemplate.home.MainActivity
import com.markss.rfidtemplate.rapidread.MapRFIDLocationFragment
import com.markss.rfidtemplate.rapidread.RapidReadFragment
import com.shashank.sony.fancytoastlib.FancyToast
import kotlinx.android.synthetic.main.fragment_view_inventory.*
import java.text.SimpleDateFormat
import java.util.*


class ViewInventoryFragment(val isFromWhat: String) :
    BaseFragment(R.layout.fragment_view_inventory), Observer {

    private var barCodeName: String = ""

    //   private var barCodeName: String = ""
    private var listOfLocations = ArrayList<LocationMaster>()
    private var currLocId = 0
    private var currMasterLocation: MasterLocation? = null
    var sharedPreference: SharedPreferences? = null
    private val inventoryViewModel: InventoryViewModel by activityViewModels()
    var deviceId = "A"


    private val dwInterface = DWInterface();
    private val receiver = DWReceiver()
    private var initialized = false
    private var version65OrOver = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreference = requireActivity().getSharedPreferences(
            Constants.PrefenceFileName,
            Context.MODE_PRIVATE
        )
        deviceId = sharedPreference?.getString(Constants.DeviceId, "A").toString()
        initialisation()
        setAdaptor()
        listeners()
    //    Log.d("tag1212121", "onViewCreated: ${Application.isReconsiled}")
        if (Application.isReconsiled) {
            getLastSync()
        }

        ObservableObject.instance.addObserver(this)
        val intentFilter = IntentFilter()
        intentFilter.addAction(DWInterface.DATAWEDGE_RETURN_ACTION)
        intentFilter.addCategory(DWInterface.DATAWEDGE_RETURN_CATEGORY)
        requireActivity().registerReceiver(receiver, intentFilter)

//        etRfid.doOnTextChanged { text, start, before, count ->
//            inventoryViewModel.b
//        }

//        ivScanBar.setOnTouchListener { _, event ->
//            when (event?.action) {
//                MotionEvent.ACTION_DOWN -> {
//                    dwInterface.sendCommandString(
//                        requireActivity().applicationContext,
//                        DWInterface.DATAWEDGE_SEND_SET_SOFT_SCAN,
//                        "START_SCANNING"
//                    )
//                    return@setOnTouchListener true
//                }
//            }
//            return@setOnTouchListener false
     //   }
    }

    override fun onStop() {
        super.onStop()
        dwInterface.sendCommandString(
            requireActivity().applicationContext,
            DWInterface.DATAWEDGE_SEND_SET_SOFT_SCAN,
            "STOP_SCANNING"
        )
    }


    private fun getLastSync() {
        progressBar.visibility = View.VISIBLE
        disableUserInteraction(requireActivity())
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val syncTime = sharedPreference?.getString(Constants.LastSyncTs, "2022-02-08")
        val currSyncTime = sdf.format(Date())
        val deviceId =
            Settings.Secure.getString(requireActivity().contentResolver, Settings.Secure.ANDROID_ID)
        Toast.makeText(activity, syncTime, Toast.LENGTH_SHORT)
            .show()

        inventoryViewModel.getLastSync(syncTime).observe(viewLifecycleOwner) {

            if (it != null && it.statuscode == 200 && it.data != null) {
                it.data.let {
                    if (!it.AssetMain.isNullOrEmpty()) {
                        Application.bookDao?.addAssetMain(it.AssetMain)
                    }

                    if (!it.InventoryScan.isNullOrEmpty()) {
                        Application.bookDao?.addInventoryScan(it.InventoryScan)
                    }

                    if (!it.MasterLocation.isNullOrEmpty()) {
                        Application.bookDao?.addMasterLocation(it.MasterLocation)
                    }


                    if (!it.MasterVendor.isNullOrEmpty()) {
                        Application.bookDao?.addMasterVendor(it.MasterVendor)
                    }

                    if (!it.Inventorymaster.isNullOrEmpty()) {
                        Application.bookDao?.addInventoryMaster(it.Inventorymaster)
                    }
                }
                Application.isReconsiled = false
            }
            //save last sync time in sp
            var editor = sharedPreference?.edit()
            editor?.putString(Constants.LastSyncTs, currSyncTime)
            editor?.putString(Constants.DeviceId, deviceId)
            editor?.commit()
            progressBar.visibility = View.INVISIBLE
            Constants.enableUserInteraction(requireActivity())
            Toast.makeText(activity, "Saved SynTime in sp:$currSyncTime", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun initialisation() {
        listOfLocations.clear()
        listOfLocations.addAll(roomDatabaseBuilder.getBookDao().getLocationMasterList())
    }

    private fun setAdaptor() {
        val listOfItems = ArrayList<String>()
        listOfItems.add(0, "Select Location")
        listOfLocations.forEach {
            listOfItems.add(it.locationName ?: "")
        }

        /* etRfid.addTextChangedListener(object : TextWatcher {

             override fun afterTextChanged(s: Editable) {
                 if (s.toString().trim().isEmpty()) {
                     tvLocation.text = ""
                     FancyToast.makeText(
                         requireActivity(),
                         "Please Enter Barcode.",
                         FancyToast.LENGTH_LONG,
                         FancyToast.WARNING,
                         false
                     ).show()
                 } else if (s.toString().length >= 4) {
                     barCodeName = s.toString().trim()
                     //here
                     currMasterLocation = Application.bookDao.getLocationMasterDataRR(barCodeName)
                     currMasterLocation?.let {
                         it.Name?.let {
                             tvLocation.text = it
                         }

                         currLocId = currMasterLocation!!.LocID
                         var lastScanId = ""
                         var lastRecodedDate = ""
                         var registeredAsPerLastScan = 0
                         var newlyRegistered = 0
                         if (currLocId != null) {
                             val invData = roomDatabaseBuilder.getBookDao()
                                 .getLastRecordedInventoryOfLocation(currLocId)
                             if (invData.count() > 0) {
                                 lastRecodedDate = invData.get(0).scanOn.toString()
                                 lastScanId = invData.get(0).scanID
                                 //registeredAsPerLastScan= roomDatabaseBuilder.getBookDao().getCountLastScanRegistered(locId,lastScanId)
                                 registeredAsPerLastScan = roomDatabaseBuilder.getBookDao()
                                     .getCountOfRegisteredAsPerLastInventoryOfLocation(
                                         currLocId,
                                         lastScanId
                                     )
                                 newlyRegistered = roomDatabaseBuilder.getBookDao()
                                     .getCountNewlyRegisteredAfterLastScan(currLocId, lastScanId)

                             } else {
                                 registeredAsPerLastScan = 0
                                 newlyRegistered =
                                     roomDatabaseBuilder.getBookDao().getCountLocationId(currLocId)
                             }
                         }


                         tvRegisteredCount.text = registeredAsPerLastScan.toString()
                         tvNewlyScanCount.text = newlyRegistered.toString()

                     }
                 } else {
                     tvLocation.text = ""
                     Log.d("tag1111", "afterTextChanged: Length ${s.length}")
                 }
             }

             override fun beforeTextChanged(
                 s: CharSequence, start: Int,
                 count: Int, after: Int
             ) {
             }

             override fun onTextChanged(
                 s: CharSequence, start: Int,
                 before: Int, count: Int
             ) {
                 // tvSample.setText("Text is : "+s)

             }
         })*/
    }

    private fun listeners() {
        ivBackButton.setOnClickListener {
            getBackToPreviousFragment()
        }

        buttonscan.setOnClickListener {
            //   Log.d("newwww", "listeners1: ${it}")
            barCodeName = etRfid.text.toString()
           // barCodeName="1000024"

            if (barCodeName.isEmpty()) {
                tvLocation.text = ""
                FancyToast.makeText(
                    requireActivity(),
                    "Please Enter Barcode.",
                    FancyToast.LENGTH_LONG,
                    FancyToast.WARNING,
                    false
                ).show()
            } else if (barCodeName.isNotEmpty()) {
                //here
                currMasterLocation = Application.bookDao.getLocationMasterDataRR(barCodeName)
                currMasterLocation?.let {
                    it.Name?.let {
                        tvLocation.text = it
                        //   Log.d("newwww", "listeners3: ${it}")
                    }

                    currLocId = currMasterLocation!!.LocID
                    var lastScanId = ""
                    var lastRecodedDate = ""
                    var registeredAsPerLastScan = 0
                    var newlyRegistered = 0
                    if (currLocId != 0) {
                        val invData = roomDatabaseBuilder.getBookDao()
                            .getLastRecordedInventoryOfLocation(currLocId)
                        if (invData.count() > 0) {
                            lastRecodedDate = invData.get(0).scanOn.toString()
                            lastScanId = invData.get(0).scanID
                            //registeredAsPerLastScan= roomDatabaseBuilder.getBookDao().getCountLastScanRegistered(locId,lastScanId)
                            registeredAsPerLastScan = roomDatabaseBuilder.getBookDao()
                                .getCountOfRegisteredAsPerLastInventoryOfLocation(
                                    currLocId,
                                    lastScanId
                                )
                            newlyRegistered = roomDatabaseBuilder.getBookDao()
                                .getCountNewlyRegisteredAfterLastScan(currLocId, lastScanId)

                        } else {
                            registeredAsPerLastScan = 0
                            newlyRegistered =
                                roomDatabaseBuilder.getBookDao().getCountLocationId(currLocId)
                        }
                    }


                    tvRegisteredCount.text = registeredAsPerLastScan.toString()
                    tvNewlyScanCount.text = newlyRegistered.toString()

                    //  requireActivity().hideKeyboard(it)
                    if (currMasterLocation == null) {
                        FancyToast.makeText(
                            requireActivity(),
                            "Please Enter Valid Bar Code.",
                            FancyToast.LENGTH_LONG,
                            FancyToast.WARNING,
                            false
                        ).show()
                    } else {
                        currLocId = currMasterLocation!!.LocID
                        val pendingInventory =
                            roomDatabaseBuilder.getBookDao().getPendingInventoryScan(currLocId)

                        val cnt = roomDatabaseBuilder.getBookDao().getInventoryMasterAllCount()
//
//            val inventoryLastItem: Inventorymaster = if (getListInventoryMaster.isNotEmpty())
//                getListInventoryMaster[getListInventoryMaster.size - 1] else Inventorymaster()
                        //inventoryMaster.setStatus(asset.trak.utils.Constants.InventoryStatus.COMPLETED);
                        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)
                        val cal = Calendar.getInstance()
                        val dateFormat = sdf.format(cal.time)
                        if (pendingInventory.isEmpty()) {
                            Log.d("tag1212", "listeners: " + UUID.randomUUID().toString())
                            val inventoryMaster = Inventorymaster(
                                scanID = "A" + UUID.randomUUID().toString(),
                                deviceId = deviceId,
                                deviceIdCount = ((cnt ?: 0) + 1),
                                status = Constants.InventoryStatus.PENDING,
                                locationId = currLocId,
                                scanStartDatetime = dateFormat
                            )
                            roomDatabaseBuilder.getBookDao().addInventoryItem(inventoryMaster)
                            roomDatabaseBuilder.getBookDao()
                                .resetScanIdOfAssetsAtLocation(currLocId)
                        }
                        val fragmentRapidReadFragment = RapidReadFragment()
                        val mapRFIDLocationFragment = MapRFIDLocationFragment()
                        val bundle = Bundle()
                        bundle.putParcelable(
                            "LocationData",
                            currMasterLocation
                        )
                        bundle.putInt(
                            "totalRegistered", tvRegisteredCount.text.toString().toInt().plus(
                                tvNewlyScanCount.text.toString().toInt()
                            )
                        )
                        fragmentRapidReadFragment.arguments = bundle
                        mapRFIDLocationFragment.arguments = bundle
                        if (isFromWhat.equals("location")) {
                            replaceFragment(
                                requireActivity().supportFragmentManager, fragmentRapidReadFragment,
                                R.id.content_frame
                            )
                        } else if (isFromWhat.equals("rfidlocation")) {
                            replaceFragment(
                                requireActivity().supportFragmentManager, mapRFIDLocationFragment,
                                R.id.content_frame
                            )
                        }
                    }
                }
            } else {
                tvLocation.text = ""
            }
        }
    }


    fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }


    override fun onResume() {
        super.onResume()
        if (!initialized) {
            //  Create profile to be associated with this application
            dwInterface.sendCommandString(
                requireActivity(),
                DWInterface.DATAWEDGE_SEND_GET_VERSION,
                ""
            )
            initialized = true
        }
    }

    override fun onStart() {
        super.onStart()
        dwInterface.sendCommandString(
            requireActivity().applicationContext,
            DWInterface.DATAWEDGE_SEND_SET_SOFT_SCAN,
            "START_SCANNING"
        )
        if (childFragmentManager.findFragmentByTag(MainActivity.TAG_CONTENT_FRAGMENT) is ViewInventoryFragment) {
            if (requireActivity().intent.hasExtra(DWInterface.DATAWEDGE_SCAN_EXTRA_DATA_STRING)) {
                val ScanData: String? =
                    requireActivity().intent.getStringExtra(DWInterface.DATAWEDGE_SCAN_EXTRA_DATA_STRING)
               // post(ScanData)
                ScanData?.let {
                    if (it.isNotEmpty()) {
                        etRfid.setText(it.trim())
                        //     val s = it
                        // barCodeName = s.trim()
                        //here
                        currMasterLocation = Application.bookDao.getLocationMasterDataRR(it.trim())

                        //       Toast.makeText(requireContext(),"Location ${currMasterLocation!!.Name.toString()}",Toast.LENGTH_LONG).show()
                        currMasterLocation?.let {
                            it.Name?.let {
                                tvLocation.text = it
                            }

                            currLocId = currMasterLocation!!.LocID
                            var lastScanId = ""
                            var lastRecodedDate = ""
                            var registeredAsPerLastScan = 0
                            var newlyRegistered = 0
                            if (currLocId != null) {
                                val invData = roomDatabaseBuilder.getBookDao()
                                    .getLastRecordedInventoryOfLocation(currLocId)
                                if (invData.count() > 0) {
                                    lastRecodedDate = invData.get(0).scanOn.toString()
                                    lastScanId = invData.get(0).scanID
                                    //registeredAsPerLastScan= roomDatabaseBuilder.getBookDao().getCountLastScanRegistered(locId,lastScanId)
                                    registeredAsPerLastScan = roomDatabaseBuilder.getBookDao()
                                        .getCountOfRegisteredAsPerLastInventoryOfLocation(
                                            currLocId,
                                            lastScanId
                                        )
                                    newlyRegistered = roomDatabaseBuilder.getBookDao()
                                        .getCountNewlyRegisteredAfterLastScan(currLocId, lastScanId)

                                } else {
                                    registeredAsPerLastScan = 0
                                    newlyRegistered =
                                        roomDatabaseBuilder.getBookDao().getCountLocationId(currLocId)
                                }
                            }


                            tvRegisteredCount.text = registeredAsPerLastScan.toString()
                            tvNewlyScanCount.text = newlyRegistered.toString()

                        }


                    }
                }

            }
        }
    }

    override fun update(p0: Observable?, p1: Any?) {
        var receivedIntent = p1 as Intent
        if (receivedIntent.hasExtra(DWInterface.DATAWEDGE_RETURN_VERSION)) {
            val version = receivedIntent.getBundleExtra(DWInterface.DATAWEDGE_RETURN_VERSION);
            val dataWedgeVersion =
                version?.getString(DWInterface.DATAWEDGE_RETURN_VERSION_DATAWEDGE);
            if (dataWedgeVersion != null && dataWedgeVersion >= "6.5" && !version65OrOver) {
                version65OrOver = true
                createDataWedgeProfile()
            }
        }
    }


    private fun createDataWedgeProfile() {
        dwInterface.sendCommandString(
            requireActivity(),
            DWInterface.DATAWEDGE_SEND_CREATE_PROFILE,
            PROFILE_NAME
        )
        val profileConfig = Bundle()
        profileConfig.putString("PROFILE_NAME", PROFILE_NAME)
        profileConfig.putString("PROFILE_ENABLED", "true") //  These are all strings
        profileConfig.putString("CONFIG_MODE", "UPDATE")
        val barcodeConfig = Bundle()
        barcodeConfig.putString("PLUGIN_NAME", "BARCODE")
        barcodeConfig.putString(
            "RESET_CONFIG",
            "true"
        ) //  This is the default but never hurts to specify
        val barcodeProps = Bundle()
        barcodeConfig.putBundle("PARAM_LIST", barcodeProps)
        profileConfig.putBundle("PLUGIN_CONFIG", barcodeConfig)
        val appConfig = Bundle()
        appConfig.putString(
            "PACKAGE_NAME",
            requireActivity().packageName
        )      //  Associate the profile with this app
        appConfig.putStringArray("ACTIVITY_LIST", arrayOf("*"))
        profileConfig.putParcelableArray("APP_LIST", arrayOf(appConfig))
        dwInterface.sendCommandBundle(
            requireActivity(),
            DWInterface.DATAWEDGE_SEND_SET_CONFIG,
            profileConfig
        )
        //  You can only configure one plugin at a time in some versions of DW, now do the intent output
        profileConfig.remove("PLUGIN_CONFIG")
        val intentConfig = Bundle()
        intentConfig.putString("PLUGIN_NAME", "INTENT")
        intentConfig.putString("RESET_CONFIG", "true")
        val intentProps = Bundle()
        intentProps.putString("intent_output_enabled", "true")
        intentProps.putString("intent_action", PROFILE_INTENT_ACTION)
        intentProps.putString("intent_delivery", PROFILE_INTENT_START_ACTIVITY)  //  "0"
        intentConfig.putBundle("PARAM_LIST", intentProps)
        profileConfig.putBundle("PLUGIN_CONFIG", intentConfig)
        dwInterface.sendCommandBundle(
            requireActivity(),
            DWInterface.DATAWEDGE_SEND_SET_CONFIG,
            profileConfig
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().unregisterReceiver(receiver)
    }
}