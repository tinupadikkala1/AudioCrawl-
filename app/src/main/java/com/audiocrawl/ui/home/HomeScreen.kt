package com.audiocrawl.ui.home

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.audiocrawl.ui.components.AudioCrawlTopAppBar
import com.audiocrawl.ui.components.PermissionRequester
import com.audiocrawl.ui.theme.AtmosphereBlue
import com.audiocrawl.ui.theme.EngineEmerald
import com.audiocrawl.ui.theme.ObsidianContainer
import com.audiocrawl.ui.theme.ObsidianContainerHigh
import com.audiocrawl.ui.theme.ObsidianContainerLow
import com.audiocrawl.ui.theme.ObsidianDelimiter
import com.audiocrawl.ui.theme.ObsidianSurface
import com.audiocrawl.ui.theme.ObsidianVoid
import com.audiocrawl.ui.theme.SpectralCyan
import com.audiocrawl.ui.theme.SpectralCyanDim
import com.audiocrawl.ui.theme.TextBody
import com.audiocrawl.ui.theme.TextDim
import com.audiocrawl.ui.theme.TextMuted
import com.audiocrawl.ui.theme.TextPhosphor

@Composable
fun HomeScreen(
    onNavigateToScan: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val scanConfig by viewModel.scanConfig.collectAsStateWithLifecycle()

    PermissionRequester(onPermissionsGranted = {}) {
        Scaffold(
            containerColor = ObsidianVoid,
            topBar = {
                AudioCrawlTopAppBar(
                    title = "AUDIOCRAWL",
                    subtitle = "V2.4 ONLINE",
                    actions = {
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = TextPhosphor
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Spacer(modifier = Modifier.height(2.dp))

                // Hero Telemetry Badge Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, ObsidianDelimiter, RoundedCornerShape(8.dp))
                        .background(ObsidianContainerLow)
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(EngineEmerald)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "7-LAYER FILTER ENGINE READY",
                                style = MaterialTheme.typography.labelSmall,
                                color = EngineEmerald,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "/storage/emulated/0",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Central Hero Radial Scan Action Trigger (Obsidian Signal radar design)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, ObsidianDelimiter, RoundedCornerShape(12.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(ObsidianContainer, ObsidianSurface)
                            )
                        )
                        .padding(vertical = 28.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Pulse radar glow ring animation
                        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                        val pulseScale by infiniteTransition.animateFloat(
                            initialValue = 1f,
                            targetValue = 1.08f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1400, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "pulseScale"
                        )

                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .border(2.dp, SpectralCyan.copy(alpha = 0.35f), CircleShape)
                                .background(SpectralCyan.copy(alpha = 0.08f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, SpectralCyan, CircleShape)
                                    .background(ObsidianVoid)
                                    .clickable {
                                        viewModel.startScanning()
                                        onNavigateToScan()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = "Start Deep Scan",
                                    tint = SpectralCyan,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "START DEEP CRAWL",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPhosphor,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Isolate music tracks • Strip speech & voice clips",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                    }
                }

                // 7-Layer Filter Strip Badges
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, ObsidianDelimiter, RoundedCornerShape(8.dp))
                        .background(ObsidianContainerLow)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ACTIVE FILTRATION PIPELINE",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                        Text(
                            text = "7/7 ACTIVE",
                            style = MaterialTheme.typography.labelSmall,
                            color = SpectralCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("SQL", "EXT", "AMR", "PATH", "REGEX", "TIME", "CUSTOM").forEach { layer ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .border(1.dp, ObsidianDelimiter, RoundedCornerShape(4.dp))
                                    .background(ObsidianContainer)
                                    .padding(horizontal = 7.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = layer,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = EngineEmerald,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Settings Cards in Obsidian Pod style
                // 1. SD Card Toggle
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, ObsidianDelimiter, RoundedCornerShape(8.dp))
                        .background(ObsidianContainerLow)
                        .padding(14.dp)
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
                            Icon(
                                imageVector = Icons.Default.SdCard,
                                contentDescription = null,
                                tint = SpectralCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Scan Removable SD Storage",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPhosphor,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Traverse secondary filesystem trees",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextMuted
                                )
                            }
                        }
                        Switch(
                            checked = scanConfig.includeSdCard,
                            onCheckedChange = { viewModel.toggleIncludeSdCard(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ObsidianVoid,
                                checkedTrackColor = SpectralCyan,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = ObsidianContainerHigh
                            )
                        )
                    }
                }

                // 2. Minimum Duration Threshold Slider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, ObsidianDelimiter, RoundedCornerShape(8.dp))
                        .background(ObsidianContainerLow)
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = SpectralCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Duration Noise Gate",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPhosphor,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            val currentSeconds = (scanConfig.minDurationMs / 1000).toInt()
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .border(1.dp, SpectralCyanDim.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                    .background(ObsidianContainer)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "≥ ${currentSeconds}s",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SpectralCyan
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Rejects quick audio clips, chimes, and voice memos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val currentSeconds = (scanConfig.minDurationMs / 1000).toInt()
                        var sliderPosition by remember(scanConfig.minDurationMs) {
                            mutableFloatStateOf(currentSeconds.toFloat())
                        }

                        Slider(
                            value = sliderPosition,
                            onValueChange = { sliderPosition = it },
                            onValueChangeFinished = {
                                viewModel.updateMinDuration(sliderPosition.toInt())
                            },
                            valueRange = 10f..120f,
                            steps = 10,
                            colors = SliderDefaults.colors(
                                thumbColor = SpectralCyan,
                                activeTrackColor = SpectralCyan,
                                inactiveTrackColor = ObsidianContainerHigh
                            )
                        )
                    }
                }

                // 3. Vault Output Target
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, ObsidianDelimiter, RoundedCornerShape(8.dp))
                        .background(ObsidianContainerLow)
                        .padding(14.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = SpectralCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Vault Destination",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPhosphor,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = scanConfig.targetFolderName,
                            onValueChange = { viewModel.updateTargetFolderName(it) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = TextPhosphor,
                                fontFamily = FontFamily.Monospace
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SpectralCyan,
                                unfocusedBorderColor = ObsidianDelimiter,
                                focusedContainerColor = ObsidianVoid,
                                unfocusedContainerColor = ObsidianVoid
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Path: /storage/emulated/0/Music/${scanConfig.targetFolderName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Navigation to Advanced Filter Rules
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, ObsidianDelimiter, RoundedCornerShape(8.dp))
                        .background(ObsidianContainerLow)
                        .clickable(onClick = onNavigateToSettings)
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = AtmosphereBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Inspect 40+ Blacklist Rules",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPhosphor,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${scanConfig.excludedFolders.size} default excluded directories armed",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextMuted
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
