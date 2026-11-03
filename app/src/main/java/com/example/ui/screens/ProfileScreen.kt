package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassTextField
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassOutlinedButton
import com.example.ui.util.LiquidHapticType
import com.example.ui.util.rememberLiquidHaptics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.ui.theme.AvatarBg
import com.example.viewmodel.AuthViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel? = null,
    onLogout: () -> Unit = {},
    isDarkTheme: Boolean = false,
    toggleTheme: () -> Unit = {}
) {
    val currentUserId by viewModel?.currentUserId?.collectAsStateWithLifecycle(initialValue = null) ?: remember { mutableStateOf(null) }
    val userProfile by viewModel?.userProfile?.collectAsStateWithLifecycle(initialValue = null) ?: remember { mutableStateOf(null) }
    
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    val haptics = rememberLiquidHaptics()

    // Edit states
    var editName by remember { mutableStateOf("") }
    var editCollege by remember { mutableStateOf("") }
    var editCourse by remember { mutableStateOf("") }
    var editStudyGoal by remember { mutableStateOf("") }
    var editProfilePictureUri by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(isEditing) {
        if (isEditing) {
            editName = userProfile?.name ?: ""
            editCollege = userProfile?.college ?: ""
            editCourse = userProfile?.course ?: ""
            editStudyGoal = userProfile?.studyGoal ?: ""
            editProfilePictureUri = userProfile?.profilePictureUri
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            editProfilePictureUri = uri.toString()
        }
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out?") },
            confirmButton = {
                TextButton(onClick = {
                    showSignOutDialog = false
                    viewModel?.logout()
                    onLogout()
                }) {
                    Text("Sign Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("About Study Nova") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Study Nova", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("An AI-powered study assistant designed to help students organize study materials, generate summaries, create quizzes and flashcards, track progress, and learn with an AI Tutor.")
                    HorizontalDivider()
                    Text("Version 1.0.0", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (currentUserId == null && viewModel?.uiState?.value != com.example.viewmodel.AuthUiState.Idle) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(androidx.compose.ui.graphics.Color.Transparent)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text("Profile", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(24.dp))
        
        if (isEditing) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (editProfilePictureUri != null) {
                    AsyncImage(
                        model = editProfilePictureUri,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit Avatar", modifier = Modifier.size(32.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            GlassTextField(
                value = editName,
                onValueChange = { editName = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            GlassTextField(
                value = editCollege,
                onValueChange = { editCollege = it },
                label = { Text("College / School") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            GlassTextField(
                value = editCourse,
                onValueChange = { editCourse = it },
                label = { Text("Course / Department") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            GlassTextField(
                value = editStudyGoal,
                onValueChange = { editStudyGoal = it },
                label = { Text("Study Goal") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                GlassOutlinedButton(
                    onClick = { 
                        haptics.perform(LiquidHapticType.LightClick)
                        isEditing = false 
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                GlassButton(
                    onClick = {
                        if (editName.isNotBlank()) {
                            haptics.perform(LiquidHapticType.Success)
                            viewModel?.updateProfile(editName, editCollege, editCourse, editStudyGoal, editProfilePictureUri)
                            isEditing = false
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save")
                }
            }
        } else {
            val isGuest = currentUserId == "guest_user"
            val displayName = if (isGuest) "Guest Student" else (userProfile?.name ?: "Student Name")
            val displayEmail = if (isGuest) "guest@offline.local" else (userProfile?.email ?: "student@example.com")
            
            val isDark = androidx.compose.foundation.isSystemInDarkTheme()
            Box(
                modifier = Modifier
                    .size(104.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (!isGuest && userProfile?.profilePictureUri != null) {
                    AsyncImage(
                        model = userProfile?.profilePictureUri,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = "Profile",
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(52.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(displayName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(displayEmail, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            if (!userProfile?.college.isNullOrBlank() || !userProfile?.course.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                val details = listOfNotNull(
                    userProfile?.college?.takeIf { it.isNotBlank() },
                    userProfile?.course?.takeIf { it.isNotBlank() }
                ).joinToString(" • ")
                Text(details, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            if (!userProfile?.studyGoal.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Study Goal", fontWeight = FontWeight.Bold)
                        Text(userProfile?.studyGoal ?: "", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            
            if (userProfile?.createdAt != null) {
                Spacer(modifier = Modifier.height(8.dp))
                val joinedDate = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(userProfile!!.createdAt))
                Text("Joined $joinedDate", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Account Section
            Text("Account", modifier = Modifier.fillMaxWidth(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    ListItem(
                        headlineContent = { Text("Edit Profile") },
                        leadingContent = { Icon(Icons.Filled.Edit, contentDescription = null, tint = Color(0xFF2563EB)) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable { 
                            haptics.perform(LiquidHapticType.LightClick)
                            isEditing = true 
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    ListItem(
                        headlineContent = { Text("Sign Out", color = MaterialTheme.colorScheme.error) },
                        leadingContent = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable { 
                            haptics.perform(LiquidHapticType.LightClick)
                            showSignOutDialog = true 
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Settings Section
            Text("Settings", modifier = Modifier.fillMaxWidth(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    ListItem(
                        headlineContent = { Text("Dark Mode") },
                        leadingContent = { Icon(Icons.Filled.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        trailingContent = {
                            Switch(
                                checked = isDarkTheme,
                                onCheckedChange = { 
                                    haptics.perform(LiquidHapticType.Toggle)
                                    toggleTheme() 
                                }
                            )
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    ListItem(
                        headlineContent = { Text("About Study Nova") },
                        leadingContent = { Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable { 
                            haptics.perform(LiquidHapticType.LightClick)
                            showAboutDialog = true 
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
