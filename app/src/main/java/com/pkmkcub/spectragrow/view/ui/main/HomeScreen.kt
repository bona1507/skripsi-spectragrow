package com.pkmkcub.spectragrow.view.ui.main

import HomeViewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.pkmkcub.spectragrow.R
import com.pkmkcub.spectragrow.core.model.Plant
import com.pkmkcub.spectragrow.view.ui.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(nav: NavController) {
    val authViewModel: AuthViewModel = viewModel()
    val homeViewModel: HomeViewModel = viewModel()
    val plantList by homeViewModel.plantList.collectAsState()
    var selectedPlant by remember { mutableStateOf<Plant?>(null) }
    var searchText by remember { mutableStateOf("") }
    val filteredPlants = plantList.filter { plant ->
        plant.name.contains(searchText, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.testTag("HomeScreenTopappbar"),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.yellow_pattern),
                    titleContentColor = colorResource(id = R.color.white_base),
                ),
                title = {
                    Text(text = stringResource(id = R.string.app_name), fontFamily = FontFamily(Font(R.font.bold)), color = colorResource(id = R.color.white_base))
                },
                actions = {
                    IconButton(onClick = {
                        nav.navigate("liststory")
                    }) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = colorResource(id = R.color.white_base))
                    }
                    IconButton(onClick = { nav.navigate("maps") }) {
                        Icon(Icons.Filled.Map, contentDescription = null, tint = colorResource(id = R.color.white_base))
                    }
                    IconButton(onClick = { authViewModel.logout(
                        logoutCallback = { nav.navigate("onboarding") }
                    )}) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = colorResource(id = R.color.white_base))
                    }
                }
            )
        }
    ) { paddingValues ->
        Image(
            painter = painterResource(id = R.drawable.ob_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Column(
            modifier = Modifier
                .padding(paddingValues)
        ) {
            Text(
                text = stringResource(R.string.welcome_username, authViewModel.getDisplayNameUser()),
                fontFamily = FontFamily(Font(R.font.semibold)),
                color = colorResource(id = R.color.black),
                modifier = Modifier.padding(16.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                elevation = CardDefaults.elevatedCardElevation(5.dp),
                shape = RectangleShape
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colorResource(id = R.color.white))
                ) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        label = { Text(text = stringResource(R.string.input_plant)) },
                        leadingIcon = {
                            Icon(painterResource(id = R.drawable.plant), contentDescription = null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp)
                    )
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp, horizontal = 5.dp)
                    ) {
                        items(filteredPlants) { plant ->
                            PlantItem(plant = plant, onClick = {
                                selectedPlant = plant
                            })
                        }
                    }
                    Spacer(modifier = Modifier.height(5.dp))
                }
            }

            selectedPlant?.let { plant ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Card(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        elevation = CardDefaults.elevatedCardElevation(10.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(colorResource(id = R.color.white))
                    ) {
                        Column(modifier = Modifier
                            .padding(5.dp),
                            horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(R.string.item_title_fertilizer),
                                fontFamily = FontFamily(Font(R.font.semibold)),
                                fontSize = 16.sp,
                                color = colorResource(id = R.color.black),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = plant.fertilizer,
                                fontFamily = FontFamily(Font(R.font.medium_italic)),
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                color = colorResource(id = R.color.black)
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(2f)
                                        .padding(2.dp),
                                    verticalArrangement = Arrangement.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(1.5f)
                                            .clip(RoundedCornerShape(12.dp))
                                    ) {
                                        Image(
                                            painter = rememberAsyncImagePainter(plant.photo_url),
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    Column(
                                        modifier = Modifier.padding(top = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = plant.name,
                                            fontFamily = FontFamily(Font(R.font.semibold)),
                                            fontSize = 12.sp,
                                            color = colorResource(id = R.color.black)
                                        )
                                        Text(
                                            text = plant.nama_lt,
                                            fontFamily = FontFamily(Font(R.font.medium_italic)),
                                            fontSize = 12.sp,
                                            color = colorResource(id = R.color.black)
                                        )
                                    }
                                }
                                Column(
                                    modifier = Modifier
                                        .weight(5f)
                                ) {
                                    SoilTable(plant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlantItem(plant: Plant, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .width(100.dp)
            .height(120.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.elevatedCardElevation(5.dp),
        colors = CardDefaults.cardColors(colorResource(id = R.color.white_base))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth()
                    .aspectRatio(1.5f)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                Image(
                    painter = rememberAsyncImagePainter(plant.photo_url),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Text(
                text = plant.name,
                fontFamily = FontFamily(Font(R.font.semibold)),
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                color = colorResource(id = R.color.black),
                maxLines = 1
            )
            Text(
                text = plant.nama_lt,
                fontFamily = FontFamily(Font(R.font.medium_italic)),
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                color = colorResource(id = R.color.black),
                maxLines = 1
            )
        }
    }
}

@Composable
fun SoilTable(plant: Plant) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 2.dp)
    ) {
        Text(
            text = stringResource(R.string.item_title),
            fontFamily = FontFamily(Font(R.font.semibold)),
            fontSize = 16.sp,
            color = colorResource(id = R.color.black),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 5.dp)
        )

        val rows = listOf(
            stringResource(R.string.ph) to plant.pH,
            stringResource(R.string.natrium) to plant.natrium,
            stringResource(R.string.fosfor) to plant.fosfat,
            stringResource(R.string.kalium) to plant.kalium,
            stringResource(R.string.bahan_organik) to plant.bahan_organik,
            stringResource(R.string.air) to plant.kadar_air,
            stringResource(R.string.skl) to plant.skl,
            stringResource(R.string.tekstur_tanah) to plant.tekstur_tanah
        )

        rows.forEach { (label, value) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = label,
                    fontFamily = FontFamily(Font(R.font.medium)),
                    fontSize = 12.sp,
                    color = colorResource(id = R.color.black)
                )
                Text(
                    text = value.toString(),
                    fontFamily = FontFamily(Font(R.font.medium)),
                    fontSize = 12.sp,
                    color = colorResource(id = R.color.black),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

