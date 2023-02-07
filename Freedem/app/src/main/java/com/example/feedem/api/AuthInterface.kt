package com.example.feedem.api

import com.example.feedem.model.AuthRequest
import com.example.feedem.model.AuthResponse
import com.example.feedem.url.Urls
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface AuthInterface {

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST(Urls.authenticationUrl)
    fun authorizeApp(
        @Body authModel : AuthRequest
    ): Call<AuthResponse>

}