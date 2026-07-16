package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LudoLanguage
import com.example.model.LudoTranslations
import com.example.model.GamePhase
import com.example.model.LudoColor
import com.example.model.LudoTheme
import com.example.model.LudoTokenStyle
import com.example.model.LudoDiceStyle
import com.example.model.LudoGameMode
import com.example.model.LudoViewModel
import com.example.model.PlayerType
import com.example.model.AdType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LudoMenu(
    viewModel: LudoViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "redeem_pulse")
    val redeemScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "redeem_scale"
    )
    val claimScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "claim_scale"
    )

    var showRulesDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showNoInternetDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showShopDialog by remember { mutableStateOf(false) }
    var newNameInput by remember { mutableStateOf("") }
    val context = androidx.compose.ui.platform.LocalContext.current

    // Vibrant cosmic background gradient for a high-quality gaming aesthetic
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1E1B4B), // Deep Dark Indigo
            Color(0xFF311B92), // Cosmic Purple
            Color(0xFF0F172A)  // Slate Black
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 60.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row - Balanced Symmetrical Layout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Volume and Language
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.toggleSound() },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0x22FFFFFF), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (uiState.isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = "Toggle Sound",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { showLanguageDialog = true },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0x22FFFFFF), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Select Language",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Right: Rules and Settings
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { showRulesDialog = true },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0x22FFFFFF), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = "Rules & Guide",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0x22FFFFFF), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- EXTREMELY COMPACT USER PROFILE & COIN HUB (ABOVE TITLE) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .background(Color(0xFF1E293B).copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Avatar, Username, Coins Info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Avatar Circle (Small & Sleek)
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6))
                                ),
                                shape = CircleShape
                            )
                            .border(1.dp, Color(0xFFFFD700), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (uiState.username.isNotEmpty()) uiState.username.first().uppercase() else "L",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }

                    // Username & Coins badge
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.clickable {
                                newNameInput = uiState.username
                                showRenameDialog = true
                            }
                        ) {
                            Text(
                                text = uiState.username,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = LudoTranslations.getTranslation("edit_username", uiState.selectedLanguage),
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(11.dp)
                            )
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = "🪙", fontSize = 11.sp)
                            Text(
                                text = "${uiState.coins}",
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Right: Mini Buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Daily Check-in Button (Mini)
                    val dailyBtnBg = if (uiState.isDailyRewardAvailable) {
                        Brush.horizontalGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706)))
                    } else {
                        Brush.horizontalGradient(listOf(Color(0xFF334155), Color(0xFF334155)))
                    }
                    
                    val finalClaimScale = if (uiState.isDailyRewardAvailable) claimScale else 1f
                    
                    Box(
                        modifier = Modifier
                            .scale(finalClaimScale)
                            .clip(RoundedCornerShape(6.dp))
                            .background(dailyBtnBg)
                            .clickable(enabled = uiState.isDailyRewardAvailable) {
                                viewModel.claimDailyReward()
                            }
                            .padding(horizontal = 8.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (uiState.isDailyRewardAvailable) {
                                "🎁 Claim"
                            } else {
                                "Claimed ✓"
                            },
                            color = if (uiState.isDailyRewardAvailable) Color.White else Color.White.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }

                    // Watch Ad Button (Mini)
                    val now = System.currentTimeMillis()
                    val isVideoCooldownActive = uiState.videoCooldownEndTime > 0L && now < uiState.videoCooldownEndTime
                    val videoBtnBg = if (isVideoCooldownActive) {
                        Brush.horizontalGradient(listOf(Color(0xFF334155), Color(0xFF334155)))
                    } else {
                        Brush.horizontalGradient(listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8)))
                    }
                    val finalRedeemScale = if (isVideoCooldownActive) 1f else redeemScale
                    
                    Box(
                        modifier = Modifier
                            .scale(finalRedeemScale)
                            .clip(RoundedCornerShape(6.dp))
                            .background(videoBtnBg)
                            .clickable(enabled = !isVideoCooldownActive) {
                                viewModel.triggerAd(AdType.WATCH_AD)
                            }
                            .padding(horizontal = 8.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            if (isVideoCooldownActive) {
                                val cooldownSecs = ((uiState.videoCooldownEndTime - now) / 1000).toInt().coerceAtLeast(0)
                                val mins = cooldownSecs / 60
                                val secs = cooldownSecs % 60
                                val videoTimeText = String.format("%02d:%02d", mins, secs)
                                Text(
                                    text = "⏳ $videoTimeText",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(11.dp)
                                )
                                Text(
                                    text = "+500 🪙 (${uiState.videoUseCount}/2)",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    // Shop Button (Mini)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Brush.horizontalGradient(listOf(Color(0xFFE11D48), Color(0xFFBE123C))))
                            .clickable {
                                showShopDialog = true
                            }
                            .padding(horizontal = 8.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = LudoTranslations.getTranslation("shop_title", uiState.selectedLanguage),
                                tint = Color.White,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = "🛒 Shop",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Centered Main Title with tiny Crown strictly above the text (Compact & Elegant)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            ) {
                Text(
                    text = "👑",
                    fontSize = 12.sp, // Extremely small crown
                    modifier = Modifier.padding(bottom = 1.dp)
                )
                Text(
                    text = LudoTranslations.getTranslation("title", uiState.selectedLanguage).uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp,
                        color = Color(0xFFFFD700),
                        fontSize = 14.sp
                    ),
                    textAlign = TextAlign.Center
                )
            }

            // Rename Username Dialog
            if (showRenameDialog) {
                AlertDialog(
                    onDismissRequest = { showRenameDialog = false },
                    title = {
                        Text(
                            text = LudoTranslations.getTranslation("enter_name_title", uiState.selectedLanguage),
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    text = {
                        OutlinedTextField(
                            value = newNameInput,
                            onValueChange = { if (it.length <= 15) newNameInput = it },
                            placeholder = {
                                Text(
                                    text = LudoTranslations.getTranslation("enter_name_placeholder", uiState.selectedLanguage),
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFFFD700),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (newNameInput.trim().isNotEmpty()) {
                                    viewModel.updateUsername(newNameInput)
                                    showRenameDialog = false
                                }
                            }
                        ) {
                            Text(
                                text = LudoTranslations.getTranslation("save", uiState.selectedLanguage),
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showRenameDialog = false }) {
                            Text(
                                text = LudoTranslations.getTranslation("cancel", uiState.selectedLanguage),
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    },
                    containerColor = Color(0xFF1E1B4B),
                    titleContentColor = Color.White,
                    textContentColor = Color.White
                )
            }

            // Theme & Token Shop Dialog
            if (showShopDialog) {
                var shopError by remember { mutableStateOf<String?>(null) }
                var activeShopTab by remember { mutableStateOf(0) } // 0: Themes, 1: Tokens, 2: Dice

                AlertDialog(
                    onDismissRequest = { 
                        showShopDialog = false 
                        shopError = null
                    },
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("🛍️", fontSize = 20.sp)
                            Text(
                                text = LudoTranslations.getTranslation("shop_title", uiState.selectedLanguage),
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 18.sp
                            )
                        }
                    },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 420.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // User's current coins badge
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0x1AFFFFFF), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Your Balance:",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                )
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("🪙", fontSize = 14.sp)
                                    Text(
                                        text = "${uiState.coins}",
                                        color = Color(0xFFFFD700),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp
                                    )
                                }
                            }

                            // Custom Interactive Tab Selector for Shop Sections
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0x0FFFFFFF), RoundedCornerShape(10.dp))
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                val tabs = listOf("🎨 Themes", "📍 Tokens", "🎲 Dice")
                                tabs.forEachIndexed { index, title ->
                                    val isSelected = activeShopTab == index
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) Color(0xFF3B82F6) else Color.Transparent)
                                            .clickable { 
                                                activeShopTab = index 
                                                shopError = null
                                            }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }

                            if (shopError != null) {
                                Text(
                                    text = shopError!!,
                                    color = Color(0xFFEF4444),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            // Scrollable list container for shop items based on the active tab
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f, fill = false)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                when (activeShopTab) {
                                    0 -> {
                                        // Tab 0: Themes (8 items)
                                        LudoTheme.values().forEach { theme ->
                                            val isUnlocked = uiState.unlockedThemes.contains(theme)
                                            val isSelected = uiState.selectedTheme == theme
                                            val cost = viewModel.getThemeCost(theme)

                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .border(
                                                        width = if (isSelected) 1.5.dp else 0.5.dp,
                                                        color = if (isSelected) Color(0xFFFFD700) else Color.White.copy(alpha = 0.1f),
                                                        shape = RoundedCornerShape(12.dp)
                                                    ),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (isSelected) Color(0xFF1E293B) else Color(0xFF0F172A)
                                                ),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(10.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    // Left info
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                        ) {
                                                            val emoji = when (theme) {
                                                                LudoTheme.CLASSIC -> "🪵"
                                                                LudoTheme.COSMIC -> "🌌"
                                                                LudoTheme.ROYAL -> "👑"
                                                                LudoTheme.FOREST -> "🌲"
                                                                LudoTheme.CANDY -> "🍬"
                                                                LudoTheme.OCEAN -> "🌊"
                                                                LudoTheme.CYBERPUNK -> "⚡"
                                                                LudoTheme.EGYPT -> "🏺"
                                                            }
                                                            Text(emoji, fontSize = 16.sp)
                                                            Text(
                                                                text = theme.displayName,
                                                                color = Color.White,
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 13.sp
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                        Text(
                                                            text = "Token: ${theme.pawnName}",
                                                            color = Color.White.copy(alpha = 0.6f),
                                                            fontSize = 10.sp
                                                        )
                                                        Text(
                                                            text = "Dice: ${theme.diceName}",
                                                            color = Color.White.copy(alpha = 0.6f),
                                                            fontSize = 10.sp
                                                        )
                                                    }

                                                    // Right button / status
                                                    if (isUnlocked) {
                                                        if (isSelected) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .clip(RoundedCornerShape(6.dp))
                                                                    .background(Color(0x2210B981))
                                                                    .border(1.dp, Color(0xFF10B981), RoundedCornerShape(6.dp))
                                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                                            ) {
                                                                Text(
                                                                    text = "Active ✓",
                                                                    color = Color(0xFF10B981),
                                                                    fontWeight = FontWeight.Bold,
                                                                    fontSize = 11.sp
                                                                )
                                                            }
                                                        } else {
                                                            Button(
                                                                onClick = { 
                                                                    viewModel.selectTheme(theme)
                                                                    shopError = null
                                                                },
                                                                colors = ButtonDefaults.buttonColors(
                                                                    containerColor = Color(0xFF3B82F6)
                                                                ),
                                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                                modifier = Modifier.height(28.dp),
                                                                shape = RoundedCornerShape(6.dp)
                                                            ) {
                                                                Text(
                                                                    text = LudoTranslations.getTranslation("use_btn", uiState.selectedLanguage),
                                                                    color = Color.White,
                                                                    fontWeight = FontWeight.Bold,
                                                                    fontSize = 11.sp
                                                                )
                                                            }
                                                        }
                                                    } else {
                                                        Button(
                                                            onClick = {
                                                                if (uiState.coins >= cost) {
                                                                    viewModel.unlockTheme(theme)
                                                                    shopError = null
                                                                } else {
                                                                    shopError = "Not enough coins! Watch sponsor ads to earn more."
                                                                }
                                                            },
                                                            colors = ButtonDefaults.buttonColors(
                                                                containerColor = Color(0xFFF59E0B)
                                                            ),
                                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                            modifier = Modifier.height(28.dp),
                                                            shape = RoundedCornerShape(6.dp)
                                                        ) {
                                                            Text(
                                                                text = "${cost} 🪙",
                                                                color = Color.White,
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 11.sp
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    1 -> {
                                        // Tab 1: Token Styles (6 items)
                                        LudoTokenStyle.values().forEach { tokenStyle ->
                                            val isUnlocked = uiState.unlockedTokenStyles.contains(tokenStyle)
                                            val isSelected = uiState.selectedTokenStyle == tokenStyle
                                            val cost = tokenStyle.cost

                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .border(
                                                        width = if (isSelected) 1.5.dp else 0.5.dp,
                                                        color = if (isSelected) Color(0xFFFFD700) else Color.White.copy(alpha = 0.1f),
                                                        shape = RoundedCornerShape(12.dp)
                                                    ),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (isSelected) Color(0xFF1E293B) else Color(0xFF0F172A)
                                                ),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(10.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Text(tokenStyle.emoji, fontSize = 22.sp)
                                                        Column {
                                                            Text(
                                                                text = tokenStyle.displayName,
                                                                color = Color.White,
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 13.sp
                                                            )
                                                            Text(
                                                                text = "Custom game piece style",
                                                                color = Color.White.copy(alpha = 0.6f),
                                                                fontSize = 10.sp
                                                            )
                                                        }
                                                    }

                                                    if (isUnlocked) {
                                                        if (isSelected) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .clip(RoundedCornerShape(6.dp))
                                                                    .background(Color(0x2210B981))
                                                                    .border(1.dp, Color(0xFF10B981), RoundedCornerShape(6.dp))
                                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                                            ) {
                                                                Text(
                                                                    text = "Active ✓",
                                                                    color = Color(0xFF10B981),
                                                                    fontWeight = FontWeight.Bold,
                                                                    fontSize = 11.sp
                                                                )
                                                            }
                                                        } else {
                                                            Button(
                                                                onClick = { 
                                                                    viewModel.selectTokenStyle(tokenStyle)
                                                                    shopError = null
                                                                },
                                                                colors = ButtonDefaults.buttonColors(
                                                                    containerColor = Color(0xFF3B82F6)
                                                                ),
                                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                                modifier = Modifier.height(28.dp),
                                                                shape = RoundedCornerShape(6.dp)
                                                            ) {
                                                                Text(
                                                                    text = LudoTranslations.getTranslation("use_btn", uiState.selectedLanguage),
                                                                    color = Color.White,
                                                                    fontWeight = FontWeight.Bold,
                                                                    fontSize = 11.sp
                                                                )
                                                            }
                                                        }
                                                    } else {
                                                        Button(
                                                            onClick = {
                                                                if (uiState.coins >= cost) {
                                                                    viewModel.unlockTokenStyle(tokenStyle)
                                                                    shopError = null
                                                                } else {
                                                                    shopError = "Not enough coins! Watch sponsor ads to earn more."
                                                                }
                                                            },
                                                            colors = ButtonDefaults.buttonColors(
                                                                containerColor = Color(0xFFF59E0B)
                                                            ),
                                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                            modifier = Modifier.height(28.dp),
                                                            shape = RoundedCornerShape(6.dp)
                                                        ) {
                                                            Text(
                                                                text = "${cost} 🪙",
                                                                color = Color.White,
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 11.sp
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    2 -> {
                                        // Tab 2: Dice Styles (6 items)
                                        LudoDiceStyle.values().forEach { diceStyle ->
                                            val isUnlocked = uiState.unlockedDiceStyles.contains(diceStyle)
                                            val isSelected = uiState.selectedDiceStyle == diceStyle
                                            val cost = diceStyle.cost

                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .border(
                                                        width = if (isSelected) 1.5.dp else 0.5.dp,
                                                        color = if (isSelected) Color(0xFFFFD700) else Color.White.copy(alpha = 0.1f),
                                                        shape = RoundedCornerShape(12.dp)
                                                    ),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (isSelected) Color(0xFF1E293B) else Color(0xFF0F172A)
                                                ),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(10.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Text(diceStyle.emoji, fontSize = 22.sp)
                                                        Column {
                                                            Text(
                                                                text = diceStyle.displayName,
                                                                color = Color.White,
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 13.sp
                                                            )
                                                            Text(
                                                                text = "Custom dice style",
                                                                color = Color.White.copy(alpha = 0.6f),
                                                                fontSize = 10.sp
                                                            )
                                                        }
                                                    }

                                                    if (isUnlocked) {
                                                        if (isSelected) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .clip(RoundedCornerShape(6.dp))
                                                                    .background(Color(0x2210B981))
                                                                    .border(1.dp, Color(0xFF10B981), RoundedCornerShape(6.dp))
                                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                                            ) {
                                                                Text(
                                                                    text = "Active ✓",
                                                                    color = Color(0xFF10B981),
                                                                    fontWeight = FontWeight.Bold,
                                                                    fontSize = 11.sp
                                                                )
                                                            }
                                                        } else {
                                                            Button(
                                                                onClick = { 
                                                                    viewModel.selectDiceStyle(diceStyle)
                                                                    shopError = null
                                                                },
                                                                colors = ButtonDefaults.buttonColors(
                                                                    containerColor = Color(0xFF3B82F6)
                                                                ),
                                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                                modifier = Modifier.height(28.dp),
                                                                shape = RoundedCornerShape(6.dp)
                                                            ) {
                                                                Text(
                                                                    text = LudoTranslations.getTranslation("use_btn", uiState.selectedLanguage),
                                                                    color = Color.White,
                                                                    fontWeight = FontWeight.Bold,
                                                                    fontSize = 11.sp
                                                                )
                                                            }
                                                        }
                                                    } else {
                                                        Button(
                                                            onClick = {
                                                                if (uiState.coins >= cost) {
                                                                    viewModel.unlockDiceStyle(diceStyle)
                                                                    shopError = null
                                                                } else {
                                                                    shopError = "Not enough coins! Watch sponsor ads to earn more."
                                                                }
                                                            },
                                                            colors = ButtonDefaults.buttonColors(
                                                                containerColor = Color(0xFFF59E0B)
                                                            ),
                                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                            modifier = Modifier.height(28.dp),
                                                            shape = RoundedCornerShape(6.dp)
                                                        ) {
                                                            Text(
                                                                text = "${cost} 🪙",
                                                                color = Color.White,
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 11.sp
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = { 
                                showShopDialog = false 
                                shopError = null
                            }
                        ) {
                            Text(
                                text = "Close",
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    containerColor = Color(0xFF1E1B4B),
                    titleContentColor = Color.White,
                    textContentColor = Color.White
                )
            }

            if (uiState.gamePhase == GamePhase.MODE_SELECT) {
                Spacer(modifier = Modifier.height(8.dp))

                // Beautiful, compact Crown Ludo visual logo (perfectly fitting on all screens)
                Card(
                    modifier = Modifier
                        .size(110.dp)
                        .shadow(12.dp, RoundedCornerShape(20.dp))
                        .border(
                            width = 3.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFFFFF7ED), Color(0xFFF59E0B), Color(0xFFB45309))
                            ),
                            shape = RoundedCornerShape(20.dp)
                        ), // Golden Gradient Border
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // A miniature representation of 4 classic Ludo bases in the background
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(modifier = Modifier.weight(1f)) {
                                Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFFEF4444))) // Red Base
                                Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFF10B981))) // Green Base
                            }
                            Row(modifier = Modifier.weight(1f)) {
                                Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFF3B82F6))) // Blue Base
                                Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFFF59E0B))) // Yellow Base
                            }
                        }

                        // Central circular deep blue shield/emblem with a gold rim
                        Box(
                            modifier = Modifier
                                .size(84.dp)
                                .shadow(6.dp, CircleShape)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(Color(0xFF1E3A8A), Color(0xFF0F172A))
                                    ),
                                    shape = CircleShape
                                )
                                .border(
                                    width = 2.dp,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Color(0xFFFFE082), Color(0xFFD97706))
                                    ),
                                    shape = CircleShape
                                )
                                .align(Alignment.Center),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(bottom = 1.dp)
                            ) {
                                // 👑 Crown icon / emoji
                                Text(
                                    text = "👑",
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(bottom = 1.dp)
                                )
                                // Text "crown" in serif gold
                                Text(
                                    text = "crown",
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    color = Color(0xFFFFD700),
                                    letterSpacing = 0.5.sp,
                                    modifier = Modifier.padding(bottom = 1.dp)
                                )
                                // Divider line
                                Box(
                                    modifier = Modifier
                                        .width(36.dp)
                                        .height(1.dp)
                                        .background(
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(Color.Transparent, Color(0xFFFFD700), Color.Transparent)
                                            )
                                        )
                                )
                                Spacer(modifier = Modifier.height(1.dp))
                                // Text "Ludo" in colored letters with gold styling
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(text = "L", color = Color(0xFF60A5FA), fontWeight = FontWeight.Black, fontSize = 12.sp, style = LocalTextStyle.current.copy(shadow = androidx.compose.ui.graphics.Shadow(color = Color(0xFFB45309), offset = androidx.compose.ui.geometry.Offset(0.5f, 0.5f), blurRadius = 0.5f)))
                                    Text(text = "u", color = Color(0xFFF87171), fontWeight = FontWeight.Black, fontSize = 12.sp, style = LocalTextStyle.current.copy(shadow = androidx.compose.ui.graphics.Shadow(color = Color(0xFFB45309), offset = androidx.compose.ui.geometry.Offset(0.5f, 0.5f), blurRadius = 0.5f)))
                                    Text(text = "d", color = Color(0xFF34D399), fontWeight = FontWeight.Black, fontSize = 12.sp, style = LocalTextStyle.current.copy(shadow = androidx.compose.ui.graphics.Shadow(color = Color(0xFFB45309), offset = androidx.compose.ui.geometry.Offset(0.5f, 0.5f), blurRadius = 0.5f)))
                                    Text(text = "o", color = Color(0xFFFBBF24), fontWeight = FontWeight.Black, fontSize = 12.sp, style = LocalTextStyle.current.copy(shadow = androidx.compose.ui.graphics.Shadow(color = Color(0xFFB45309), offset = androidx.compose.ui.geometry.Offset(0.5f, 0.5f), blurRadius = 0.5f)))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (uiState.gamePhase == GamePhase.MODE_SELECT) {
                // Subtle Mode select tag - clean and extremely small to avoid scrolling
                Text(
                    text = LudoTranslations.getTranslation("title", uiState.selectedLanguage).uppercase() + " - MODE",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 1.sp,
                        fontSize = 11.sp
                    ),
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                // Beautiful 2x2 Grid of game modes - saves 220dp vertical height!
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // 1. Classic Game
                        GameModeCard(
                            title = LudoTranslations.getTranslation("classic_title", uiState.selectedLanguage),
                            subtitle = LudoTranslations.getTranslation("classic_desc", uiState.selectedLanguage),
                            icon = Icons.Default.Casino,
                            gradientColors = listOf(Color(0xFF10B981), Color(0xFF059669)), // Emerald Green
                            testTag = "mode_classic",
                            onClick = { viewModel.selectGameMode(LudoGameMode.CLASSIC) },
                            modifier = Modifier.weight(1f)
                        )

                        // 2. 1v1 Game
                        GameModeCard(
                            title = LudoTranslations.getTranslation("one_vs_one_title", uiState.selectedLanguage),
                            subtitle = LudoTranslations.getTranslation("one_vs_one_desc", uiState.selectedLanguage),
                            icon = Icons.Default.Bolt,
                            gradientColors = listOf(Color(0xFFD946EF), Color(0xFF8B5CF6)), // Purple/Magenta
                            testTag = "mode_1v1",
                            onClick = {
                                if (isInternetAvailable(context)) {
                                    viewModel.selectGameMode(LudoGameMode.ONE_VS_ONE)
                                } else {
                                    showNoInternetDialog = true
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // 3. Computer Game
                        GameModeCard(
                            title = LudoTranslations.getTranslation("computer_title", uiState.selectedLanguage),
                            subtitle = LudoTranslations.getTranslation("computer_desc", uiState.selectedLanguage),
                            icon = Icons.Default.Android,
                            gradientColors = listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8)), // Ocean Blue
                            testTag = "mode_computer",
                            onClick = {
                                viewModel.selectGameMode(LudoGameMode.VS_COMPUTER)
                            },
                            modifier = Modifier.weight(1f)
                        )

                        // 4. Team Up
                        GameModeCard(
                            title = LudoTranslations.getTranslation("team_up_title", uiState.selectedLanguage),
                            subtitle = LudoTranslations.getTranslation("team_up_desc", uiState.selectedLanguage),
                            icon = Icons.Default.Groups,
                            gradientColors = listOf(Color(0xFFF59E0B), Color(0xFFD97706)), // Orange/Gold
                            testTag = "mode_team_up",
                            onClick = { viewModel.selectGameMode(LudoGameMode.TEAM_UP) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            } else if (uiState.gamePhase == GamePhase.SETUP) {
                val selectedCount by viewModel.selectedPlayerCount.collectAsState()
                val selectedColor by viewModel.selectedUserColor.collectAsState()

                // Single unified glassmorphic console setup card containing all 3 selectors
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(12.dp, RoundedCornerShape(24.dp))
                        .border(1.dp, Color(0x33B19FFB), RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0x99241F55))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Section 1: PLAYERS (only for CLASSIC & VS_COMPUTER)
                        if (uiState.gameMode == LudoGameMode.CLASSIC || uiState.gameMode == LudoGameMode.VS_COMPUTER) {
                            Text(
                                text = LudoTranslations.getTranslation("players_count", uiState.selectedLanguage),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFFFD700),
                                    fontSize = 12.sp,
                                    letterSpacing = 1.sp
                                )
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val counts = listOf(2, 3, 4)
                                counts.forEachIndexed { index, count ->
                                    CompactPlayerCountOption(
                                        count = count,
                                        selected = selectedCount == count,
                                        onClick = { viewModel.selectedPlayerCount.value = count }
                                    )
                                    if (index < counts.size - 1) {
                                        Spacer(modifier = Modifier.width(16.dp))
                                    }
                                }
                            }
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0x1AFFFFFF)))
                        }

                        // Section 2: YOUR COLOR
                        Text(
                            text = LudoTranslations.getTranslation("your_color", uiState.selectedLanguage),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFFD700),
                                fontSize = 12.sp,
                                letterSpacing = 1.sp
                            )
                        )
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(LudoColor.BLUE, LudoColor.GREEN, LudoColor.RED, LudoColor.YELLOW).forEachIndexed { index, color ->
                                CompactColorOption(
                                    color = color,
                                    selected = selectedColor == color,
                                    onClick = { viewModel.selectedUserColor.value = color }
                                )
                                if (index < 3) {
                                    Spacer(modifier = Modifier.width(12.dp))
                                }
                            }
                        }

                        // Section 3: Wager Selector (For 1v1 and Computer modes!)
                        if (uiState.gameMode == LudoGameMode.ONE_VS_ONE || uiState.gameMode == LudoGameMode.VS_COMPUTER) {
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0x1AFFFFFF)))
                            Text(
                                text = "🪙 COIN ENTRY FEE / WAGER",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFFFD700),
                                    fontSize = 12.sp,
                                    letterSpacing = 1.sp
                                )
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val wagers = listOf(500, 1000, 5000, 10000, 50000)
                                wagers.forEach { wager ->
                                    val isSelected = uiState.selectedWagerAmount == wager
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) Color(0xFFF59E0B) else Color(0x1AFFFFFF))
                                            .clickable { viewModel.selectWagerAmount(wager) }
                                            .border(1.dp, if (isSelected) Color(0xFFFFD700) else Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${wager}",
                                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.8f),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Controls (Back button & PLAY button) side-by-side
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back button (Green, square with rounded corners)
                    IconButton(
                        onClick = { viewModel.resetToSetup() },
                        modifier = Modifier
                            .size(50.dp)
                            .shadow(8.dp, RoundedCornerShape(16.dp))
                            .background(Color(0xFF4CAF50), RoundedCornerShape(16.dp)) // Bright lime green
                            .border(1.5.dp, Color(0xFFC5E1A5), RoundedCornerShape(16.dp)),
                        content = {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    )

                    // PLAY button (Green, pill-shaped, glowing)
                    Button(
                        onClick = {
                            if (uiState.gameMode == LudoGameMode.ONE_VS_ONE && !isInternetAvailable(context)) {
                                showNoInternetDialog = true
                            } else {
                                viewModel.startGame()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .shadow(8.dp, RoundedCornerShape(25.dp))
                            .border(1.5.dp, Color(0xFFC5E1A5), RoundedCornerShape(25.dp))
                            .testTag("play_game_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50), // Vibrant green
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Text(
                            text = LudoTranslations.getTranslation("start_game", uiState.selectedLanguage).uppercase(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Error or Status Warning Messages
            AnimatedVisibility(
                visible = uiState.statusMessage.startsWith("⚠️") || uiState.statusMessage.startsWith("❌"),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = uiState.statusMessage,
                    color = Color(0xFFEF4444),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(
                    text = "👑 Developed by Kamar Pathan",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = Color(0xFFFFD700).copy(alpha = 0.9f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }

        // Rules Guide Dialog
        if (showRulesDialog) {
            AlertDialog(
                onDismissRequest = { showRulesDialog = false },
                title = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Help, contentDescription = "Rules", tint = Color(0xFFFBC02D))
                        Text(LudoTranslations.getTranslation("rules_title", uiState.selectedLanguage), fontWeight = FontWeight.Bold, color = Color.White)
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RuleRow("1", LudoTranslations.getTranslation("rules_1", uiState.selectedLanguage))
                        RuleRow("2", LudoTranslations.getTranslation("rules_2", uiState.selectedLanguage))
                        RuleRow("3", LudoTranslations.getTranslation("rules_3", uiState.selectedLanguage))
                        RuleRow("4", LudoTranslations.getTranslation("rules_4", uiState.selectedLanguage))
                        RuleRow("5", LudoTranslations.getTranslation("rules_5", uiState.selectedLanguage))
                        RuleRow("6", LudoTranslations.getTranslation("rules_6", uiState.selectedLanguage))
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showRulesDialog = false }) {
                        Text(LudoTranslations.getTranslation("got_it", uiState.selectedLanguage).uppercase(), fontWeight = FontWeight.Bold, color = Color(0xFFFBC02D))
                    }
                },
                containerColor = Color(0xFF1E293B),
                shape = RoundedCornerShape(16.dp)
            )
        }

        // Language Selection Dialog
        if (showLanguageDialog) {
            AlertDialog(
                onDismissRequest = { showLanguageDialog = false },
                title = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Language, contentDescription = "Language", tint = Color(0xFFFBC02D))
                        Text(LudoTranslations.getTranslation("choose_language", uiState.selectedLanguage), fontWeight = FontWeight.Bold, color = Color.White)
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        LudoLanguage.values().forEach { lang ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (uiState.selectedLanguage == lang) Color(0xFF4CAF50).copy(alpha = 0.2f) else Color.Transparent)
                                    .border(
                                        width = if (uiState.selectedLanguage == lang) 2.dp else 1.dp,
                                        color = if (uiState.selectedLanguage == lang) Color(0xFF4CAF50) else Color.White.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        viewModel.selectLanguage(lang)
                                        showLanguageDialog = false
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(lang.flag, fontSize = 24.sp)
                                    Column {
                                        Text(
                                            text = lang.countryName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = Color.White
                                        )
                                        Text(
                                            text = lang.label,
                                            fontSize = 12.sp,
                                            color = Color.White.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                                if (uiState.selectedLanguage == lang) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = Color(0xFF4CAF50)
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showLanguageDialog = false }) {
                        Text(LudoTranslations.getTranslation("got_it", uiState.selectedLanguage).uppercase(), fontWeight = FontWeight.Bold, color = Color(0xFFFBC02D))
                    }
                },
                containerColor = Color(0xFF1E293B),
                shape = RoundedCornerShape(16.dp)
            )
        }

        if (showSettingsDialog) {
            SettingsDialog(
                selectedLanguage = uiState.selectedLanguage,
                onDismissRequest = { showSettingsDialog = false }
            )
        }

        if (showNoInternetDialog) {
            AlertDialog(
                onDismissRequest = { showNoInternetDialog = false },
                title = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.WifiOff, contentDescription = "Offline", tint = Color(0xFFEF4444))
                        Text(
                            text = LudoTranslations.getTranslation("internet_required_title", uiState.selectedLanguage),
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                text = {
                    Text(
                        text = LudoTranslations.getTranslation("internet_required_desc", uiState.selectedLanguage),
                        color = Color(0xFFE2E8F0),
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showNoInternetDialog = false }) {
                        Text("OK", fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
                    }
                },
                containerColor = Color(0xFF1E293B),
                shape = RoundedCornerShape(16.dp)
            )
        }

        // Pinned Banner Ad at the bottom!
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            BannerAd()
        }

        // Interactive sponsor ad player overlay!
        if (uiState.adType != null) {
            AlertDialog(
                onDismissRequest = {},
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color(0xFFFFD700),
                            strokeWidth = 2.5.dp
                        )
                        Text(
                            text = LudoTranslations.getTranslation("watching_ad", uiState.selectedLanguage),
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = when (uiState.adType) {
                                AdType.GUARANTEED_SIX -> LudoTranslations.getTranslation("ad_guaranteed_six", uiState.selectedLanguage)
                                AdType.EXTEND_TIME -> LudoTranslations.getTranslation("ad_extend_time", uiState.selectedLanguage)
                                AdType.GAME_FINISH -> LudoTranslations.getTranslation("ad_game_finish", uiState.selectedLanguage)
                                AdType.GAME_START -> LudoTranslations.getTranslation("ad_game_start", uiState.selectedLanguage)
                                AdType.RESET -> LudoTranslations.getTranslation("ad_reset", uiState.selectedLanguage)
                                AdType.WATCH_AD -> LudoTranslations.getTranslation("ad_watch_ad", uiState.selectedLanguage)
                                else -> LudoTranslations.getTranslation("ad_watching", uiState.selectedLanguage)
                            },
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = LudoTranslations.getTranslation("reward_claims", uiState.selectedLanguage).replace("%d", uiState.adSecondsLeft.toString()),
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp,
                            color = Color(0xFFFFD700)
                        )

                        LinearProgressIndicator(
                            progress = { (5f - uiState.adSecondsLeft) / 5f },
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFFFFD700),
                            trackColor = Color.White.copy(alpha = 0.2f),
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.dismissAd() }) {
                        Text("Skip Ad", color = Color.White.copy(alpha = 0.6f))
                    }
                },
                containerColor = Color(0xFF1E1B4B),
                titleContentColor = Color.White,
                textContentColor = Color.White
            )
        }
    }
}

