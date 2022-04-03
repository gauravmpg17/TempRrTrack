package asset.trak.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import asset.trak.database.entity.*
import asset.trak.modelsrrtrack.*
import asset.trak.utils.DateTypeConverter

@Database(
    entities = [AssetMain::class, InventoryScan::class, MasterLocation::class, MasterVendor::class, BookAttributes::class, CategoryMaster::class, AssetClassification::class, AssetClassCatMap::class,
        LocationMaster::class, AssetCatalogue::class, CatSubCatMap::class, SubCategoryMaster::class, Inventorymaster::class,
        ScanTag::class, MapRFIDLocation::class, AppTimeStamp::class, OffLocation::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateTypeConverter::class)
abstract class BookDatabase : RoomDatabase() {
    abstract fun getBookDao(): BookDao
}