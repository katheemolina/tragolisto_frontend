package com.example.tragolisto.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import android.content.Context
import androidx.room.Room

@Database(entities = [TragoLocal::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract val dao: TragoDao

    object DatabaseProvider {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tragos_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}