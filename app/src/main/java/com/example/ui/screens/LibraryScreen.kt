package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.StudyMaterialEntity
import com.example.data.local.SubjectEntity
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassFilterChip
import com.example.ui.components.GlassTextField
import com.example.ui.theme.GlassMaterial
import com.example.ui.util.LiquidHapticType
import com.example.ui.util.rememberLiquidHaptics
import androidx.compose.ui.graphics.Brush
import com.example.viewmodel.StudyMaterialViewModel
import com.example.viewmodel.SubjectsViewModel
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: StudyMaterialViewModel?,
    subjectsViewModel: SubjectsViewModel?,
    onMaterialClick: ((Long) -> Unit)? = null,
    onNavigateToSummary: ((Long) -> Unit)? = null,
    onNavigateToSubjects: (() -> Unit)? = null,
    onStartQuiz: ((Long) -> Unit)? = null,
    onGenerateFlashcards: ((Long) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val handleOpenSummary = onNavigateToSummary ?: onMaterialClick ?: {}
    val materials by viewModel?.materials?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(emptyList()) }
    val subjects by subjectsViewModel?.subjects?.collectAsStateWithLifecycle(initialValue = emptyList()) ?: remember { mutableStateOf(emptyList()) }
    
    val searchQuery by viewModel?.searchQuery?.collectAsStateWithLifecycle() ?: remember { mutableStateOf("") }
    val selectedFilter by viewModel?.selectedFilter?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(0) }
    val selectedSubjectId by viewModel?.selectedSubjectId?.collectAsStateWithLifecycle() ?: remember { mutableStateOf<Long?>(null) }
    
    val context = LocalContext.current
    var materialToDelete by remember { mutableStateOf<StudyMaterialEntity?>(null) }

    if (materialToDelete != null) {
        AlertDialog(
            onDismissRequest = { materialToDelete = null },
            title = { Text("Delete Material", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '${materialToDelete?.title}'? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    materialToDelete?.let { viewModel?.deleteMaterial(it) }
                    materialToDelete = null
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { materialToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Study Library",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(14.dp))
            
            GlassTextField(
                value = searchQuery,
                onValueChange = { viewModel?.setSearchQuery(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search your study notes...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) }
            )
            
            Spacer(modifier = Modifier.height(14.dp))
            
            // Filters
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GlassFilterChip(selected = selectedFilter == 0, onClick = { viewModel?.setFilter(0) }, label = { Text("All") })
                GlassFilterChip(selected = selectedFilter == 1, onClick = { viewModel?.setFilter(1) }, label = { Text("Notes") })
                GlassFilterChip(selected = selectedFilter == 2, onClick = { viewModel?.setFilter(2) }, label = { Text("PDFs") })
            }
            
            Spacer(modifier = Modifier.height(10.dp))

            // Subjects Filter
            if (subjects.isNotEmpty()) {
                var expanded by remember { mutableStateOf(false) }
                val currentSubjectName = subjects.find { it.id == selectedSubjectId }?.subjectName ?: "All Subjects"

                Box {
                    AssistChip(
                        onClick = { expanded = true },
                        label = { Text(currentSubjectName) },
                        leadingIcon = { Icon(Icons.Filled.FilterList, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color.White.copy(alpha = 0.5f)
                        )
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Subjects") },
                            onClick = { 
                                viewModel?.setSubjectFilter(null)
                                expanded = false 
                            }
                        )
                        subjects.forEach { subject ->
                            DropdownMenuItem(
                                text = { Text(subject.subjectName) },
                                onClick = {
                                    viewModel?.setSubjectFilter(subject.id)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(14.dp))
            
            if (materials.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            tint = Color(0xFF3B82F6).copy(alpha = 0.6f),
                            modifier = Modifier.size(54.dp)
                        )
                        Text(
                            "Your library is empty",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Upload some notes or PDFs to get started.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(materials, key = { it.id }) { material ->
                        val subject = subjects.find { it.id == material.subjectId }
                        MaterialItemCard(
                            material = material,
                            subject = subject,
                            onOpen = { openFile(context, material.fileUri, material.fileType) },
                            onSummarize = { handleOpenSummary(material.id) },
                            onDelete = { materialToDelete = material }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
fun MaterialItemCard(
    material: StudyMaterialEntity,
    subject: SubjectEntity?,
    onOpen: () -> Unit,
    onSummarize: () -> Unit,
    onDelete: () -> Unit
) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val isPdf = material.fileType.contains("pdf", ignoreCase = true)
    val haptics = rememberLiquidHaptics()

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onOpen
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isPdf) Icons.Filled.PictureAsPdf else Icons.Filled.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(14.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = material.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatDate(material.createdAt),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "•",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatFileSize(material.fileSize),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                IconButton(
                    onClick = {
                        haptics.perform(LiquidHapticType.LightClick)
                        onDelete()
                    }
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.75f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(14.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (subject != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF3B82F6).copy(alpha = 0.12f))
                            .border(0.8.dp, Color(0xFF3B82F6).copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = subject.subjectName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isDark) Color(0xFF93C5FD) else Color(0xFF1D4ED8),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }
                
                Button(
                    onClick = {
                        haptics.perform(LiquidHapticType.LightClick)
                        onSummarize()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2563EB),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Summarize",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

private fun formatFileSize(size: Long): String {
    if (size <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
    return DecimalFormat("#,##0.#").format(size / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
}

private fun formatDate(timeInMillis: Long): String {
    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return formatter.format(Date(timeInMillis))
}

private fun openFile(context: Context, uriString: String, mimeType: String) {
    try {
        val uri = Uri.parse(uriString)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "No app found to open this file", Toast.LENGTH_SHORT).show()
    }
}

