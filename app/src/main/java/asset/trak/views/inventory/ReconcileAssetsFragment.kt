package asset.trak.views.inventory

import android.app.Dialog
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import asset.trak.database.daoModel.BookAndAssetData
import asset.trak.database.entity.Inventorymaster
import asset.trak.database.entity.LocationMaster
import asset.trak.database.entity.ScanTag
import asset.trak.model.LocationUpdate
import asset.trak.modelsrrtrack.AssetMain
import asset.trak.modelsrrtrack.MasterLocation
import asset.trak.utils.inter.UpdateItemInterface
import asset.trak.utils.mainCoroutines
import asset.trak.views.adapter.ReconcileAssetsPagerAdapter
import asset.trak.views.adapter.UpdateLocationAdapter
import asset.trak.views.baseclasses.BaseFragment
import asset.trak.views.listener.RapidReadCallback
import com.google.android.material.tabs.TabLayout
import com.markss.rfidtemplate.R
import com.markss.rfidtemplate.application.Application
import com.markss.rfidtemplate.application.Application.bookDao
import com.markss.rfidtemplate.common.ResponseHandlerInterfaces
import com.markss.rfidtemplate.home.MainActivity
import com.markss.rfidtemplate.inventory.InventoryListItem
import com.markss.rfidtemplate.locate_tag.LocateOperationsFragment
import com.markss.rfidtemplate.rfid.RFIDController
import com.shashank.sony.fancytoastlib.FancyToast
import com.zebra.rfid.api3.RFIDResults
import kotlinx.android.synthetic.main.fragment_reconcile_assets.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class ReconcileAssetsFragment : BaseFragment(R.layout.fragment_reconcile_assets),
    UpdateItemInterface,
    ResponseHandlerInterfaces.ResponseTagHandler, ResponseHandlerInterfaces.TriggerEventHandler,
    ResponseHandlerInterfaces.ResponseStatusHandler {
    private var whichInventory: String = ""
    private var inventoryMasterList: List<Inventorymaster> = ArrayList()
    private var loginErrorDialog: Dialog? = null
    private var inventorymaster: Inventorymaster? = null
    private lateinit var updateLocationAdapter: UpdateLocationAdapter
    var listBook = ArrayList<BookAndAssetData>()
    private lateinit var adapter: ReconcileAssetsPagerAdapter
    var locationId = 0
    private var listOfLocations = ArrayList<LocationMaster>()

    companion object {
        var selectedPosition12: Int = 0
        lateinit var fragmentCallback1: RapidReadCallback
        fun setFragmentCallback(callback1: RapidReadCallback) {
            fragmentCallback1 = callback1
        }
    }

    val listInventoryList = HashSet<String>()
    override fun handleTagResponse(inventoryListItem: InventoryListItem?, isAddedToList: Boolean) {
        listInventoryList.clear()
        listInventoryList.add(inventoryListItem?.tagID ?: "")
        addScan()

        if (isAddedToList) {
            Log.d("test", "testresponse${isAddedToList}")
            Log.d("test", "testresponse${inventoryListItem!!.tagID}")
            if (!Application.TAG_LIST_MATCH_MODE) {

            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }


    override fun handleStatusResponse(results: RFIDResults?) {

    }


    override fun triggerPressEventRecieved() {
        if (!RFIDController.mIsInventoryRunning && activity != null) {

//            lifecycleScope.launch {
//                val activity = activity as MainActivity?
//                activity?.inventoryStartOrStop()
//            }
        }
    }

    override fun triggerReleaseEventRecieved() {
        if (RFIDController.mIsInventoryRunning == true && activity != null) {
            //RFIDController.mInventoryStartPending = false;
            lifecycleScope.launch {
                val activity = activity as MainActivity?
                activity?.inventoryStartOrStop()
            }
        }

    }

    private fun notFoundTab(textValue: String) {
        val tabOne = LayoutInflater.from(requireActivity())
            .inflate(R.layout.custom_tab, null) as TextView
        tabOne.text = textValue
        if (tablayout.getTabAt(0)?.customView==null) {
            tablayout.getTabAt(0)?.customView = tabOne
        }else{
            tablayout.getTabAt(0)?.customView = null
            tablayout.getTabAt(0)?.customView = tabOne
        }
    }

    private fun differentLocationTab(textValue: String) {
        val tabOne = LayoutInflater.from(requireActivity())
            .inflate(R.layout.custom_tab, null) as TextView
        tabOne.text = textValue
        if (tablayout.getTabAt(1)?.customView==null) {
            tablayout.getTabAt(1)?.customView = tabOne
        }else{
            tablayout.getTabAt(1)?.customView = null
            tablayout.getTabAt(1)?.customView = tabOne
        }
    }

    private fun notRegisterTab(textValue: String) {
        val tabOne = LayoutInflater.from(requireActivity())
            .inflate(R.layout.custom_tab, null) as TextView
        tabOne.text = textValue
        if (tablayout.getTabAt(2)?.customView==null) {
            tablayout.getTabAt(2)?.customView = tabOne
        }else{
            tablayout.getTabAt(2)?.customView = null
            tablayout.getTabAt(2)?.customView = tabOne
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ivBackButton.setOnClickListener {
            getBackToPreviousFragment()
        }
        locationId = arguments?.getInt("locationId") ?: 0
        whichInventory = arguments?.getString("INVENTORY_NAME") ?: ""
        val locationData = arguments?.getParcelable<MasterLocation>("LocationData")

        tvFloorTitle.text = locationData?.Name

        mainCoroutines {

            var locationRegsiterCount = CoroutineScope(Dispatchers.IO).async {
                bookDao.getCountLocationId(locationId)
            }.await()
            val totalcount = "Total Registered Assets : $locationRegsiterCount"
            tvTotalRegisteredAssets.text = totalcount
            inventoryMasterList = bookDao.getPendingInventoryScan(locationId)
            if (inventoryMasterList.isEmpty() || inventoryMasterList == null) {
                var notFoundCount = 0
                val notFound = "Not\nFound ($notFoundCount)"
                var differntLocationCount = 0
                val differentLocation = "Different\nLocation ($differntLocationCount)"
                var countofNotRegistered = 0
                val notRegistered = "Not\nRegistered ($countofNotRegistered)"

//            tablayout.addTab(tablayout.newTab().setText(notFound));
                notFoundTab(notFound)

                // pendingInventoryScan = bookDao.getPendingInventoryScan(locationData.getId());
                if (!whichInventory.equals("global")) {
//                tablayout.addTab(tablayout.newTab().setText(differentLocation))
                    differentLocationTab(differentLocation)
                }
//            tablayout.addTab(tablayout.newTab().setText(notRegistered))
                notRegisterTab(notRegistered)
            } else {
                inventorymaster = inventoryMasterList.get(0)
                var notFoundCount = bookDao.getCountOfTagsNotFound()
                val notFound = "Not\nFound ($notFoundCount)"
                var differntLocationCount =
                    bookDao.getCountFoundDifferentLoc(locationId)
                val differentLocation = "Different\nLocation ($differntLocationCount)"
                var countofNotRegistered = bookDao.getCountNotRegistered()
                val notRegistered = "Not\nRegistered ($countofNotRegistered)"
//            tablayout.addTab(tablayout.newTab().setText(notFound));
                notFoundTab(notFound)
                if (!whichInventory.equals("global")) {
//                tablayout.addTab(tablayout.newTab().setText(differentLocation))
                    differentLocationTab(differentLocation)
                }
//            tablayout.addTab(tablayout.newTab().setText(notRegistered))
                notRegisterTab(notRegistered)
            }
        }
        tablayout.tabGravity = TabLayout.GRAVITY_FILL
        viewPager.offscreenPageLimit = 3

        adapter = ReconcileAssetsPagerAdapter(this)
        viewPager.adapter = adapter
        viewPager.currentItem = selectedPosition12
        Log.d("newval", "onViewCreated: ${viewPager.currentItem}")

        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tablayout))

        buttonScan.setOnClickListener {
            if (viewPager.currentItem == 0) { // Not Found
                lifecycleScope.launch {
                    val activity = activity as MainActivity?
                    activity?.inventoryStartOrStop()
                }
                addScan()

            } else if (viewPager.currentItem == 1) { // Different Loc
//                if (adapter.getCurrentFragment() is DifferentLoactionFragment) {
//                    val listBook = ArrayList<AssetMain>()
//                    (adapter.getCurrentFragment() as DifferentLoactionFragment).listBook.forEach {
//                        if (it.isSelected) {
//                            val assetCatalog = it
//                            assetCatalog.LocationId = locationId
//                            listBook.add(assetCatalog)
//                        }
//                    }
//                    if (listBook.isNotEmpty()) {
//                        bookDao.updateBookAndAssetData(listBook)
//                        refreshRegisteredAssetCount()
//                        (adapter.getCurrentFragment() as DifferentLoactionFragment).updateList()
//                    } else
////                        Toast.makeText(
////                            requireContext(), "No Item Selected",
////                            Toast.LENGTH_LONG
////                        ).show()
//
//                    FancyToast.makeText(
//                        requireActivity(),
//                        "No Item Selected.",
//                        FancyToast.LENGTH_LONG,
//                        FancyToast.WARNING,
//                        false
//                    ).show()
//                }

                if (inventorymaster == null) {
                    val differentLocation = "Different\nLocation (0)"
//                    tablayout.getTabAt(1)?.text = differentLocation
                    differentLocationTab(differentLocation)
                } else {
                    var differntLocationCount =
                        bookDao.getCountFoundDifferentLoc(locationId)
                    val differentLocation = "Different\nLocation ($differntLocationCount)"
//                    tablayout.getTabAt(1)?.text = differentLocation
                    differentLocationTab(differentLocation)
                }
            } else if (viewPager.currentItem == 2) {
//                Application.locateTag = inventorymaster.rfidTag
//                RFIDController.accessControlTag = ""
//                Application.PreFilterTag =""
                if (adapter.getCurrentFragment() is NotRegisteredFragment) {
                    val listRfids = ArrayList<ScanTag>()
                    (adapter.getCurrentFragment() as NotRegisteredFragment).rfidTags.forEach {
                        if (it.isSelected) {
                            listRfids.add(it)
                        }
                    }
                    if (listRfids.isNotEmpty()) {
                        Application.comefrom = "hide"
                        replaceFragment(
                            requireActivity().supportFragmentManager, LocateOperationsFragment(),
                            R.id.content_frame
                        )
                    } else
                        Toast.makeText(
                            requireContext(), "Please select item",
                            Toast.LENGTH_LONG
                        ).show()
                }

            }

        }



        tvUpdate.setOnClickListener {
            when (viewPager.currentItem) {
                0 -> {//faizan ne mana kiya
                    if (adapter.getCurrentFragment() is NotFoundFragment) {
                        Application.isReconsiled = true
                        Log.d("UpdateLocation", "onViewCreated: ")
                        val inventoryMasterList = bookDao.getPendingInventoryScan(locationId)

                        var totalItemCount = 0
                        //   val listBook = (adapter.getCurrentFragment() as NotFoundFragment).listBook
                        (adapter.getCurrentFragment() as NotFoundFragment).listBook.forEach {
                            if (it.isSelected) {
                                val assetCatalog = it
                                assetCatalog.LocationId = locationId
                                assetCatalog.inventorySyncFlag = 1
                                // listBook.add(assetCatalog)
                                totalItemCount += 1
                            }
                        }
                        if (totalItemCount == 0) {
//                            Toast.makeText(
//                                requireContext(), "No Item Selected",
//                                Toast.LENGTH_LONG
//                            ).show()
                            FancyToast.makeText(
                                requireActivity(),
                                "No Item Selected.",
                                FancyToast.LENGTH_LONG,
                                FancyToast.WARNING,
                                false
                            ).show()
                        } else {

                            //   val listRfids = ArrayList<ScanTag>()

                            //     val scanList = bookDao.getScanTagAll()
                            val listBook =
                                (adapter.getCurrentFragment() as NotFoundFragment).listBook
                            val pendingInventoryScan = bookDao.getPendingInventoryScan(
                                locationData!!.LocID
                            )
                            val lastItem = pendingInventoryScan.get(0)
//                            Log.e(
//                                "sdd",
//                                "" + Gson().toJson((adapter.getCurrentFragment() as DifferentLoactionFragment).listBook)
//                            )

                            val changedFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                            var scanEndTime: String? = ""
                            try {
                                //   String currentDate = changedFormat.format(new Date());
                                scanEndTime = changedFormat.format(Date())
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                            listBook.forEach {
                                if (it.isSelected) {
                                    val assetCatalogue = it
                                    if (it.AssetRFID.isNullOrEmpty()) {
                                        FancyToast.makeText(
                                            requireActivity(),
                                            "Asset RFID Tag Not Found",
                                            FancyToast.LENGTH_LONG,
                                            FancyToast.WARNING,
                                            false
                                        ).show()
                                        return@setOnClickListener
                                    }

                                    Log.e("DATA","LOC :$locationId NEW_LOC: ${it.LocationId} SCAN_ID: ${lastItem.scanID} RFID: ${it.AssetRFID}")
                                    /*Update the LastScannedLocID to 'F' in ScanTag table for Ignored Records.*/
                                    bookDao.updateLocationAssetMain(
                                        0,
                                        lastItem.scanID,
                                        it.AssetRFID
                                    )
                                }
                            }
                            if (inventorymaster == null) {
                                var notFoundCount = 0
                                val notFound = "Not\nFound ($notFoundCount)"
//                                tablayout.getTabAt(0)?.text = notFound
                                notFoundTab(notFound)
                            } else {
                                refreshRegisteredAssetCount()
                                (adapter.getCurrentFragment() as NotFoundFragment).updateList()
                                inventorymaster = inventoryMasterList[inventoryMasterList.size - 1]
                                var notFoundCount = bookDao.getCountOfTagsNotFound()
                                val notFound = "Not\nFound ($notFoundCount)"
//                                tablayout.getTabAt(0)?.text = notFound
                                notFoundTab(notFound)
                            }
                        }

                    }
                }
                1 -> {
                    if (adapter.getCurrentFragment() is DifferentLoactionFragment) {
                        Application.isReconsiled = true
                        val listBook = ArrayList<ScanTag>()
                        var totalItemCount = 0
                        (adapter.getCurrentFragment() as DifferentLoactionFragment).listBook.forEach {
                            if (it.isSelected) {
                               val assetCatalog = ScanTag()
                                assetCatalog.scanId=it.ScanID
                              assetCatalog.locationId = locationId
                                assetCatalog.rfidTag=it.AssetRFID
                            assetCatalog.assetId=it.AssetID
                                assetCatalog.inventorySyncFlag=1
                                assetCatalog.isSelected=it.isSelected
                              listBook.add(assetCatalog)
                                totalItemCount += 1
                            }
                        }
                        if (listBook.isEmpty()) {
                            FancyToast.makeText(
                                requireActivity(),
                                "No Item Selected.",
                                FancyToast.LENGTH_LONG,
                                FancyToast.WARNING,
                                false
                            ).show()
                        } else {
                            listBook.forEach {
                                if (it.isSelected) {
                                    bookDao.updateBookAndAssetData(
                                        locationId,
                                        inventorymaster!!.scanID,
                                        1,
                                        it.rfidTag.toString()
                                    )
                                }
                            }
                            refreshRegisteredAssetCount()
                            (adapter.getCurrentFragment() as DifferentLoactionFragment).updateList()
                        }
                        if (inventorymaster == null) {
                            var countofNotRegistered = 0
                            val notRegistered = "Different\nLocation ($countofNotRegistered)"
//                        tablayout.getTabAt(1)?.text = notRegistered
                            differentLocationTab(notRegistered)
                        } else {
                            var countofDiffLocation =
                                bookDao.getCountFoundDifferentLoc(locationId)
                            val diffLoc = "Different\nLocation ($countofDiffLocation)"
//                        tablayout.getTabAt(1)?.text = diffLoc
                            differentLocationTab(diffLoc)
                        }


                    }
                }
                2 -> {
                    if (adapter.getCurrentFragment() is NotRegisteredFragment) {
                        val listRfids = ArrayList<ScanTag>()
                        (adapter.getCurrentFragment() as NotRegisteredFragment).rfidTags.forEach {
                            if (it.isSelected) {
                                listRfids.add(it)
                            }
                        }
                        if (listRfids.isNotEmpty()) {
                            /* listRfids.forEach {
                                 bookDao.deleteScanTagNotReg(it.scanId!!, it!!.rfidTag!!)
                             }*/
                            bookDao.deleteScanTag(listRfids)
                            (adapter.getCurrentFragment() as NotRegisteredFragment).updateList()
                        } else {
                            FancyToast.makeText(
                                requireActivity(),
                                "No Item Selected.",
                                FancyToast.LENGTH_LONG,
                                FancyToast.WARNING,
                                false
                            ).show()
                        }

//                            Toast.makeText(
//                                requireContext(), "No Item Selected",
//                                Toast.LENGTH_LONG
//                            ).show()

                        if (inventorymaster == null) {
                            val countofNotRegistered = 0
                            val notRegistered = "Not\nRegistered ($countofNotRegistered)"
//                            tablayout.getTabAt(2)?.text = notRegistered
                            notRegisterTab(notRegistered)

                        } else {
                            val countofNotRegistered =
                                bookDao.getCountNotRegistered()
                            val notRegistered = "Not\nRegistered ($countofNotRegistered)"
//                            tablayout.getTabAt(2)?.text = notRegistered
                            notRegisterTab(notRegistered)
                        }
                    }
                }
            }
        }


        tablayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                selectedPosition12 = tab.position
                Log.d("test", "onTabSelected:${selectedPosition12} ")
                viewPager.currentItem = selectedPosition12
                adapter.notifyDataSetChanged()
//                Handler().postDelayed(
//                    { tablayout.getTabAt(selectedPosition12)?.select() }, 100
//                )


                if (viewPager.currentItem == 0) {
                    //buttonScan.visibility=View.VISIBLE
                    //buttonScan.text = "Scan"
                    tvUpdate.text = "Ignore"

                } else if (viewPager.currentItem == 1) {
                    // buttonScan.visibility=View.VISIBLE
                    // buttonScan.text = "Update to Current Location"
                    tvUpdate.text = "Update to Current Location"

                } else if (viewPager.currentItem == 2) {
                    // buttonScan.visibility=View.GONE
                    tvUpdate.text = "Ignore"

                }

            }


            override fun onTabUnselected(tab: TabLayout.Tab) {
                Log.d("location", "onTabUnselected: ${tab.position}")
                if (adapter.getCurrentFragment() is DifferentLoactionFragment) {
                    if (!(adapter.getCurrentFragment() as DifferentLoactionFragment).listBook.isNullOrEmpty()) {
                        (adapter.getCurrentFragment() as DifferentLoactionFragment).listBook.forEach {
                            it.isSelected = false
                        }
                        adapter = ReconcileAssetsPagerAdapter(this@ReconcileAssetsFragment)
                        viewPager.adapter = adapter
                    }
                } else if (adapter.getCurrentFragment() is NotFoundFragment) {
                    if (!(adapter.getCurrentFragment() as NotFoundFragment).listBook.isNullOrEmpty()) {

                        (adapter.getCurrentFragment() as NotFoundFragment).listBook.forEach {
                            it.isSelected = false
                        }
                        adapter = ReconcileAssetsPagerAdapter(this@ReconcileAssetsFragment)
                        viewPager.adapter = adapter
                    }
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                Log.d("location", "onTabReselected: ")

                if (adapter.getCurrentFragment() is NotFoundFragment) {
                    if (!(adapter.getCurrentFragment() as NotFoundFragment).listBook.isNullOrEmpty()) {

                        (adapter.getCurrentFragment() as NotFoundFragment).listBook.forEach {
                            it.isSelected = false
                        }
                        adapter = ReconcileAssetsPagerAdapter(this@ReconcileAssetsFragment)
                        viewPager.adapter = adapter
                    }
                }
            }
        })

    }

    fun refreshRegisteredAssetCount() {
        mainCoroutines {
            var count = CoroutineScope(Dispatchers.IO).async {
                bookDao.getCountLocationId(locationId)
            }.await()
            val totalcount = "Total Registered Assets : $count"
            tvTotalRegisteredAssets.text = totalcount
        }
    }

    fun addScan() {
        val inventoryMasterList: List<Inventorymaster>? =
            bookDao.getPendingInventoryScan(locationId)

        if (inventoryMasterList == null || inventoryMasterList.isEmpty()) {

        } else {
            val inventorymaster: Inventorymaster? =
                inventoryMasterList[inventoryMasterList.size - 1]
            if (inventorymaster == null) {

            } else {
                for (inventoryTag in listInventoryList) {
                    val scanTag = ScanTag()
                    scanTag.scanId = inventorymaster.scanID
                    scanTag.locationId = locationId
                    scanTag.rfidTag = inventoryTag
                    val getCountOfTagAlready = bookDao.getCountOfTagAlready(
                        scanTag.rfidTag!!, scanTag.scanId!!
                    )
                    if (getCountOfTagAlready == 0) {
                        bookDao.addScanTag(scanTag)
                        if (adapter.getCurrentFragment() is NotFoundFragment)
                            (adapter.getCurrentFragment() as NotFoundFragment).updateList()

                        var notFoundCount =
                            bookDao.getCountOfTagsNotFound()
                        val notFound = "Not\nFound ($notFoundCount)"
//                        tablayout.getTabAt(0)?.text = notFound
                        notFoundTab(notFound)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (viewPager != null) {

            Handler().postDelayed(
                { tablayout.getTabAt(selectedPosition12)?.select() }, 100
            )
            try {
                viewPager.offscreenPageLimit = 3
                adapter = ReconcileAssetsPagerAdapter(this)
                viewPager.adapter = adapter
                viewPager.setCurrentItem(selectedPosition12)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        requireView().isFocusableInTouchMode = true
        requireView().requestFocus()
        requireView().setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent): Boolean {
                return if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    Log.d("backpress", "onKey:Back ")
                    fragmentCallback1.onDataSent(true)
                    //  requireActivity().supportFragmentManager.popBackStackImmediate()
                    requireActivity().onBackPressed()
                    true
                } else false
            }
        })

        //   viewPager.adapter?.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()
        selectedPosition12 = 0
    }

    private fun showUpdateLocationDialog() {
        progressBar2.visibility = View.VISIBLE
        initialisation()
        loginErrorDialog = Dialog(requireContext())
        if (loginErrorDialog?.window != null) loginErrorDialog?.window!!
            .setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        loginErrorDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)

        //set window params and attributes
        val window: Window? = loginErrorDialog?.window
        window?.setGravity(Gravity.CENTER)
        val params: WindowManager.LayoutParams = window?.attributes!!
        params.y = 60
        params.width = ViewGroup.LayoutParams.MATCH_PARENT
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT
        params.dimAmount = 0.50f
        params.flags = params.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
        window.attributes = params

//        val binding: DeleteQueryPopupBinding =
//            DeleteQueryPopupBinding.inflate(LayoutInflater.from(activity))
//        binding.viewmodel = DynamicString
        loginErrorDialog?.setContentView(R.layout.layout_list)

        val reCycleview: RecyclerView =
            loginErrorDialog!!.findViewById(R.id.rvUpdateLocation)
        setAdaptor(reCycleview, loginErrorDialog!!)
        val proceed_btn: AppCompatButton =
            loginErrorDialog!!.findViewById(R.id.proceed_btn)

        //    setAdaptor(reCycleview,loginErrorDialog!!)
        //setAdaptor(reCycleview)


        loginErrorDialog?.setCanceledOnTouchOutside(false)
        loginErrorDialog?.setCancelable(false)
        setWidthPercentOfDialog(loginErrorDialog!!)
        loginErrorDialog!!.show()
    }


    private fun setWidthPercentOfDialog(dialog: Dialog) {
        val percentage = 90
        val percent = percentage.toFloat() / 100
        val dm = Resources.getSystem().displayMetrics
        val rect = dm.run { Rect(0, 0, widthPixels, heightPixels) }
        val percentWidth = rect.width() * percent
        dialog.window?.setLayout(percentWidth.toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
    }


    private fun setAdaptor(reCycleview: RecyclerView, loginErrorDialog: Dialog) {
        val listOfItems = ArrayList<LocationUpdate>()

        listOfLocations.forEach {
            if (!it.locationName.equals(tvFloorTitle.text.toString()))
            //  listOfItems.add(LocationUpdate(it.locationName))
                listOfItems.add(LocationUpdate(it.locationName, false, it.id))
        }

        updateLocationAdapter = UpdateLocationAdapter(
            requireContext(),
            requireActivity().supportFragmentManager,
            listOfItems,
            this,
            loginErrorDialog
        )
        reCycleview.adapter = updateLocationAdapter

    }

    private fun initialisation() {
        mainCoroutines {
            listOfLocations.clear()
            listOfLocations.addAll(CoroutineScope(Dispatchers.IO).async {
                Application.roomDatabaseBuilder.getBookDao().getLocationMasterList()
            }.await())
        }
    }

    override fun onUpdateItemCallback(locationData: LocationUpdate) {
        Log.d("locationData", locationData.title.toString());
//        val listBook = ArrayList<AssetMain>()
//        (adapter.getCurrentFragment() as NotFoundFragment).listBook.forEach {
//            if (it.isSelected) {
//                val assetCatalog = it
//                assetCatalog.LocationId = locationId
//                listBook.add(assetCatalog)
//            }
//        }
//        if (listBook.isNotEmpty()) {
//            listBook.forEach {
//                bookDao.updateLocationIdOfAssets(locationData.id!!,it.id)
//            }
//            refreshRegisteredAssetCount()
//            (adapter.getCurrentFragment() as NotFoundFragment).updateList()
//            progressBar2.visibility=View.INVISIBLE
//            FancyToast.makeText(
//                requireActivity(),
//                "Asset(s) Location Updated Successfully.",
//                FancyToast.LENGTH_LONG,
//                FancyToast.SUCCESS,
//                false
//            ).show()
//
//
//
//        } else
//        {
////            Toast.makeText(
////                requireContext(), "No Item Selected",
////                Toast.LENGTH_LONG
////            ).show()
//
//            FancyToast.makeText(
//                requireActivity(),
//                "No Item Selected.",
//                FancyToast.LENGTH_LONG,
//                FancyToast.WARNING,
//                false
//            ).show()
//            progressBar2.visibility=View.INVISIBLE
//        }
//        inventorymaster = inventoryMasterList[inventoryMasterList.size - 1]
//        var notFoundCount = bookDao.getCountOfTagsNotFound(locationId,inventorymaster!!.scanID)
//        val notFound = "Not Found ($notFoundCount)"
//        tablayout.getTabAt(0)?.text = notFound
//        loginErrorDialog?.cancel()

    }


}