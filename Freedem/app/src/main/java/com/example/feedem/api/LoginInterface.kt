package com.example.feedem.api

import com.example.feedem.model.LoginDetails
import com.example.feedem.url.Urls
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface LoginInterface {
    @POST(Urls.loginUrl)
    fun userLogin(
        @Body LoginDetails: LoginDetails,
        @Header("APP_TOKEN_KEY") appTokenKey: String
    ): Call<LoginResponse>
}