package asset.trak.views.inventory

import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.View
import androidx.appcompat.widget.SearchView
import asset.trak.database.daoModel.BookAndAssetData
import asset.trak.views.adapter.NotFoundAdapter
import asset.trak.views.baseclasses.BaseFragment
import com.markss.rfidtemplate.R
import com.markss.rfidtemplate.application.Application
import com.markss.rfidtemplate.application.Application.bookDao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_my_library_search.*
import kotlinx.android.synthetic.main.fragment_not_found.*
import kotlinx.android.synthetic.main.fragment_not_found.searchView

@AndroidEntryPoint
class NotFoundFragment(private val locationId: Int) : BaseFragment(R.layout.fragment_not_found)
 {

    private lateinit var notFoundAdapter: NotFoundAdapter

     var listBook = ArrayList<BookAndAssetData>()
     override fun onResume() {
         super.onResume()
         setAdaptor()
     }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdaptor()
        Log.d("NotFoundFragment", "setAdaptor: ${locationId}")
        searchView.queryHint = Html.fromHtml("<font color = #D3D3D3>" + getResources().getString(R.string.search) + "</font>");

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                notFoundAdapter.filter.filter(newText)
                return true
            }
        })
    }


    private fun setAdaptor() {
        listBook.clear()
        Log.d("NotFoundFrag", "setAdaptor: ${locationId}")
        val inventoryMasterList = bookDao.getPendingInventoryScan(locationId)
        if(inventoryMasterList.isEmpty()|| inventoryMasterList==null)
        {


        }
        else{
            val inventorymaster= inventoryMasterList.get(0)

            listBook.addAll(bookDao?.getAssetNotFound(locationId,inventorymaster.scanID) ?: arrayListOf())
        }





        for (i in 0 until listBook.size)  {
            var locationName= Application.roomDatabaseBuilder?.getBookDao()?.getLocationName(listBook.get(i).assetCatalogue.locationId?:0)
            listBook[i].assetCatalogue?.locationName= locationName?.locationName?:""
            var category= Application.roomDatabaseBuilder?.getBookDao()?.getCatgeoryName(listBook.get(i).assetCatalogue.categoryId?:0)
            listBook[i].assetCatalogue?.categoryName= category?.categoryName?:""
            var subcategory= Application.roomDatabaseBuilder?.getBookDao()?.getSubCatgeoryName(listBook.get(i).assetCatalogue.subCategoryId?:0)
            listBook[i].assetCatalogue?.categoryName= subcategory?.subCategoryName?:""

        }


            notFoundAdapter = NotFoundAdapter(requireActivity(),requireActivity().supportFragmentManager ,listBook)
            rvNotFound.adapter = notFoundAdapter


    }
     fun updateList() {
         setAdaptor()


     }





}