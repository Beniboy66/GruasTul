package com.gruastul.app.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

object TollGuruHelper {

    private const val API_KEY = "tg_53E6E0BBED7548E6BCD0A59FB8B2537D"
    private const val BASE_URL = "https://dev.tollguru.com/v1/calc/route"

    data class ResultadoCasetas(
        val numCasetas: Int,
        val costoTotal: Double,
        val detalle: List<Caseta>
    )

    data class Caseta(
        val nombre: String,
        val costo: Double
    )

    private val client = OkHttpClient()

    /**
     * @param coordenadasRuta Lista de pares [lng, lat] que forman la ruta
     */
    suspend fun calcularCasetas(
        coordenadasRuta: List<Pair<Double, Double>>
    ): ResultadoCasetas = withContext(Dispatchers.IO) {
        try {
            // Construir el array de polyline para TollGuru
            val polylineArray = JSONArray()
            coordenadasRuta.forEach { (lng, lat) ->
                val punto = JSONArray()
                punto.put(lat)
                punto.put(lng)
                polylineArray.put(punto)
            }

            val body = JSONObject().apply {
                put("vehicle", JSONObject().apply {
                    put("type", "2AxlesTruck") // tipo de grúa
                })
                put("departure_time", "now")
                put("polyline", polylineArray)
            }

            val requestBody = body.toString()
                .toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url(BASE_URL)
                .post(requestBody)
                .addHeader("x-api-key", API_KEY)
                .addHeader("Content-Type", "application/json")
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                // Fallback si la API falla
                return@withContext ResultadoCasetas(0, 0.0, emptyList())
            }

            parsearRespuesta(responseBody)

        } catch (e: Exception) {
            // Fallback silencioso
            ResultadoCasetas(0, 0.0, emptyList())
        }
    }

    private fun parsearRespuesta(json: String): ResultadoCasetas {
        return try {
            val obj = JSONObject(json)
            val summary = obj.optJSONObject("summary") ?: return ResultadoCasetas(0, 0.0, emptyList())

            // TollGuru regresa costos en diferentes monedas; buscar MXN o USD
            val costs = summary.optJSONObject("totalToll") ?: return ResultadoCasetas(0, 0.0, emptyList())
            val costoMXN = costs.optDouble("MXN", 0.0)
            val costoUSD = costs.optDouble("USD", 0.0)
            val costo = if (costoMXN > 0) costoMXN else costoUSD * 17.5 // fallback conversión

            // Contar casetas individuales
            val tolls = obj.optJSONArray("tolls") ?: JSONArray()
            val listaCasetas = mutableListOf<Caseta>()

            for (i in 0 until tolls.length()) {
                val toll = tolls.getJSONObject(i)
                val nombre = toll.optString("name", "Caseta ${i + 1}")
                val tarifas = toll.optJSONObject("tagCost") ?: toll.optJSONObject("cashCost")
                val costoCaseta = tarifas?.optDouble("MXN", tarifas.optDouble("USD", 0.0) * 17.5) ?: 0.0
                listaCasetas.add(Caseta(nombre, costoCaseta))
            }

            ResultadoCasetas(
                numCasetas = listaCasetas.size,
                costoTotal = costo,
                detalle = listaCasetas
            )
        } catch (e: Exception) {
            ResultadoCasetas(0, 0.0, emptyList())
        }
    }
}