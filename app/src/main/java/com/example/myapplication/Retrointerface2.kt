package com.example.myapplication

import MySModel
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface Retrointerface2 { //측정소정보
    //http://apis.data.go.kr/B552584/ArpltnInforInqireSvc/getMsrstnAcctoRltmMesureDnsty?
    @GET("getMsrstnAcctoRltmMesureDnsty?")
    fun getRetrofit2(
        @Query("stationName") stationName: String?,
        @Query("dataTerm") dataTerm: String?,
        @Query("pageNo") pageNo: String?,
        @Query("numOfRows") numOfRows: String?,
        @Query("returnType") returnType: String?,
        @Query("serviceKey") serviceKey: String?,
        @Query("ver") ver:String?
    ): Call<MySModel>
}