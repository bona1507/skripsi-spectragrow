package com.pkmkc.spectragrow.story.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.pkmkcub.spectragrow.R
import com.pkmkc.spectragrow.story.StoryViewModel
import com.pkmkcub.spectragrow.core.model.Story

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListStoryScreen(
    onAddStory: () -> Unit,
    onStoryClick: (Story) -> Unit,
    viewModel: StoryViewModel = viewModel()
) {
    val stories by viewModel.storyList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

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
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddStory, containerColor = colorResource(id = R.color.yellow_pattern), modifier = Modifier.testTag("AddStoryFAB")) {
                Icon(Icons.Default.Add, contentDescription = "Add Story", tint = colorResource(id = R.color.white_base))
            }
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
            } else {
                LazyColumn(modifier = Modifier.testTag("ListStory")) {
                    items(stories) { story ->
                        StoryItem(story, onStoryClick)
                    }
                }
            }
        }
    }
}

@Composable
fun StoryItem(story: Story, onStoryClick: (Story) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onStoryClick(story) },
        elevation = CardDefaults.elevatedCardElevation(4.dp),
        colors = CardDefaults.cardColors(colorResource(id = R.color.white))
    ) {
        Box(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth()
                .aspectRatio(1.5f)
                .clip(RoundedCornerShape(8.dp))
        ) {
            Image(
                painter = rememberAsyncImagePainter(story.photo_url),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = story.title, color = colorResource(id = R.color.black),
                fontFamily = FontFamily(Font(R.font.semibold))
            )
            Text(text = story.content, color = colorResource(id = R.color.black),
                fontFamily = FontFamily(Font(R.font.medium)))
        }
    }
}
