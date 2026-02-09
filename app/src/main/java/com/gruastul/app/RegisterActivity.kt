package com.gruastul.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.gruastul.app.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        setupUI()
    }

    private fun setupUI() {
        binding.btnRegistrar.setOnClickListener {
            val nombre = binding.etNombre.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            if (validarCampos(nombre, email, password, confirmPassword)) {
                registrarUsuario(email, password)
            }
        }

        binding.tvYaTienesCuenta.setOnClickListener {
            finish()
        }
    }

    private fun validarCampos(
        nombre: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        if (nombre.isEmpty()) {
            binding.etNombre.error = "Ingresa tu nombre"
            return false
        }
        if (email.isEmpty()) {
            binding.etEmail.error = "Ingresa tu email"
            return false
        }
        if (password.isEmpty()) {
            binding.etPassword.error = "Ingresa una contraseña"
            return false
        }
        if (password.length < 6) {
            binding.etPassword.error = "Mínimo 6 caracteres"
            return false
        }
        if (password != confirmPassword) {
            binding.etConfirmPassword.error = "Las contraseñas no coinciden"
            return false
        }
        return true
    }

    private fun registrarUsuario(email: String, password: String) {
        binding.btnRegistrar.isEnabled = false

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                binding.btnRegistrar.isEnabled = true

                if (task.isSuccessful) {
                    Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(
                        this,
                        "Error: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}