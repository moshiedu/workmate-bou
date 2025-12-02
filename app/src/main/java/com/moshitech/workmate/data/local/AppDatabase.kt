package com.moshitech.workmate.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.moshitech.workmate.feature.speedtest.data.SpeedTestDao
import com.moshitech.workmate.feature.speedtest.data.SpeedTestResult
import com.moshitech.workmate.feature.unitconverter.data.local.ConversionFavoriteDao
import com.moshitech.workmate.feature.unitconverter.data.local.ConversionFavoriteEntity
import com.moshitech.workmate.feature.unitconverter.data.local.UnitConversionHistoryDao
import com.moshitech.workmate.feature.compass.data.WaypointEntity
import com.moshitech.workmate.feature.compass.data.TripEntity
import com.moshitech.workmate.feature.compass.data.WaypointDao
import com.moshitech.workmate.feature.compass.data.TripDao
import com.moshitech.workmate.feature.unitconverter.data.local.UnitCategoryConverter
import com.moshitech.workmate.feature.unitconverter.data.local.UnitConversionHistoryEntity

import com.moshitech.workmate.feature.deviceinfo.data.BenchmarkHistoryEntity
import com.moshitech.workmate.feature.deviceinfo.data.BenchmarkHistoryDao

import com.moshitech.workmate.feature.photoconversion.data.local.ConversionHistoryEntity
import com.moshitech.workmate.feature.photoconversion.data.local.ConversionHistoryDao

@Database(
    entities = [
        SpeedTestResult::class,
        ConversionFavoriteEntity::class,
        UnitConversionHistoryEntity::class,
        WaypointEntity::class,
        TripEntity::class,
        BenchmarkHistoryEntity::class,
        ConversionHistoryEntity::class
    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(UnitCategoryConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun speedTestDao(): SpeedTestDao
    abstract fun conversionFavoriteDao(): ConversionFavoriteDao
    abstract fun unitConversionHistoryDao(): UnitConversionHistoryDao
    abstract fun waypointDao(): WaypointDao
    abstract fun tripDao(): TripDao
    abstract fun benchmarkHistoryDao(): BenchmarkHistoryDao
    abstract fun conversionHistoryDao(): ConversionHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "workmate_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
