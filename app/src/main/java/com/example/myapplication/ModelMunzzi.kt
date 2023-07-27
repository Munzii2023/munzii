data class MYModel (val response: Response)// 측정소 - tm좌표
data class Response (
    val body: Body,
    val header: Header
)
data class Body (
    val totalCount: Long,
    val items: List<MunziiItem>,
    val pageNo: Long,
    val numOfRows: Long
)
data class MunziiItem (
    val stationCode: String ?=null,
    val tm: Double ?=null,
    val addr: String ?=null,
    val stationName: String ?=null
)
data class Header (
    val resultMsg: String,
    val resultCode: String
)

data class MySModel (val response: SResponse) // 미세먼지 - 측정소 이름
data class SResponse (
    val body: SBody,
    val header: SHeader
)
data class SBody (
    val totalCount: Long,
    val items: List<Map<String, String?>>,
    val pageNo: Long,
    val numOfRows: Long
)
data class SHeader (
    val resultMsg: String,
    val resultCode: String
)

data class MyAModel (val response: AResponse) // 미세먼지 - 시도이름
data class AResponse (
    val body: ABody,
    val header: AHeader
)
data class ABody (
    val totalCount: Long,
    val items: List<AMunziiItem>,
    val pageNo: Long,
    val numOfRows: Long
)
data class AHeader (
    val resultMsg: String,
    val resultCode: String
)
data class AMunziiItem (
    val pm10Value : String ?= null,
    val pm25Value : String ?= null,
    val sidoName : String ?= null,
    val stationName: String? = null
)


data class MyBModel (val response: BResponse) // 측정소 - 시도 이름, 측정소 이름
data class BResponse (
    val body: BBody,
    val header: BHeader
)
data class BBody (
    val totalCount: Long,
    val items: List<BMunziiItem>,
    val pageNo: Long,
    val numOfRows: Long
)
data class BHeader (
    val resultMsg: String,
    val resultCode: String
)
data class BMunziiItem (
    val dmX : String ?= null,
    val dmY : String ?= null,
    val addr : String ?= null,
    val stationName: String? = null
)
