package asset.trak.modelsrrtrack

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "masterLocation")
data class MasterLocation(
    @PrimaryKey(autoGenerate = false)
    val LocID: Int,
    val CreatedBy: String?,
    val CreatedOn: String?,
    val Description: String?,
    val DisplayName: String?,
    val ImageURL: String?,
    val InactiveOn: String?,
    val LocBarcode: String?,
    val LocQRCode: String?,
    val LocRFID: String?,
    val ModifiedBy: String?,
    val ModifiedOn: String?,
    val Name: String?,
    val ParentLocID: Int?
){
    var id:Int=0
}