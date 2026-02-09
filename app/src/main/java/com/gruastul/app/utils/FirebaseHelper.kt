package com.gruastul.app.utils

import com.google.firebase.firestore.FirebaseFirestore
import com.gruastul.app.models.*

object FirebaseHelper {

    private val db = FirebaseFirestore.getInstance()

    // Inicializar datos en Firestore (ejecutar solo UNA vez)
    fun inicializarDatos(onComplete: (Boolean) -> Unit) {
        var completados = 0
        val total = 3

        fun verificarCompletado() {
            completados++
            if (completados == total) {
                onComplete(true)
            }
        }

        // Vehículos
        val vehiculos = listOf(
            hashMapOf(
                "nombre" to "Auto Compacto",
                "precioBase" to 350.0,
                "precioPorKm" to 15.0,
                "categoria" to "A"
            ),
            hashMapOf(
                "nombre" to "Sedán",
                "precioBase" to 400.0,
                "precioPorKm" to 15.0,
                "categoria" to "B"
            ),
            hashMapOf(
                "nombre" to "SUV/Camioneta",
                "precioBase" to 500.0,
                "precioPorKm" to 20.0,
                "categoria" to "C"
            ),
            hashMapOf(
                "nombre" to "Van",
                "precioBase" to 550.0,
                "precioPorKm" to 20.0,
                "categoria" to "C"
            ),
            hashMapOf(
                "nombre" to "Camión",
                "precioBase" to 700.0,
                "precioPorKm" to 25.0,
                "categoria" to "D"
            ),
            hashMapOf(
                "nombre" to "Moto",
                "precioBase" to 250.0,
                "precioPorKm" to 12.0,
                "categoria" to "A"
            )
        )

        vehiculos.forEach { vehiculo ->
            db.collection("vehiculos_tipos").add(vehiculo)
        }
        verificarCompletado()

        // Condiciones
        val condiciones = listOf(
            hashMapOf("nombre" to "Normal", "recargoPorcentaje" to 0.0),
            hashMapOf("nombre" to "Accidentado/Dañado", "recargoPorcentaje" to 90.0),
            hashMapOf("nombre" to "Volcado", "recargoPorcentaje" to 60.0),
            hashMapOf("nombre" to "Sin llantas", "recargoPorcentaje" to 50.0)
        )

        condiciones.forEach { condicion ->
            db.collection("condiciones_servicio").add(condicion)
        }
        verificarCompletado()

        // Horarios
        val horarios = listOf(
            hashMapOf("tipo" to "Normal", "recargoPorcentaje" to 0.0),
            hashMapOf("tipo" to "Nocturno", "recargoPorcentaje" to 30.0),
            hashMapOf("tipo" to "Día festivo", "recargoPorcentaje" to 50.0)
        )

        horarios.forEach { horario ->
            db.collection("horarios").add(horario)
        }
        verificarCompletado()
    }

    // Obtener tipos de vehículos
    fun obtenerVehiculosTipos(callback: (List<VehiculoTipo>) -> Unit) {
        db.collection("vehiculos_tipos")
            .get()
            .addOnSuccessListener { result ->
                val vehiculos = result.map { document ->
                    VehiculoTipo(
                        id = document.id,
                        nombre = document.getString("nombre") ?: "",
                        precioBase = document.getDouble("precioBase") ?: 0.0,
                        precioPorKm = document.getDouble("precioPorKm") ?: 0.0,
                        categoria = document.getString("categoria") ?: ""
                    )
                }
                callback(vehiculos)
            }
    }

    // Obtener condiciones de servicio
    fun obtenerCondicionesServicio(callback: (List<CondicionServicio>) -> Unit) {
        db.collection("condiciones_servicio")
            .get()
            .addOnSuccessListener { result ->
                val condiciones = result.map { document ->
                    CondicionServicio(
                        id = document.id,
                        nombre = document.getString("nombre") ?: "",
                        recargoPorcentaje = document.getDouble("recargoPorcentaje") ?: 0.0
                    )
                }
                callback(condiciones)
            }
    }

    // Obtener horarios
    fun obtenerHorarios(callback: (List<Horario>) -> Unit) {
        db.collection("horarios")
            .get()
            .addOnSuccessListener { result ->
                val horarios = result.map { document ->
                    Horario(
                        id = document.id,
                        tipo = document.getString("tipo") ?: "",
                        recargoPorcentaje = document.getDouble("recargoPorcentaje") ?: 0.0
                    )
                }
                callback(horarios)
            }
    }

    // Guardar cotización
    fun guardarCotizacion(cotizacion: Cotizacion, callback: (Boolean) -> Unit) {
        db.collection("cotizaciones")
            .add(cotizacion)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
    }
}