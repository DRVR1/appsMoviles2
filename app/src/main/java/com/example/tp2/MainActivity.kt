package com.example.tp2

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnCargar = findViewById<Button>(R.id.btnCargarCiudad)
        val btnBuscar = findViewById<Button>(R.id.btnBuscarCiudad)

        btnCargar.setOnClickListener {
            try {
            startActivity(Intent(this, CargarCapitalActivity::class.java))
            } catch (e: Exception) {
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }

        }

        btnBuscar.setOnClickListener {
            startActivity(Intent(this, BuscarCapitalActivity::class.java))
        }
    }
}