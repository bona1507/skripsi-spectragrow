package com.pkmkcub.spectragrow.view.ui.maps

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.Location
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
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
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.heatmaps.HeatmapTileProvider
import com.pkmkcub.spectragrow.BuildConfig
import com.pkmkcub.spectragrow.R
import com.pkmkcub.spectragrow.databinding.FragmentMapsBinding
import com.pkmkcub.spectragrow.model.Plant
import com.pkmkcub.spectragrow.model.Story
import com.pkmkcub.spectragrow.view.adapter.PlantAdapter

class MapsFragment : Fragment(), PlantAdapter.OnItemClickListener {

    private lateinit var binding: FragmentMapsBinding
    private lateinit var placesClient: PlacesClient
    private lateinit var mMap: GoogleMap
    private lateinit var autocomplete: AutocompleteSupportFragment
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var firestore: FirebaseFirestore
    private lateinit var slideUpAnimation: Animation
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
        fetchAllPlantData()

        mMap.setOnMapLongClickListener { latLng ->
            clearRegularMarkers()
            mMap.addMarker(MarkerOptions().position(latLng).title("${latLng.longitude}, ${latLng.latitude}"))
                ?.let { regularMarkers.add(it) }
            binding.cardResult.visibility = View.VISIBLE
            binding.cardResult.startAnimation(slideUpAnimation)
        }
        mMap.setOnMapClickListener {
            clearRegularMarkers()
            binding.cardResult.visibility = View.GONE
        }
    }

    private var storyList = mutableListOf<Story>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapsBinding.inflate(inflater, container, false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firestore = FirebaseFirestore.getInstance("plant")
        slideUpAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)
        Places.initialize(requireContext(), BuildConfig.MAPS_API_KEY)
        placesClient = Places.createClient(requireActivity())
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
        autocomplete = childFragmentManager.findFragmentById(R.id.auto_complete_fragment) as AutocompleteSupportFragment
        autocomplete.setPlaceFields(listOf(Place.Field.ID, Place.Field.ADDRESS, Place.Field.LAT_LNG))
        autocomplete.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onError(status: Status) {
                Toast.makeText(requireContext(), status.statusMessage, Toast.LENGTH_SHORT).show()
            }

            override fun onPlaceSelected(place: Place) {
                place.latLng?.let {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(it, 12f))
                }
            }
        })
    }

    private fun getUserLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
            val locationResult: Task<Location> = fusedLocationClient.lastLocation
            locationResult.addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val location: Location? = task.result
                    location?.let {
                        val currentLatLng = LatLng(location.latitude, location.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
                    }
                }
            }
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    private fun fetchAllStoryData() {
        firestore.collection("stories")
            .get()
            .addOnSuccessListener { result ->
                storyList.clear()
                for (document in result) {
                    val story = document.toObject(Story::class.java)
                    storyList.add(story)
                    val location = LatLng(story.lat, story.lon)
                    customMarker = mMap.addMarker(MarkerOptions().position(location).title(story.title).icon(
                        BitmapDescriptorFactory.fromBitmap(drawableToBitmap(ContextCompat.getDrawable(requireContext(), R.drawable.custommarker)))))
                    mMap.setOnMarkerClickListener { marker ->
                        if (marker == customMarker) {
                            val selectedStory = storyList.find { it.title == marker.title }
                            selectedStory?.let {
                                findNavController().navigate(MapsFragmentDirections.actionMapsToDetailStory(it))
                            }
                            true // Consume the click for custom marker
                        } else {
                            false // Let default behavior apply for regular markers
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("com.pkmkcub.spectragrow.view.ui.maps.MapsFragment", "Error fetching stories", exception)
            }
    }

    private fun addHeatMap() {
        val latLngList = mutableListOf<LatLng>()
        storyList.forEach { story ->
            latLngList.add(LatLng(story.lat, story.lon))
        }
        val heatmapTileProvider = HeatmapTileProvider.Builder()
            .data(latLngList)
            .build()
        mMap.addTileOverlay(TileOverlayOptions().tileProvider(heatmapTileProvider))
    }

    private fun drawableToBitmap(drawable: Drawable?): Bitmap {
        val bitmap = Bitmap.createBitmap(drawable?.intrinsicWidth ?: 0,
            drawable?.intrinsicHeight ?: 0,
            Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable?.setBounds(0, 0, canvas.width, canvas.height)
        drawable?.draw(canvas)
        return bitmap
    }

    private fun clearRegularMarkers() {
        for (marker in regularMarkers) {
            marker.remove()
        }
        regularMarkers.clear()
    }

    private fun setupRecyclerView() {
        plantAdapter = PlantAdapter(this)
        binding.rvSearch.apply {
            adapter = plantAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun fetchAllPlantData() {
        firestore.collection("plant")
            .get()
            .addOnSuccessListener { result ->
                plantList.clear()
                for (document in result) {
                    val plant = document.toObject(Plant::class.java)
                    plantList.add(plant)
                }
                plantAdapter.setOriginalPlantList(plantList)
                plantAdapter.submitList(plantList)
                binding.apply {
                    phValue.text = plantList[0].pH.toString()
                    fosfatValue.text = plantList[0].fosfat
                    natriumValue.text = plantList[0].natrium
                    kaliumValue.text = plantList[0].kalium
                    boValue.text = plantList[0].bahan_organik
                    sklValue.text = plantList[0].skl
                    ttValue.text = plantList[0].tekstur_tanah
                    airValue.text = plantList[0].kadar_air.toString()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Plant Item", "Error fetching plants", exception)
            }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onItemClick(item: Plant) {
    }
}
