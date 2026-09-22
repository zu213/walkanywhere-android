package com.zachupstone.walkanywhere.data

import androidx.room.TypeConverter
import com.google.android.gms.maps.model.LatLng
import java.util.Date

class DatabaseConverter {

    @TypeConverter
    fun fromDate(date: Date?): Long? = date?.time

    @TypeConverter
    fun toDate(timestamp: Long?): Date? = timestamp?.let { Date(it) }

    @TypeConverter
    fun fromLatLng(latLng: LatLng?): String? {
        return latLng?.let { "${it.latitude},${it.longitude}" }
    }

    @TypeConverter
    fun toLatLng(value: String?): LatLng? {
        if (value.isNullOrEmpty()) return null
        val pieces = value.split(",")
        return if (pieces.size == 2) {
            LatLng(pieces[0].toDouble(), pieces[1].toDouble())
        } else null
    }
}