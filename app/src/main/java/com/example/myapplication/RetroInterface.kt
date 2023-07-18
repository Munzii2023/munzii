package com.example.myapplication

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface RetroInterface {
    //https://apis.data.go.kr/B552584/MsrstnInfoInqireSvc/getNearbyMsrstnList?serviceKey=ubXQmzOKtgQA4qGn1x%2FX9iibyvbpy3dYpk%2FGC9EyPZSPqCKUc7FM9xdkGK7xmQaQrZwB0%2BhIov6JyWPr8SwBBA%3D%3D&returnType=json&tmX=244148.546388&tmY=412423.75772&ver=1.0
    @GET("getNearbyMsrstnList?")
    fun getRetrofit(
        @Query("tmX") tmX: String?,
        @Query("tmY") tmY: String?,
        @Query("returnType") returnType: String?,
        @Query("serviceKey") serviceKey: String?,
        @Query("ver") ver: String?
    ): Call<MyModel>
}