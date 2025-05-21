package com.example.tp2

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.tp2.models.Ciudad
import com.example.tp2.models.Pais

class BuscarCapitalActivity : AppCompatActivity() {
    private lateinit var dbHelper: GeoSQLiteHelper
    private lateinit var radioPais: RadioButton
    private lateinit var radioCiudad: RadioButton
    private lateinit var editFiltro: EditText
    private lateinit var listaResultados: ListView
    private lateinit var botonVolver: ImageButton

    private var ciudades: List<Ciudad> = emptyList()
    private var paises: List<Pais> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buscar_capital)

        dbHelper = GeoSQLiteHelper(this)
        radioPais = findViewById(R.id.radioPais)
        radioCiudad = findViewById(R.id.radioCiudad)
        editFiltro = findViewById(R.id.editFiltro)
        listaResultados = findViewById(R.id.listaResultados)
        botonVolver = findViewById(R.id.imageButton)

        configurarListeners()
        mostrarResultadosPorPais("")
    }

    private fun configurarListeners() {
        radioPais.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                editFiltro.text.clear()
                mostrarResultadosPorPais("")
            }
        }

        radioCiudad.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                editFiltro.text.clear()
                mostrarResultadosPorCiudad("")
            }
        }

        editFiltro.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val texto = s.toString()
                if (radioPais.isChecked) {
                    mostrarResultadosPorPais(texto)
                } else if (radioCiudad.isChecked) {
                    mostrarResultadosPorCiudad(texto)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        botonVolver.setOnClickListener {
            finish()
        }
    }

    private fun mostrarResultadosPorPais(filtro: String) {
        val listaPaises = if (filtro.isEmpty()) {
            dbHelper.readAllCountries()
        } else {
            dbHelper.readCountriesByName(filtro)
        }

        listaResultados.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            listaPaises.map { it.nombre }
        )

        listaResultados.setOnItemClickListener { _, _, position, _ ->
            if (position in listaPaises.indices) {
                val paisSeleccionado = listaPaises[position]
                val ciudades = dbHelper.readCitiesByCountryId(paisSeleccionado.id)

                android.app.AlertDialog.Builder(this)
                    .setTitle("País: ${paisSeleccionado.nombre}")
                    .setMessage(
                        if (ciudades.isNotEmpty()) {
                            "¿Deseás eliminar TODAS las ${ciudades.size} ciudades de este país?"
                        } else {
                            "Este país no tiene ciudades para eliminar"
                        }
                    )
                    .setPositiveButton("Eliminar ciudades",
                        if (ciudades.isNotEmpty()) { _, _ ->
                            try {
                                val eliminadas = dbHelper.deleteCitiesByCountryId(paisSeleccionado.id)
                                Toast.makeText(this,
                                    "Se eliminaron $eliminadas ciudades",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } catch (e: Exception) {
                                Toast.makeText(this,
                                    "Error: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } finally {
                                mostrarResultadosPorPais(editFiltro.text.toString())
                            }
                        } else null
                    )
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }
    }

    private fun mostrarResultadosPorCiudad(filtro: String) {
        ciudades = if (filtro.isEmpty()) {
            dbHelper.readAllCities()
        } else {
            dbHelper.searchCitiesByName(filtro)
        }

        paises = dbHelper.readAllCountries()

        val resultados = ciudades.map { ciudad ->
            val pais = paises.find { it.id == ciudad.pais_id }
            val capitalInfo = if (ciudad.es_capital) "Capital" else "No capital"
            "${ciudad.nombre} (${pais?.nombre ?: "Desconocido"}) - $capitalInfo - Población: ${ciudad.poblacion}"
        }

        listaResultados.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            resultados
        )

        listaResultados.setOnItemClickListener { _, _, position, _ ->
            val ciudadSeleccionada = ciudades[position]
            val pais = paises.find { it.id == ciudadSeleccionada.pais_id }

            val builder = android.app.AlertDialog.Builder(this)
            builder.setTitle("Ciudad: ${ciudadSeleccionada.nombre}")
            builder.setMessage(
                "País: ${pais?.nombre ?: "Desconocido"}\n" +
                        "Población: ${ciudadSeleccionada.poblacion}\n" +
                        "¿Qué desea hacer?"
            )

            builder.setPositiveButton("Modificar población") { _, _ ->
                mostrarDialogoModificar(ciudadSeleccionada)
            }

            builder.setNeutralButton("Eliminar") { _, _ ->
                confirmarEliminacion(ciudadSeleccionada)
            }

            builder.setNegativeButton("Cancelar", null)
            builder.show()
        }
    }

    private fun mostrarDialogoModificar(ciudad: Ciudad) {
        val input = EditText(this)
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        input.setText(ciudad.poblacion.toString())

        android.app.AlertDialog.Builder(this)
            .setTitle("Modificar población de ${ciudad.nombre}")
            .setView(input)
            .setPositiveButton("Guardar") { _, _ ->
                val nuevaPoblacion = input.text.toString().toIntOrNull()
                if (nuevaPoblacion != null) {
                    dbHelper.updateCityPopulation(ciudad.id, nuevaPoblacion)
                    Toast.makeText(this, "Población actualizada", Toast.LENGTH_SHORT).show()
                    mostrarResultadosPorCiudad(editFiltro.text.toString())
                } else {
                    Toast.makeText(this, "Número inválido", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun confirmarEliminacion(ciudad: Ciudad) {
        android.app.AlertDialog.Builder(this)
            .setTitle("Eliminar ${ciudad.nombre}")
            .setMessage("¿Estás seguro de que querés eliminar esta ciudad?")
            .setPositiveButton("Sí") { _, _ ->
                dbHelper.deleteCityById(ciudad.id)
                Toast.makeText(this, "Ciudad eliminada", Toast.LENGTH_SHORT).show()
                mostrarResultadosPorCiudad(editFiltro.text.toString())
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
