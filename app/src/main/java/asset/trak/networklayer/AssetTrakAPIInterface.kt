package asset.trak.networklayer

import asset.trak.database.entity.LastSyncObject
import asset.trak.model.AssetSyncRequestDataModel
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface AssetTrakAPIInterface {

    @GET("api/assetsync")
    suspend fun geLastSync(@QueryMap hashMap: Map<String, String>): Response<LastSyncObject>

    @POST("api/assetsync")
    suspend fun posAssetSync(@Body body:RequestBody): Response<ResponseBody>
}