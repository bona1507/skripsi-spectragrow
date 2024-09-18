package com.pkmkcub.spectragrow.view.ui.maps

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.pkmkcub.spectragrow.BuildConfig
import com.pkmkcub.spectragrow.R
import com.pkmkcub.spectragrow.databinding.FragmentMapsBinding
import com.pkmkcub.spectragrow.model.Plant
import com.pkmkcub.spectragrow.model.Story
import com.pkmkcub.spectragrow.view.adapter.PlantAdapter
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class MapsFragment : Fragment(), PlantAdapter.OnItemClickListener {

    private lateinit var binding: FragmentMapsBinding
    private lateinit var placesClient: PlacesClient
    private lateinit var mMap: GoogleMap
    private lateinit var autocomplete: AutocompleteSupportFragment
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var firestore: FirebaseFirestore
    private var customMarker: Marker? = null
    private var regularMarkers = mutableListOf<Marker>()
    private lateinit var plantAdapter: PlantAdapter
    private var plantList = mutableListOf<Plant>()

    private val callback = OnMapReadyCallback { googleMap ->
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
        getUserLocation()
        fetchAllStoryData()
        setupRecyclerView()
        mMap.setOnMapLongClickListener { latLng ->
            clearRegularMarkers()
            regularMarkers.add(mMap.addMarker(MarkerOptions().position(latLng))!!)
            fetchPlantDataByLocation(latLng)
            binding.cardResult.apply {
                visibility = View.VISIBLE
                startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up))
            }
        }
        mMap.setOnMapClickListener { clearRegularMarkers(); binding.cardResult.visibility = View.GONE }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMapsBinding.inflate(inflater, container, false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firestore = FirebaseFirestore.getInstance("plant")
        Places.initialize(requireContext(), BuildConfig.MAPS_API_KEY)
        placesClient = Places.createClient(requireActivity())
        (childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?)?.getMapAsync(callback)
        setupAutocomplete()
    }

    private fun setupAutocomplete() {
        autocomplete = childFragmentManager.findFragmentById(R.id.auto_complete_fragment) as AutocompleteSupportFragment
        autocomplete.setPlaceFields(listOf(Place.Field.ID, Place.Field.ADDRESS, Place.Field.LAT_LNG))
        autocomplete.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onError(status: Status) {
                Toast.makeText(requireContext(), status.statusMessage, Toast.LENGTH_SHORT).show()
            }

            override fun onPlaceSelected(place: Place) {
                place.latLng?.let { mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(it, 12f)) }
            }
        })
    }

    private fun getUserLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnCompleteListener { task ->
                task.result?.let { mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 12f)) }
            }
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    private fun fetchAllStoryData() {
        firestore.collection("stories").get().addOnSuccessListener { result ->
            val storyList = result.map { it.toObject(Story::class.java) }
            storyList.forEach { story ->
                val location = LatLng(story.lat, story.lon)
                customMarker = mMap.addMarker(MarkerOptions().position(location).title(story.title).icon(BitmapDescriptorFactory.fromBitmap(drawableToBitmap(ContextCompat.getDrawable(requireContext(), R.drawable.custommarker)))))
            }
            mMap.setOnMarkerClickListener { marker ->
                storyList.find { it.title == marker.title }?.let { findNavController().navigate(MapsFragmentDirections.actionMapsToDetailStory(it)) }
                true
            }
        }.addOnFailureListener { Log.e("Fetch Stories", "Error fetching stories", it) }
    }

    private fun fetchPlantDataByLocation(latLng: LatLng) {
        val radiusInMeters = 5000.0
        var nearestDocument: DocumentSnapshot? = null
        var nearestDistance = Double.MAX_VALUE

        plantList.clear()
        plantAdapter.setOriginalPlantList(emptyList())
        plantAdapter.submitList(emptyList())
        plantAdapter.notifyDataSetChanged()

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
                    nearestDocument?.let {
                        binding.apply {
                            phValue.text = it.getDouble("pH")?.toString() ?: "N/A"
                            fosfatValue.text = it.getString("fosfat") ?: "N/A"
                            natriumValue.text = it.getString("natrium") ?: "N/A"
                            kaliumValue.text = it.getString("kalium") ?: "N/A"
                            boValue.text = it.getString("bahan_organik") ?: "N/A"
                            sklValue.text = it.getString("skl") ?: "N/A"
                            ttValue.text = it.getString("tekstur_tanah") ?: "N/A"
                            airValue.text = it.getDouble("kadar_air")?.toString() ?: "N/A"
                        }

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
                                                // Update the adapter after adding all plants
                                                if (tempPlantList.size == list.size) {
                                                    plantList.clear()
                                                    plantList.addAll(tempPlantList)
                                                    plantAdapter.setOriginalPlantList(plantList)
                                                    plantAdapter.submitList(plantList)
                                                    plantAdapter.notifyDataSetChanged()
                                                }
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
                    Toast.makeText(requireContext(), "Lokasi ini belum berhasil terdeteksi", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Plant Data", "Error fetching plant data", exception)
            }
    }

    private fun calculateDistance(start: LatLng, end: LatLng): Double {
        val earthRadius = 6371000.0
        val dLat = Math.toRadians(end.latitude - start.latitude)
        val dLon = Math.toRadians(end.longitude - start.longitude)
        val a = sin(dLat / 2) * sin(dLat / 2) + cos(Math.toRadians(start.latitude)) * cos(Math.toRadians(end.latitude)) * sin(dLon / 2) * sin(dLon / 2)
        return earthRadius * (2 * atan2(sqrt(a), sqrt(1 - a)))
    }

    private fun drawableToBitmap(drawable: Drawable?): Bitmap {
        return Bitmap.createBitmap(drawable?.intrinsicWidth ?: 0, drawable?.intrinsicHeight ?: 0, Bitmap.Config.ARGB_8888).also {
            Canvas(it).apply { drawable?.setBounds(0, 0, width, height); drawable?.draw(this) }
        }
    }

    private fun clearRegularMarkers() {
        regularMarkers.forEach { it.remove() }
        regularMarkers.clear()
    }

    private fun setupRecyclerView() {
        plantAdapter = PlantAdapter(this)
        binding.rvSearch.apply {
            adapter = plantAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onItemClick(item: Plant) {}
}
