package asset.trak.views.adapter

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
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
import asset.trak.database.BookDao
import asset.trak.database.daoModel.BookAndAssetData
import asset.trak.database.entity.LocationMaster
import asset.trak.modelsrrtrack.AssetMain
import asset.trak.utils.Constants.setTitleImage
import asset.trak.utils.decreaseRangeToThirty
import asset.trak.utils.getFormattedDate
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.markss.rfidtemplate.R
import com.markss.rfidtemplate.application.Application.bookDao
import com.markss.rfidtemplate.application.Application.roomDatabaseBuilder
import java.io.File
import java.text.SimpleDateFormat

interface OnResultClickListener {
    fun onGoalClick(navToScreen: AssetMain)
}

//
class ResultAdapter(
    private val context: Context,
    private val onGoalClickListener: OnResultClickListener,
    private var items: ArrayList<AssetMain>?,
    private var isFromLib: Boolean? = false
) :
    RecyclerView.Adapter<ResultAdapter.HomeGoalsHolder>(), Filterable {
    private var mFilteredList: List<AssetMain>? = null

    init {
        mFilteredList=items
    }

    inner class HomeGoalsHolder(view: View) : RecyclerView.ViewHolder(view) {
        var tvTitle: AppCompatTextView = view.findViewById(R.id.tvTitle)
        var tvAuthor: AppCompatTextView = view.findViewById(R.id.tvAuthor)
        var tvTag: AppCompatTextView = view.findViewById(R.id.tvTag)
        var tvCategory: AppCompatTextView = view.findViewById(R.id.tvCategory)
        var ivBook: AppCompatImageView = view.findViewById(R.id.ivBook)
        var tv: TextView = view.findViewById(R.id.tv)
        var tvSearch: TextView = view.findViewById(R.id.tvSearch)
    }

    @NonNull
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeGoalsHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.itme_result, parent, false)
        return HomeGoalsHolder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: HomeGoalsHolder, position: Int) {
        val homeGoalsItem = mFilteredList?.get(position)
        homeGoalsItem?.let {
            holder.tvTitle.text = homeGoalsItem.Supplier

            if (homeGoalsItem.SampleType.isNullOrEmpty()) {
                holder.tvAuthor.text = "-"
            } else {
                holder.tvAuthor.text = homeGoalsItem.SampleType
            }
            holder.tvCategory.text = homeGoalsItem.SampleNature + " | " + homeGoalsItem.Season
            holder.tvTag.text =
                homeGoalsItem.Location + if (homeGoalsItem.ScanDate.isNullOrEmpty()) {
                    ""
                } else {
                    if (homeGoalsItem.Location.isNullOrEmpty()) {
                        ""
                    } else {
                        " | "
                    } + getFormattedDate(
                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"),
                        SimpleDateFormat("dd-MM-yyyy"), homeGoalsItem.ScanDate.toString()
                    )
                }
            holder.itemView.setOnClickListener {
                try {
                    decreaseRangeToThirty(300)
                } catch (e: Exception) {
                    Log.d("decreaseRangeToThirty", e.message.toString())
                }
                onGoalClickListener.onGoalClick(homeGoalsItem)
            }
        }


    }

    override fun getItemCount(): Int {
        if (mFilteredList.isNullOrEmpty()) return 0 else return mFilteredList!!.size
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
                    if (items != null && items!!.isNotEmpty()) {
                        for (mFilterData in items!!) {
                            if (mFilterData.Supplier?.lowercase()
                                    ?.contains(constraint.toString().lowercase()) == true
                                || mFilterData.Location?.lowercase()
                                    ?.contains(constraint.toString().lowercase()) == true
                                || mFilterData.SampleType?.lowercase()
                                    ?.contains(constraint.toString().lowercase()) == true ||
                                mFilterData.SampleNature?.lowercase()
                                    ?.contains(constraint.toString().lowercase()) == true ||
                                mFilterData.Season?.lowercase()
                                    ?.contains(constraint.toString().lowercase()) == true ||
                                getFormattedDate(
                                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"),
                                    SimpleDateFormat("dd-MM-yyyy"), mFilterData.ScanDate.toString
                                        ()
                                ).lowercase()?.contains(constraint.toString().lowercase()) == true
                            )

                                results.add(mFilterData)
                        }
                    }
                    oReturn.count = results.size
                    oReturn.values = results
                } else {
                    oReturn.count = items!!.size
                    oReturn.values = items
                }
                return oReturn
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun publishResults(constraint: CharSequence, results: FilterResults?) {
                mFilteredList = results?.values as ArrayList<AssetMain>
                Log.d("tag1212121", "setAdaptor: ${mFilteredList?.size} ")
                notifyDataSetChanged()
                val intent = Intent("COUNT_UPDATE_SEARCH")
                intent.putExtra("searchCount", mFilteredList?.size)
                com.markss.rfidtemplate.application.Application.context.sendBroadcast(intent)

            }
        }
    }
}