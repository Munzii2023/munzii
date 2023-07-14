import com.example.myapplication.RetroInterface
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitObject{
    private fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://apis.data.go.kr/B552584/ArpltnInforInqireSvc/")
            .addConverterFactory(GsonConverterFactory.create()) // Json데이터를 사용자가 정의한 Java 객채로 변환해주는 라이브러리
            .build()
    }

    fun getRetrofitService(): RetroInterface {
        return getRetrofit().create(RetroInterface::class.java) //retrofit객체 만듦!
    }

}