@Composable
fun GameModeCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradientColors: List<Color>,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(gradientColors))
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontSize = 13.sp,
                        letterSpacing = 0.5.sp
                    ),
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun PlayerTypeButton(
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit,
    tooltip: String
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) color else Color(0x13FFFFFF))
            .clickable(onClick = onClick)
            .border(
                width = 1.5.dp,
                color = if (selected) Color.White else Color(0x22FFFFFF),
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = tooltip,
            tint = if (selected) Color.White else Color.White.copy(alpha = 0.4f), // Outlined color
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun RuleRow(num: String, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(Color(0x33FFD700), CircleShape)
                .border(1.dp, Color(0xFFFFD700), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(num, fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color(0xFFFFD700))
        }
        Text(text, color = Color(0xFFE2E8F0), fontSize = 13.sp, modifier = Modifier.weight(1f))
    }
}

@Composable
fun CompactPlayerCountOption(
    count: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(76.dp)
            .height(34.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) Color(0xFFFFD700) else Color(0x13FFFFFF))
            .border(
                width = if (selected) 1.5.dp else 0.5.dp,
                color = if (selected) Color.White else Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${count} Players",
            fontWeight = FontWeight.Black,
            fontSize = 11.sp,
            color = if (selected) Color(0xFF1E1B4B) else Color.White
        )
    }
}

