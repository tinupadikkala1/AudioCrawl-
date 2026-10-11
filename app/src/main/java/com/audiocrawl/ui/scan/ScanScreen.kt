package com.audiocrawl.ui.scan

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.audiocrawl.model.ScanState
import com.audiocrawl.ui.components.AudioCrawlTopAppBar
import com.audiocrawl.ui.theme.AtmosphereBlue
import com.audiocrawl.ui.theme.EngineEmerald
import com.audiocrawl.ui.theme.FilterRed
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
fun ScanScreen(
    onNavigateToResults: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: ScanViewModel = hiltViewModel()
) {
    val scanState by viewModel.scanState.collectAsStateWithLifecycle()

    LaunchedEffect(scanState) {
        if (scanState is ScanState.Complete) {
            onNavigateToResults()
        }
    }

    Scaffold(
        containerColor = ObsidianVoid,
        topBar = {
            AudioCrawlTopAppBar(
                title = "AUDIOCRAWL",
                subtitle = "DEEP_CRAWL_ACTIVE // LIVE",
                onBackClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when (val state = scanState) {
                is ScanState.Scanning -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Central FFT / Waveform Visualizer & Radial Gauge
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, ObsidianDelimiter, RoundedCornerShape(12.dp))
                                .background(ObsidianContainerLow)
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val infiniteTransition = rememberInfiniteTransition(label = "scanPulse")
                                val pulseGlow by infiniteTransition.animateFloat(
                                    initialValue = 0.8f,
                                    targetValue = 1.0f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(800, easing = FastOutSlowInEasing),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "pulseGlow"
                                )

                                Box(
                                    modifier = Modifier.size(130.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.fillMaxSize(),
                                        strokeWidth = 3.dp,
                                        color = SpectralCyan.copy(alpha = pulseGlow),
                                        trackColor = ObsidianDelimiter
                                    )
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "SCANNING",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SpectralCyan,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${state.filesFound + state.filesFiltered}",
                                            style = MaterialTheme.typography.headlineMedium,
                                            color = TextPhosphor,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "FILES CHECKED",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                            color = TextMuted
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Simulated spectral FFT bars
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.Bottom,
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    val barHeights = listOf(14.dp, 24.dp, 36.dp, 18.dp, 28.dp, 12.dp, 32.dp, 20.dp, 16.dp, 30.dp, 22.dp)
                                    barHeights.forEachIndexed { index, height ->
                                        Box(
                                            modifier = Modifier
                                                .width(4.dp)
                                                .height(height)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(if (index % 2 == 0) SpectralCyan else EngineEmerald)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "LIVE SPECTRAL & METADATA ANALYSIS",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = TextMuted
                                )
                            }
                        }

                        // Active Terminal File Inspector Pod
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, ObsidianDelimiter, RoundedCornerShape(8.dp))
                                .background(ObsidianVoid)
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "CURRENT TRAVERSAL NODE",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = TextMuted
                                    )
                                    Text(
                                        text = "I/O ACTIVE",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = EngineEmerald
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = state.currentFolder.ifBlank { "/storage/emulated/0" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPhosphor,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Telemetry Metrics Pods (2x2 Grid)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Genuine Music Found
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, EngineEmerald.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .background(ObsidianContainerLow)
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "PASSED TO VAULT",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = EngineEmerald
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${state.filesFound}",
                                        style = MaterialTheme.typography.headlineLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPhosphor,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = "Genuine Music",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextMuted
                                    )
                                }
                            }

                            // Non-Music Blocked
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, ObsidianDelimiter, RoundedCornerShape(8.dp))
                                    .background(ObsidianContainerLow)
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "CLIPPED & FILTERED",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = SpectralCyan
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${state.filesFiltered}",
                                        style = MaterialTheme.typography.headlineLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPhosphor,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = "Speech & App FX",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextMuted
                                    )
                                }
                            }
                        }

                        // Bottom Actions
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedButton(
                            onClick = {
                                viewModel.cancelScan()
                                onNavigateBack()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = TextPhosphor
                            )
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = FilterRed)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ABORT CRAWL", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                is ScanState.Complete -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, EngineEmerald.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .background(ObsidianContainerLow)
                            .padding(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = EngineEmerald
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "CRAWL COMPLETE",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPhosphor
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${state.totalFound} Genuine Tracks Isolated\n${state.totalFiltered} Non-Music Clipped",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onNavigateToResults,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SpectralCyan)
                        ) {
                            Text(
                                text = "VIEW VAULT STAGING",
                                fontWeight = FontWeight.Bold,
                                color = ObsidianVoid,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }

                is ScanState.Error -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, FilterRed.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .background(ObsidianContainerLow)
                            .padding(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = FilterRed
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "SCAN ABORTED / FAILED",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = FilterRed
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onNavigateBack,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("RETURN TO HOME")
                        }
                    }
                }

                is ScanState.Idle -> {
                    CircularProgressIndicator(color = SpectralCyan)
                }
            }
        }
    }
}
