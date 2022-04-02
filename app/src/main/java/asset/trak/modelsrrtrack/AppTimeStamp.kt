package asset.trak.modelsrrtrack

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "appTimeStamp")
data class AppTimeStamp(

var syncDate:Date?,
@PrimaryKey(autoGenerate = true)
var id:Int=0,
)