@Composable
fun CompactColorOption(
    color: LudoColor,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(color.value)
            .border(
                width = if (selected) 3.dp else 1.dp,
                color = if (selected) Color(0xFFFFD700) else Color.White.copy(alpha = 0.4f),
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = if (color == LudoColor.YELLOW) Color.Blue else Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun CompactThemeOption(
    theme: LudoTheme,
    selected: Boolean,
    onClick: () -> Unit
) {
    val themeBgColor = when (theme) {
        LudoTheme.CLASSIC -> Color(0xFFD3A370)
        LudoTheme.COSMIC -> Color(0xFF1E1B4B)
        LudoTheme.ROYAL -> Color(0xFFFFD700)
        LudoTheme.FOREST -> Color(0xFF1B4332)
        LudoTheme.CANDY -> Color(0xFFEC4899)
        LudoTheme.OCEAN -> Color(0xFF06B6D4)
        LudoTheme.CYBERPUNK -> Color(0xFF6366F1)
        LudoTheme.EGYPT -> Color(0xFFD97706)
    }
    val themeTextColor = if (theme == LudoTheme.ROYAL) Color.Black else Color.White
    val emoji = when (theme) {
        LudoTheme.CLASSIC -> "🪵"
        LudoTheme.COSMIC -> "🌌"
        LudoTheme.ROYAL -> "👑"
        LudoTheme.FOREST -> "🌲"
        LudoTheme.CANDY -> "🍬"
        LudoTheme.OCEAN -> "🌊"
        LudoTheme.CYBERPUNK -> "⚡"
        LudoTheme.EGYPT -> "🏺"
    }

    Box(
        modifier = Modifier
            .width(72.dp)
            .height(34.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(themeBgColor)
            .border(
                width = if (selected) 2.dp else 0.5.dp,
                color = if (selected) Color.White else Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(emoji, fontSize = 13.sp)
            Text(
                text = theme.displayName.split(" ").first(),
                fontWeight = FontWeight.Black,
                fontSize = 10.sp,
                color = themeTextColor
            )
        }
    }
}

@Composable
fun CompactLanguageOption(
    language: LudoLanguage,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Color(0xFF4CAF50) else Color(0x33FFFFFF))
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) Color.White else Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(language.flag, fontSize = 15.sp)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = language.countryName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = Color.White
                )
                Text(
                    text = language.label,
                    fontWeight = FontWeight.Normal,
                    fontSize = 9.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

fun isInternetAvailable(context: android.content.Context): Boolean {
    return try {
        val connectivityManager = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
    } catch (e: Exception) {
        true
    }
}

