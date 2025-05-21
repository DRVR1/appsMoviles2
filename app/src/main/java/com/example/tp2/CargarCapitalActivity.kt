
package com.example.tp2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CargarCapitalActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cargar_capital)

        val etPais = findViewById<EditText>(R.id.etPais)
        val etCiudad = findViewById<EditText>(R.id.etCiudad)
        val etHabitantes = findViewById<EditText>(R.id.etHabitantes)
        val btnGuardar = findViewById<Button>(R.id.btnGuardarCiudad)
        val checkEsCapital = findViewById<CheckBox>(R.id.checkEsCapital)


        val dbHelper = GeoSQLiteHelper(this)

        btnGuardar.setOnClickListener {
            val nombrePais = etPais.text.toString().trim()
            val nombreCiudad = etCiudad.text.toString().trim()
            val habitantes = etHabitantes.text.toString().toIntOrNull()
            val esCapital = checkEsCapital.isChecked

            if (nombrePais.isEmpty() || nombreCiudad.isEmpty() || habitantes == null) {
                Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Buscar si ya existe el país
            val paisExistente = dbHelper.readCountriesByName(nombrePais).firstOrNull()

            // Si el país existe, verificar si ya tiene esa ciudad
            if (paisExistente != null) {
                val ciudadExistente = dbHelper.readCitiesByCountryId(paisExistente.id)
                    .any { it.nombre.equals(nombreCiudad, ignoreCase = true) }

                if (ciudadExistente) {
                    Toast.makeText(this, "Ya existe esa ciudad en ese país", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            // Guardar los datos
            val paisId = paisExistente?.id ?: dbHelper.createCountry(nombrePais)
            dbHelper.createCity(nombreCiudad, esCapital, habitantes, paisId)

            Toast.makeText(this,
                "Ciudad ${if (esCapital) "capital" else ""} guardada exitosamente",
                Toast.LENGTH_SHORT).show()

            // Limpiar campos y redirigir
            etPais.text.clear()
            etCiudad.text.clear()
            etHabitantes.text.clear()

            // Redirección a MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish() // Cierra esta actividad para no volver atrás
        }
        findViewById<ImageButton>(R.id.btnBackCargar).setOnClickListener {
            finish() // Cierra esta actividad y regresa a MainActivity
        }
    }
}