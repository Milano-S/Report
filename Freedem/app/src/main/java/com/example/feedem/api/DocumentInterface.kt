package com.example.feedem.api

import com.example.feedem.model.DocumentResponse
import com.example.feedem.model.PeriodData
import com.example.feedem.url.Urls
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface DocumentInterface {
    @POST(Urls.documentUrl)
    fun getReports(
        @Body FromDate: PeriodData,
        @Header("APP_TOKEN_KEY") appTokenKey: String,
        @Header("USER_TOKEN_KEY") userTokenKey: String
    ): Call<DocumentResponse>
}