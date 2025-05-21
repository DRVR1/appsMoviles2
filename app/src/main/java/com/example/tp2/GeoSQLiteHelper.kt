package com.example.tp2

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues
import com.example.tp2.models.Ciudad
import com.example.tp2.models.Pais

private const val DATABASE_NAME = "GeoBase.db"
private const val DATABASE_VERSION = 2

class GeoSQLiteHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val createCountriesTable = """
            CREATE TABLE paises (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL
            );
        """.trimIndent()

        val createCitiesTable = """
            CREATE TABLE ciudades (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL,
                es_capital INTEGER NOT NULL,
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

    // ----------- PAIS ------------

    fun createCountry(nombre: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("nombre", nombre)
        }
        return db.insert("paises", null, values)
    }

    fun readAllCountries(): List<Pais> {
        val db = readableDatabase
        val result = mutableListOf<Pais>()
        val cursor = db.rawQuery("SELECT id, nombre FROM paises", null)
        with(cursor) {
            while (moveToNext()) {
                val id = getLong(getColumnIndexOrThrow("id"))
                val nombre = getString(getColumnIndexOrThrow("nombre"))
                result.add(Pais(id, nombre))
            }
        }
        cursor.close()
        return result
    }

    fun readCountriesByName(partialName: String): List<Pais> {
        val db = readableDatabase
        val result = mutableListOf<Pais>()
        val query = """
            SELECT id, nombre
            FROM paises
            WHERE nombre LIKE ? COLLATE NOCASE;
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf("%$partialName%"))
        with(cursor) {
            while (moveToNext()) {
                val id = getLong(getColumnIndexOrThrow("id"))
                val nombre = getString(getColumnIndexOrThrow("nombre"))
                result.add(Pais(id, nombre))
            }
        }
        cursor.close()
        return result
    }

    fun readCountryById(id: Long): Pais? {
        val db = readableDatabase
        var country: Pais? = null
        val query = """
            SELECT id, nombre
            FROM paises
            WHERE id = ?;
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(id.toString()))
        with(cursor) {
            if (moveToFirst()) {
                val nombre = getString(getColumnIndexOrThrow("nombre"))
                country = Pais(id, nombre)
            }
        }
        cursor.close()
        return country
    }

    fun deleteCountryById(id: Long): Int {
        val db = writableDatabase
        return db.delete("paises", "id = ?", arrayOf(id.toString()))
    }

    // ----------- CIUDAD ------------

    fun createCity(nombre: String, esCapital: Boolean, poblacion: Int, paisId: Long): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("nombre", nombre)
            put("es_capital", if (esCapital) 1 else 0)
            put("poblacion", poblacion)
            put("pais_id", paisId)
        }
        return db.insert("ciudades", null, values)
    }

    fun readCitiesByName(partialName: String): List<Ciudad> {
        val db = readableDatabase
        val result = mutableListOf<Ciudad>()
        val query = """
            SELECT id, nombre, es_capital, poblacion, pais_id
            FROM ciudades
            WHERE nombre LIKE ? COLLATE NOCASE;
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf("%$partialName%"))
        with(cursor) {
            while (moveToNext()) {
                val id = getLong(getColumnIndexOrThrow("id"))
                val nombre = getString(getColumnIndexOrThrow("nombre"))
                val esCapital = getInt(getColumnIndexOrThrow("es_capital")) == 1
                val poblacion = getInt(getColumnIndexOrThrow("poblacion"))
                val paisId = getLong(getColumnIndexOrThrow("pais_id"))
                result.add(Ciudad(id, nombre, esCapital, poblacion, paisId))
            }
        }
        cursor.close()
        return result
    }

    fun readCitiesByCountryId(paisId: Long): List<Ciudad> {
        val db = readableDatabase
        val result = mutableListOf<Ciudad>()
        val query = """
            SELECT id, nombre, es_capital, poblacion, pais_id
            FROM ciudades
            WHERE pais_id = ?;
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(paisId.toString()))
        with(cursor) {
            while (moveToNext()) {
                val id = getLong(getColumnIndexOrThrow("id"))
                val nombre = getString(getColumnIndexOrThrow("nombre"))
                val esCapital = getInt(getColumnIndexOrThrow("es_capital")) == 1
                val poblacion = getInt(getColumnIndexOrThrow("poblacion"))
                val paisIdResult = getLong(getColumnIndexOrThrow("pais_id"))
                result.add(Ciudad(id, nombre, esCapital, poblacion, paisIdResult))
            }
        }
        cursor.close()
        return result
    }

    fun readCityById(id: Long): Ciudad? {
        val db = readableDatabase
        var ciudad: Ciudad? = null
        val query = """
            SELECT id, nombre, es_capital, poblacion, pais_id
            FROM ciudades
            WHERE id = ?;
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(id.toString()))
        with(cursor) {
            if (moveToFirst()) {
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

    fun updateCityPopulation(id: Long, nuevaPoblacion: Int): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("poblacion", nuevaPoblacion)
        }
        return db.update("ciudades", values, "id = ?", arrayOf(id.toString()))
    }

    fun deleteCityById(id: Long): Int {
        val db = writableDatabase
        return db.delete("ciudades", "id = ?", arrayOf(id.toString()))
    }


    fun readCapitalCities(): List<Ciudad> {
        val db = readableDatabase
        val result = mutableListOf<Ciudad>()
        val query = """
        SELECT id, nombre, es_capital, poblacion, pais_id
        FROM ciudades
        WHERE es_capital = 1;
    """.trimIndent()

        val cursor = db.rawQuery(query, null)
        with(cursor) {
            while (moveToNext()) {
                val id = getLong(getColumnIndexOrThrow("id"))
                val nombre = getString(getColumnIndexOrThrow("nombre"))
                val esCapital = getInt(getColumnIndexOrThrow("es_capital")) == 1
                val poblacion = getInt(getColumnIndexOrThrow("poblacion"))
                val paisId = getLong(getColumnIndexOrThrow("pais_id"))
                result.add(Ciudad(id, nombre, esCapital, poblacion, paisId))
            }
        }
        cursor.close()
        return result
    }

    fun searchCapitalCitiesByName(name: String): List<Ciudad> {
        val db = readableDatabase
        val result = mutableListOf<Ciudad>()
        val query = """
        SELECT id, nombre, es_capital, poblacion, pais_id
        FROM ciudades
        WHERE es_capital = 1 AND nombre LIKE ? COLLATE NOCASE;
    """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf("%$name%"))
        with(cursor) {
            while (moveToNext()) {
                val id = getLong(getColumnIndexOrThrow("id"))
                val nombre = getString(getColumnIndexOrThrow("nombre"))
                val esCapital = getInt(getColumnIndexOrThrow("es_capital")) == 1
                val poblacion = getInt(getColumnIndexOrThrow("poblacion"))
                val paisId = getLong(getColumnIndexOrThrow("pais_id"))
                result.add(Ciudad(id, nombre, esCapital, poblacion, paisId))
            }
        }
        cursor.close()
        return result
    }


    fun readAllCities(): List<Ciudad> {
        val db = readableDatabase
        val result = mutableListOf<Ciudad>()
        val query = """
        SELECT id, nombre, es_capital, poblacion, pais_id
        FROM ciudades;
    """.trimIndent()

        val cursor = db.rawQuery(query, null)
        with(cursor) {
            while (moveToNext()) {
                val id = getLong(getColumnIndexOrThrow("id"))
                val nombre = getString(getColumnIndexOrThrow("nombre"))
                val esCapital = getInt(getColumnIndexOrThrow("es_capital")) == 1
                val poblacion = getInt(getColumnIndexOrThrow("poblacion"))
                val paisId = getLong(getColumnIndexOrThrow("pais_id"))
                result.add(Ciudad(id, nombre, esCapital, poblacion, paisId))
            }
        }
        cursor.close()
        return result
    }

    fun searchCitiesByName(name: String): List<Ciudad> {
        val db = readableDatabase
        val result = mutableListOf<Ciudad>()
        val query = """
        SELECT id, nombre, es_capital, poblacion, pais_id
        FROM ciudades
        WHERE nombre LIKE ? COLLATE NOCASE;
    """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf("%$name%"))
        with(cursor) {
            while (moveToNext()) {
                val id = getLong(getColumnIndexOrThrow("id"))
                val nombre = getString(getColumnIndexOrThrow("nombre"))
                val esCapital = getInt(getColumnIndexOrThrow("es_capital")) == 1
                val poblacion = getInt(getColumnIndexOrThrow("poblacion"))
                val paisId = getLong(getColumnIndexOrThrow("pais_id"))
                result.add(Ciudad(id, nombre, esCapital, poblacion, paisId))
            }
        }
        cursor.close()
        return result
    }
    fun deleteCitiesByCountryId(paisId: Long): Int {
        val db = writableDatabase
        return db.delete("ciudades", "pais_id = ?", arrayOf(paisId.toString()))
    }
}
