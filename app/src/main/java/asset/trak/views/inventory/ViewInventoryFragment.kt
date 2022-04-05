package asset.trak.views.inventory

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import asset.trak.database.entity.Inventorymaster
import asset.trak.database.entity.LocationMaster
import asset.trak.modelsrrtrack.AppTimeStamp
import asset.trak.modelsrrtrack.MasterLocation
import asset.trak.utils.*
import asset.trak.utils.Constants.disableUserInteraction
import asset.trak.views.activity.TestActivity
import asset.trak.views.baseclasses.BaseFragment
import asset.trak.views.module.InventoryViewModel
import com.markss.rfidtemplate.R
import com.markss.rfidtemplate.application.Application
import com.markss.rfidtemplate.application.Application.*
import com.markss.rfidtemplate.rapidread.MapRFIDLocationFragment
import com.markss.rfidtemplate.rapidread.RapidReadFragment
import com.shashank.sony.fancytoastlib.FancyToast
import kotlinx.android.synthetic.main.fragment_view_inventory.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class ViewInventoryFragment(val isFromWhat: String, var barCodeTag: String? = null) :
    BaseFragment(R.layout.fragment_view_inventory) {
    private var barCodeName: String = ""
    private var listOfLocations = ArrayList<LocationMaster>()
    private var currLocId = 0
    private var currMasterLocation: MasterLocation? = null
    var sharedPreference: SharedPreferences? = null
    private val inventoryViewModel: InventoryViewModel by activityViewModels()
    var deviceId = "A"

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        range_seekbar2.thumbTintList = ColorStateList.valueOf(resources.getColor(R.color.green))
        range_seekbar2.setLabelFormatter { value: Float ->
            return@setLabelFormatter value.toInt().toString()
        }

        if (isFromWhat.equals("location")) {
            range_seekbar2.valueFrom = 0f
            range_seekbar2.valueTo = 200f
            range_seekbar2.value = 200f
        }

        range_seekbar2.addOnChangeListener { rangeSlider, value, fromUser ->
            // Responds to when slider's value is changed
            if (isFromWhat.equals("location")) {
                if (value.toInt() == 0 || value < 100) {
                    range_seekbar2.thumbTintList =
                        ColorStateList.valueOf(resources.getColor(R.color.red))
                } else if (value.toInt() == 100 || value < 200) {
                    range_seekbar2.thumbTintList =
                        ColorStateList.valueOf(resources.getColor(R.color.colorAccent))
                } else {
                    range_seekbar2.thumbTintList =
                        ColorStateList.valueOf(resources.getColor(R.color.green))
                }
            } else {
                if (value.toInt() == 18 || value < 24) {
                    range_seekbar2.thumbTintList =
                        ColorStateList.valueOf(resources.getColor(R.color.red))
                } else if (value.toInt() == 24 || value < 30) {
                    range_seekbar2.thumbTintList =
                        ColorStateList.valueOf(resources.getColor(R.color.colorAccent))
                } else {
                    range_seekbar2.thumbTintList =
                        ColorStateList.valueOf(resources.getColor(R.color.green))
                }
            }
        }

        sharedPreference = requireActivity().getSharedPreferences(
            Constants.PrefenceFileName,
            Context.MODE_PRIVATE
        )
        deviceId = sharedPreference?.getString(Constants.DeviceId, "A").toString()
        initialisation()
        setAdaptor()
        listeners()
        if (inventoryViewModel.isFirstTime || isRecordInventory) {
            Log.d("ViewInventoryFragment", "onViewCreated: ")
            if (Constants.isInternetAvailable(requireContext())) {
                disableUserInteraction(requireActivity())
                progressBar1.visibility = View.VISIBLE
                lifecycleScope.launch {
                    Application.bookDao?.saveAppTimeStamp(AppTimeStamp(Date()))
                    val appTimeStamp = async {
                        Application.bookDao?.retriveTimeStamp()
                    }.await()
                    Log.e("dhdgdhdh", "getLastSync First11 ${appTimeStamp}")
                    inventoryViewModel.dateLastSync = apiDateFormat(appTimeStamp?.syncDate!!)
                    inventoryViewModel.getLastSync(
                        inventoryViewModel.dateLastSync
                    )

//                    inventoryViewModel.getLastSync(
//                      ""
//                    )
                    inventoryViewModel.dataSyncStatus.observe(viewLifecycleOwner) { isDataSynced ->
                        progressBar1.visibility = View.INVISIBLE
                        if (isDataSynced) {

                                if (currLocId != 0) {

                                    CoroutineScope(Dispatchers.IO).launch {
                                        val newlyRegistered=async {
                                            roomDatabaseBuilder.getBookDao()
                                                .getCountLocationIdKt(currLocId)
                                        }.await()
                                        mainCoroutines {
                                            Log.d("new123", "onViewCreated: ${currLocId}")
                                            tvNewlyScanCount.text = newlyRegistered.toString()
                                        }
                                    }


                                }

                        }
                    }
                    Application.isRecordInventory = false
                    Constants.enableUserInteraction(requireActivity())
                }
            } else {
                CommonAlertDialog(
                    requireActivity(),
                    getString(R.string.check_internet),
                    "OK",
                    "",
                    object : CommonAlertDialog.OnButtonClickListener {
                        override fun onPositiveButtonClicked() {
                        }

                        override fun onNegativeButtonClicked() {

                        }
                    }).show()
            }


        }

        ivScanBar.setOnClickListener {
            val type = if (isFromWhat.equals("rfidlocation")) {
                "2"
            } else {
                "1"
            }
            etRfid.setText("")
            startActivityForResult(
                Intent(
                    requireActivity(),
                    TestActivity::class.java
                ).putExtra("type", type), 102
            )
        }
    }

    override fun onStop() {
        super.onStop()
        inventoryViewModel.isFirstTime = false
        isAbandoned = false
    }


    private fun initialisation() {
        listOfLocations.clear()
        mainCoroutines {
            listOfLocations.addAll(CoroutineScope(Dispatchers.IO).async {
                roomDatabaseBuilder.getBookDao().getLocationMasterList()
            }.await())
            if (isFromWhat.equals("rfidlocation")) {
                tvTitle.text = "Put Away Inventory"
                //   range_seekbar1.visibility=View.VISIBLE
                tvInventoryReport.visibility = View.INVISIBLE
                tvILastRecord.visibility = View.INVISIBLE
                registered.visibility = View.INVISIBLE
            }
        }
    }

    private fun setAdaptor() {
        val listOfItems = ArrayList<String>()
        listOfItems.add(0, "Select Location")
        listOfLocations.forEach {
            listOfItems.add(it.locationName ?: "")
        }

        etRfid.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (s.toString().trim().isEmpty()) {
                    tvLocation.text = ""
                    tvRegisteredCount.text = "0"
                    tvNewlyScanCount.text = "0"
                } else if (s.toString().length >= 4) {
                    mainCoroutines {
                        tvLocation.text = ""
                        tvRegisteredCount.text = "0"
                        tvNewlyScanCount.text = "0"
                        barCodeName = s.toString().trim()
                        //here
                        currMasterLocation = CoroutineScope(Dispatchers.IO).async {
                            Application.bookDao.getLocationMasterDataRR(barCodeName)
                        }.await()
                        if (currMasterLocation == null) {
                            tvLocation.text = ""
                            tvRegisteredCount.text = "0"
                            tvNewlyScanCount.text = "0"
                        }
                        currMasterLocation?.let {
                            it.Name?.let {
                                tvLocation.text = it
                            }

                            currLocId = currMasterLocation!!.LocID
                            var lastScanId = ""
                            var lastRecodedDate = ""
                            var registeredAsPerLastScan = 0
                            var newlyRegistered = 0
                            if (currLocId != 0) {
                                val invData = CoroutineScope(Dispatchers.IO).async {
                                    roomDatabaseBuilder.getBookDao()
                                        .getLastRecordedInventoryOfLocation(currLocId)
                                }.await()
                                if (invData.count() > 0) {
                                    lastRecodedDate = invData.get(0).scanOn.toString()
                                    lastScanId = invData.get(0).scanID
                                    //registeredAsPerLastScan= roomDatabaseBuilder.getBookDao().getCountLastScanRegistered(locId,lastScanId)
                                    registeredAsPerLastScan = CoroutineScope(Dispatchers.IO).async {
                                        roomDatabaseBuilder.getBookDao()
                                            .getCountOfRegisteredAsPerLastInventoryOfLocation(
                                                currLocId,
                                                lastScanId
                                            )
                                    }.await()
                                    //    newlyRegistered = roomDatabaseBuilder.getBookDao().getCountNewlyRegisteredAfterLastScan(currLocId, lastScanId)

                                } else {
                                    registeredAsPerLastScan = 0
                                }
                                newlyRegistered = CoroutineScope(Dispatchers.IO).async {
                                    roomDatabaseBuilder.getBookDao().getCountLocationIdKt(currLocId)
                                }.await()
                            }
                            tvRegisteredCount.text = registeredAsPerLastScan.toString()
                            tvNewlyScanCount.text = newlyRegistered.toString()
                        }
                    }
                } else {
                    tvLocation.text = ""
                    //  Log.d("tag1111", "afterTextChanged: Length ${s.length}")
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
            }
        })


        barCodeTag?.let {
            etRfid.setText(it)
            etRfid.setSelection(it.length)
        }
    }

    private fun listeners() {
        ivBackButton.setOnClickListener {
            getBackToPreviousFragment()
        }

        buttonscan.setOnClickListener {
            if (barCodeName.isEmpty()) {
                FancyToast.makeText(
                    requireActivity(),
                    "Please Enter Barcode.",
                    FancyToast.LENGTH_LONG,
                    FancyToast.WARNING,
                    false
                ).show()
            } else {
                mainCoroutines {
                    try {
                        // if (isFromWhat.equals("rfidlocation")) {
                        Log.d("range", "listeners:${range_seekbar2.value.toInt()} ")
                        decreaseRangeToThirty(range_seekbar2.value.toInt())
                        //}
                    } catch (e: Exception) {
                        Log.d("decreaseRangeToThirty", e.message.toString())
                    }

                    //  connectRFIDReader()
                    currMasterLocation = CoroutineScope(Dispatchers.IO).async {
                        Application.bookDao.getLocationMasterDataRR(barCodeName)
                    }.await()
                    currMasterLocation?.let {
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
                            val pendingInventory = CoroutineScope(Dispatchers.IO).async {
                                roomDatabaseBuilder.getBookDao()
                                    .getPendingInventoryScanKt(currLocId)
                            }.await()
                            val cnt = CoroutineScope(Dispatchers.IO).async {
                                roomDatabaseBuilder.getBookDao().getInventoryMasterAllCount()
                            }.await()
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
                                ioCoroutines {
                                    roomDatabaseBuilder.getBookDao()
                                        .addInventoryItem(inventoryMaster)
                                    roomDatabaseBuilder.getBookDao()
                                        .resetScanIdOfAssetsAtLocation(currLocId)
                                }
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

                                /*RFID Reader Power changed to 50dbm. Please Scan closely*/
                                if (inventoryViewModel.isFirstTime) {
                                    CommonAlertDialog(
                                        requireActivity(),
                                        "RFID Reader Power changed to ${range_seekbar2.value.toInt()}dbm. Please Scan closely",
                                        "OK",
                                        "",
                                        object : CommonAlertDialog.OnButtonClickListener {
                                            override fun onPositiveButtonClicked() {
                                                replaceFragment(
                                                    requireActivity().supportFragmentManager,
                                                    fragmentRapidReadFragment,
                                                    R.id.content_frame
                                                )
                                            }

                                            override fun onNegativeButtonClicked() {

                                            }
                                        }).show()

                                } else {
                                    if (range_seekbar2.value.toInt() != 200) {
                                        CommonAlertDialog(
                                            requireActivity(),
                                            "RFID Reader Power changed to ${range_seekbar2.value.toInt()}dbm. Please Scan closely",
                                            "OK",
                                            "",
                                            object : CommonAlertDialog.OnButtonClickListener {
                                                override fun onPositiveButtonClicked() {
                                                    replaceFragment(
                                                        requireActivity().supportFragmentManager,
                                                        fragmentRapidReadFragment,
                                                        R.id.content_frame
                                                    )
                                                }

                                                override fun onNegativeButtonClicked() {

                                                }
                                            }).show()
                                    } else {
                                        replaceFragment(
                                            requireActivity().supportFragmentManager,
                                            fragmentRapidReadFragment,
                                            R.id.content_frame
                                        )
                                    }
                                }


                            } else if (isFromWhat.equals("rfidlocation")) {
                                if (inventoryViewModel.isFirstTime) {
                                    CommonAlertDialog(
                                        requireActivity(),
                                        "RFID Reader Power changed to ${range_seekbar2.value.toInt()}dbm. Please Scan closely",
                                        "OK",
                                        "",
                                        object : CommonAlertDialog.OnButtonClickListener {
                                            override fun onPositiveButtonClicked() {
                                                replaceFragment(
                                                    requireActivity().supportFragmentManager,
                                                    mapRFIDLocationFragment,
                                                    R.id.content_frame
                                                )
                                            }

                                            override fun onNegativeButtonClicked() {

                                            }
                                        }).show()
                                } else {
                                    if (range_seekbar2.value.toInt() != 30) {

                                        CommonAlertDialog(
                                            requireActivity(),
                                            "RFID Reader Power changed to ${range_seekbar2.value.toInt()}dbm. Please Scan closely",
                                            "OK",
                                            "",
                                            object : CommonAlertDialog.OnButtonClickListener {
                                                override fun onPositiveButtonClicked() {
                                                    replaceFragment(
                                                        requireActivity().supportFragmentManager,
                                                        mapRFIDLocationFragment,
                                                        R.id.content_frame
                                                    )
                                                }

                                                override fun onNegativeButtonClicked() {

                                                }
                                            }).show()
                                    } else {
                                        replaceFragment(
                                            requireActivity().supportFragmentManager,
                                            mapRFIDLocationFragment,
                                            R.id.content_frame
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 102 && resultCode == Activity.RESULT_OK) {
            val returnValue: String = data?.getStringExtra("barCode").toString()
            etRfid.setText(returnValue)
        }
    }


}