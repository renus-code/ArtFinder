@file:OptIn(ExperimentalMaterial3Api::class)

package com.humber.artfinder.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.humber.artfinder.ui.viewmodel.ArtState
import com.humber.artfinder.ui.viewmodel.ArtViewModel

@Composable
fun ArtDetailScreen(
    artworkId: Int,
    artVM: ArtViewModel,
    onNavigateBack: () -> Unit
) {
    val artState = artVM.artState.value
    
    // Find the specific artwork in our current list
    val artwork = if (artState is ArtState.Success) {
        artState.artworks.find { it.id == artworkId }
    } else null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Artwork Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (artwork == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Artwork not found")
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Large Image
                AsyncImage(
                    model = "https://www.artic.edu/iiif/2/${artwork.imageId}/full/843,/0/default.jpg",
                    contentDescription = artwork.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp),
                    contentScale = ContentScale.Fit
                )

                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = artwork.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    
                    artwork.dateDisplay?.let {
                        Text(text = it, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Artist Section
                    Text(text = "Artist", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Text(text = artwork.artistDisplay ?: "Unknown Artist", style = MaterialTheme.typography.bodyLarge)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    DetailItem(label = "Type", value = artwork.artworkType ?: "Unknown")
                    DetailItem(label = "Origin", value = artwork.placeOfOrigin ?: "Unknown")
                    DetailItem(label = "Medium", value = artwork.mediumDisplay ?: "Unknown")
                    DetailItem(label = "Dimensions", value = artwork.dimensions ?: "Unknown")
                    
                    // Gallery / On View Status
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text(text = "Gallery Status", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (artwork.isOnView) {
                                FilterChip(
                                    selected = true,
                                    onClick = { },
                                    label = { Text("Live in Museum") },
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(text = artwork.galleryTitle ?: "", style = MaterialTheme.typography.bodyLarge)
                            } else {
                                Text(text = "Not on view", style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
                    }

                    DetailItem(label = "Department", value = artwork.departmentTitle ?: "Unknown")
                    
                    // Description section
                    artwork.shortDescription?.let {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "About this work", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    if (artwork.isPublicDomain) {
                        SuggestionChip(
                            onClick = { },
                            label = { Text("Public Domain") },
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp), thickness = 0.5.dp)
    }
}
