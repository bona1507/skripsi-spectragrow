package com.pkmkcub.spectragrow.core.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class Story(
    val photo_url: String = "",
    val title: String = "",
    val content: String = "",
    val lon: Double = 0.0,
    val lat: Double = 0.0
) : Parcelable
