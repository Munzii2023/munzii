package com.example.myapplication

import com.google.gson.annotations.SerializedName

// 날씨 정보를 담는 데이터 클래스
data class ModelMunzzi(
    var tm : String? = null,
    var addr : String? = null,
    var stationName : String? = null,
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

data class MyItem(val item: ModelMunzzi)
data class MyStationItem(val item : ModelStation)

data class MyItems(val items: MutableList<MyItem>)
data class MyStationItems(val items: MutableList<MyStationItem>)

data class MyModel(val body: MyItems)
data class MyStationModel(val body: MyStationItems)