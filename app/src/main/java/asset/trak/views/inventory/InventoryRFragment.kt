package asset.trak.views.inventory

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import asset.trak.views.baseclasses.BaseFragment
import asset.trak.views.module.InventoryViewModel
import com.markss.rfidtemplate.R
import com.markss.rfidtemplate.application.Application
import com.markss.rfidtemplate.application.Application.roomDatabaseBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_inventory.*
import kotlinx.android.synthetic.main.fragment_record_inventory_asset.*
import kotlinx.android.synthetic.main.fragment_result.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class InventoryRFragment : BaseFragment(R.layout.fragment_record_inventory_asset) {
   private val inventoryViewModel: InventoryViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            if (!inventoryViewModel.listBookAttributes.isNullOrEmpty()) {
                Application.bookDao?.addBookAttributeList(inventoryViewModel.listBookAttributes)
            }
        }
        catch (e:Exception)
        {
            e.printStackTrace()
        }

        initialisation()
        listeners()
    }

    private fun initialisation() {
        tvDeviceCount.text = roomDatabaseBuilder.getBookDao().getCount().toString()
        tvAssetCount.text = roomDatabaseBuilder.getBookDao().getCountReconciled().toString()
        tvCountLibrary.text = roomDatabaseBuilder.getBookDao().getCountNotReconciled().toString()

    }

    private fun listeners() {
        ivBackButton.setOnClickListener {
            getBackToPreviousFragment()
        }

        letstrack_btn.setOnClickListener {
            replaceFragment(
                requireActivity().supportFragmentManager, ViewInventoryFragment(),
                R.id.content_frame
            )

        }
    }

}