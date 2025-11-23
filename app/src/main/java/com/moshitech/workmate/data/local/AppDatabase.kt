package com.moshitech.workmate.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.moshitech.workmate.feature.photoconversion.data.local.ConversionHistoryDao
import com.moshitech.workmate.feature.photoconversion.data.local.ConversionHistoryEntity

@Database(entities = [ConversionHistoryEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
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
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
