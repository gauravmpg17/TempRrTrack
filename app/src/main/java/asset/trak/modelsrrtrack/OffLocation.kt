package asset.trak.modelsrrtrack

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class OffLocation(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var locationName: String
)