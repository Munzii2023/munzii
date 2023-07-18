package com.example.myapplication

import com.google.gson.annotations.SerializedName

// 날씨 정보를 담는 데이터 클래스
data class ModelMunzzi (
    @SerializedName("pm25Value") var pm25Value: String = "", //미세먼지
    @SerializedName("pm10Value") var pm10Value: String = "", //초미세먼지
    @SerializedName("dataTime") var dataTime: String = "", //측정시각
)

// xml 파일 형식을 data class로 구현
data class Munzzi (val response : RESPONSE)
data class RESPONSE(val header : HEADER, val body : BODY)
data class HEADER(val resultCode : Int, val resultMsg : String)
data class BODY(val items : ITEMS, val numOfRows : String, val pageNo : String ,val totalCount : Int)
data class ITEMS(val item : List<ITEM>)


data class ITEM(val pm25Value : String, val pm10Value : String, val dataTime : String)