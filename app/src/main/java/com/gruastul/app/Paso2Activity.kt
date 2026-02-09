package com.gruastul.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.gruastul.app.databinding.ActivityPaso2Binding
import com.gruastul.app.models.VehiculoTipo
import com.gruastul.app.utils.FirebaseHelper

class Paso2Activity : AppCompatActivity() {

    private lateinit var binding: ActivityPaso2Binding
    private var vehiculoSeleccionado: VehiculoTipo? = null
    private lateinit var origen: String
    private lateinit var destino: String
    private var distanciaKm: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaso2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recibir datos del Paso 1
        origen = intent.getStringExtra("origen") ?: ""
        destino = intent.getStringExtra("destino") ?: ""
        distanciaKm = intent.getDoubleExtra("distanciaKm", 0.0)

        setupUI()
        cargarVehiculos()
    }

    private fun setupUI() {
        // Toolbar
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        // Info de ubicaciones
        binding.tvInfoUbicaciones.text = "De: $origen\nA: $destino\nDistancia aprox: ${distanciaKm.toInt()} km"

        // RecyclerView
        binding.rvVehiculos.layoutManager = LinearLayoutManager(this)

        // Ocultar mensaje de selección inicialmente
        binding.tvVehiculoSeleccionado.visibility = View.GONE

        // Botón Siguiente
        binding.btnSiguiente.setOnClickListener {
            if (vehiculoSeleccionado != null) {
                val intent = Intent(this, Paso3Activity::class.java)
                intent.putExtra("origen", origen)
                intent.putExtra("destino", destino)
                intent.putExtra("distanciaKm", distanciaKm)
                intent.putExtra("vehiculoId", vehiculoSeleccionado!!.id)
                intent.putExtra("vehiculoNombre", vehiculoSeleccionado!!.nombre)
                intent.putExtra("precioBase", vehiculoSeleccionado!!.precioBase)
                intent.putExtra("precioPorKm", vehiculoSeleccionado!!.precioPorKm)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Selecciona un tipo de vehículo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun cargarVehiculos() {
        FirebaseHelper.obtenerVehiculosTipos { vehiculos ->
            val adapter = VehiculoAdapter(vehiculos) { vehiculo ->
                vehiculoSeleccionado = vehiculo

                // Mostrar vehículo seleccionado
                binding.tvVehiculoSeleccionado.visibility = View.VISIBLE
                binding.tvVehiculoSeleccionado.text = "✓ Seleccionado: ${vehiculo.nombre}"
            }
            binding.rvVehiculos.adapter = adapter
        }
    }
}