package com.zachupstone.walkanywhere.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [RouteEntity::class, StepsEntity::class], version = 1, exportSchema = false)
@TypeConverters(DatabaseConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao
    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "trip_database"
                )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
