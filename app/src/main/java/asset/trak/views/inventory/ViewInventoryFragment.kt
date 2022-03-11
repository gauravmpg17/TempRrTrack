package asset.trak.views.inventory

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import asset.trak.database.entity.Inventorymaster
import asset.trak.database.entity.LocationMaster
import asset.trak.modelsrrtrack.MasterLocation
import asset.trak.utils.Constants
import asset.trak.views.baseclasses.BaseFragment
import com.markss.rfidtemplate.R
import com.markss.rfidtemplate.application.Application
import com.markss.rfidtemplate.application.Application.roomDatabaseBuilder
import com.markss.rfidtemplate.rapidread.RapidReadFragment
import com.shashank.sony.fancytoastlib.FancyToast
import kotlinx.android.synthetic.main.fragment_view_inventory.*
import java.util.*


class ViewInventoryFragment(val isFromWhat: String) : BaseFragment(R.layout.fragment_view_inventory) {

    private var barCodeName: String=""
    private var listOfLocations = ArrayList<LocationMaster>()
    private var currLocId =0
    private var currMasterLocation: MasterLocation?=null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialisation()
        setAdaptor()
        listeners()
    }

    private fun initialisation() {

        listOfLocations.clear()
        listOfLocations.addAll(roomDatabaseBuilder.getBookDao().getLocationMasterList())
    }

    private fun setAdaptor() {
        val listOfItems = ArrayList<String>()
        listOfItems.add(0,"Select Location")
        listOfLocations.forEach {
            listOfItems.add(it.locationName ?: "")
        }

        val spinnerArrayAdapter: ArrayAdapter<String> =
            ArrayAdapter<String>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                listOfItems
            )


        etRfid.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {
                if(s.toString().trim().isEmpty())
                {
                    FancyToast.makeText(
                        requireActivity(),
                        "Please Enter Barcode.",
                        FancyToast.LENGTH_LONG,
                        FancyToast.WARNING,
                        false
                    ).show()
                }
                else
                {
                    barCodeName= etRfid.text.toString().trim()
                    //here
                    currMasterLocation = Application.bookDao.getLocationMasterDataRR(barCodeName)
                    currLocId=currMasterLocation!!.LocID
                    var lastScanId=""
                    var lastRecodedDate=""
                    var registeredAsPerLastScan=0
                    var newlyRegistered=0
                    if (currLocId != null) {
                        val invData =  roomDatabaseBuilder.getBookDao().getLastRecordedInventoryOfLocation(currLocId)
                        if(invData.count()>0){
                            lastRecodedDate= invData.get(0).scanOn.toString()
                            lastScanId=invData.get(0).scanID
                            //registeredAsPerLastScan= roomDatabaseBuilder.getBookDao().getCountLastScanRegistered(locId,lastScanId)
                            registeredAsPerLastScan= roomDatabaseBuilder.getBookDao().getCountOfRegisteredAsPerLastInventoryOfLocation(currLocId,lastScanId)
                            newlyRegistered= roomDatabaseBuilder.getBookDao().getCountNewlyRegisteredAfterLastScan(currLocId,lastScanId)

                        }else{
                            registeredAsPerLastScan=0
                            newlyRegistered= roomDatabaseBuilder.getBookDao().getCountLocationId(currLocId)
                        }
                    }


                    tvRegisteredCount.text = registeredAsPerLastScan.toString()
                    tvNewlyScanCount.text = newlyRegistered.toString()


                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
               // tvSample.setText("Text is : "+s)

            }
        })
    }

    private fun listeners() {
        ivBackButton.setOnClickListener {
            getBackToPreviousFragment()
        }

        buttonscan.setOnClickListener {
            /*Add a Entry to Table with Pending Status*/

            if(currMasterLocation==null)
            {
                                FancyToast.makeText(
                    requireActivity(),
                    "Please Enter Bar Code.",
                    FancyToast.LENGTH_LONG,
                    FancyToast.WARNING,
                    false
                ).show()
            }
            else
            {
                currLocId=currMasterLocation!!.LocID
                val pendingInventory  = roomDatabaseBuilder.getBookDao().getPendingInventoryScan(currLocId)

                val cnt = roomDatabaseBuilder.getBookDao().getInventoryMasterAllCount()
//
//            val inventoryLastItem: Inventorymaster = if (getListInventoryMaster.isNotEmpty())
//                getListInventoryMaster[getListInventoryMaster.size - 1] else Inventorymaster()

                if (pendingInventory.isEmpty()) {
                    val inventoryMaster=Inventorymaster(
                        scanID = "A" + ((cnt ?: 0) + 1),
                        deviceId = "A",
                        deviceIdCount = ((cnt ?: 0) + 1),
                        status = Constants.InventoryStatus.PENDING,
                        locationId = currLocId
                    )
                    roomDatabaseBuilder.getBookDao().addInventoryItem(inventoryMaster)
                    roomDatabaseBuilder.getBookDao().resetScanIdOfAssetsAtLocation(currLocId)
            }

                val fragment = RapidReadFragment()
                val bundle = Bundle()
                bundle.putParcelable(
                    "LocationData",
                   currMasterLocation
                )
                bundle.putString("INVENTORY_NAME",isFromWhat)
                bundle.putInt("totalRegistered", tvRegisteredCount.text.toString().toInt().plus(
                    tvNewlyScanCount.text.toString().toInt()
                ))

                fragment.arguments = bundle
                replaceFragment(
                    requireActivity().supportFragmentManager, fragment,
                    R.id.content_frame
                )

            }

        }
    }


}