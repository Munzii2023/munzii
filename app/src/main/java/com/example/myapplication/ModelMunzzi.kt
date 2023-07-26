
/*
MyModel : 근처 측정소 api 연결 모델
MySModel : 미세먼지 정보 연결 모델
 */
data class MyModel (val response: Response)

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

data class ModelStation(
    var numOfRows : String? = null,
    var pageNo : String? = null,
    var totalCount : String? = null,
    var dataTime : String ?= null,
    var stationName : String ?= null,
    var pm10Value : String ?= null, //미세먼지 10농도
    var pm25Value : String ?= null //미세먼지 2.5 농도
)

data class MySModel (val response: SResponse)

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
