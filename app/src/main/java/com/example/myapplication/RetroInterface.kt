package com.example.myapplication

import MYModel
import MyAModel
import MyBModel
import MySModel
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
// Retrofit2로 연결할 api의 parameter를 정의합니다.
interface RetroInterface { // 측정소 공공데이터 - tm
    //https://apis.data.go.kr/B552584/MsrstnInfoInqireSvc/getNearbyMsrstnList?
    @GET("getNearbyMsrstnList?")
    fun getRetrofit(
        @Query("tmX") tmX: String?,
        @Query("tmY") tmY: String?,
        @Query("returnType") returnType: String?,
        @Query("serviceKey") serviceKey: String?,
        @Query("ver") ver: String?
    ): Call<MYModel>
}

interface Retrointerface2 { // 미세먼지 공공데이터 - 측정소 이름
    //http://apis.data.go.kr/B552584/ArpltnInforInqireSvc/getMsrstnAcctoRltmMesureDnsty?
    @GET("getMsrstnAcctoRltmMesureDnsty?")
    fun getRetrofit2(
        @Query("stationName") stationName: String?,
        @Query("dataTerm") dataTerm: String?,
        @Query("pageNo") pageNo: String?,
        @Query("numOfRows") numOfRows: String?,
        @Query("returnType") returnType: String?,
        @Query("ver") ver: String?,
        @Query("serviceKey") serviceKey: String?
    ): Call<MySModel>
}


interface Retrointerface3 { // 미세먼지 공공데이터 - 시도 이름
    //http://apis.data.go.kr/B552584/ArpltnInforInqireSvc/getMsrstnAcctoRltmMesureDnsty?
    @GET("getCtprvnRltmMesureDnsty?")
    fun getRetrofit3(
        @Query("sidoName") sidoName: String?,
        @Query("pageNo") pageNo: String?,
        @Query("numOfRows") numOfRows: String?,
        @Query("returnType") returnType: String?,
        @Query("serviceKey") serviceKey: String?,
        @Query("ver") ver: String?,
    ): Call<MyAModel>
}

interface Retrointerface4 { // 측정소 공공데이터 - 측정소 이름
    //https://apis.data.go.kr/B552584/MsrstnInfoInqireSvc/getMsrstnList?
    @GET("getMsrstnList?")
    fun getRetrofit4(
        @Query("serviceKey") serviceKey: String?,
        @Query("returnType") returnType: String?,
        @Query("numOfRows") numOfRows: String?,
        @Query("pageNo") pageNo: String?,
        @Query("sidoName") sidoName: String?,
        @Query("stationName") stationName: String?
        ): Call<MyBModel>
}

