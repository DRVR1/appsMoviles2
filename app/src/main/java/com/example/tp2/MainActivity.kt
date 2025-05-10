// Cargar una ciudad asociada a un pais, y puede ser capital y tiene poblacion.
// Consultar ciudad por su nombre, ver sus datos (poblacion, escapital, pais)
// poder modificar su poblacion y poder eliminarla

// buscar un pais
// borrar todas sus ciudades

package com.example.tp2

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.util.Log
import com.example.tp2.models.Ciudad

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val dbHelper = GeoSQLiteHelper(this)

        // Insertar un país
        val paisId = dbHelper.insertarPais("Argentina")

        // Insertar varias ciudades para ese país
        dbHelper.insertarCiudad("Buenos Aires", true, 12299, paisId)
        dbHelper.insertarCiudad("Córdoba", false, 4444, paisId)
        dbHelper.insertarCiudad("Rosario", false, 12300,  paisId)

        // Buscar ciudades por su nombre
        val busquedaCiudades = dbHelper.buscarCiudadesPorNombre("buenos")
        for (ciudad in busquedaCiudades){
            Log.d("consola: ",ciudad.nombre)
        }
        // Modificar la poblacion de una ciudad
        // Simulando la seleccion de una ciudad especifica con la GUI
        val ciudadBuscada = busquedaCiudades[0] // Selecciono la primera ciudad que me aparece en la busqueda
        val nuevaPoblacion = 90 // Seteo una nueva poblacion
        Log.d("consola", "Seleccionando: ${ciudadBuscada.nombre} con id ${ciudadBuscada.id} y poblacion ${ciudadBuscada.poblacion}. Cambiando su poblacion a ${nuevaPoblacion}")
        dbHelper.modificarPoblacionCiudad(ciudadBuscada.id,nuevaPoblacion) // Aplico los cambios
        // Comprobando cambios
        var ciudadModificada = dbHelper.buscarCiudadPorId(ciudadBuscada.id)
        Log.d("consola", "Nueva poblacion para ${ciudadModificada?.nombre}: ${ciudadModificada?.poblacion}")

        // TODO: Borrar una ciudad ingresando su nombre (mejor hacerlo por ID por si hay ciudades que se llaman igual)

        // TODO: Borrar todas las ciudades de un pais


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}