package com.example.myapplication

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface RetroInterface {
    //https://apis.data.go.kr/B552584/ArpltnInforInqireSvc/getMsrstnAcctoRltmMesureDnsty?serviceKey=ubXQmzOKtgQA4qGn1x%2FX9iibyvbpy3dYpk%2FGC9EyPZSPqCKUc7FM9xdkGK7xmQaQrZwB0%2BhIov6JyWPr8SwBBA%3D%3D&returnType=xml&numOfRows=50&pageNo=1&stationName=%EA%B0%95%EB%B6%81%EA%B5%AC&dataTerm=DAILY&ver=1.0
    @GET("getMsrstnAcctoRltmMesureDnsty?")
    fun getRetrofit(
        @Query("serviceKey") key: String?,
        @Query("returnType") returnType: String?,
        @Query("numOfRows") numOfRows: String?,
        @Query("pageNo") pageNo: String?,
        @Query("sidoName") stationName: String?,
        @Query("ver") ver: String?
    ): Call<ModelMunzzi>
}