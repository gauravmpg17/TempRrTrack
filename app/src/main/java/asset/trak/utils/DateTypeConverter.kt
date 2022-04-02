package asset.trak.utils

import androidx.room.TypeConverter
import java.util.*

class DateTypeConverter {
    @TypeConverter
    fun fromTimeStamp(value:Long?) :Date?{
        return if(value==null) null else Date(value)
    }

    @TypeConverter
    fun dateToTimeStamp(date: Date?):Long?{
        return date?.time
    }


}