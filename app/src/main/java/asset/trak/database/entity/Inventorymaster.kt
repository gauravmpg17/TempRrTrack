package asset.trak.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "tblInventorymaster")
data class Inventorymaster (
    @PrimaryKey(autoGenerate = true)
    var _id: Int?=null,
    var scanID: String = "",
    var deviceId: String? = null,
    var deviceIdCount: Int? = null,
    var scanOn: String? = null,
    var foundOnLocation: Int? = null,
    var foundOfDiffLocation: Int? = null,
    var registered: Int? = null,
    var notRegistered: Int? = null,
    var notFound: Int? = null,
    var locationId: Int? = null,
    var status: String? = null

)