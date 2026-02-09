package com.gruastul.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.gruastul.app.databinding.ActivityMainBinding
import com.gruastul.app.utils.FirebaseHelper

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Verificar si está logueado
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setupUI()


    }

    private fun setupUI() {
        binding.btnCerrarSesion.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.btnNuevaCotizacion.setOnClickListener {
            // Ir al Paso 1
            startActivity(Intent(this, Paso1Activity::class.java))
        }
    }

    private fun inicializarDatosFirestore() {
        FirebaseHelper.inicializarDatos { exitoso ->
            if (exitoso) {
                Toast.makeText(
                    this,
                    "Datos inicializados correctamente",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}