@file:OptIn(ExperimentalMaterial3Api::class)

package com.humber.artfinder.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.humber.artfinder.data.model.Artwork
import com.humber.artfinder.ui.viewmodel.ArtState
import com.humber.artfinder.ui.viewmodel.ArtViewModel
import com.humber.artfinder.ui.viewmodel.UserViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ArtDetailScreen(
    artworkId: Int,
    artVM: ArtViewModel,
    userVM: UserViewModel,
    onNavigateBack: () -> Unit
) {
    val artState = artVM.artState.value
    val context = LocalContext.current
    
    // 1. Try to find in current search list or visited history
    var artwork = if (artState is ArtState.Success) {
        artState.artworks.find { it.id == artworkId }
    } else null

    if (artwork == null) {
        val visited = userVM.getVisitedArtworkById(artworkId)
        if (visited != null) {
            artwork = Artwork(
                id = visited.id,
                title = visited.title,
                artistDisplay = visited.artistDisplay,
                imageId = visited.imageId,
                isPublicDomain = true,
                artworkType = visited.artworkType,
                placeOfOrigin = null,
                dimensions = null,
                mediumDisplay = visited.mediumDisplay,
                galleryTitle = null,
                isOnView = false,
                dateDisplay = null,
                shortDescription = null,
                latitude = visited.latitude,
                longitude = visited.longitude,
                departmentTitle = null,
                artistTitle = null
            )
        }
    }

    val isVisited = userVM.isVisited(artworkId)

    val locationPermissionState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Artwork Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (artwork != null) {
                        IconButton(onClick = { userVM.toggleVisited(artwork!!) }) {
                            Icon(
                                imageVector = if (isVisited) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                                contentDescription = "Mark as Visited",
                                tint = if (isVisited) Color(0xFF4CAF50) else LocalContentColor.current
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (artwork == null) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("Artwork details not available")
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                AsyncImage(
                    model = "https://www.artic.edu/iiif/2/${artwork.imageId}/full/843,/0/default.jpg",
                    contentDescription = artwork.title,
                    modifier = Modifier.fillMaxWidth().height(350.dp),
                    contentScale = ContentScale.Fit
                )

                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = artwork.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    
                    artwork.dateDisplay?.let {
                        Text(text = it, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(text = "Artist", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Text(text = artwork.artistDisplay ?: "Unknown Artist", style = MaterialTheme.typography.bodyLarge)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    DetailItem(label = "Type", value = artwork.artworkType ?: "Unknown")
                    DetailItem(label = "Medium", value = artwork.mediumDisplay ?: "Unknown")
                    DetailItem(label = "Dimensions", value = artwork.dimensions ?: "Unknown")
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Location", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        
                        // Added "Get Directions" button to match Professor's mention of inbuilt Google Maps
                        Button(
                            onClick = {
                                val lat = artwork!!.latitude ?: 41.8796
                                val lng = artwork!!.longitude ?: -87.6237
                                val gmmIntentUri = Uri.parse("google.navigation:q=$lat,$lng")
                                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                mapIntent.setPackage("com.google.android.apps.maps")
                                context.startActivity(mapIntent)
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Directions", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    // Professor's requirement: Handling null coordinates gracefully
                    val displayLat = artwork.latitude ?: 41.8796
                    val displayLng = artwork.longitude ?: -87.6237
                    val artworkLocation = LatLng(displayLat, displayLng)
                    
                    if (artwork.latitude == null) {
                        Text(
                            text = "Note: Using default Art Institute location",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    val cameraPositionState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(artworkLocation, 15f)
                    }

                    LaunchedEffect(Unit) {
                        locationPermissionState.launchMultiplePermissionRequest()
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(250.dp)) {
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraPositionState,
                            properties = MapProperties(
                                isMyLocationEnabled = locationPermissionState.allPermissionsGranted
                            ),
                            uiSettings = MapUiSettings(
                                myLocationButtonEnabled = true,
                                zoomControlsEnabled = true
                            )
                        ) {
                            Marker(
                                state = MarkerState(position = artworkLocation),
                                title = artwork.title,
                                icon = if (isVisited) {
                                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                                } else {
                                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                                }
                            )
                        }
                    }

                    artwork.shortDescription?.let {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "About this work", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(text = it, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
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
