package com.gruastul.app.models

import com.google.firebase.Timestamp

data class Cotizacion(
    val id: String = "",
    val userId: String = "",
    val origen: Ubicacion = Ubicacion(),
    val destino: Ubicacion = Ubicacion(),
    val distanciaKm: Double = 0.0,
    val vehiculoTipo: String = "",
    val condicionServicio: String = "",
    val horario: String = "",
    val numCasetas: Int = 0,
    val costoCasetas: Double = 0.0,
    val costoBase: Double = 0.0,
    val costoDistancia: Double = 0.0,
    val recargos: Double = 0.0,
    val total: Double = 0.0,
    val fecha: Timestamp? = null
)