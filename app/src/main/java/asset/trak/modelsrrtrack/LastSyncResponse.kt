package asset.trak.modelsrrtrack

data class LastSyncResponse(
    val `data`: LastSyncData?=LastSyncData(),
    val statusCode: Int?
)