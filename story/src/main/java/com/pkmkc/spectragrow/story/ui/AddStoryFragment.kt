package com.pkmkc.spectragrow.story.ui

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.pkmkc.spectragrow.story.StoryViewModel
import com.pkmkcub.spectragrow.R
import com.pkmkcub.spectragrow.core.model.Story
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun AddStoryScreen(
    viewModel: StoryViewModel = viewModel(),
    onNavigateToListStory: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var currentLocation by remember { mutableStateOf<Location?>(null) }
    var previewImage by remember { mutableStateOf(false) }

    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    val imageCapture = remember { ImageCapture.Builder().build() }
    remember { Executors.newSingleThreadExecutor() }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> if (isGranted) previewImage = true }
    )

    LaunchedEffect (locationPermissionState.status.isGranted) {
        if (locationPermissionState.status.isGranted) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        currentLocation = it
                    } ?: run {
                        Toast.makeText(context, "Failed to get location", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            locationPermissionState.launchPermissionRequest()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                imageUri = uri
            } else {
                Toast.makeText(context, "Failed to select image", Toast.LENGTH_SHORT).show()
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.yellow_pattern),
                    titleContentColor = colorResource(id = R.color.white_base),
                ),
                title = {
                    Text(
                        text = "Tambah Cerita Pertanian",
                        fontFamily = FontFamily(Font(R.font.bold)),
                        color = colorResource(id = R.color.white_base)
                    )
                },
            )
        },
        content = { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(padding)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ob_bg),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                if (previewImage) {
                    CameraPreviewView(
                        context = context,
                        imageCapture = imageCapture,
                        onClose = { previewImage = false },
                        onImageCaptured = { uri ->
                            imageUri = uri
                            previewImage = false
                        }
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .height(150.dp)
                                .fillMaxWidth()
                                .background(Color.LightGray),
                            contentAlignment = Alignment.Center
                        ) {
                            if (imageUri != null) {
                                Image(
                                    painter = rememberAsyncImagePainter(imageUri),
                                    contentDescription = "Selected Image",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .testTag("ImagePreview"),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text("No image selected")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(onClick = {permissionLauncher.launch(Manifest.permission.CAMERA)}, modifier = Modifier.testTag("ButtonCamera")) {
                                Text("Camera")
                            }
                            Button(onClick = { galleryLauncher.launch("image/*") }) {
                                Text("Gallery")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Enter title") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Enter description") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Upload Button
                        Button(
                            onClick = {
                                if (title.isNotEmpty() && description.isNotEmpty() && imageUri != null && currentLocation != null) {
                                    isUploading = true
                                    scope.launch {
                                        viewModel.addStory(
                                            story = Story(
                                                title = title,
                                                content = description,
                                                photo_url = "",
                                                lat = currentLocation?.latitude ?: 0.0,
                                                lon = currentLocation?.longitude ?: 0.0
                                            ),
                                            imageUri = imageUri!!,
                                            onSuccess = {
                                                isUploading = false
                                                Toast.makeText(
                                                    context,
                                                    "Story added successfully",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                onNavigateToListStory()
                                            },
                                            onFailure = { errorMessage ->
                                                isUploading = false
                                                Toast.makeText(
                                                    context,
                                                    errorMessage,
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        )
                                    }
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Please fill all fields, select an image, and enable location access",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            enabled = !isUploading,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isUploading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Text("Upload Story")
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun CameraPreviewView(
    context: Context,
    imageCapture: ImageCapture,
    onClose: (() -> Unit)? = null,
    onImageCaptured: (Uri) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    var cameraReady by remember { mutableStateOf(false) } // State to track camera readiness

    LaunchedEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

            try {
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
                // Set camera as ready once initialized
                cameraReady = true
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Failed to initialize camera: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (cameraReady) { // Show button only if camera is ready
                Button(
                    onClick = {
                        takePicture(context, imageCapture) { uri ->
                            onImageCaptured(uri)
                        }
                    },
                    modifier = Modifier.testTag("ButtonTakePicture")
                ) {
                    Text("Take Picture")
                }
            }
            onClose?.let {
                Button(onClick = it) {
                    Text("Close")
                }
            }
        }
    }
}

fun takePicture(
    context: Context,
    imageCapture: ImageCapture,
    onImageCaptured: (Uri) -> Unit
) {
    val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
        .format(System.currentTimeMillis())
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
    }

    val outputOptions = ImageCapture.OutputFileOptions
        .Builder(
            context.contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )
        .build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val savedUri = outputFileResults.savedUri
                if (savedUri != null) {
                    Toast.makeText(context, "Image capture successful!", Toast.LENGTH_SHORT).show()
                    onImageCaptured(savedUri) // Pass the URI back to the caller
                }
            }

            override fun onError(exception: ImageCaptureException) {
                Toast.makeText(context, "Failed to save picture: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        }
    )
}


