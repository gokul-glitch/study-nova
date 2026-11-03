package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import com.example.ui.util.LiquidHapticType
import com.example.ui.util.rememberLiquidHaptics
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.SubjectEntity
import com.example.viewmodel.StudyMaterialViewModel
import com.example.viewmodel.SubjectsViewModel

import com.example.ui.components.GlassCard
import com.example.ui.components.GlassTextField
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassOutlinedButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectsScreen(
    subjectsViewModel: SubjectsViewModel?,
    studyViewModel: StudyMaterialViewModel?,
    onNavigateBack: () -> Unit
) {
    val subjects by subjectsViewModel?.subjects?.collectAsStateWithLifecycle(initialValue = emptyList()) ?: remember { mutableStateOf(emptyList()) }
    val allMaterials by studyViewModel?.allMaterialsUnfiltered?.collectAsStateWithLifecycle(initialValue = emptyList()) ?: remember { mutableStateOf(emptyList()) }

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<SubjectEntity?>(null) }
    var showDeleteDialog by remember { mutableStateOf<SubjectEntity?>(null) }
    val haptics = rememberLiquidHaptics()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = { Text("Manage Subjects", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    haptics.perform(LiquidHapticType.LightClick)
                    showAddDialog = true
                },
                containerColor = Color(0xFF2563EB),
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Subject")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "My Subjects",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Organize your study materials efficiently.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (subjects.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.Folder,
                            contentDescription = "Empty",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No subjects yet",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Tap + to create your first subject.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(subjects) { subject ->
                        val materialCount = allMaterials.count { it.subjectId == subject.id }
                        SubjectCard(
                            subject = subject,
                            materialCount = materialCount,
                            onEdit = { showEditDialog = subject },
                            onDelete = { showDeleteDialog = subject }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        SubjectDialog(
            title = "New Subject",
            onDismiss = { showAddDialog = false },
            onConfirm = { name, desc ->
                subjectsViewModel?.addSubject(name, desc)
                showAddDialog = false
            }
        )
    }

    showEditDialog?.let { subject ->
        SubjectDialog(
            title = "Edit Subject",
            initialName = subject.subjectName,
            initialDesc = subject.description ?: "",
            onDismiss = { showEditDialog = null },
            onConfirm = { name, desc ->
                subjectsViewModel?.editSubject(subject, name, desc)
                showEditDialog = null
            }
        )
    }

    showDeleteDialog?.let { subject ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Subject") },
            text = { Text("Are you sure you want to delete '${subject.subjectName}'? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    subjectsViewModel?.deleteSubject(subject)
                    showDeleteDialog = null
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SubjectCard(
    subject: SubjectEntity,
    materialCount: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val haptics = rememberLiquidHaptics()

    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = subject.subjectName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!subject.description.isNullOrBlank()) {
                    Text(
                        text = subject.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$materialCount study material${if (materialCount != 1) "s" else ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF2563EB)
                )
            }
            
            IconButton(
                onClick = {
                    haptics.perform(LiquidHapticType.LightClick)
                    onEdit()
                }
            ) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(
                onClick = {
                    haptics.perform(LiquidHapticType.LightClick)
                    onDelete()
                }
            ) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun SubjectDialog(
    title: String,
    initialName: String = "",
    initialDesc: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var description by remember { mutableStateOf(initialDesc) }
    val haptics = rememberLiquidHaptics()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                GlassTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Subject Name *") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                GlassTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    minLines = 3,
                    maxLines = 5,
                    singleLine = false
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    haptics.perform(LiquidHapticType.LightClick)
                    onConfirm(name, description)
                },
                enabled = name.isNotBlank()
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    haptics.perform(LiquidHapticType.LightClick)
                    onDismiss()
                }
            ) {
                Text("Cancel")
            }
        }
    )
}
