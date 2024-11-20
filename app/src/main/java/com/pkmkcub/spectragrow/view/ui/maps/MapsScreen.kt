package com.pkmkcub.spectragrow.view.ui.maps

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.pkmkcub.spectragrow.R
import java.util.Locale

@Composable
fun MapsScreen(viewModel: MapsViewModel = viewModel(), navController: NavController) {
    var showCard by remember { mutableStateOf(false) }
    var markerPosition by remember { mutableStateOf<LatLng?>(null) }
    var streetName by remember { mutableStateOf("") }
    val context = LocalContext.current
    val locationPermissionGranted = remember { mutableStateOf(false) }
    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        } else {
            locationPermissionGranted.value = true
        }
    }

    if (locationPermissionGranted.value) {
        val fusedLocationProviderClient = remember {
            LocationServices.getFusedLocationProviderClient(context)
        }

        LaunchedEffect(Unit) {
            try {
                fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
                    location?.let {
                        val latLng = LatLng(it.latitude, it.longitude)
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 15f)
                    }
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }

        val geocoder = Geocoder(context, Locale.getDefault())
        val properties = remember { mutableStateOf(MapProperties(mapType = MapType.SATELLITE)) }
        val uiSettings = remember { MapUiSettings() }

        Box(Modifier.fillMaxSize()) {
            var text by remember { mutableStateOf("") }
            GoogleMap(
                modifier = Modifier.matchParentSize(),
                properties = properties.value,
                uiSettings = uiSettings,
                cameraPositionState = cameraPositionState,
                onMapLongClick = { latLng ->
                    markerPosition = latLng
                    viewModel.fetchElevation(latLng)
                    viewModel.fetchCurrentTemperature(latLng)
                    viewModel.fetchPlantDataByLocation(latLng)
                    streetName = try {
                        val addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                        if (!addressList.isNullOrEmpty()) {
                            addressList[0].getAddressLine(0) ?: "Unknown"
                        } else {
                            "Unknown"
                        }
                    } catch (e: Exception) {
                        "Error getting address"
                    }
                    showCard = true
                },
                onMapClick = {
                    markerPosition = null
                    showCard = false
                }
            ) {
                markerPosition?.let { position ->
                    Marker(
                        state = rememberMarkerState(position = position),
                        title = streetName
                    )
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .height(70.dp)
                    .background(colorResource(id = R.color.yellow_pattern))
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = {
                        text = it
                        viewModel.searchPlaces(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .background(colorResource(id = R.color.white_base)),
                    placeholder = { Text(text = "Search places...") }
                )
            }
            AnimatedVisibility(
                visible = viewModel.locationAutofill.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 70.dp)
                    .background(colorResource(id = R.color.yellow_pattern))
            ) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.background(colorResource(id = R.color.white_base))
                ) {
                    items(viewModel.locationAutofill) { result ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .clickable {
                                    text = result.address
                                    viewModel.locationAutofill.clear()
                                    viewModel.getCoordinates(result) { latLng ->
                                        latLng?.let {
                                            cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 15f)
                                        }
                                    }
                                }
                        ) {
                            Text(result.address)
                        }
                    }
                }
            }

            if (showCard && viewModel.plantList.value.isNotEmpty()) {
                ResultCard(viewModel, navController)
            }
        }
    }
}


@Composable
fun ResultCard(viewModel: MapsViewModel = viewModel(), navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp)
                .padding(0.dp)
                .align(Alignment.BottomCenter),
            elevation = CardDefaults.elevatedCardElevation(10.dp),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            colors = CardDefaults.cardColors(colorResource(id = R.color.white))
        ) {
            // Add vertical scroll
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TempAndElevationCard(viewModel, navController)
                Text(
                    text = "Karakteristik Tanah",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 10.dp)
                )

                Spacer(modifier = Modifier.height(5.dp))
                SoilCharacteristicsTable(viewModel)
                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = "Rekomendasi Tanaman",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(5.dp))
                RecommendedPlantsRow(viewModel)
                Spacer(modifier = Modifier.height(5.dp))
            }
        }
    }
}
@Composable
fun SoilCharacteristicsTable(viewModel: MapsViewModel) {
    val soilProperties = viewModel.soilProperties.value

    val soilList = listOf(
        "pH" to soilProperties.pH.toString(),
        "Nitrogen" to (soilProperties.natrium),
        "Fosfat" to (soilProperties.fosfat),
        "Kalium" to (soilProperties.kalium),
        "Bahan Organik" to (soilProperties.bahan_organik),
        "Tekstur Tanah" to (soilProperties.tekstur_tanah),
        "SKL" to (soilProperties.skl),
        "Kadar Air" to (soilProperties.kadar_air.toString())
    )

    soilList.forEach { (name, value) ->
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = name, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.Black)
            Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.Black)
        }
    }
}



@Composable
fun RecommendedPlantsRow(viewModel: MapsViewModel) {
    val plantList = viewModel.plantList.value

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(plantList) { plant ->
            Card(
                modifier = Modifier
                    .width(100.dp)
                    .height(120.dp),
                elevation = CardDefaults.elevatedCardElevation(5.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = plant.name, fontSize = 12.sp)
                }
            }
        }
    }
}


@Composable
fun TempAndElevationCard(viewModel: MapsViewModel = viewModel(), navController: NavController) {
    val elevation by viewModel.elevation
    val temperature by viewModel.temperature

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(50.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(
            modifier = Modifier
                .weight(1f)
                .padding(4.dp),
            elevation = CardDefaults.elevatedCardElevation(4.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = elevation.ifEmpty { "..." },
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Card(
            modifier = Modifier
                .weight(1f)
                .padding(4.dp),
            elevation = CardDefaults.elevatedCardElevation(4.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = temperature.ifEmpty { "..." },
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .weight(2f),
            colors = ButtonDefaults.buttonColors(colorResource(id = R.color.yellow_pattern)),
            onClick = { navController.navigate("detailitem")}
        ) {
            Text(text = stringResource(id = R.string.detail_item), color = colorResource(id = R.color.white))
        }
    }
}
