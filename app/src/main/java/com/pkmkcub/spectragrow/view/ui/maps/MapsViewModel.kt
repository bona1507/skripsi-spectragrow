package com.pkmkcub.spectragrow.view.ui.maps

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.pkmkcub.spectragrow.BuildConfig
import com.pkmkcub.spectragrow.model.Plant
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class MapsViewModel(private val context: Context) : ViewModel() {

    val locationAutofill = mutableStateListOf<AutocompleteResult>()
    private var job: Job? = null
    private val placesClient: PlacesClient = Places.createClient(context)
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance("plant")
    val elevation = mutableStateOf("")
    val temperature = mutableStateOf("")

    val soilProperties = mutableStateOf(
        Plant(
            pH = 0f,
            natrium = "N/A",
            fosfat = "N/A",
            kalium = "N/A",
            bahan_organik = "N/A",
            tekstur_tanah = "N/A",
            skl = "N/A",
            kadar_air = 0f
        )
    )
    private val _plantList = mutableStateOf<List<Plant>>(emptyList()) // Internal state
    val plantList: State<List<Plant>> = _plantList

    fun fetchPlantDataByLocation(latLng: LatLng) {
        val radiusInMeters = 5000.0
        var nearestDocument: DocumentSnapshot? = null
        var nearestDistance = Double.MAX_VALUE

        _plantList.value = emptyList()

        firestore.collection("places")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val documentLat = document.getDouble("lat") ?: continue
                    val documentLon = document.getDouble("lon") ?: continue

                    val documentLatLng = LatLng(documentLat, documentLon)
                    val distance = calculateDistance(latLng, documentLatLng)

                    if (distance <= radiusInMeters && distance < nearestDistance) {
                        nearestDistance = distance
                        nearestDocument = document
                    }
                }

                if (nearestDocument != null) {
                    nearestDocument?.let { document ->
                        soilProperties.value = Plant(
                            pH = document.getDouble("pH")?.toFloat() ?:0f,
                            natrium = document.getString("nitrogen") ?: "N/A",
                            fosfat = document.getString("fosfat") ?: "N/A",
                            kalium = document.getString("kalium") ?: "N/A",
                            bahan_organik = document.getString("bahan_organik") ?: "N/A",
                            tekstur_tanah = document.getString("tekstur_tanah") ?: "N/A",
                            skl = document.getString("skl") ?: "N/A",
                            kadar_air = document.getDouble("kadar_air")?.toFloat() ?: 0f
                        )

                        val plantArray = nearestDocument?.get("plant") as? List<*>
                        plantArray?.let { list ->
                            val tempPlantList = mutableListOf<Plant>()

                            for (plantId in list) {
                                if (plantId is String) {
                                    firestore.collection("plant")
                                        .document(plantId)
                                        .get()
                                        .addOnSuccessListener { plantDocument ->
                                            val plantResult = plantDocument.toObject(Plant::class.java)
                                            plantResult?.let {
                                                tempPlantList.add(it)
                                            }

                                            // Update the state when all plants are fetched
                                            if (tempPlantList.size == list.size) {
                                                _plantList.value = tempPlantList
                                            }
                                        }
                                        .addOnFailureListener { exception ->
                                            Log.e("Fetch Plant", "Error fetching plant with ID: $plantId", exception)
                                        }
                                }
                            }
                        }
                    }
                } else {
                    // No data found, update the plant list to empty
                    _plantList.value = emptyList()
                    Log.e("Plant Data", "No plants found for this location")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Plant Data", "Error fetching plant data", exception)
            }
    }

    fun searchPlaces(query: String) {
        job?.cancel()
        locationAutofill.clear()
        job = viewModelScope.launch {
            val request = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .build()
            placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener { response ->
                    locationAutofill += response.autocompletePredictions.map {
                        AutocompleteResult(
                            it.getFullText(null).toString(),
                            it.placeId
                        )
                    }
                }
                .addOnFailureListener {
                    it.printStackTrace()
                    println(it.cause)
                    println(it.message)
                }
        }
    }

    fun getCoordinates(result: AutocompleteResult, onSuccess: (LatLng?) -> Unit) {
        val placeFields = listOf(Place.Field.LAT_LNG)
        val request = FetchPlaceRequest.builder(result.placeId, placeFields).build()
        placesClient.fetchPlace(request)
            .addOnSuccessListener { response ->
                val place = response.place
                val latLng = place.latLng
                onSuccess(latLng)
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
                onSuccess(null)
            }
    }

    fun fetchElevation(latLng: LatLng) {
        val apiKey = BuildConfig.MAPS_API_KEY
        val url = "https://maps.googleapis.com/maps/api/elevation/json?locations=${latLng.latitude},${latLng.longitude}&key=$apiKey"

        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                val results = response.getJSONArray("results")
                if (results.length() > 0) {
                    val elevationValue = results.getJSONObject(0).getDouble("elevation")
                    elevation.value = String.format("%.2f", elevationValue) + " meters"
                }
            },
            { error ->
                Log.e("Elevation Fetch", "Error fetching elevation", error)
            }
        )

        Volley.newRequestQueue(context).add(request)
    }

    fun fetchCurrentTemperature(latLng: LatLng) {
        val url = "https://api.open-meteo.com/v1/forecast?latitude=${latLng.latitude}&longitude=${latLng.longitude}&current=temperature_2m"

        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                val current = response.getJSONObject("current")
                val temperatureValue = current.getDouble("temperature_2m")
                temperature.value = String.format("%.1f", temperatureValue) + "°C"
            },
            { error ->
                Log.e("Weather Fetch", "Error fetching weather", error)
            }
        )

        Volley.newRequestQueue(context).add(request)
    }

    private fun calculateDistance(start: LatLng, end: LatLng): Double {
        val earthRadius = 6371000.0
        val dLat = Math.toRadians(end.latitude - start.latitude)
        val dLon = Math.toRadians(end.longitude - start.longitude)
        val a = sin(dLat / 2) * sin(dLat / 2) + cos(Math.toRadians(start.latitude)) * cos(Math.toRadians(end.latitude)) * sin(dLon / 2) * sin(dLon / 2)
        return earthRadius * (2 * atan2(sqrt(a), sqrt(1 - a)))
    }

}
data class AutocompleteResult(
    val address: String,
    val placeId: String
)