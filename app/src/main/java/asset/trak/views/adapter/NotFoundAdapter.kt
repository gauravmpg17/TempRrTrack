package asset.trak.views.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.appcompat.widget.AppCompatImageView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import asset.trak.database.daoModel.BookAndAssetData
import asset.trak.modelsrrtrack.AssetMain
import asset.trak.utils.decreaseRangeToThirty
import asset.trak.utils.getFormattedDate
import asset.trak.views.fragments.ScanFragment
import com.bumptech.glide.Glide

import com.markss.rfidtemplate.R
import com.markss.rfidtemplate.application.Application
import com.markss.rfidtemplate.home.MainActivity
import com.markss.rfidtemplate.locate_tag.LocateOperationsFragment
import com.markss.rfidtemplate.locate_tag.SingleTagLocateFragment
import com.markss.rfidtemplate.rfid.RFIDController
import java.io.File
import java.text.SimpleDateFormat


class NotFoundAdapter(
    private val context: Context,
    private val fragment: FragmentManager,
    private var items: ArrayList<AssetMain>,
) :
    RecyclerView.Adapter<NotFoundAdapter.NotFoundHolder>(), Filterable {
    private var mFilteredList: List<AssetMain>? = null

    inner class NotFoundHolder(view: View) : RecyclerView.ViewHolder(view) {
        var tvTitle: TextView = view.findViewById(R.id.tvTitle)
        var tvAuthor: TextView = view.findViewById(R.id.tvAuthor)
        var tvEdition: TextView = view.findViewById(R.id.tvTag)
        var tvSearch: TextView = view.findViewById(R.id.tvSearch)
        var tvCategory: TextView = view.findViewById(R.id.tvCategory)
        var tv: TextView = view.findViewById(R.id.tv)
        var ivBook: AppCompatImageView = view.findViewById(R.id.ivBook)
        var clMain: CardView = view.findViewById(R.id.constLay123)
        var ivCheck: ImageView = view.findViewById(R.id.ivCheck)


    }

    @NonNull
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotFoundHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.itme_not_found, parent, false)
        return NotFoundHolder(itemView)
    }

    override fun onBindViewHolder(holder: NotFoundHolder, position: Int) {
        val item = items[position]



        holder.tvTitle.text = item?.Supplier

        if (item.SampleType.isNullOrEmpty()) {
            holder.tvAuthor.text = "-"
        } else {
            holder.tvAuthor.text = item.SampleType
        }
        holder.tvCategory.text = item.SampleNature + " | " + item.Season
        holder.tvEdition.text = item.Location + if (item.ScanDate.isNullOrEmpty()) {
            ""
        } else {
            if (item.Location.isNullOrEmpty()) {
                ""
            } else {
                " | "
            } + getFormattedDate(
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"),
                SimpleDateFormat("dd-MM-yyyy"), item.ScanDate.toString
                    ()
            )
        }

        holder.clMain.setBackgroundResource(if (item.isSelected) R.drawable.rectangle_background_light_blue else R.drawable.rectangle_background_border)



        holder.clMain.setOnClickListener {
            items[position].isSelected = !items[position].isSelected
            notifyDataSetChanged()
        }

        holder.tvSearch.visibility = View.VISIBLE
        holder.tvSearch.setOnClickListener {
    /*        try {
                decreaseRangeToThirty(300)
            }
            catch (e: Exception){
                Log.d("decreaseRangeToThirty", e.message.toString())
            }
*/
            Application.locateTag = item.AssetRFID
            RFIDController.accessControlTag = item.AssetRFID
            Application.PreFilterTag = item.AssetRFID
            Application.comefrom = "show"

            replaceFragment(
                fragment, LocateOperationsFragment(),
                R.id.content_frame
            )

        }
        holder.tv.visibility = View.GONE
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

                            if (mFilterData.Supplier?.lowercase()
                                    ?.contains(constraint.toString().lowercase()) == true
                                || mFilterData.Location?.lowercase()
                                    ?.contains(constraint.toString().lowercase()) == true
                                || mFilterData.SampleType?.lowercase()
                                    ?.contains(constraint.toString().lowercase()) == true ||
                                mFilterData.SampleNature?.lowercase()
                                    ?.contains(constraint.toString().lowercase()) == true ||
                                mFilterData.Season?.lowercase()
                                    ?.contains(constraint.toString().lowercase()) == true
                                || getFormattedDate(
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

    fun replaceFragment(fragmentManager: FragmentManager?, fragment: Fragment, id: Int) {
        fragmentManager?.beginTransaction()
            ?.replace(id, fragment, MainActivity.TAG_CONTENT_FRAGMENT)?.addToBackStack(null)
            ?.commit()
    }
}