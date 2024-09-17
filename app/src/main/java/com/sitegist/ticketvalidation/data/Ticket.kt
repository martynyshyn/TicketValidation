package com.sitegist.ticketvalidation.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Ticket(
    val status: String,
    val message: String,
    val order_id: String
) : Parcelable
