package com.example.tragolisto.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import android.content.Context
import androidx.room.Room

@Database(entities = [TragoLocal::class, JuegoFiestaLocal::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract val dao: TragoDao
    abstract val juegoFiestaDao: JuegoFiestaDao

    object DatabaseProvider {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tragos_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}