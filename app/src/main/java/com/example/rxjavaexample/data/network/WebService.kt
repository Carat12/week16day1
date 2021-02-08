package com.example.rxjavaexample.data.network

import com.example.rxjavaexample.data.model.CatResponse
import com.example.rxjavaexample.data.model.SubCatResponse
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import io.reactivex.Single
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Path

interface WebService {

    @GET(Constant.CAT_ENDPOINT)
    fun getCategoryById(): Single<CatResponse>

    @GET("subcategory/{id}")
    fun getSubCatById(@Path("id") id: Int): Single<SubCatResponse>

    companion object{
        operator fun invoke() = Retrofit.Builder()
            .baseUrl(Constant.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create<WebService>()
    }

    object Constant{
        const val BASE_URL = "https://grocery-second-app.herokuapp.com/api/"
        const val CAT_ENDPOINT = "category/5de491ab69ee2123b48daae7"
        const val SUBCAT_ENDPONT = "subcategory/{id}"
    }
}