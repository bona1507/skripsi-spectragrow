package com.pkmkcub.spectragrow.view.ui.userstory

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.fragment.findNavController
import coil.load
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.pkmkcub.spectragrow.R
import com.pkmkcub.spectragrow.databinding.FragmentAddStoryBinding
import com.pkmkcub.spectragrow.model.Story
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val IMAGE_TYPE = "image/*"

class AddStoryFragment : Fragment() {

    private lateinit var binding: FragmentAddStoryBinding
    private val firestore = FirebaseFirestore.getInstance("plant")
    private val storage = FirebaseStorage.getInstance()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0
    private var selectedImageUri: Uri? = null

    private val launcherGallery = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            binding.ivPreview.load(it)
        }
    }

    private val launcherCamera = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri?.let {
                binding.ivPreview.load(it)
            }
        }
    }

    private val requestCameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            Log.d("Camera Permission", "Permission denied")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddStoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        checkLocationPermissionAndFetchLocation()
        setupUi()
    }

    private fun setupUi() {
        binding.apply {
            btnAdd.setOnClickListener {
                uploadStory()
            }
            btnCamera.setOnClickListener {
                handleCamera()
            }
            btnGallery.setOnClickListener {
                launcherGallery.launch(IMAGE_TYPE)
            }
        }
    }

    private fun handleCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            requestCameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    private fun openCamera() {
        createImageFile()?.let { file ->
            selectedImageUri = FileProvider.getUriForFile(requireContext(), "com.pkmkcub.project.fileprovider", file)
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, selectedImageUri)
            }
            launcherCamera.launch(cameraIntent)
        }
    }

    private fun createImageFile(): File? {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            selectedImageUri = Uri.fromFile(this)
        }
    }

    private fun checkLocationPermissionAndFetchLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fetchLocation()
        } else {
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    @SuppressLint("MissingPermission")
    private fun fetchLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    currentLatitude = location.latitude
                    currentLongitude = location.longitude
                    updateLocationName()
                } else {
                    Toast.makeText(requireContext(), "Failed to get location", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun updateLocationName() {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(currentLatitude, currentLongitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val locationName = address.getAddressLine(0)
                binding.toogleLoc.text = locationName
            }
        } catch (e: Exception) {
            e.printStackTrace()
            binding.toogleLoc.text = getString(R.string.unknown_location)
        }
    }

    private fun uploadStory() {
        val title = binding.writeTitle.text.toString().trim()
        val content = binding.writeDesc.text.toString().trim()

        if (content.isEmpty() || title.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (currentLatitude == 0.0 || currentLongitude == 0.0) {
            Toast.makeText(requireContext(), "Waiting for location...", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        selectedImageUri?.let {
            uploadImageToStorage(it) { imageUrl ->
                val story = Story(photo_url = imageUrl, title = title, content = content, lon = currentLongitude, lat = currentLatitude)
                uploadStoryToFirestore(story)
            }
        } ?: run {
            binding.progressBar.visibility = View.GONE
            Toast.makeText(requireContext(), "Please select an image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadImageToStorage(imageUri: Uri, onSuccess: (String) -> Unit) {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageRef = storage.reference.child("images/${timeStamp}.jpg")

        imageRef.putFile(imageUri)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    onSuccess(uri.toString())
                }
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadStoryToFirestore(story: Story) {
        firestore.collection("stories")
            .add(story)
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(
                    requireContext(),
                    "Story uploaded successfully",
                    Toast.LENGTH_SHORT
                ).show()
                findNavController().navigate(R.id.action_addStory_to_listStory)
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                Toast.makeText(
                    requireContext(),
                    "Failed to upload story: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLocation()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Location permission denied",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
}
