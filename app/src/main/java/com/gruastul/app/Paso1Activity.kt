package com.gruastul.app

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.gruastul.app.databinding.ActivityPaso1Binding
import com.gruastul.app.utils.MapboxHelper
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import kotlinx.coroutines.launch

class Paso1Activity : AppCompatActivity() {

    private lateinit var binding: ActivityPaso1Binding

    private var puntoOrigen: Point? = null
    private var puntoDestino: Point? = null
    private var direccionOrigen: String = ""
    private var direccionDestino: String = ""

    private val token by lazy { getString(R.string.mapbox_access_token) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaso1Binding.inflate(layoutInflater)
        setContentView(binding.root)
        setupMapa()
        setupUI()
    }

    private fun setupMapa() {
        binding.mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) {
            // Centrar en Tulancingo al iniciar
            binding.mapView.getMapboxMap().setCamera(
                CameraOptions.Builder()
                    .center(Point.fromLngLat(-98.3630, 20.0853))
                    .zoom(12.0)
                    .build()
            )
        }
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener { finish() }

        // Buscar origen al presionar Enter en el teclado
        binding.etOrigen.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                buscarLugar(binding.etOrigen.text.toString(), esOrigen = true)
                true
            } else false
        }

        // Buscar destino al presionar Enter en el teclado
        binding.etDestino.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                buscarLugar(binding.etDestino.text.toString(), esOrigen = false)
                true
            } else false
        }

        // Botones de búsqueda
        binding.btnBuscarOrigen.setOnClickListener {
            buscarLugar(binding.etOrigen.text.toString(), esOrigen = true)
        }
        binding.btnBuscarDestino.setOnClickListener {
            buscarLugar(binding.etDestino.text.toString(), esOrigen = false)
        }

        // Botón siguiente
        binding.btnSiguiente.setOnClickListener {
            when {
                puntoOrigen == null -> Toast.makeText(this, "Busca el punto de origen primero", Toast.LENGTH_SHORT).show()
                puntoDestino == null -> Toast.makeText(this, "Busca el punto de destino primero", Toast.LENGTH_SHORT).show()
                else -> calcularRuta()
            }
        }
    }

    private fun buscarLugar(query: String, esOrigen: Boolean) {
        if (query.isBlank()) {
            Toast.makeText(this, "Escribe una dirección", Toast.LENGTH_SHORT).show()
            return
        }

        val btnBuscar = if (esOrigen) binding.btnBuscarOrigen else binding.btnBuscarDestino
        btnBuscar.isEnabled = false

        lifecycleScope.launch {
            val resultado = MapboxHelper.geocodificar(query, token)

            btnBuscar.isEnabled = true

            if (resultado == null) {
                Toast.makeText(this@Paso1Activity, "No se encontró la dirección", Toast.LENGTH_SHORT).show()
                return@launch
            }

            if (esOrigen) {
                puntoOrigen = resultado.punto
                direccionOrigen = resultado.nombre
                binding.etOrigen.setText(resultado.nombre)
            } else {
                puntoDestino = resultado.punto
                direccionDestino = resultado.nombre
                binding.etDestino.setText(resultado.nombre)
            }

            actualizarMarcadores()
        }
    }

    private fun actualizarMarcadores() {
        val annotationManager = binding.mapView.annotations.createPointAnnotationManager()
        annotationManager.deleteAll()

        puntoOrigen?.let {
            annotationManager.create(PointAnnotationOptions().withPoint(it))
        }
        puntoDestino?.let {
            annotationManager.create(PointAnnotationOptions().withPoint(it))
        }

        // Si existen ambos puntos, centrar cámara entre ellos
        if (puntoOrigen != null && puntoDestino != null) {
            val lat = (puntoOrigen!!.latitude() + puntoDestino!!.latitude()) / 2
            val lng = (puntoOrigen!!.longitude() + puntoDestino!!.longitude()) / 2
            binding.mapView.getMapboxMap().setCamera(
                CameraOptions.Builder()
                    .center(Point.fromLngLat(lng, lat))
                    .zoom(9.0)
                    .build()
            )
        } else {
            val punto = puntoOrigen ?: puntoDestino!!
            binding.mapView.getMapboxMap().setCamera(
                CameraOptions.Builder()
                    .center(punto)
                    .zoom(14.0)
                    .build()
            )
        }
    }

    private fun calcularRuta() {
        binding.btnSiguiente.isEnabled = false
        binding.btnSiguiente.text = "CALCULANDO RUTA..."

        lifecycleScope.launch {
            val resultado = MapboxHelper.calcularRuta(puntoOrigen!!, puntoDestino!!, token)

            binding.btnSiguiente.isEnabled = true
            binding.btnSiguiente.text = "SIGUIENTE →"

            if (resultado == null) {
                Toast.makeText(this@Paso1Activity, "No se pudo calcular la ruta", Toast.LENGTH_SHORT).show()
                return@launch
            }

            // Serializar coordenadas como String para pasar al siguiente paso
            val coordsString = resultado.coordenadas.joinToString("|") { "${it.first},${it.second}" }

            val intent = Intent(this@Paso1Activity, Paso2Activity::class.java).apply {
                putExtra("origen", direccionOrigen)
                putExtra("destino", direccionDestino)
                putExtra("distanciaKm", resultado.distanciaKm)
                putExtra("duracionMin", resultado.duracionMin)
                putExtra("rutaCoordenadas", coordsString)
            }
            startActivity(intent)
        }
    }
}