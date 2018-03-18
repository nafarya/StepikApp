package com.example.dan.stepikapp

/**
 * Created by dan on 14.03.18.
 */

import com.example.dan.stepikapp.commons.Model
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Query

interface StepikApiService {

    @GET("search-results")
    fun findCourses(@Query("page") page: Int,
                    @Query("query") subject: String): Observable<Model.Result>

    companion object {
        fun create(): StepikApiService {

            val retrofit = Retrofit.Builder()
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl("https://stepik.org/api/")
                    .build()

            return retrofit.create(StepikApiService::class.java)
        }
    }

}