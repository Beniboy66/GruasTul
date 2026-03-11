package com.gruastul.app

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.gruastul.app.databinding.ActivityPaso3Binding
import com.gruastul.app.models.CondicionServicio
import com.gruastul.app.models.Horario
import com.gruastul.app.utils.FirebaseHelper
import com.gruastul.app.utils.TollGuruHelper
import kotlinx.coroutines.launch

class Paso3Activity : AppCompatActivity() {

    private lateinit var binding: ActivityPaso3Binding
    private val condiciones = mutableListOf<CondicionServicio>()
    private val horarios = mutableListOf<Horario>()

    private lateinit var origen: String
    private lateinit var destino: String
    private var distanciaKm: Double = 0.0
    private lateinit var vehiculoId: String
    private lateinit var vehiculoNombre: String
    private var precioBase: Double = 0.0
    private var precioPorKm: Double = 0.0
    private var rutaCoordenadas: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaso3Binding.inflate(layoutInflater)
        setContentView(binding.root)

        origen           = intent.getStringExtra("origen") ?: ""
        destino          = intent.getStringExtra("destino") ?: ""
        distanciaKm      = intent.getDoubleExtra("distanciaKm", 0.0)
        vehiculoId       = intent.getStringExtra("vehiculoId") ?: ""
        vehiculoNombre   = intent.getStringExtra("vehiculoNombre") ?: ""
        precioBase       = intent.getDoubleExtra("precioBase", 0.0)
        precioPorKm      = intent.getDoubleExtra("precioPorKm", 0.0)
        rutaCoordenadas  = intent.getStringExtra("rutaCoordenadas") ?: ""

        setupUI()
        cargarDatos()
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.tvInfoPasosAnteriores.text = """
            De: $origen
            A: $destino
            Vehículo: $vehiculoNombre
            Distancia: ${"%.1f".format(distanciaKm)} km
        """.trimIndent()

        binding.btnCalcular.setOnClickListener { calcularCotizacion() }
    }

    private fun cargarDatos() {
        FirebaseHelper.obtenerCondicionesServicio { lista ->
            condiciones.clear()
            condiciones.addAll(lista)
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, lista.map { it.nombre })
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerCondicion.adapter = adapter
        }
        FirebaseHelper.obtenerHorarios { lista ->
            horarios.clear()
            horarios.addAll(lista)
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, lista.map { it.tipo })
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerHorario.adapter = adapter
        }
    }

    private fun calcularCotizacion() {
        if (condiciones.isEmpty() || horarios.isEmpty()) {
            Toast.makeText(this, "Espera a que carguen los datos", Toast.LENGTH_SHORT).show()
            return
        }

        val condicion = condiciones[binding.spinnerCondicion.selectedItemPosition]
        val horario   = horarios[binding.spinnerHorario.selectedItemPosition]

        binding.btnCalcular.isEnabled = false
        binding.btnCalcular.text = "CONSULTANDO CASETAS..."

        lifecycleScope.launch {
            // Parsear coordenadas del string
            val coordenadas = parsearCoordenadas(rutaCoordenadas)

            // Consultar TollGuru
            val casetas = if (coordenadas.isNotEmpty()) {
                TollGuruHelper.calcularCasetas(coordenadas)
            } else {
                TollGuruHelper.ResultadoCasetas(0, 0.0, emptyList())
            }

            // Cálculo final
            val costoBase       = precioBase
            val costoDistancia  = distanciaKm * precioPorKm
            val subtotal        = costoBase + costoDistancia
            val recargoCondicion = subtotal * (condicion.recargoPorcentaje / 100)
            val recargoHorario   = subtotal * (horario.recargoPorcentaje / 100)
            val recargosTotal    = recargoCondicion + recargoHorario
            val costoCasetas    = casetas.costoTotal
            val total           = subtotal + recargosTotal + costoCasetas

            val intent = Intent(this@Paso3Activity, ResultadoActivity::class.java).apply {
                putExtra("origen",          origen)
                putExtra("destino",         destino)
                putExtra("distanciaKm",     distanciaKm)
                putExtra("vehiculoNombre",  vehiculoNombre)
                putExtra("condicionNombre", condicion.nombre)
                putExtra("horarioNombre",   horario.tipo)
                putExtra("costoBase",       costoBase)
                putExtra("costoDistancia",  costoDistancia)
                putExtra("recargos",        recargosTotal)
                putExtra("numCasetas",      casetas.numCasetas)
                putExtra("costoCasetas",    costoCasetas)
                putExtra("total",           total)
            }
            startActivity(intent)

            binding.btnCalcular.isEnabled = true
            binding.btnCalcular.text = "CALCULAR COTIZACIÓN"
        }
    }

    private fun parsearCoordenadas(coordsString: String): List<Pair<Double, Double>> {
        if (coordsString.isBlank()) return emptyList()
        return try {
            coordsString.split("|").map { par ->
                val (lng, lat) = par.split(",")
                Pair(lng.toDouble(), lat.toDouble())
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}