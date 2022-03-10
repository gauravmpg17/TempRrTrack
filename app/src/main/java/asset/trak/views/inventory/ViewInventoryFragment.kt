package asset.trak.views.inventory

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import asset.trak.database.entity.Inventorymaster
import asset.trak.database.entity.LocationMaster
import asset.trak.utils.Constants
import asset.trak.views.baseclasses.BaseFragment
import com.markss.rfidtemplate.R
import com.markss.rfidtemplate.application.Application.roomDatabaseBuilder
import com.markss.rfidtemplate.rapidread.RapidReadFragment
import com.shashank.sony.fancytoastlib.FancyToast
import kotlinx.android.synthetic.main.fragment_view_inventory.*
import java.text.SimpleDateFormat
import java.util.*


class ViewInventoryFragment : BaseFragment(R.layout.fragment_view_inventory) {

    private var listOfLocations = ArrayList<LocationMaster>()
    private var currLocId =0

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
        spinnerLocation.adapter = spinnerArrayAdapter

    }

    private fun listeners() {
        ivBackButton.setOnClickListener {
            getBackToPreviousFragment()
        }

        spinnerLocation.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if(position > 0)
                {
                    /*.....*/
                    Log.d("tagnew", "onItemSelected: ${listOfLocations[position].id}")
                    /*...*/
                    currLocId= listOfLocations[position-1].id!!
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
                            registeredAsPerLastScan= invData.get(0).registered!!
                            newlyRegistered= roomDatabaseBuilder.getBookDao().getCountNewlyRegisteredAfterLastScan(currLocId,lastScanId)

                        }else{
                            registeredAsPerLastScan=0
                            newlyRegistered= roomDatabaseBuilder.getBookDao().getCountLocationId(currLocId)
                        }
                    }


                    tvRegisteredCount.text = registeredAsPerLastScan.toString()
                    tvNewlyScanCount.text = newlyRegistered.toString()
                    Log.d("locationid====>", "onItemSelected: ${listOfLocations[spinnerLocation.selectedItemPosition-1].id!!}")

                    //val getListInventoryMasterTemp = roomDatabaseBuilder.getBookDao().getInventoryMaster(listOfLocations[spinnerLocation.selectedItemPosition-1].id!!)
                    // 2022-02-23T16:40:20


                   val formater = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                   val changedFormat = SimpleDateFormat("dd-MM-yyyy hh:mm a")
                    try {
                        if (lastRecodedDate!=""){
                            val latestDate: Date = formater.parse(lastRecodedDate)
                            tvILastRecord.text="${getString(R.string.last_recorded_02_02_2022_10_43_am)} ${changedFormat.format(latestDate)}"
                            Log.d("newtag", "onItemSelected: ${ changedFormat.format(latestDate)}")
                        }else{
                            tvILastRecord.text="${getString(R.string.last_recorded_02_02_2022_10_43_am)} NA"
                        }

                    }
                    catch (e:Exception)
                    {
                        e.printStackTrace()
                    }
               //  val changedDate=changedFormat.format(latestDate)
//                    Log.d("main", "onItemSelected:$changedDate")
//                    tvILastRecord.text="${getString(R.string.last_recorded_02_02_2022_10_43_am)} ${latestDate}"

                }
                else{
                    currLocId=0
                }
            }
        }

        /*    registered.setOnClickListener {
                replaceFragment(
                    requireActivity().supportFragmentManager, RecordInventoryNewFragment(),
                    R.id.content_frame
                )
            }*/

        buttonscan.setOnClickListener {
            /*Add a Entry to Table with Pending Status*/
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

            if(spinnerLocation.selectedItemPosition==0)
            {
//                val fragment = RapidReadFragment()
//                val bundle = Bundle()
//                bundle.putParcelable(
//                    "LocationData",
//                    LocationMaster()
//                )
//                fragment.arguments = bundle
//                replaceFragment(
//                    requireActivity().supportFragmentManager, fragment,
//                    R.id.content_frame
//                )
                FancyToast.makeText(
                    requireActivity(),
                    "Please select Location.",
                    FancyToast.LENGTH_LONG,
                    FancyToast.WARNING,
                    false
                ).show()

                //        Toast.makeText(requireActivity(),"Please select Location", Toast.LENGTH_SHORT).show()
            }
            else
            {
                val fragment = RapidReadFragment()
                val bundle = Bundle()
                bundle.putParcelable(
                    "LocationData",
                    listOfLocations[spinnerLocation.selectedItemPosition-1]
                )
                bundle.putInt("totalRegistered", tvRegisteredCount.text.toString().toInt().plus(
                    tvNewlyScanCount.text.toString().toInt()
                ))

                fragment.arguments = bundle
                replaceFragment(
                    requireActivity().supportFragmentManager, fragment,
                    R.id.content_frame
                )
            }

          //  Log.d("new", "listeners: "+spinnerLocation.selectedItemPosition)

        }
    }


}