package asset.trak.modelsrrtrack

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assetMain")
data class AssetMain(
    @PrimaryKey(autoGenerate = false)
    val AssetID: String,
    val AssetRFID: String?,
    val CategoryID: String?,
    val ClassID: String?,
    val CreatedBy: String?,
    val CreatedOn: String?,
    val EntryDate: String?,
    val ExitDate: String?,
    val InactiveOn: String?,
    val LocID: Int?,
    val ModifiedBy: String?,
    val ModifiedOn: String?,
    val ScanDate: String?,
    val ScanID: Int?,
    val SubCategoryID: String?
)