package com.sitegist.ticketvalidation.services

import com.sitegist.ticketvalidation.data.User
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface AuthorizationService {
    @GET
    fun fetchData(@Url url: String): Call<User>
}
