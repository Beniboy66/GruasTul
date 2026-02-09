package com.gruastul.app

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gruastul.app.databinding.ActivityPaso3Binding
import com.gruastul.app.models.CondicionServicio
import com.gruastul.app.models.Horario
import com.gruastul.app.utils.FirebaseHelper

class Paso3Activity : AppCompatActivity() {

    private lateinit var binding: ActivityPaso3Binding
    private var condicionSeleccionada: CondicionServicio? = null
    private var horarioSeleccionado: Horario? = null
    private val condiciones = mutableListOf<CondicionServicio>()
    private val horarios = mutableListOf<Horario>()

    // Datos recibidos de pasos anteriores
    private lateinit var origen: String
    private lateinit var destino: String
    private var distanciaKm: Double = 0.0
    private lateinit var vehiculoId: String
    private lateinit var vehiculoNombre: String
    private var precioBase: Double = 0.0
    private var precioPorKm: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaso3Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recibir datos de pasos anteriores
        origen = intent.getStringExtra("origen") ?: ""
        destino = intent.getStringExtra("destino") ?: ""
        distanciaKm = intent.getDoubleExtra("distanciaKm", 0.0)
        vehiculoId = intent.getStringExtra("vehiculoId") ?: ""
        vehiculoNombre = intent.getStringExtra("vehiculoNombre") ?: ""
        precioBase = intent.getDoubleExtra("precioBase", 0.0)
        precioPorKm = intent.getDoubleExtra("precioPorKm", 0.0)

        setupUI()
        cargarDatos()
    }

    private fun setupUI() {
        // Toolbar
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        // Info de pasos anteriores
        binding.tvInfoPasosAnteriores.text = """
            De: $origen
            A: $destino
            Vehículo: $vehiculoNombre
            Distancia: ${distanciaKm.toInt()} km
        """.trimIndent()

        // Botón Calcular
        binding.btnCalcular.setOnClickListener {
            calcularCotizacion()
        }
    }

    private fun cargarDatos() {
        // Cargar condiciones de servicio
        FirebaseHelper.obtenerCondicionesServicio { listaCondiciones ->
            condiciones.clear()
            condiciones.addAll(listaCondiciones)

            val nombresCondiciones = condiciones.map { it.nombre }
            val adapterCondiciones = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                nombresCondiciones
            )
            adapterCondiciones.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerCondicion.adapter = adapterCondiciones
        }

        // Cargar horarios
        FirebaseHelper.obtenerHorarios { listaHorarios ->
            horarios.clear()
            horarios.addAll(listaHorarios)

            val nombresHorarios = horarios.map { it.tipo }
            val adapterHorarios = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                nombresHorarios
            )
            adapterHorarios.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerHorario.adapter = adapterHorarios
        }
    }

    private fun calcularCotizacion() {
        // Obtener selecciones
        val posicionCondicion = binding.spinnerCondicion.selectedItemPosition
        val posicionHorario = binding.spinnerHorario.selectedItemPosition

        if (posicionCondicion < 0 || posicionHorario < 0) {
            Toast.makeText(this, "Selecciona todas las opciones", Toast.LENGTH_SHORT).show()
            return
        }

        condicionSeleccionada = condiciones[posicionCondicion]
        horarioSeleccionado = horarios[posicionHorario]

        // CÁLCULOS
        val costoBase = precioBase
        val costoDistancia = distanciaKm * precioPorKm

        // Recargos por condición y horario
        val recargoCondicion = (costoBase + costoDistancia) * (condicionSeleccionada!!.recargoPorcentaje / 100)
        val recargoHorario = (costoBase + costoDistancia) * (horarioSeleccionado!!.recargoPorcentaje / 100)
        val recargosTotal = recargoCondicion + recargoHorario

        // Casetas (simulado por ahora)
        val numCasetas = (1..5).random()
        val costoCasetas = numCasetas * (15..25).random().toDouble()

        // TOTAL
        val total = costoBase + costoDistancia + recargosTotal + costoCasetas

        // Ir a la pantalla de resultado
        val intent = Intent(this, ResultadoActivity::class.java)
        intent.putExtra("origen", origen)
        intent.putExtra("destino", destino)
        intent.putExtra("distanciaKm", distanciaKm)
        intent.putExtra("vehiculoNombre", vehiculoNombre)
        intent.putExtra("condicionNombre", condicionSeleccionada!!.nombre)
        intent.putExtra("horarioNombre", horarioSeleccionado!!.tipo)
        intent.putExtra("costoBase", costoBase)
        intent.putExtra("costoDistancia", costoDistancia)
        intent.putExtra("recargos", recargosTotal)
        intent.putExtra("numCasetas", numCasetas)
        intent.putExtra("costoCasetas", costoCasetas)
        intent.putExtra("total", total)
        startActivity(intent)
    }
}