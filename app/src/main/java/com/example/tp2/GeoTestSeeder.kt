package com.example.tp2

import android.util.Log
import com.example.tp2.models.Ciudad

class GeoTestSeeder(private val dbHelper: GeoSQLiteHelper) {

    fun runTest() {
        // Insertar un país
        val paisId = dbHelper.createCountry("Argentina")

        // Insertar varias ciudades para ese país
        dbHelper.createCity("Buenos Aires", true, 12299, paisId)
        dbHelper.createCity("Córdoba", false, 4444, paisId)
        dbHelper.createCity("Rosario", false, 12300, paisId)

        // Buscar ciudades por su nombre
        val busquedaCiudades = dbHelper.readCitiesByName("buenos")
        for (ciudad in busquedaCiudades) {
            Log.d("GeoTestSeeder", ciudad.nombre)
        }

        // Modificar la población de una ciudad
        if (busquedaCiudades.isNotEmpty()) {
            val ciudadBuscada = busquedaCiudades[0]
            val nuevaPoblacion = 90
            Log.d("GeoTestSeeder", "Seleccionando: ${ciudadBuscada.nombre} con id ${ciudadBuscada.id} y poblacion ${ciudadBuscada.poblacion}. Cambiando su poblacion a $nuevaPoblacion")
            dbHelper.updateCityPopulation(ciudadBuscada.id, nuevaPoblacion)

            // Comprobando cambios
            val ciudadModificada = dbHelper.readCityById(ciudadBuscada.id)
            Log.d("GeoTestSeeder", "Nueva poblacion para ${ciudadModificada?.nombre}: ${ciudadModificada?.poblacion}")
        }

        // Borrar una ciudad por nombre (por ID)
        val ciudadABorrar = dbHelper.readCitiesByName("buenos").firstOrNull()
        ciudadABorrar?.let {
            dbHelper.deleteCityById(it.id)
            Log.d("GeoTestSeeder", "Ciudad borrada: ${it.nombre}")
        }

        // Borrar todas las ciudades de un país
        val paisBorrarCiudades = dbHelper.readCountriesByName("arg").firstOrNull()
        paisBorrarCiudades?.let { pais ->
            val ciudades = dbHelper.readCitiesByCountryId(pais.id)
            Log.d("GeoTestSeeder", "El país a borrarle las ciudades es ${pais.nombre} y sus ciudades son: $ciudades")

            for (ciudad in ciudades) {
                dbHelper.deleteCityById(ciudad.id)
            }

            val ciudadesRestantes = dbHelper.readCitiesByCountryId(pais.id)
            Log.d("GeoTestSeeder", "[Luego del borrado] Cantidad de ciudades del país ${pais.nombre}: ${ciudadesRestantes.size}")
        }
    }
}
