package com.example.tragolisto

import android.app.Application
import android.util.Log
import androidx.room.Room
import com.example.tragolisto.data.local.AppDatabase
import com.example.tragolisto.data.local.JuegosFiestaData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TragoListoApplication : Application() {
    private val TAG = "TragoListoApplication"
    
    companion object {
        lateinit var database: AppDatabase
            private set
    }

    override fun onCreate() {
        super.onCreate()
        
        // Inicializar la base de datos
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "tragos_db"
        ).fallbackToDestructiveMigration().build()
        
        // Poblar la base de datos con datos iniciales
        poblarBaseDeDatos()
    }
    
    private fun poblarBaseDeDatos() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val juegoFiestaDao = database.juegoFiestaDao
                val juegosExistentes = juegoFiestaDao.obtenerTodos()
                
                if (juegosExistentes.isEmpty()) {
                    Log.d(TAG, "Poblando base de datos con juegos de fiesta iniciales")
                    juegoFiestaDao.insertarTodos(JuegosFiestaData.juegosFiestaOffline)
                    Log.d(TAG, "Base de datos poblada exitosamente con ${JuegosFiestaData.juegosFiestaOffline.size} juegos")
                } else {
                    Log.d(TAG, "Base de datos ya contiene ${juegosExistentes.size} juegos, no es necesario poblar")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al poblar la base de datos: ${e.message}")
            }
        }
    }
} 