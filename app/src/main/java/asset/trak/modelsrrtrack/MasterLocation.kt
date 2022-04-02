package asset.trak.modelsrrtrack

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "masterLocation")
@Parcelize
data class MasterLocation(
    @PrimaryKey(autoGenerate = false)
    var LocID: Int,
    val Name: String?,
    val LocBarcode: String,
    val ParentLocID: Int?
):Parcelable