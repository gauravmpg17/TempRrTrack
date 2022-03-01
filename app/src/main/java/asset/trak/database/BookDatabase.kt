package asset.trak.database

import androidx.room.Database
import androidx.room.RoomDatabase
import asset.trak.database.entity.*

@Database(
    entities = [BookAttributes::class, CategoryMaster::class, AssetClassification::class, AssetClassCatMap::class,
        LocationMaster::class, AssetCatalogue::class, CatSubCatMap::class, SubCategoryMaster::class,Inventorymaster::class,
        ScanTag::class],
    version = 1,
    exportSchema = false
)
abstract class BookDatabase : RoomDatabase() {
    abstract fun getBookDao(): BookDao
}