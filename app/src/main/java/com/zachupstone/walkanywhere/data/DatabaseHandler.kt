package com.zachupstone.walkanywhere.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Embedded
import androidx.room.Relation
import com.google.android.gms.maps.model.LatLng
import java.util.Date

@Entity(tableName = "routes")
data class RouteEntity(
    @PrimaryKey(autoGenerate = true) val routeId: Int = 0,
    val origin: LatLng,
    val destination: LatLng,
    val encodedPolyline: String,
    val dateTimestamp: Date = Date(),
    val selected: Boolean = false
)

@Entity(
    tableName = "steps",
    foreignKeys = [
        ForeignKey(
            entity = RouteEntity::class,
            parentColumns = ["routeId"],
            childColumns = ["parentRouteId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class StepsEntity(
    @PrimaryKey(autoGenerate = true) val routeId: Int = 0,
    val parentRouteId: Int,
    val date: Date,
    val steps: Int
)

data class RouteWithSteps(
    @Embedded val route: RouteEntity,

    @Relation(
        parentColumn = "routeId",
        entityColumn = "parentRouteId"
    )
    val steps: List<StepsEntity>
)

