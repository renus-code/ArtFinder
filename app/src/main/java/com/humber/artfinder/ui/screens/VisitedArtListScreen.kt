@file:OptIn(ExperimentalMaterial3Api::class)

package com.humber.artfinder.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.humber.artfinder.data.model.VisitedArtwork
import com.humber.artfinder.ui.viewmodel.UserViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun VisitedArtListScreen(
    userVM: UserViewModel,
    onNavigateToDetails: (Int) -> Unit,
    onNavigateBack: () -> Unit
) {
    val visitedArtworks by userVM.visitedArtworks

    LaunchedEffect(Unit) {
        userVM.fetchVisitedArtworks()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Visited Artworks") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Navigate Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (visitedArtworks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("No visited artworks yet.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(visitedArtworks) { visited ->
                    VisitedArtworkListItem(
                        visited = visited,
                        onClick = { onNavigateToDetails(visited.id) },
                        onRemove = { 
                            userVM.removeVisitedById(visited.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun VisitedArtworkListItem(
    visited: VisitedArtwork,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    val sdf = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }
    val visitedDate = remember(visited.visitedAt) { sdf.format(visited.visitedAt.toDate()) }

    Card(
        onClick = onClick,
        // Milestone 4: Explicitly ensuring minimum touch target and adding action label
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .height(120.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = "https://www.artic.edu/iiif/2/${visited.imageId}/full/200,/0/default.jpg",
                // Milestone 4: More descriptive content description
                contentDescription = "Photograph of the artwork titled ${visited.title}",
                modifier = Modifier.size(100.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    // Milestone 4: Semantic grouping for screen readers
                    .clearAndSetSemantics {
                        contentDescription = "Artwork: ${visited.title}, by ${visited.artistDisplay ?: "Unknown artist"}, visited on $visitedDate"
                    }
            ) {
                Text(text = visited.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(text = visited.artistDisplay ?: "Unknown Artist", style = MaterialTheme.typography.bodySmall, maxLines = 1)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Visited on: $visitedDate",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(48.dp) // Milestone 4: Enforce 48dp touch target
            ) {
                Icon(
                    Icons.Default.Delete, 
                    contentDescription = "Remove ${visited.title} from visited list", 
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
