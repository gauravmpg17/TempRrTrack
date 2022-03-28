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
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import asset.trak.database.entity.Inventorymaster
import asset.trak.database.entity.LocationMaster
import asset.trak.modelsrrtrack.MasterLocation
import asset.trak.utils.Constants
import asset.trak.utils.Constants.disableUserInteraction
import asset.trak.utils.decreaseRangeToThirty
import asset.trak.utils.getThumb
import asset.trak.utils.getThumbView
import asset.trak.views.activity.TestActivity
import asset.trak.views.baseclasses.BaseFragment
import asset.trak.views.module.InventoryViewModel
import com.markss.rfidtemplate.R
import com.markss.rfidtemplate.application.Application
import com.markss.rfidtemplate.application.Application.isAbandoned
import com.markss.rfidtemplate.application.Application.roomDatabaseBuilder
import com.markss.rfidtemplate.rapidread.MapRFIDLocationFragment
import com.markss.rfidtemplate.rapidread.RapidReadFragment
import com.shashank.sony.fancytoastlib.FancyToast
import kotlinx.android.synthetic.main.fragment_view_inventory.*
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
        range_seekbar1.setAnimated(true, 3000L)
        range_seekbar2.thumbTintList= ColorStateList.valueOf(resources.getColor(R.color.green))
        range_seekbar2.setLabelFormatter { value: Float ->
        return@setLabelFormatter value.toInt().toString()
        }
        range_seekbar2.addOnChangeListener { rangeSlider, value, fromUser ->
            // Responds to when slider's value is changed
            if(value.toInt() ==18 || value < 24)
            {
                range_seekbar2.thumbTintList= ColorStateList.valueOf(resources.getColor(R.color.red))
            }
            else if(value.toInt() ==24 || value < 30)
            {
                range_seekbar2.thumbTintList= ColorStateList.valueOf(resources.getColor(R.color.colorAccent))
            }
            else
            {
                range_seekbar2.thumbTintList= ColorStateList.valueOf(resources.getColor(R.color.green))
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
        if (inventoryViewModel.isFirstTime || (Application.isReconsiled && !isAbandoned)) {
            Log.d("ViewInventoryFragment", "onViewCreated: ")
            getLastSync()
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

        seekBar.progress = 30
        seekBar.thumb = getThumb(30, requireActivity().resources, requireActivity().getThumbView())

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                seekBar.thumb = getThumb(
                    p1, requireActivity().resources, requireActivity().getThumbView()
                )
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }
        })
    }

    override fun onStop() {
        super.onStop()
        inventoryViewModel.isFirstTime = false
        isAbandoned = false
    }


    private fun getLastSync() {
        progressBar1.visibility = View.VISIBLE
        disableUserInteraction(requireActivity())
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val syncTime = sharedPreference?.getString(Constants.LastSyncTs, "2022-02-08")
        val currSyncTime = sdf.format(Date())
        val deviceId =
            Settings.Secure.getString(requireActivity().contentResolver, Settings.Secure.ANDROID_ID)
        // Toast.makeText(activity, syncTime, Toast.LENGTH_SHORT).show()

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
            progressBar1.visibility = View.INVISIBLE
            Constants.enableUserInteraction(requireActivity())
            // Toast.makeText(activity, "Saved SynTime in sp:$currSyncTime", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initialisation() {
        listOfLocations.clear()
        listOfLocations.addAll(roomDatabaseBuilder.getBookDao().getLocationMasterList())
        if (isFromWhat.equals("rfidlocation")) {
            tvTitle.text = "Put Away Inventory"
            //   range_seekbar1.visibility=View.VISIBLE
            tvInventoryReport.visibility = View.INVISIBLE
            tvILastRecord.visibility = View.INVISIBLE
            registered.visibility = View.INVISIBLE
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
                                //    newlyRegistered = roomDatabaseBuilder.getBookDao().getCountNewlyRegisteredAfterLastScan(currLocId, lastScanId)

                            } else {
                                registeredAsPerLastScan = 0
                            }
                            newlyRegistered =
                                roomDatabaseBuilder.getBookDao().getCountLocationId(currLocId)
                        }
                        tvRegisteredCount.text = registeredAsPerLastScan.toString()
                        tvNewlyScanCount.text = newlyRegistered.toString()

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
                try {
                    if (isFromWhat.equals("rfidlocation")) {
                        Log.d("range", "listeners:${seekBar.progress} ")
                        decreaseRangeToThirty(seekBar.progress)
                    }
                } catch (e: Exception) {
                    Log.d("decreaseRangeToThirty", e.message.toString())
                }

                //  connectRFIDReader()
                currMasterLocation = Application.bookDao.getLocationMasterDataRR(barCodeName)
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
                        val pendingInventory =
                            roomDatabaseBuilder.getBookDao().getPendingInventoryScan(currLocId)
                        val cnt = roomDatabaseBuilder.getBookDao().getInventoryMasterAllCount()
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

                            if (inventoryViewModel.isFirstTime) {
                                val builder1 = AlertDialog.Builder(requireActivity())
                                builder1.setMessage("RFID Reader Scan Range changed to ${seekBar.progress}m. Please Scan Closely")
                                builder1.setCancelable(false)
                                builder1.setPositiveButton(
                                    "Ok"
                                ) { dialog, id ->
                                    dialog.cancel()
                                    replaceFragment(
                                        requireActivity().supportFragmentManager,
                                        fragmentRapidReadFragment,
                                        R.id.content_frame
                                    )
                                }
                                val alert11 = builder1.create()
                                alert11.show()
                            } else {
                                replaceFragment(
                                    requireActivity().supportFragmentManager,
                                    fragmentRapidReadFragment,
                                    R.id.content_frame
                                )
                            }


                        } else if (isFromWhat.equals("rfidlocation")) {
                            if (inventoryViewModel.isFirstTime) {
                                val builder1 = AlertDialog.Builder(requireActivity())
                                builder1.setMessage("RFID Reader Scan Range changed to ${seekBar.progress}m. Please Scan Closely")
                                builder1.setCancelable(false)
                                builder1.setPositiveButton(
                                    "Ok"
                                ) { dialog, id ->
                                    dialog.cancel()
                                    replaceFragment(
                                        requireActivity().supportFragmentManager,
                                        mapRFIDLocationFragment,
                                        R.id.content_frame
                                    )
                                }
                                val alert11 = builder1.create()
                                alert11.show()
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
            } else {
                tvLocation.text = ""
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