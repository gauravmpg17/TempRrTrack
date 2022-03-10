package asset.trak.views.adapter

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.NonNull
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import asset.trak.database.daoModel.BookAndAssetData
import asset.trak.database.entity.LocationMaster
import asset.trak.modelsrrtrack.AssetMain
import asset.trak.utils.Constants.setTitleImage
import com.bumptech.glide.Glide
import com.markss.rfidtemplate.R
import com.markss.rfidtemplate.application.Application.bookDao
import com.markss.rfidtemplate.application.Application.roomDatabaseBuilder
import java.io.File

interface OnResultClickListener {
    fun onGoalClick(navToScreen: BookAndAssetData)
}
//
class ResultAdapter(
    private val context: Context,
    private val onGoalClickListener: OnResultClickListener,
    private var items: ArrayList<AssetMain>,
    private var isFromLib:Boolean?=false
) :
    RecyclerView.Adapter<ResultAdapter.HomeGoalsHolder>(), Filterable {
    private var mFilteredList: List<AssetMain>? = null


    inner class HomeGoalsHolder(view: View) : RecyclerView.ViewHolder(view) {
        var tvTitle: AppCompatTextView = view.findViewById(R.id.tvTitle)
        var tvAuthor: AppCompatTextView = view.findViewById(R.id.tvAuthor)
        var tvTag: AppCompatTextView = view.findViewById(R.id.tvTag)
        var tvCategory: AppCompatTextView = view.findViewById(R.id.tvCategory)
        var ivBook:AppCompatImageView=view.findViewById(R.id.ivBook)
        var tv: TextView =view.findViewById(R.id.tv)
        var tvSearch: TextView =view.findViewById(R.id.tvSearch)
    }

    @NonNull
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeGoalsHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.itme_result, parent, false)
        return HomeGoalsHolder(itemView)
    }

    override fun onBindViewHolder(holder: HomeGoalsHolder, position: Int) {
        val  homeGoalsItem = items[position]
//        holder.tvTitle.text = homeGoalsItem.assetCatalogue?.assetName
//
//        if( homeGoalsItem.bookAttributes?.author.isNullOrEmpty())
//        {
//            holder.tvAuthor.text = "-"
//        }
//        else
//        {
//            holder.tvAuthor.text = homeGoalsItem.bookAttributes?.author
//        }
//
//        if( homeGoalsItem.assetCatalogue.subCategoryName.equals("") || homeGoalsItem.assetCatalogue.subCategoryName.isNullOrBlank())
//        {
//            holder.tvCategory.text = homeGoalsItem.assetCatalogue.categoryName
//        }
//        else
//        {
//            holder.tvCategory.text ="${homeGoalsItem.assetCatalogue.categoryName} - ${homeGoalsItem.assetCatalogue.subCategoryName}"
//        }
//
//        if(homeGoalsItem.assetCatalogue?.locationName.equals("") || homeGoalsItem.assetCatalogue?.locationName.isNullOrBlank())
//        {
//            holder.tvTag.text = "-"
//        }
//        else
//        {
//            holder.tvTag.text = homeGoalsItem.assetCatalogue?.locationName
//        }
//
//
//        if(isFromLib==true)
//        {
//            holder.tvSearch.visibility=View.VISIBLE
//        }
//        else
//        {
//            holder.tvSearch.visibility=View.GONE
//        }
//
//            holder.tv.visibility=View.GONE
//            Glide.with(context)
//                .load(File(homeGoalsItem.assetCatalogue.imagePathFile.toString()))
//                .placeholder(R.color.light_gray)
//                .fitCenter()
//                .error(R.drawable.ic_not_found_error)
//                .into(holder.ivBook)
//
//
//        holder.itemView.setOnClickListener {
//            onGoalClickListener.onGoalClick(homeGoalsItem)
//        }
    }

    override fun getItemCount(): Int {
        return items.size
    }





    override fun getFilter(): Filter {
        return object : Filter() {

            @SuppressLint("DefaultLocale")
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val oReturn = FilterResults()
                val results = ArrayList<AssetMain>()
                if (mFilteredList == null)
                    mFilteredList = items
                if (constraint != null) {
                    if (mFilteredList != null && mFilteredList!!.isNotEmpty()) {
                        for (mFilterData in mFilteredList!!) {
//                            if (mFilterData.assetCatalogue.assetName?.lowercase()?.contains(constraint.toString().lowercase())==true
//                                || mFilterData.bookAttributes?.author?.lowercase()?.contains(constraint.toString().lowercase())==true ||
//                                mFilterData.assetCatalogue.rfidTag?.lowercase()?.contains(constraint.toString().lowercase())==true||
//                                mFilterData.assetCatalogue.categoryName?.lowercase()?.contains(constraint.toString().lowercase())==true||
//                                mFilterData.assetCatalogue.subCategoryName?.lowercase()?.contains(constraint.toString().lowercase())==true||
//                                mFilterData.assetCatalogue.locationName?.lowercase()?.contains(constraint.toString().lowercase())==true||
//                                mFilterData.assetCatalogue.searchTags?.lowercase()?.contains(constraint.toString().lowercase())==true||
//                                mFilterData.bookAttributes?.publisher?.lowercase()?.contains(constraint.toString().lowercase())==true)

                                results.add(mFilterData)
                        }
                    }
                    oReturn.count = results.size
                    oReturn.values = results
                } else {
                    oReturn.count = items.size
                    oReturn.values = items
                }
                return oReturn
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun publishResults(constraint: CharSequence, results: FilterResults) {
                items = (results.values as ArrayList<AssetMain>)
                notifyDataSetChanged()
            }
        }
    }
}