package com.gruastul.app.utils

import com.mapbox.geojson.Point
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder

object MapboxHelper {

    private val client = OkHttpClient()

    data class ResultadoRuta(
        val distanciaKm: Double,
        val duracionMin: Double,
        val coordenadas: List<Pair<Double, Double>>
    )

    data class ResultadoGeocode(
        val nombre: String,
        val punto: Point
    )

    suspend fun geocodificar(
        query: String,
        token: String
    ): ResultadoGeocode? = withContext(Dispatchers.IO) {
        try {
            val q = URLEncoder.encode(query, "UTF-8")
            val url = "https://api.mapbox.com/geocoding/v5/mapbox.places/$q.json" +
                    "?proximity=-98.3630,20.0853" +
                    "&country=mx" +
                    "&limit=1" +
                    "&access_token=$token"

            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext null

            val json = JSONObject(body)
            val features = json.getJSONArray("features")
            if (features.length() == 0) return@withContext null

            val feature = features.getJSONObject(0)
            val nombre = feature.getString("place_name")
            val coords = feature.getJSONObject("geometry").getJSONArray("coordinates")
            val lng = coords.getDouble(0)
            val lat = coords.getDouble(1)

            ResultadoGeocode(
                nombre = nombre,
                punto = Point.fromLngLat(lng, lat)
            )
        } catch (e: Exception) {
            null
        }
    }

    suspend fun calcularRuta(
        origen: Point,
        destino: Point,
        token: String
    ): ResultadoRuta? = withContext(Dispatchers.IO) {
        try {
            val url = "https://api.mapbox.com/directions/v5/mapbox/driving/" +
                    "${origen.longitude()},${origen.latitude()};" +
                    "${destino.longitude()},${destino.latitude()}" +
                    "?alternatives=false" +
                    "&geometries=geojson" +
                    "&overview=full" +
                    "&steps=false" +
                    "&access_token=$token"

            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext null

            val json = JSONObject(body)
            val routes = json.getJSONArray("routes")
            if (routes.length() == 0) return@withContext null

            val route = routes.getJSONObject(0)
            val distanciaMetros = route.getDouble("distance")
            val duracionSegundos = route.getDouble("duration")

            val coordsArray = route
                .getJSONObject("geometry")
                .getJSONArray("coordinates")

            val coordenadas = mutableListOf<Pair<Double, Double>>()
            for (i in 0 until coordsArray.length()) {
                val punto = coordsArray.getJSONArray(i)
                coordenadas.add(Pair(punto.getDouble(0), punto.getDouble(1)))
            }

            ResultadoRuta(
                distanciaKm = distanciaMetros / 1000.0,
                duracionMin = duracionSegundos / 60.0,
                coordenadas = coordenadas
            )
        } catch (e: Exception) {
            null
        }
    }
}