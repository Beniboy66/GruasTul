package com.gruastul.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gruastul.app.databinding.ActivityPaso1Binding

class Paso1Activity : AppCompatActivity() {

    private lateinit var binding: ActivityPaso1Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaso1Binding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
    }

    private fun setupUI() {
        // Toolbar
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        // Botón Siguiente
        binding.btnSiguiente.setOnClickListener {
            val origen = binding.etOrigen.text.toString().trim()
            val destino = binding.etDestino.text.toString().trim()

            if (validarCampos(origen, destino)) {
                // Calcular distancia simulada (temporal sin APIs)
                val distanciaKm = calcularDistanciaSimulada(origen, destino)

                // Ir al Paso 2
                val intent = Intent(this, Paso2Activity::class.java)
                intent.putExtra("origen", origen)
                intent.putExtra("destino", destino)
                intent.putExtra("distanciaKm", distanciaKm)
                startActivity(intent)
            }
        }
    }

    private fun validarCampos(origen: String, destino: String): Boolean {
        if (origen.isEmpty()) {
            binding.etOrigen.error = "Ingresa el punto de origen"
            return false
        }
        if (destino.isEmpty()) {
            binding.etDestino.error = "Ingresa el punto de destino"
            return false
        }
        if (origen.equals(destino, ignoreCase = true)) {
            Toast.makeText(this, "Origen y destino no pueden ser iguales", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    // Simulación temporal de distancia (sin APIs)
    private fun calcularDistanciaSimulada(origen: String, destino: String): Double {
        // Por ahora retornamos una distancia aleatoria entre 10 y 100 km
        return (10..100).random().toDouble()
    }
}