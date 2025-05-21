package com.example.tp2.models

data class Ciudad(
    val id: Long,
    val nombre: String,
    val es_capital: Boolean,
    val poblacion: Int,
    val pais_id: Long
)
