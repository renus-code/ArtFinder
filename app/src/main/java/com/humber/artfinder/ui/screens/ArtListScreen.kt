@file:OptIn(ExperimentalMaterial3Api::class)

package com.humber.artfinder.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.humber.artfinder.data.model.Artwork
import com.humber.artfinder.ui.viewmodel.ArtState
import com.humber.artfinder.ui.viewmodel.ArtViewModel
import com.humber.artfinder.ui.viewmodel.AuthViewModel
import com.humber.artfinder.ui.viewmodel.UserViewModel

@Composable
fun ArtListScreen(
    authVM: AuthViewModel,
    artVM: ArtViewModel,
    userVM: UserViewModel,
    onNavigateToDetails: (Int) -> Unit,
    onNavigateToProfile: () -> Unit,
    onSignOut: () -> Unit
) {
    val artState by artVM.artState
    val searchQuery by artVM.searchQuery
    val currentPage by artVM.currentPage

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ArtFinder", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                    TextButton(onClick = { 
                        authVM.signOut()
                        userVM.clearProfile()
                        onSignOut()
                    }) {
                        Text("Sign Out")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { artVM.onSearchQueryChange(it) },
                label = { Text("Search Artworks") },
                placeholder = { Text("Search by artwork name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true
            )

            // Art List Content
            Box(modifier = Modifier.weight(1f)) {
                when (val state = artState) {
                    is ArtState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is ArtState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = state.message, color = MaterialTheme.colorScheme.error)
                        }
                    }
                    is ArtState.Success -> {
                        if (state.artworks.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No artworks found")
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(state.artworks) { artwork ->
                                    val isVisited = userVM.isVisited(artwork.id)
                                    ArtworkListItem(
                                        artwork = artwork,
                                        isVisited = isVisited,
                                        onClick = { onNavigateToDetails(artwork.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Pagination Controls
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = androidx.compose.ui.graphics.RectangleShape
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { artVM.loadPreviousPage() },
                        enabled = currentPage > 1 && artState !is ArtState.Loading,
                        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Page")
                        Spacer(Modifier.width(8.dp))
                        Text("Prev")
                    }

                    Text(
                        text = "Page $currentPage",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Button(
                        onClick = { artVM.loadNextPage() },
                        enabled = artState is ArtState.Success && (artState as ArtState.Success).artworks.isNotEmpty() && artState !is ArtState.Loading,
                        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                    ) {
                        Text("Next")
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Page")
                    }
                }
            }
        }
    }
}

@Composable
fun ArtworkListItem(
    artwork: Artwork,
    isVisited: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 100.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .height(100.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                AsyncImage(
                    model = "https://www.artic.edu/iiif/2/${artwork.imageId}/full/200,/0/default.jpg",
                    contentDescription = artwork.title,
                    modifier = Modifier
                        .size(100.dp)
                        .padding(4.dp),
                    contentScale = ContentScale.Crop
                )
                
                if (isVisited) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Visited",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(2.dp)
                            .background(Color.White, CircleShape)
                            .clip(CircleShape)
                            .size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = artwork.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    text = artwork.artistTitle ?: "Unknown Artist",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = artwork.artworkType ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    
                    if (isVisited) {
                        Text(
                            text = "Visited",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
