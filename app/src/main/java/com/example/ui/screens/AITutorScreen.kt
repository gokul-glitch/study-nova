package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import com.example.ui.theme.GlassMaterial
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.GlassTextField
import com.example.ui.util.LiquidHapticType
import com.example.ui.util.rememberLiquidHaptics
import com.example.viewmodel.AITutorViewModel
import com.example.viewmodel.ChatMessage
import com.example.viewmodel.TutorRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AITutorScreen(
    modifier: Modifier = Modifier, 
    viewModel: AITutorViewModel? = null,
    onNavigateBack: () -> Unit = {}
) {
    val subjects by viewModel?.subjects?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(emptyList()) }
    val materials by viewModel?.materials?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(emptyList()) }
    val selectedSubject by viewModel?.selectedSubject?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(null) }
    val selectedMaterial by viewModel?.selectedMaterial?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(null) }
    val selectedRole by viewModel?.selectedRole?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(TutorRole.ACADEMIC_RESEARCH) }
    val isSearchGroundingEnabled by viewModel?.isSearchGroundingEnabled?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(true) }
    val chatHistory by viewModel?.messages?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(emptyList()) }
    val isTyping by viewModel?.isLoading?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(false) }
    
    var inputText by remember { mutableStateOf("") }
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val haptics = rememberLiquidHaptics()
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val clipboardManager = LocalClipboardManager.current
    val listState = rememberLazyListState()

    // Auto-scroll smoothly when new messages arrive
    LaunchedEffect(chatHistory.size, isTyping) {
        val totalCount = chatHistory.size + if (isTyping) 1 else 0
        if (totalCount > 0) {
            listState.animateScrollToItem(totalCount - 1)
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6)))
                                )
                                .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.AutoAwesome, 
                                contentDescription = null, 
                                tint = Color.White, 
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("AI Study Tutor", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(
                                if (isSearchGroundingEnabled) "Google Search Grounded • gemini-2.5-flash"
                                else "${selectedRole.displayName} • ${selectedRole.modelName}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            haptics.perform(LiquidHapticType.Selection)
                            viewModel?.clearChat()
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF2563EB))
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "New Chat", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Chat", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 14.dp, vertical = 4.dp)
        ) {
            // Subject & Material Selectors
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                var subjectExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = subjectExpanded,
                    onExpandedChange = { subjectExpanded = !subjectExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    GlassTextField(
                        value = selectedSubject?.subjectName ?: "Subject: All",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        singleLine = true
                    )
                    ExposedDropdownMenu(expanded = subjectExpanded, onDismissRequest = { subjectExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text("All Subjects (General)") },
                            onClick = {
                                viewModel?.selectSubject(null)
                                viewModel?.selectMaterial(null)
                                subjectExpanded = false
                            }
                        )
                        subjects.forEach { subject ->
                            DropdownMenuItem(
                                text = { Text(subject.subjectName) },
                                onClick = {
                                    viewModel?.selectSubject(subject)
                                    viewModel?.selectMaterial(null)
                                    subjectExpanded = false
                                }
                            )
                        }
                    }
                }

                var materialExpanded by remember { mutableStateOf(false) }
                val filteredMaterials = if (selectedSubject != null) {
                    materials.filter { it.subjectId == selectedSubject?.id }
                } else {
                    materials
                }
                
                ExposedDropdownMenuBox(
                    expanded = materialExpanded,
                    onExpandedChange = { materialExpanded = !materialExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    GlassTextField(
                        value = selectedMaterial?.title ?: "Material (Optional)",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = materialExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        singleLine = true
                    )
                    ExposedDropdownMenu(expanded = materialExpanded, onDismissRequest = { materialExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text("None (Broad Search & Q&A)") },
                            onClick = {
                                viewModel?.selectMaterial(null)
                                materialExpanded = false
                            }
                        )
                        filteredMaterials.forEach { material ->
                            DropdownMenuItem(
                                text = { Text(material.title) },
                                onClick = {
                                    viewModel?.selectMaterial(material)
                                    materialExpanded = false
                                }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            // Tutor Role Selector Carousel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TutorRole.entries.forEach { role ->
                    val isSelected = selectedRole == role
                    val roleIcon = when (role) {
                        TutorRole.ACADEMIC_RESEARCH -> Icons.Filled.Search
                        TutorRole.STEM_EXAM_COACH -> Icons.Filled.Psychology
                        TutorRole.RAPID_REVISION -> Icons.Filled.Speed
                        TutorRole.SOCRATIC_MENTOR -> Icons.Filled.AutoAwesome
                    }
                    
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) {
                                    Brush.linearGradient(
                                        listOf(
                                            Color(0xFF2563EB).copy(alpha = if (isDark) 0.35f else 0.20f),
                                            Color(0xFF8B5CF6).copy(alpha = if (isDark) 0.30f else 0.15f)
                                        )
                                    )
                                } else {
                                    Brush.linearGradient(
                                        listOf(
                                            Color.White.copy(alpha = if (isDark) 0.05f else 0.40f),
                                            Color.White.copy(alpha = if (isDark) 0.02f else 0.25f)
                                        )
                                    )
                                }
                            )
                            .border(
                                width = if (isSelected) 1.2.dp else 0.5.dp,
                                color = if (isSelected) Color(0xFF3B82F6) else Color.White.copy(alpha = if (isDark) 0.18f else 0.60f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                haptics.perform(LiquidHapticType.Selection)
                                viewModel?.selectRole(role)
                            }
                            .padding(horizontal = 10.dp, vertical = 7.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                roleIcon,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (isSelected) Color(0xFF2563EB) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                role.displayName,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFF1E40AF) else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (isSelected) Color(0xFF2563EB).copy(alpha = 0.15f) 
                                        else Color.Black.copy(alpha = 0.05f)
                                    )
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    role.badgeLabel,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isSelected) Color(0xFF2563EB) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Google Search Grounding Toggle Bar
            val groundingBackgroundModifier = if (isSearchGroundingEnabled) {
                Modifier.background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF0284C7).copy(alpha = if (isDark) 0.22f else 0.12f),
                            Color(0xFF2563EB).copy(alpha = if (isDark) 0.18f else 0.08f),
                            Color(0xFF4F46E5).copy(alpha = if (isDark) 0.15f else 0.06f)
                        )
                    )
                )
            } else {
                Modifier.background(Color.White.copy(alpha = if (isDark) 0.04f else 0.35f))
            }

            val searchIconBackgroundModifier = if (isSearchGroundingEnabled) {
                Modifier.background(Brush.linearGradient(listOf(Color(0xFF0EA5E9), Color(0xFF2563EB))))
            } else {
                Modifier.background(Color.Gray.copy(alpha = 0.2f))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .then(groundingBackgroundModifier)
                    .border(
                        width = if (isSearchGroundingEnabled) 1.dp else 0.5.dp,
                        color = if (isSearchGroundingEnabled) Color(0xFF38BDF8).copy(alpha = 0.70f) else Color.White.copy(alpha = if (isDark) 0.12f else 0.50f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clickable {
                        haptics.perform(LiquidHapticType.Toggle)
                        viewModel?.toggleSearchGrounding()
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .then(searchIconBackgroundModifier),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Language,
                                contentDescription = "Google Search",
                                tint = if (isSearchGroundingEnabled) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Google Search Grounding",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF0EA5E9).copy(alpha = 0.15f))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text("gemini-2.5-flash", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
                                }
                            }
                            Text(
                                if (isSearchGroundingEnabled) "Grounds answers with real-time web citations"
                                else "Search disabled • using model knowledge only",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = isSearchGroundingEnabled,
                        onCheckedChange = {
                            haptics.perform(LiquidHapticType.Toggle)
                            viewModel?.setSearchGrounding(it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF0284C7),
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            uncheckedTrackColor = Color.Transparent
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            // Scrollable Chat Thread
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                // Empty state greeting
                if (chatHistory.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillParentMaxWidth()
                                .padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF0EA5E9).copy(alpha = 0.25f), Color(0xFF8B5CF6).copy(alpha = 0.25f))
                                        )
                                    )
                                    .border(1.dp, Color.White.copy(alpha = 0.7f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFF0284C7),
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                "Study Nova AI Assistant",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Ask anything with real-time Google Search grounding or choose a specialized tutor role.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 24.dp),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            
                            // Role suggestions
                            val suggestions = listOf(
                                "🔍 Latest scientific breakthroughs in quantum computing" to TutorRole.ACADEMIC_RESEARCH,
                                "📐 Solve this step-by-step with deep reasoning" to TutorRole.STEM_EXAM_COACH,
                                "⚡ Rapid bullet summary of cell division stages" to TutorRole.RAPID_REVISION,
                                "💡 Guide me through how neural networks learn" to TutorRole.SOCRATIC_MENTOR
                            )
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
                            ) {
                                suggestions.forEach { (prompt, role) ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.White.copy(alpha = if (isDark) 0.08f else 0.45f))
                                            .border(0.5.dp, Color.White.copy(alpha = if (isDark) 0.18f else 0.70f), RoundedCornerShape(12.dp))
                                            .clickable {
                                                haptics.perform(LiquidHapticType.Selection)
                                                viewModel?.selectRole(role)
                                                inputText = prompt.substring(2).trim()
                                            }
                                            .padding(horizontal = 14.dp, vertical = 10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                prompt,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF2563EB),
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text(
                                                role.badgeLabel,
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Chat History in standard top-to-bottom scrollable order
                items(chatHistory) { msg ->
                    ChatMessageItem(
                        msg = msg,
                        isDark = isDark,
                        onCopyText = { text ->
                            haptics.perform(LiquidHapticType.LightClick)
                            clipboardManager.setText(AnnotatedString(text))
                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        onOpenUrl = { url ->
                            haptics.perform(LiquidHapticType.LightClick)
                            try {
                                uriHandler.openUri(url)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Could not open URL", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onRetry = {
                            haptics.perform(LiquidHapticType.LightClick)
                            viewModel?.retryLastMessage()
                        }
                    )
                }

                // Typing indicator
                if (isTyping) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (isDark) Color(0xFF1E293B).copy(alpha = 0.85f) 
                                        else Color.White.copy(alpha = 0.80f)
                                    )
                                    .border(
                                        1.dp,
                                        Color.White.copy(alpha = if (isDark) 0.18f else 0.70f),
                                        RoundedCornerShape(20.dp)
                                    )
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp), 
                                        strokeWidth = 2.dp, 
                                        color = if (isSearchGroundingEnabled) Color(0xFF0EA5E9) else Color(0xFF3B82F6)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        if (isSearchGroundingEnabled) "Grounding with Google Search..." 
                                        else "${selectedRole.displayName} is thinking...", 
                                        fontSize = 12.sp, 
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Bottom Input Field
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(top = 6.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GlassTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { 
                        Text(
                            if (isSearchGroundingEnabled) "Ask anything (Google Search active)..." 
                            else "Ask ${selectedRole.displayName}..."
                        ) 
                    },
                    leadingIcon = {
                        Icon(
                            if (isSearchGroundingEnabled) Icons.Filled.Language else Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = if (isSearchGroundingEnabled) Color(0xFF0EA5E9) else Color(0xFF8B5CF6),
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        if (inputText.isNotBlank() && !isTyping) {
                            haptics.perform(LiquidHapticType.LightClick)
                            viewModel?.sendMessage(inputText)
                            inputText = ""
                        }
                    }),
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                if (inputText.isNotBlank() && !isTyping) {
                                    haptics.perform(LiquidHapticType.LightClick)
                                    viewModel?.sendMessage(inputText)
                                    inputText = ""
                                }
                            },
                            enabled = inputText.isNotBlank() && !isTyping
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = if (inputText.isNotBlank() && !isTyping) Color(0xFF2563EB) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ChatMessageItem(
    msg: ChatMessage,
    isDark: Boolean,
    onCopyText: (String) -> Unit,
    onOpenUrl: (String) -> Unit,
    onRetry: () -> Unit
) {
    val isUser = msg.isUser
    val isError = msg.text.startsWith("Error:") || msg.text.startsWith("API Error:")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (isUser) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.82f)
                    .shadow(
                        elevation = 2.dp, 
                        shape = RoundedCornerShape(20.dp, 4.dp, 20.dp, 20.dp)
                    )
                    .clip(RoundedCornerShape(20.dp, 4.dp, 20.dp, 20.dp))
                    .background(Color(0xFF2563EB))
                    .border(
                        1.dp,
                        Color(0xFF1D4ED8),
                        RoundedCornerShape(20.dp, 4.dp, 20.dp, 20.dp)
                    )
                    .padding(14.dp)
            ) {
                Text(
                    text = msg.text,
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .shadow(
                        elevation = 6.dp, 
                        shape = RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp), 
                        spotColor = Color(0xFF2563EB).copy(alpha = 0.08f)
                    )
                    .clip(RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp))
                    .background(
                        if (isError) {
                            MaterialTheme.colorScheme.errorContainer
                        } else {
                            if (isDark) Color(0xFF1E293B) else Color(0xFFFFFFFF)
                        }
                    )
                    .border(
                        1.dp,
                        if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0),
                        RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp)
                    )
                    .padding(14.dp)
            ) {
                Column {
                    // Header metadata: Role and Model
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFF3B82F6),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                msg.roleName ?: "Study Nova AI",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2563EB)
                            )
                        }

                        if (msg.modelUsed != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF3B82F6).copy(alpha = 0.10f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    msg.modelUsed,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Main Answer text
                    Text(
                        text = msg.text,
                        fontSize = 14.sp,
                        lineHeight = 21.sp,
                        color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )

                    // Grounding metadata section: Google Search Queries and Sources
                    if (msg.isGrounded || msg.sources.isNotEmpty() || msg.webSearchQueries.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0284C7).copy(alpha = if (isDark) 0.15f else 0.08f))
                                .border(0.5.dp, Color(0xFF38BDF8).copy(alpha = 0.40f), RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Filled.Language,
                                        contentDescription = "Search Grounding",
                                        tint = Color(0xFF0284C7),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Grounded with Google Search",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF0284C7)
                                    )
                                }

                                if (msg.webSearchQueries.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        msg.webSearchQueries.forEach { query ->
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color.White.copy(alpha = if (isDark) 0.12f else 0.60f))
                                                    .border(0.5.dp, Color(0xFF38BDF8).copy(alpha = 0.30f), RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    "🔍 $query",
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }

                                if (msg.sources.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        "Sources:",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        msg.sources.take(4).forEach { source ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color.White.copy(alpha = if (isDark) 0.10f else 0.50f))
                                                    .clickable { onOpenUrl(source.url) }
                                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    source.title,
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF0284C7),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Icon(
                                                    Icons.AutoMirrored.Filled.OpenInNew,
                                                    contentDescription = "Open Source",
                                                    tint = Color(0xFF0284C7),
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Action buttons (Copy and Retry)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { onCopyText(msg.text) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Filled.ContentCopy,
                                contentDescription = "Copy message",
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }

                        if (isError) {
                            TextButton(
                                onClick = onRetry,
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.height(24.dp)
                            ) {
                                Icon(Icons.Filled.Refresh, contentDescription = "Retry", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Retry", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}
