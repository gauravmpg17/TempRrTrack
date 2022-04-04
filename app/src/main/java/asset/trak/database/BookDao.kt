package asset.trak.database

import androidx.lifecycle.LiveData
import androidx.room.*
import asset.trak.database.daoModel.BookAndAssetData
import asset.trak.database.entity.*
import asset.trak.modelsrrtrack.*

@Dao
interface BookDao {
    /*RrTrack*/
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAssetMain(assetMain: List<AssetMain>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addInventoryScan(inventoryScan: List<InventoryScan>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addMasterLocation(masterLocation: List<MasterLocation>)

    //MasterVendor
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addMasterVendor(masterVendor: List<MasterVendor>)
    /**/

    @Query("SELECT * FROM assetMain")
    suspend fun getBooks(): List<AssetMain>

    @Query("SELECT COUNT(*) FROM tblAssetCatalogue WHERE assetClassId IN (:assetId)")
    fun getAssetCount(assetId: Int): Int

    @Query("SELECT COUNT(*) FROM tblAssetCatalogue WHERE categoryId IN (:catId)")
    fun getCategoryAssetCount(catId: Int): Int


    @Query("SELECT * FROM masterLocation WHERE LocID IN (:locationId)")
    fun getLocationName(locationId: Int): MasterLocation

//    @Query("SELECT * FROM assetMain WHERE id IN (:catId)")
//    fun getCatgeoryName(catId: Int): CategoryMaster


    @Query("SELECT COUNT(*) FROM tblAssetCatalogue WHERE subCategoryId IN (:subCatId)")
    fun getSubCategoryAssetCount(subCatId: Int): Int

    @Query("SELECT COUNT(id) FROM tblAssetCatalogue")
    fun getCount(): Int

    @Query("SELECT COUNT(locationId) FROM assetMain WHERE locationId IN (:locationId)")
    suspend fun getCountLocationIdKt(locationId: Int): Int

    @Query("SELECT COUNT(locationId) FROM assetMain WHERE locationId IN (:locationId)")
    fun getCountLocationId(locationId: Int): Int

    @Query("SELECT COUNT(DISTINCT AssetRFID) FROM assetMain")
    fun getCountRegisterForGlobal(): Int

    @Query("SELECT COUNT(id) FROM tblAssetCatalogue WHERE inventoryScanId IS NULL")
    fun getCountNotReconciled(): Int

    @Query("SELECT COUNT(id) FROM tblAssetCatalogue WHERE inventoryScanId IS NOT NULL")
    fun getCountReconciled(): Int

    @Query("SELECT * FROM tblAssetCatalogue WHERE categoryId IN (:categoryId)")
    fun getBooksCategory(categoryId: Int): List<BookAndAssetData>

    @Query("SELECT * FROM tblCatSubCatMap WHERE categoryId IN (:CatId)")
    fun getCatSubCatMapByCatId(CatId: Int): List<CatSubCatMap>

    @Query("SELECT * FROM tblSubCategoryMaster WHERE id IN (:ids)")
    fun getSubCategoriesByCatIds(ids: ArrayList<Int>): List<SubCategoryMaster>

    @Query("SELECT * FROM tblCategoryMaster")
    fun getCategoriesMasterList(): List<CategoryMaster>

    @Query("SELECT * FROM tblCategoryMaster WHERE Id IN (:catIds)")
    fun getCategoryListByCategoriesIds(catIds: ArrayList<Int>): List<CategoryMaster>

    @Query("SELECT * FROM tblLocationMaster")
    suspend fun getLocationMasterList(): List<LocationMaster>


    @Query("SELECT * FROM tblAssetClassCatMap WHERE classificationId IN (:classId) ")
    fun getCategoryIdByClassId(classId: Int): List<AssetClassCatMap>


    @Query("SELECT * FROM tblAssetClassification")
    fun getAssetClassficationMasterList(): List<AssetClassification>

    @Query("SELECT * FROM assetMain WHERE AssetRFID IN (:rfidTag)")
    fun getBookForRFID(rfidTag: String): List<AssetMain>

    @Query("SELECT * FROM tblInventorymaster WHERE _id IN (:id)")
    fun getInventoryMaster(id: Int): List<Inventorymaster>

    @Query("SELECT COUNT(_id) FROM tblInventorymaster")
    suspend fun getInventoryMasterAllCount(): Int

    //temporary
    @Query("SELECT * FROM assetMain WHERE inventorySyncFlag=1")
    fun getAssetsPendingToSync(): List<AssetMain>

    @Query("SELECT * FROM masterLocation WHERE LocBarcode=:loccode")
    suspend fun getLocationMasterDataRR(loccode: String): MasterLocation

//    @Query("UPDATE assetMain SET  inventorySyncFlag= 0  WHERE id IN (:ids)")
//    fun clearSyncFlagOfAssets(ids:List<String>)

    @Query("SELECT * FROM tblInventorymaster where locationId in (:locationId) AND Status='Pending' ORDER BY _id desc LIMIT 1")
    suspend fun getPendingInventoryScanKt(locationId: Int): List<Inventorymaster>


    @Query("SELECT * FROM tblInventorymaster where locationId in (:locationId) AND Status='Pending' ORDER BY _id desc LIMIT 1")
    fun getPendingInventoryScan(locationId: Int): List<Inventorymaster>


    @Query("SELECT * FROM tblInventorymaster where  Status='Pending' ORDER BY _id desc LIMIT 1")
    fun getGlobalPendingInventoryScan(): List<Inventorymaster>


    @Query("SELECT * FROM tblInventorymaster WHERE locationId IN (:id) AND Status ='Completed' ORDER BY ScanOn Desc LIMIT 1")
    suspend fun getLastRecordedInventoryOfLocation(id: Int): List<Inventorymaster>

    @Query("SELECT  COUNT(ScanID) FROM assetMain WHERE locationId IN (:locationId) AND ScanID =(:scanId)")
    suspend fun getCountOfRegisteredAsPerLastInventoryOfLocation(
        locationId: Int,
        scanId: String
    ): Int

    @Query("SELECT * FROM tblScanTag")
    fun getScanTagAll(): List<ScanTag>

    @Query("DELETE FROM mapRFIDLocation")
    fun deletemapRFIDLocationAll()

    @Query("SELECT * FROM mapRFIDLocation")
    fun getMapRFIDLocationAll(): List<MapRFIDLocation>


    @Query("SELECT rfidTag FROM tblScanTag  WHERE locationId IN (:locationId) AND scanId IN (:scanId)")
    fun getScanRfid(locationId: Int, scanId: String): List<String>

    @Query("select count(*) from tblScanTag where LocationId=:locationId and assetId is not null")
    fun getCountOfTagsFound(locationId: Int): Int

    @Query("SELECT rfidTag FROM tblScanTag  WHERE  scanId IN (:scanId)")
    fun getGlobalScanRfid(scanId: String): List<String>

    /*Insert data in ScanTag table
select * from assetmain where locid = ZoneID and assetid not in (select assetid from scantag where scanid = currentscanID)
During Insert set the LastScannedLocID to 'NF' for the inserted records.*/
    //   @Query("SELECT COUNT(*) FROM tblAssetCatalogue AC LEFT JOIN tblScanTag ST ON ST.rfidTag = AC.rfidTag WHERE ST.scanId IS NULL AND AC.locationId IN (:locationId)")
    @Query("select count(*) from tblScanTag where locationId=0 and assetId is not null")
    fun getCountOfTagsNotFound(): Int


 //   @Query("SELECT COUNT(*) FROM assetMain WHERE locationId NOT IN (:locationId) AND AssetRFID  IN (SELECT rfidTag FROM tblScanTag where ScanId IN (:scanId))")
   @Query("SELECT COUNT(*) FROM tblScanTag WHERE assetId IS NOT NULL AND (locationId IS NULL OR locationId != (:locationId)) AND locationId NOT IN(:locationId,0)")
    fun getCountFoundDifferentLoc(locationId: Int): Int

    /*from scantag
where assetID is null*/
    @Query("SELECT COUNT(*) FROM tblScanTag WHERE assetId IS NULL")
    fun getCountNotRegistered(): Int

//    @Query("SELECT COUNT(*) FROM tblScanTag  WHERE  scanId IN (:scanId) AND rfidTag NOT IN (SELECT rfidTag FROM assetMain WHERE rfidTag IS NOT NULL)")
//    fun getCountNotRegistered(scanId: String): Int

    @Query("SELECT COUNT(*) FROM tblScanTag  WHERE rfidTag IN (:rfid) AND scanId IN (:scanId)")
    fun getCountOfTagAlready(rfid: String, scanId: String): Int

    @Query("SELECT COUNT(*) FROM mapRFIDLocation  WHERE rfidTag IN (:rfid) AND scanId IN (:scanId)")
    fun getCountOfMapLocationAlready(rfid: String, scanId: String): Int

    @Query("SELECT * FROM assetMain  WHERE locationId IN (:locationId) AND AssetRFID NOT IN (SELECT rfidTag FROM tblScanTag where ScanId IN (:scanId)) OR (locationId IN (:locationId) AND AssetRFID is null)")
    fun getAssetNotFound(locationId: Int, scanId: String): List<AssetMain>

    @Query("SELECT * FROM assetMain  WHERE locationId NOT IN (:locationId) AND AssetRFID  IN (SELECT rfidTag FROM tblScanTag where ScanId IN (:scanId) )")
    fun getAssetDifferentLoc(scanId: String, locationId: Int): List<AssetMain>

    @Query("SELECT * FROM tblScanTag WHERE scanId IN (:scanId) AND rfidTag NOT IN (SELECT AssetRFID from assetMain WHERE AssetRFID IS NOT NULL)")
    fun getAssetNotRegistered(scanId: String): List<ScanTag>

    /*  @Query("SELECT * FROM tblAssetCatalogue  WHERE locationId  IN (:locationId) AND rfidTag  IN (SELECT rfidTag FROM tblScanTag where ScanId IN (:scanId) )")
    fun getFoundAtLocation(scanId: String, locationId: Int): List<BookAndAssetData>*/

    @Query("SELECT * FROM assetMain  WHERE locationId  IN (:locationId) AND AssetRFID  IN (SELECT rfidTag FROM tblScanTag where ScanId IN (:scanId) )")
    fun getFoundAtLocation(scanId: String, locationId: Int): List<AssetMain>

    @Query("SELECT * FROM assetMain  WHERE AssetRFID  IN (SELECT rfidTag FROM tblScanTag where ScanId IN (:scanId) )")
    fun getFoundAtLocationGlobal(scanId: String): List<AssetMain>


    @Query("UPDATE assetMain  SET ScanID=(:scanId) WHERE locationId  IN (:locationId) AND AssetRFID  IN (SELECT rfidTag FROM tblScanTag where ScanId IN (:scanId) )")
    fun updateScanIdOfReconciledAssets(scanId: String, locationId: Int)

    @Query("UPDATE assetMain  SET ScanID=NULL WHERE locationId  IN (:locationId)")
    suspend fun resetScanIdOfAssetsAtLocation(locationId: Int)


//    @Query("UPDATE assetMain SET locationId= (:locationId) WHERE LocationId=(:id)")
//    fun updateLocationIdOfAssets(locationId: Int,id:String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addBookAttributeList(book: List<BookAttributes>)

    //  fun addAssetCatalogueList(book: AssetCatalogue)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addInventoryMaster(inventoryMasterList: List<Inventorymaster>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addInventoryItem(inventoryMaster: Inventorymaster)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addScanTag(scanTag: ScanTag)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addMapRFIDLocation(mapRFIDLocation: MapRFIDLocation)

    /*Update*/
    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateInventoryItem(inventoryMaster: Inventorymaster)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateBookAndAssetData(listBookAndAssetData: List<ScanTag>)

    /*   /*Update the LastScannedLocID to 'F' in ScanTag table for Ignored Records.*/
                                 */
    @Query("UPDATE tblscantag SET LocationId=(:newlocationId),ScanID=(:scanId),inventorySyncFlag=(:flag) WHERE LocationId IN (:locationId) AND rfidTag IN (:rfidTag)")
    fun updateLocationAssetMain(
        newlocationId: Int,
        locationId: Int,
        scanId: String,
        flag: Int,
        rfidTag: String
    )

    @Query("DELETE FROM tblScanTag WHERE scanId IN (:scanId) AND rfidTag IN (:rfidTag)")
    fun deleteScanTagNotReg(scanId: String, rfidTag: String)

    @Query("DELETE FROM tblScanTag WHERE scanId=(:scanId)")
    fun deleteScanTagSingle(scanId: String)

    @Query("DELETE FROM tblInventorymaster WHERE scanId=(:scanId)")
    fun deleteInventorySingle(scanId: String)

    //  @Query("SELECT * FROM assetMain WHERE ExitDate > (:exitDate) AND ScanID=(:scanId) AND LocationId=(:locationId)  ")

    @Query("SELECT * FROM assetMain WHERE ScanID=(:scanId) AND LocationId=(:locationId)")
    fun selectAssetMainLocationNullRecords(scanId: String, locationId: Int): List<AssetMain>

//    @Delete
//    fun deleteScanTagSingle(listScanTag: ScanTag)

//    @Query("UPDATE orders SET order_amount = :amount, price = :price WHERE order_id =:id")
//void update(Float amount, Float price, int id);


    @Query("DELETE FROM assetMain")
    suspend fun deleteAssetMainTable()


    @Query("DELETE FROM inventoryScan")
    suspend fun deleteInventoryScanTable()

    @Query("DELETE FROM mapRFIDLocation")
    suspend fun deleteMapRFIDLocationTable()

    @Query("DELETE FROM masterLocation")
    suspend fun deleteMasterLocationTable()

    @Query("DELETE FROM masterVendor")
    suspend fun deleteMasterVendorTable()

    @Query("DELETE FROM tblAssetCatalogue")
    suspend fun deleteTblAssetCatalogueTable()

    @Query("DELETE FROM tblAssetClassCatMap")
    suspend fun deleteTblAssetClassCatMapTable()

    @Query("DELETE FROM tblAssetClassification")
    suspend fun deleteTblAssetClassificationTable()

    @Query("DELETE FROM tblBookAttributes")
    suspend fun deleteTblBookAttributesTable()

    @Query("DELETE FROM tblCatSubCatMap")
    suspend fun deleteTblCatSubCatMapTable()

    @Query("DELETE FROM tblCategoryMaster")
    suspend fun deleteTblCategoryMasterTable()

    @Query("DELETE FROM tblInventorymaster")
    suspend fun deleteTblInventoryMasterTable()

    @Query("DELETE FROM tblLocationMaster")
    suspend fun deleteTblLocationMasterTable()

    @Query("DELETE FROM tblScanTag")
    suspend fun deleteTblScanTagTable()

    @Query("DELETE FROM tblSubCategoryMaster")
    suspend fun deleteTblSubCategoryMasterTable()

    @Delete
    fun deleteScanTag(listScanTag: List<ScanTag>)

    @Insert
    suspend fun saveAppTimeStamp(appTimeStamp: AppTimeStamp)

    @Query("SELECT * FROM appTimeStamp ORDER BY id DESC LIMIT 1,1")
    suspend fun retriveTimeStamp(): AppTimeStamp

    @Query("DELETE FROM assetMain WHERE ExitDate IS NOT NULL")
    suspend fun deleteOutwardRecords()

    @Query("SELECT Name FROM masterLocation WHERE ParentLocID IS NOT NULL")
    suspend fun appConfigLocationNames(): List<String>

    @Insert
    suspend fun saveOffLocation(offLocation: OffLocation)

    @Query("SELECT COUNT(*) FROM offlocation")
    suspend fun checkTableIsEmpty(): Int

    @Query("UPDATE offlocation SET locationName=:name")
    suspend fun updateOffLocation(name: String)

    @Query("SELECT * FROM offlocation ORDER BY id DESC LIMIT 1")
    suspend fun getOffLocation(): OffLocation

    @Query("DELETE FROM offlocation")
    suspend fun deleteOffLocation()

    @Query("SELECT * FROM assetMain where AssetRFID=:rfidTag")
    fun getRFIDTagDetails(rfidTag: String): AssetMain
}
