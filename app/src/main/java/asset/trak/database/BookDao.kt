package asset.trak.database

import androidx.room.*
import asset.trak.database.daoModel.BookAndAssetData
import asset.trak.database.entity.*
import asset.trak.modelsrrtrack.AssetMain
import asset.trak.modelsrrtrack.InventoryScan
import asset.trak.modelsrrtrack.MasterLocation
import asset.trak.modelsrrtrack.MasterVendor

@Dao
interface BookDao {
    /*RrTrack*/
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addAssetMain(assetMain: List<AssetMain>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addInventoryScan(inventoryScan: List<InventoryScan>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addMasterLocation(masterLocation: List<MasterLocation>)

    //MasterVendor
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addMasterVendor(masterVendor: List<MasterVendor>)
    /**/

    @Query("SELECT * FROM tblAssetCatalogue")
    fun getBooks(): List<BookAndAssetData>

    @Query("SELECT COUNT(*) FROM tblAssetCatalogue WHERE assetClassId IN (:assetId)")
    fun getAssetCount(assetId: Int): Int

    @Query("SELECT COUNT(*) FROM tblAssetCatalogue WHERE categoryId IN (:catId)")
    fun getCategoryAssetCount(catId: Int): Int

    @Query("SELECT * FROM tblLocationMaster WHERE id IN (:locationId)")
    fun getLocationName(locationId: Int): LocationMaster

    @Query("SELECT * FROM tblCategoryMaster WHERE id IN (:catId)")
    fun getCatgeoryName(catId: Int): CategoryMaster

    @Query("SELECT * FROM tblSubCategoryMaster WHERE id IN (:catId)")
    fun getSubCatgeoryName(catId: Int): SubCategoryMaster

    @Query("SELECT COUNT(*) FROM tblAssetCatalogue WHERE subCategoryId IN (:subCatId)")
    fun getSubCategoryAssetCount(subCatId: Int): Int

    @Query("SELECT COUNT(id) FROM tblAssetCatalogue")
    fun getCount(): Int

    @Query("SELECT COUNT(id) FROM tblAssetCatalogue WHERE locationId IN (:locationId)")
    fun getCountLocationId(locationId: Int): Int

    @Query("SELECT COUNT(id) FROM tblAssetCatalogue WHERE inventoryScanId IS NULL")
    fun getCountNotReconciled(): Int

    @Query("SELECT COUNT(id) FROM tblAssetCatalogue WHERE inventoryScanId IS NOT NULL")
    fun getCountReconciled(): Int

    @Query("SELECT COUNT(id) FROM tblAssetCatalogue WHERE locationId IN (:locationId) AND inventoryScanId IS NOT NULL")
    fun getCountRegisteredLastScan(locationId: Int): Int

    @Query("SELECT COUNT(id) FROM tblAssetCatalogue WHERE locationId IN (:locationId) AND inventoryScanId IN (:scanId)")
    fun getCountLastScanRegistered(locationId: Int,scanId: String): Int

    @Query("SELECT COUNT(id) FROM tblAssetCatalogue WHERE locationId IN (:locationId) AND createdOn > (SELECT ScanOn FROM tblinventorymaster where scanID in (:scanId))")
    fun getCountNewlyRegisteredAfterLastScan(locationId: Int,scanId: String): Int


 //  @Query("SELECT COUNT(id) FROM tblAssetCatalogue WHERE locationId IN (:locationId) AND (createdOn >(:scanOn) OR (:scanOn) is null)  AND inventoryScanId IS NULL")
 //
    @Query("SELECT COUNT(id) FROM tblAssetCatalogue WHERE locationId IN (:locationId) AND inventoryScanId IS NULL")
    fun getCountNewlyScan(locationId: Int): Int

    @Query("SELECT * FROM tblAssetCatalogue WHERE categoryId IN (:categoryId) AND subCategoryId IN (:subCategoryId)")
    fun getBooksCategoryAndSubCategory(categoryId: Int, subCategoryId: Int): List<BookAndAssetData>

    @Query("SELECT * FROM tblAssetCatalogue WHERE categoryId IN (:categoryId)")
    fun getBooksCategory(categoryId: Int): List<BookAndAssetData>

    @Query("SELECT * FROM tblCatSubCatMap WHERE categoryId IN (:CatId)")
    fun getCatSubCatMapByCatId(CatId: Int): List<CatSubCatMap>

    @Query("SELECT * FROM tblSubCategoryMaster WHERE id IN (:ids)")
    fun getSubCategoriesByCatIds(ids: ArrayList<Int>): List<SubCategoryMaster>

    @Query("SELECT * FROM tblBookAttributes LIMIT :limit OFFSET :offset")
    fun getBooksCategoryPagination(limit: Int, offset: Int): List<BookAttributes>

    @Query("SELECT * FROM tblCategoryMaster")
    fun getCategoriesMasterList(): List<CategoryMaster>

    @Query("SELECT * FROM tblCategoryMaster WHERE Id IN (:catIds)")
    fun getCategoryListByCategoriesIds(catIds: ArrayList<Int>): List<CategoryMaster>

    @Query("SELECT * FROM tblSubCategoryMaster")
    fun getSubCategoriesMasterList(): List<SubCategoryMaster>

    @Query("SELECT * FROM tblLocationMaster")
    fun getLocationMasterList(): List<LocationMaster>

    @Query("SELECT * FROM tblAssetClassCatMap")
    fun getAssetClassMapList(): List<AssetClassCatMap>

    @Query("SELECT * FROM tblAssetClassCatMap WHERE classificationId IN (:classId) ")
    fun getCategoryIdByClassId(classId: Int): List<AssetClassCatMap>

    @Query("SELECT * FROM tblCatSubCatMap")
    fun getCatSubMapList(): List<CatSubCatMap>

    @Query("SELECT * FROM tblBookAttributes")
    fun getBookAttributesList(): List<BookAttributes>

    @Query("SELECT * FROM tblAssetClassification")
    fun getAssetClassficationMasterList(): List<AssetClassification>

    @Query("SELECT * FROM tblAssetCatalogue WHERE rfidTag IN (:rfidTag)")
    fun getBookForRFID(rfidTag: String): List<BookAndAssetData>

    @Query("SELECT * FROM tblInventorymaster WHERE _id IN (:id)")
    fun getInventoryMaster(id: Int): List<Inventorymaster>

    @Query("SELECT * FROM tblInventorymaster")
    fun getInventoryMasterAll(): List<Inventorymaster>

    @Query("SELECT COUNT(_id) FROM tblInventorymaster")
    fun getInventoryMasterAllCount(): Int

    @Query("SELECT * FROM tblInventorymaster where locationId in (:locationId) AND Status='Pending' ORDER BY _id desc LIMIT 1")
    fun getPendingInventoryScan(locationId: Int): List<Inventorymaster>

    @Query("SELECT  * FROM tblInventorymaster WHERE locationId IN (:id) AND Status ='Completed' ORDER BY ScanOn Desc LIMIT 1")
    fun getLastRecordedInventoryOfLocation(id: Int): List<Inventorymaster>

    @Query("SELECT * FROM tblScanTag")
    fun getScanTagAll(): List<ScanTag>

    @Query("SELECT * FROM tblScanTag  WHERE locationId IN (:locationId) AND scanId IN (:scanId)")
    fun getScanTag(locationId: Int, scanId: String): List<ScanTag>

    @Query("SELECT rfidTag FROM tblScanTag  WHERE locationId IN (:locationId) AND scanId IN (:scanId)")
    fun getScanRfid(locationId: Int, scanId: String): List<String>

    /*  @Query("SELECT COUNT(*) FROM tblAssetCatalogue WHERE rfidTag IN (:scanTagIds) AND locationId IN (:locationId)")
      fun getCountOfTagsFound(scanTagIds: List<String>,locationId: Int): Int*/

    @Query("SELECT COUNT(*) FROM tblAssetCatalogue AC INNER JOIN tblScanTag ST ON ST.rfidTag = AC.rfidTag WHERE ST.scanId IN (:scanId) AND AC.locationId IN (:locationId)")
    fun getCountOfTagsFound(scanId: String, locationId: Int): Int

    //   @Query("SELECT COUNT(*) FROM tblAssetCatalogue AC LEFT JOIN tblScanTag ST ON ST.rfidTag = AC.rfidTag WHERE ST.scanId IS NULL AND AC.locationId IN (:locationId)")
    @Query("SELECT COUNT(*) FROM tblAssetCatalogue WHERE locationId IN (:locationId) AND rfidTag NOT IN (SELECT rfidTag FROM tblScanTag WHERE ScanId IN(:scanId)) OR (locationId IN (:locationId) AND rfidTag is null)")
    fun getCountOfTagsNotFound(locationId: Int, scanId: String): Int

    @Query("SELECT COUNT(*) FROM tblAssetCatalogue WHERE locationId NOT IN (:locationId) AND rfidTag  IN (SELECT rfidTag FROM tblScanTag where ScanId IN (:scanId))")
    fun getCountFoundDifferentLoc(scanId: String, locationId: Int): Int

    @Query("SELECT COUNT(*) FROM tblScanTag ST LEFT JOIN tblAssetCatalogue AC ON ST.rfidTag = AC.rfidTag WHERE AC.rfidTag IS NULL AND ST.scanId IN (:scanId)")
    fun getCountNotRegistered(scanId: String): Int

    @Query("SELECT COUNT(*) FROM tblScanTag  WHERE rfidTag IN (:rfid) AND scanId IN (:scanId)")
    fun getCountOfTagAlready(rfid: String, scanId: String): Int

    @Query("SELECT * FROM tblAssetCatalogue  WHERE locationId IN (:locationId) AND rfidTag NOT IN (SELECT rfidTag FROM tblScanTag where ScanId IN (:scanId)) OR (locationId IN (:locationId) AND rfidTag is null)")
    fun getAssetNotFound(locationId: Int, scanId: String): List<BookAndAssetData>

    @Query("SELECT * FROM tblAssetCatalogue  WHERE locationId NOT IN (:locationId) AND rfidTag  IN (SELECT rfidTag FROM tblScanTag where ScanId IN (:scanId) )")
    fun getAssetDifferentLoc(scanId: String, locationId: Int): List<BookAndAssetData>

    @Query("SELECT * FROM tblScanTag WHERE scanId IN (:scanId) AND rfidTag NOT IN (SELECT rfidTag from tblAssetCatalogue WHERE rfidTag IS NOT NULL)")
    fun getAssetNotRegistered(scanId: String): List<ScanTag>

    @Query("SELECT * FROM tblAssetCatalogue  WHERE locationId  IN (:locationId) AND rfidTag  IN (SELECT rfidTag FROM tblScanTag where ScanId IN (:scanId) )")
    fun getFoundAtLocation(scanId: String, locationId: Int): List<BookAndAssetData>

    @Query("UPDATE tblAssetCatalogue  SET inventoryScanId=(:scanId) WHERE locationId  IN (:locationId) AND rfidTag  IN (SELECT rfidTag FROM tblScanTag where ScanId IN (:scanId) )")
    fun updateScanIdOfReconciledAssets(scanId: String, locationId: Int)

    @Query("UPDATE tblAssetCatalogue  SET inventoryScanId=NULL WHERE locationId  IN (:locationId)")
    fun resetScanIdOfAssetsAtLocation(locationId: Int)

    @Query("UPDATE tblAssetCatalogue SET locationId= (:locationId) WHERE id=(:id)")
    fun updateLocationIdOfAssets(locationId: Int,id:String)

    /*Add Lists*/
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addAssetCatalogue(book: AssetCatalogue): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addBookAttributes(book: BookAttributes)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addCatSubClassification(book: List<CatSubCatMap>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addAssetClassfication(book: List<AssetClassification>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addAssetClassCatMap(book: List<AssetClassCatMap>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addBooksList(book: List<AssetCatalogue>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addBookAttributeList(book: List<BookAttributes>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addCategoriesMasterList(book: List<CategoryMaster>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addSubCategoriesMasterList(book: List<SubCategoryMaster>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addLocationMasterList(book: List<LocationMaster>)

    //added local image
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addAssetCatalogueList(book: AssetCatalogue)

    //  fun addAssetCatalogueList(book: AssetCatalogue)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addInventoryMaster(inventoryMasterList: List<Inventorymaster>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addInventoryItem(inventoryMaster: Inventorymaster)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addScanTag(scanTag: ScanTag)

    /*Update*/
    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateInventoryItem(inventoryMaster: Inventorymaster)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateBookAndAssetData(listBookAndAssetData: List<AssetCatalogue>)

    /*Delete*/
    @Delete
    fun deleteScanTag(listScanTag: List<ScanTag>)

//    @Query("UPDATE orders SET order_amount = :amount, price = :price WHERE order_id =:id")
//void update(Float amount, Float price, int id);




}