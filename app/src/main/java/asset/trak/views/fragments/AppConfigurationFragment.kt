package asset.trak.views.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import asset.trak.modelsrrtrack.AppTimeStamp
import asset.trak.modelsrrtrack.AssetMain
import asset.trak.utils.apiDateFormat
import asset.trak.utils.ioCoroutines
import asset.trak.utils.mainCoroutines
import asset.trak.views.adapter.OnResultClickListener
import asset.trak.views.adapter.ResultAdapter
import asset.trak.views.baseclasses.BaseFragment
import asset.trak.views.module.InventoryViewModel
import com.google.gson.Gson
import com.markss.rfidtemplate.R
import com.markss.rfidtemplate.application.Application
import com.markss.rfidtemplate.locate_tag.LocateOperationsFragment
import com.markss.rfidtemplate.rfid.RFIDController
import kotlinx.android.synthetic.main.fragment_app_configuration.*
import kotlinx.android.synthetic.main.fragment_app_configuration.ivBack
import kotlinx.android.synthetic.main.fragment_my_library_search.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.util.*
import kotlin.collections.ArrayList


class AppConfigurationFragment : BaseFragment(R.layout.fragment_app_configuration),
    OnResultClickListener {

    private val inventoryViewModel: InventoryViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainCoroutines {
            val namesList = CoroutineScope(Dispatchers.IO).async {
                Application.bookDao?.appConfigLocationNames()
            }.await()
            Log.e("DATA", Gson().toJson(namesList))

            namesList?.let {
                val arrayAdapter = ArrayAdapter(
                    requireActivity(),
                    android.R.layout.simple_spinner_dropdown_item,
                    it
                )
                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                locationNames.adapter = arrayAdapter


                ioCoroutines {
                    if (Application.bookDao?.checkTableIsEmpty() != 0) {
                        val result = CoroutineScope(Dispatchers.IO).async {
                            Application.bookDao?.getOffLocation()
                        }.await()
                        locationNames.setSelection(it.indexOf(result?.locationName.toString()))
                    }
                }

                locationNames.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                        inventoryViewModel.updateOffLocation(it[p2])
                        mainCoroutines {
                            Application.bookDao?.saveAppTimeStamp(AppTimeStamp(Date()))
                            val appTimeStamp = CoroutineScope(Dispatchers.IO).async {
                                Application.bookDao?.retriveTimeStamp()
                            }.await()
                            inventoryViewModel.dateLastSync =
                                apiDateFormat(appTimeStamp?.syncDate!!)
                            Log.e(
                                "dhdgdhdh",
                                "getLastSync First11 ${inventoryViewModel.dateLastSync}"
                            )
                        }

                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {
                    }
                }

            }

        }

        ivBack.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    override fun onGoalClick(bookAttributes: AssetMain) {
        Application.locateTag = bookAttributes.AssetRFID
        RFIDController.accessControlTag = bookAttributes.AssetRFID
        Application.PreFilterTag = bookAttributes.AssetRFID
        Application.comefrom = "show"

        replaceFragment(
            requireActivity().supportFragmentManager,
            LocateOperationsFragment(),
            R.id.content_frame
        )
    }
}