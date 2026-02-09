package com.gruastul.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.gruastul.app.databinding.ActivityResultadoBinding
import com.gruastul.app.models.Cotizacion
import com.gruastul.app.models.Ubicacion
import com.gruastul.app.utils.FirebaseHelper

class ResultadoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultadoBinding
    private lateinit var auth: FirebaseAuth

    // Datos recibidos
    private lateinit var origen: String
    private lateinit var destino: String
    private var distanciaKm: Double = 0.0
    private lateinit var vehiculoNombre: String
    private lateinit var condicionNombre: String
    private lateinit var horarioNombre: String
    private var costoBase: Double = 0.0
    private var costoDistancia: Double = 0.0
    private var recargos: Double = 0.0
    private var numCasetas: Int = 0
    private var costoCasetas: Double = 0.0
    private var total: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultadoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Recibir datos
        origen = intent.getStringExtra("origen") ?: ""
        destino = intent.getStringExtra("destino") ?: ""
        distanciaKm = intent.getDoubleExtra("distanciaKm", 0.0)
        vehiculoNombre = intent.getStringExtra("vehiculoNombre") ?: ""
        condicionNombre = intent.getStringExtra("condicionNombre") ?: ""
        horarioNombre = intent.getStringExtra("horarioNombre") ?: ""
        costoBase = intent.getDoubleExtra("costoBase", 0.0)
        costoDistancia = intent.getDoubleExtra("costoDistancia", 0.0)
        recargos = intent.getDoubleExtra("recargos", 0.0)
        numCasetas = intent.getIntExtra("numCasetas", 0)
        costoCasetas = intent.getDoubleExtra("costoCasetas", 0.0)
        total = intent.getDoubleExtra("total", 0.0)

        setupUI()
        mostrarResultados()
    }

    private fun setupUI() {
        // Toolbar
        binding.toolbar.setNavigationOnClickListener {
            // Volver al inicio
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        // Botón Guardar
        binding.btnGuardar.setOnClickListener {
            guardarCotizacion()
        }

        // Botón Nueva Cotización
        binding.btnNuevaCotizacion.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun mostrarResultados() {
        // Ubicaciones
        binding.tvOrigenDestino.text = "De: $origen\nA: $destino"

        // Detalles
        binding.tvVehiculo.text = "Vehículo: $vehiculoNombre"
        binding.tvDistancia.text = "Distancia: ${distanciaKm.toInt()} km"
        binding.tvCondicion.text = "Condición: $condicionNombre"
        binding.tvHorario.text = "Horario: $horarioNombre"

        // Desglose de costos
        binding.tvCostoBase.text = "Costo base: $${String.format("%.2f", costoBase)}"
        binding.tvCostoDistancia.text = "Costo por distancia: $${String.format("%.2f", costoDistancia)}"
        binding.tvRecargos.text = "Recargos: $${String.format("%.2f", recargos)}"
        binding.tvCasetas.text = "Casetas ($numCasetas): $${String.format("%.2f", costoCasetas)}"

        // Total
        binding.tvTotal.text = "$${String.format("%.2f", total)}"
    }

    private fun guardarCotizacion() {
        val userId = auth.currentUser?.uid ?: ""

        val cotizacion = Cotizacion(
            userId = userId,
            origen = Ubicacion(direccion = origen),
            destino = Ubicacion(direccion = destino),
            distanciaKm = distanciaKm,
            vehiculoTipo = vehiculoNombre,
            condicionServicio = condicionNombre,
            horario = horarioNombre,
            numCasetas = numCasetas,
            costoCasetas = costoCasetas,
            costoBase = costoBase,
            costoDistancia = costoDistancia,
            recargos = recargos,
            total = total,
            fecha = Timestamp.now()
        )

        binding.btnGuardar.isEnabled = false
        binding.btnGuardar.text = "GUARDANDO..."

        FirebaseHelper.guardarCotizacion(cotizacion) { exitoso ->
            binding.btnGuardar.isEnabled = true
            binding.btnGuardar.text = "GUARDAR COTIZACIÓN"

            if (exitoso) {
                Toast.makeText(this, "✓ Cotización guardada correctamente", Toast.LENGTH_LONG).show()

                // Deshabilitar botón después de guardar
                binding.btnGuardar.isEnabled = false
                binding.btnGuardar.text = "✓ GUARDADO"
            } else {
                Toast.makeText(this, "Error al guardar la cotización", Toast.LENGTH_SHORT).show()
            }
        }
    }
}