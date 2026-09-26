package com.zachupstone.walkanywhere.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface TripDao {

    // Routes
    @Transaction
    @Query("SELECT * FROM routes ORDER BY dateTimestamp ASC")
    fun getAllRoutes(): Flow<List<RouteWithSteps>>

    @Transaction
    @Query("SELECT * FROM routes WHERE selected == true LIMIT 1")
    fun getSelectedRoute(): Flow<RouteWithSteps?>

    @Insert
    suspend fun insertRoute(route: RouteEntity): Long

    @Query("DELETE FROM routes WHERE routeId = :routeId")
    suspend fun deleteRoute(routeId: Int)

    @Query("UPDATE routes SET selected = :selected WHERE routeId = :routeId")
    suspend fun setSelected(routeId: Int, selected: Boolean)

    @Query("UPDATE routes SET selected = 0")
    suspend fun clearSelected()

    @Transaction
    suspend fun selectRoute(routeId: Int) {
        clearSelected()
        setSelected(routeId, true)
    }

    // Steps
    @Insert
    suspend fun insertSteps(steps: List<StepsEntity>)

    @Transaction
    @Query("SELECT * FROM steps ORDER BY date ASC")
    fun getAllSteps(): Flow<List<StepsEntity>>

    // dates
    @Query("SELECT MAX(date) FROM steps")
    suspend fun lastSyncedDate(): LocalDate?
}
