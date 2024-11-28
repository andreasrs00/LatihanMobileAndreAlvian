package com.example.latihanmobileandrealvian

import android.os.Parcelable

@Parcelize
data class Homework(
    var id: Int = 0,
    var title: String? = null,
    var description: String? = null,
    var date: String? = null
) : Parcelable