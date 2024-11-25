package com.pkmkc.spectragrow.story.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.pkmkc.spectragrow.story.R
import com.pkmkc.spectragrow.story.StoryViewModel
import com.pkmkcub.spectragrow.core.model.Story

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailStoryScreen(
    title: String,
    viewModel: StoryViewModel = viewModel()
) {
    val story by viewModel.selectedStory.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(title) {
        viewModel.fetchStoryByTitle(title)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.yellow_pattern),
                    titleContentColor = colorResource(id = R.color.white_base),
                ),
                title = {
                    Text(text = "Berbagi Cerita Pertanian", fontFamily = FontFamily(Font(R.font.bold)), color = colorResource(id = R.color.white_base))
                },
            )
        }
    ) { paddingValues ->
        Image(
            painter = painterResource(id = R.drawable.ob_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(modifier = Modifier.padding(paddingValues)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (story != null) {
                StoryDetailContent(story!!)
            } else {
                Text("Story not found", modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun StoryDetailContent(story: Story) {
    Column(modifier = Modifier.padding(16.dp)) {
        Image(
            painter = rememberAsyncImagePainter(model = story.photo_url),
            contentDescription = "Story Thumbnail",
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = story.title, color = colorResource(id = R.color.black),
            fontFamily = FontFamily(Font(R.font.semibold)))
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = story.content, color = colorResource(id = R.color.black),
            fontFamily = FontFamily(Font(R.font.medium)))
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Location: ${story.lat}, ${story.lon}",
            color = colorResource(id = R.color.black),
            fontFamily = FontFamily(Font(R.font.semibold))
        )
    }
}

