package com.gruastul.app.utils

import com.google.firebase.firestore.FirebaseFirestore
import com.gruastul.app.models.CondicionServicio
import com.gruastul.app.models.Cotizacion
import com.gruastul.app.models.Horario
import com.gruastul.app.models.VehiculoTipo

object FirebaseHelper {

    private val db = FirebaseFirestore.getInstance()

    fun inicializarDatos(onComplete: (Boolean) -> Unit) {
        var completados = 0
        val total = 3

        fun verificarCompletado() {
            completados++
            if (completados == total) onComplete(true)
        }

        val vehiculos = listOf(
            hashMapOf("nombre" to "Auto Compacto", "precioBase" to 350.0, "precioPorKm" to 15.0, "categoria" to "A"),
            hashMapOf("nombre" to "Sedán",          "precioBase" to 400.0, "precioPorKm" to 15.0, "categoria" to "B"),
            hashMapOf("nombre" to "SUV/Camioneta",  "precioBase" to 500.0, "precioPorKm" to 20.0, "categoria" to "C"),
            hashMapOf("nombre" to "Van",            "precioBase" to 550.0, "precioPorKm" to 20.0, "categoria" to "C"),
            hashMapOf("nombre" to "Camión",         "precioBase" to 700.0, "precioPorKm" to 25.0, "categoria" to "D"),
            hashMapOf("nombre" to "Moto",           "precioBase" to 250.0, "precioPorKm" to 12.0, "categoria" to "A")
        )
        vehiculos.forEach { db.collection("vehiculos_tipos").add(it) }
        verificarCompletado()

        val condiciones = listOf(
            hashMapOf("nombre" to "Normal",              "recargoPorcentaje" to 0.0),
            hashMapOf("nombre" to "Accidentado/Dañado",  "recargoPorcentaje" to 90.0),
            hashMapOf("nombre" to "Volcado",             "recargoPorcentaje" to 60.0),
            hashMapOf("nombre" to "Sin llantas",         "recargoPorcentaje" to 50.0)
        )
        condiciones.forEach { db.collection("condiciones_servicio").add(it) }
        verificarCompletado()

        val horarios = listOf(
            hashMapOf("tipo" to "Normal",       "recargoPorcentaje" to 0.0),
            hashMapOf("tipo" to "Nocturno",     "recargoPorcentaje" to 30.0),
            hashMapOf("tipo" to "Día festivo",  "recargoPorcentaje" to 50.0)
        )
        horarios.forEach { db.collection("horarios").add(it) }
        verificarCompletado()
    }

    fun obtenerVehiculosTipos(callback: (List<VehiculoTipo>) -> Unit) {
        db.collection("vehiculos_tipos").get()
            .addOnSuccessListener { result ->
                val lista = result.map { doc ->
                    VehiculoTipo(
                        id           = doc.id,
                        nombre       = doc.getString("nombre")       ?: "",
                        precioBase   = doc.getDouble("precioBase")   ?: 0.0,
                        precioPorKm  = doc.getDouble("precioPorKm")  ?: 0.0,
                        categoria    = doc.getString("categoria")    ?: ""
                    )
                }
                callback(lista)
            }
            .addOnFailureListener { callback(emptyList()) }
    }

    fun obtenerCondicionesServicio(callback: (List<CondicionServicio>) -> Unit) {
        db.collection("condiciones_servicio").get()
            .addOnSuccessListener { result ->
                val lista = result.map { doc ->
                    CondicionServicio(
                        id                 = doc.id,
                        nombre             = doc.getString("nombre")             ?: "",
                        recargoPorcentaje  = doc.getDouble("recargoPorcentaje")  ?: 0.0
                    )
                }
                callback(lista)
            }
            .addOnFailureListener { callback(emptyList()) }
    }

    fun obtenerHorarios(callback: (List<Horario>) -> Unit) {
        db.collection("horarios").get()
            .addOnSuccessListener { result ->
                val lista = result.map { doc ->
                    Horario(
                        id                 = doc.id,
                        tipo               = doc.getString("tipo")               ?: "",
                        recargoPorcentaje  = doc.getDouble("recargoPorcentaje")  ?: 0.0
                    )
                }
                callback(lista)
            }
            .addOnFailureListener { callback(emptyList()) }
    }

    fun guardarCotizacion(cotizacion: Cotizacion, callback: (Boolean) -> Unit) {
        db.collection("cotizaciones")
            .add(cotizacion)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }
}