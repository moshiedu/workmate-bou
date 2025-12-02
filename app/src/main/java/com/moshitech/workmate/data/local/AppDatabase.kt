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
import com.moshitech.workmate.feature.unitconverter.data.local.UnitConversionHistoryEntity
import com.moshitech.workmate.feature.unitconverter.data.local.UnitCategoryConverter

@Database(
    entities = [
        SpeedTestResult::class,
        ConversionFavoriteEntity::class,
        UnitConversionHistoryEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(UnitCategoryConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun speedTestDao(): SpeedTestDao
    abstract fun conversionFavoriteDao(): ConversionFavoriteDao
    abstract fun unitConversionHistoryDao(): UnitConversionHistoryDao

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
