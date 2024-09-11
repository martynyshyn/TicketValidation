package com.example.ticketvalidation

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface ApiService {
    @GET
    fun fetchData(@Url url: String): Call<Any>
}
