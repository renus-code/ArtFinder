@file:OptIn(ExperimentalMaterial3Api::class)

package com.humber.artfinder.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import com.humber.artfinder.ui.viewmodel.UserState
import com.humber.artfinder.ui.viewmodel.UserViewModel

@Composable
fun ProfileScreen(
    userVM: UserViewModel,
    onNavigateToVisitedList: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val userProfile by userVM.userProfile
    val userState by userVM.userState
    val context = LocalContext.current

    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components { add(SvgDecoder.Factory()) }
            .build()
    }

    var displayName by remember { mutableStateOf(userProfile?.displayName ?: "") }
    var email by remember { mutableStateOf(userProfile?.email ?: "") }
    var selectedAvatar by remember { mutableStateOf(userProfile?.profileImageUrl ?: "") }
    var isEditing by remember { mutableStateOf(false) }

    // Fetch profile if it's null (e.g., on app start)
    LaunchedEffect(Unit) {
        if (userProfile == null) {
            userVM.fetchProfile()
        }
    }

    // Sync local state when profile is fetched
    LaunchedEffect(userProfile) {
        userProfile?.let {
            displayName = it.displayName
            email = it.email
            selectedAvatar = it.profileImageUrl ?: ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (userState is UserState.Loading && userProfile == null) {
                CircularProgressIndicator()
            } else if (userProfile != null) {
                // Avatar Display
                AsyncImage(
                    model = selectedAvatar,
                    imageLoader = imageLoader,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(16.dp))
                
                // Milestone 3: Display Points and Badges
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.secondaryContainer)
                            ), 
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                        .defaultMinSize(minHeight = 48.dp)
                ) {
                    Icon(Icons.Default.Stars, contentDescription = "Points Icon", tint = Color(0xFFFFD700), modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${userProfile?.points ?: 0} Points",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                if (userProfile?.badges?.isNotEmpty() == true) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Achievement Badges", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Display badges side by side
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        userProfile!!.badges.forEach { badge ->
                            BadgeItem(badge)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (isEditing) {
                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        label = { Text("Display Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Choose Avatar", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Avatar Selection Grid
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(60.dp),
                        modifier = Modifier.height(140.dp),
                        contentPadding = PaddingValues(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(userVM.prebuiltAvatars) { avatarUrl ->
                            AsyncImage(
                                model = avatarUrl,
                                imageLoader = imageLoader,
                                contentDescription = "Avatar option",
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = if (selectedAvatar == avatarUrl) 3.dp else 1.dp,
                                        color = if (selectedAvatar == avatarUrl) MaterialTheme.colorScheme.primary else Color.LightGray,
                                        shape = CircleShape
                                    )
                                    .clickable(onClickLabel = "Select this avatar") { selectedAvatar = avatarUrl },
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                userVM.updateProfile(displayName, email, selectedAvatar)
                                isEditing = false
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save Changes")
                        }
                        OutlinedButton(
                            onClick = {
                                isEditing = false
                                // Reset local state
                                displayName = userProfile?.displayName ?: ""
                                email = userProfile?.email ?: ""
                                selectedAvatar = userProfile?.profileImageUrl ?: ""
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "Name", style = MaterialTheme.typography.labelMedium)
                            Text(text = displayName, style = MaterialTheme.typography.titleLarge)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Email", style = MaterialTheme.typography.labelMedium)
                            Text(text = email, style = MaterialTheme.typography.bodyLarge)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { isEditing = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Edit Profile")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = onNavigateToVisitedList,
                        modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = "List Icon")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("View Visited Artworks")
                    }
                }

                if (userState is UserState.Loading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator()
                }

                if (userState is UserState.Error) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = (userState as UserState.Error).message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } else if (userState is UserState.Error) {
                Text(
                    text = (userState as UserState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun BadgeItem(badgeName: String) {
    val (color, label) = when (badgeName) {
        "Archivist" -> Color(0xFF9C27B0) to "Archivist"
        "Curator" -> Color(0xFF2196F3) to "Curator"
        else -> Color(0xFF4CAF50) to "Explorer"
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(color.copy(alpha = 0.2f), CircleShape)
                .border(2.dp, color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Stars,
                contentDescription = "$label Badge",
                tint = color,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
