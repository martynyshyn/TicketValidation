package com.sitegist.ticketvalidation.services

import com.sitegist.ticketvalidation.data.Ticket
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface ValidationService {
    @GET("api/validator/check")
    fun checkOrder(
        @Query("order") orderNumber: String,
        @Header("Authorization") authToken: String
    ): Call<Ticket>
}

