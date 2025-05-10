package com.example.tp2

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues
import com.example.tp2.models.Ciudad

private const val DATABASE_NAME = "GeoBase.db"
private const val DATABASE_VERSION = 2

class GeoSQLiteHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        // Crear tabla paises
        val createCountriesTable = """
            CREATE TABLE paises (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL
            );
        """.trimIndent()

        // Crear tabla ciudades (con referencia a pais_id)
        val createCitiesTable = """
            CREATE TABLE ciudades (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL,
                es_capital INTEGER NOT NULL, -- 0 = false, 1 = true
                poblacion INTEGER NOT NULL,
                pais_id INTEGER NOT NULL,
                FOREIGN KEY (pais_id) REFERENCES paises(id)
            );
        """.trimIndent()

        db.execSQL(createCountriesTable)
        db.execSQL(createCitiesTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS ciudades")
        db.execSQL("DROP TABLE IF EXISTS paises")
        onCreate(db)
    }

    // Insertar país (solo una vez)
    fun insertarPais(nombre: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("nombre", nombre)
        }
        return db.insert("paises", null, values)
    }


    // Insertar ciudad asociada a un país
    fun insertarCiudad(nombre: String, esCapital: Boolean, poblacion : Int, paisId: Long): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("nombre", nombre)
            put("es_capital", if (esCapital) 1 else 0)
            put("poblacion", poblacion)
            put("pais_id", paisId)
        }
        return db.insert("ciudades", null, values)
    }

    // Devuelve una lista de ciudades buscando por nombre
    fun buscarCiudadesPorNombre(nombreParcial: String): List<Ciudad> {
        val db = readableDatabase
        val resultado = mutableListOf<Ciudad>()
        val query = """
        SELECT id, nombre, es_capital, poblacion, pais_id
        FROM ciudades
        WHERE nombre LIKE ? COLLATE NOCASE;
    """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf("%$nombreParcial%"))
        with(cursor) {
            while (moveToNext()) {
                val id = getLong(getColumnIndexOrThrow("id"))
                val nombre = getString(getColumnIndexOrThrow("nombre"))
                val esCapital = getInt(getColumnIndexOrThrow("es_capital")) == 1
                val poblacion = getInt(getColumnIndexOrThrow("poblacion"))
                val paisId = getLong(getColumnIndexOrThrow("pais_id"))
                resultado.add(Ciudad(id, nombre, esCapital, poblacion, paisId))
            }
        }
        cursor.close()
        return resultado
    }

    // Buscar una ciudad por su ID
    fun buscarCiudadPorId(ciudadId: Long): Ciudad? {
        val db = readableDatabase
        var ciudad: Ciudad? = null
        val query = """
        SELECT id, nombre, es_capital, poblacion, pais_id
        FROM ciudades
        WHERE id = ?;
    """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(ciudadId.toString()))
        with(cursor) {
            if (moveToFirst()) {
                val id = getLong(getColumnIndexOrThrow("id"))
                val nombre = getString(getColumnIndexOrThrow("nombre"))
                val esCapital = getInt(getColumnIndexOrThrow("es_capital")) == 1
                val poblacion = getInt(getColumnIndexOrThrow("poblacion"))
                val paisId = getLong(getColumnIndexOrThrow("pais_id"))
                ciudad = Ciudad(id, nombre, esCapital, poblacion, paisId)
            }
        }
        cursor.close()
        return ciudad
    }


    fun modificarPoblacionCiudad(ciudadId: Long, nuevaPoblacion: Int): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("poblacion", nuevaPoblacion)
        }
        // El método update devuelve el número de filas afectadas
        return db.update(
            "ciudades",
            values,
            "id = ?",
            arrayOf(ciudadId.toString())
        )
    }

    fun eliminarCiudadPorId(ciudadId: Long): Int {
        val db = writableDatabase
        // El método delete devuelve el número de filas eliminadas
        return db.delete(
            "ciudades",
            "id = ?",
            arrayOf(ciudadId.toString())
        )
    }


    // Obtener todas las ciudades de un país
    fun obtenerCiudadesDePais(paisId: Long): List<String> {
        val db = readableDatabase
        val resultado = mutableListOf<String>()
        val query = """
            SELECT c.nombre AS ciudad, c.es_capital
            FROM ciudades c
            WHERE c.pais_id = ?;
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(paisId.toString()))
        with(cursor) {
            while (moveToNext()) {
                val ciudad = getString(getColumnIndexOrThrow("ciudad"))
                val esCapital = getInt(getColumnIndexOrThrow("es_capital")) == 1
                resultado.add("Ciudad: $ciudad, ¿Es capital?: $esCapital")
            }
        }
        cursor.close()
        return resultado
    }

    // Obtener todos los países
    fun obtenerTodosLosPaises(): List<Pair<Long, String>> {
        val db = readableDatabase
        val resultado = mutableListOf<Pair<Long, String>>()
        val cursor = db.rawQuery("SELECT id, nombre FROM paises", null)
        with(cursor) {
            while (moveToNext()) {
                val id = getLong(getColumnIndexOrThrow("id"))
                val nombre = getString(getColumnIndexOrThrow("nombre"))
                resultado.add(Pair(id, nombre))
            }
        }
        cursor.close()
        return resultado
    }
}
