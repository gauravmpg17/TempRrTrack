package asset.trak.modelsrrtrack

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventoryScan")
 class InventoryScan(
    @PrimaryKey(autoGenerate = false)
    val ScanID:String,
    val ScanDate: String?,
    val LocID: Int?,
    var status: String? = null,
    val ScanStartDatetime: String?,
    val ScanEndDatetime: String?,
    val NoofAssetsScanned: Int?,
    val FoundForLoc: Int?,
    val NotFoundForLoc: Int?,
    val FoundForOtherLoc: Int?,
    val ScannedBy: String?
)
