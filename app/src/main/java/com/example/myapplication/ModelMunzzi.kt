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

data class ModelStation(
    var pm25Grade1h: String? = null,
    var pm10Value24: String? = null,
    var so2Value: String? = null,
    var pm10Grade1h: String? = null,
    var o3Grade: String? = null,
    var pm10Value: String? = null,
    var pm25Flag: String? = null,
    var khaiGrade: String? = null,
    var pm25Value: String? = null,
    var no2Flag: String? = null,
    var mangName: String? = null,
    var stationName: String? = null,
    var no2Value: String? = null,
    var so2Grade: String? = null,
    var stationCode: String? = null,
    var coFlag: String? = null,
    var khaiValue: String? = null,
    var coValue: String? = null,
    var pm10Flag: String? = null,
    var no2Grade: String? = null,
    var pm25Value24: String? = null,
    var o3Flag: String? = null,
    var pm25Grade: String? = null,
    var so2Flag: String? = null,
    var coGrade: String? = null,
    var dataTime: String? = null,
    var pm10Grade: String? = null,
    var o3Value: String? = null,
    var numOfRows: String? = null,
    var pageNo: String? = null,
    var totalCount: String? = null,
    var tmX: Double,
    var tmY: Double
)




data class MySModel (val response: SResponse) // 미세먼지 - 측정소 이름
data class SResponse (
    val body: SBody,
    val header: SHeader
)
data class SBody (
    val totalCount: Long,
    val items: List<ModelStation?>,
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
