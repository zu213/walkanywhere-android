package com.zachupstone.walkanywhere.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {

    @Transaction
    @Query("SELECT * FROM routes ORDER BY dateTimestamp ASC")
    fun getAllTripPlans(): Flow<List<RouteWithSteps>>

    @Insert
    suspend fun insertRoute(route: RouteEntity): Long // Returns the new routeId

    @Insert
    suspend fun insertSteps(steps: List<StepsEntity>)
}
