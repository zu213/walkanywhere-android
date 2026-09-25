package com.zachupstone.walkanywhere.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {

    @Transaction
    @Query("SELECT * FROM routes ORDER BY dateTimestamp ASC")
    fun getAllRoutes(): Flow<List<RouteWithSteps>>

    @Transaction
    @Query("SELECT * FROM routes WHERE selected == true LIMIT 1")
    fun getSelectedRoute(): Flow<RouteWithSteps?>

    @Insert
    suspend fun insertRoute(route: RouteEntity): Long

    @Insert
    suspend fun insertSteps(steps: List<StepsEntity>)

    @Query("DELETE FROM routes WHERE routeId = :routeId")
    suspend fun deleteRoute(routeId: Int)
}
