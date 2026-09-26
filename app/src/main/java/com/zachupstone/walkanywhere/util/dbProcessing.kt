package com.zachupstone.walkanywhere.util

import com.google.android.gms.maps.model.LatLng

fun decodeDBPolylineString(string: String): List<LatLng> {
    return string.substring(1, string.length - 2).split("),").map {
        val latlngList = it.split("(").last().split(",")
        return@map LatLng(latlngList[0].toDouble(), latlngList[1].toDouble())
    }
}