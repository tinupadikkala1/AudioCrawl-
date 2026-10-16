package com.audiocrawl.ui.copy

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.audiocrawl.model.CopyState
import com.audiocrawl.model.DeleteState
import com.audiocrawl.ui.components.AudioCrawlTopAppBar
import com.audiocrawl.ui.theme.AtmosphereBlue
import com.audiocrawl.ui.theme.EngineEmerald
import com.audiocrawl.ui.theme.FilterRed
import com.audiocrawl.ui.theme.ObsidianContainer
import com.audiocrawl.ui.theme.ObsidianContainerLow
import com.audiocrawl.ui.theme.ObsidianDelimiter
import com.audiocrawl.ui.theme.ObsidianSurface
import com.audiocrawl.ui.theme.ObsidianVoid
import com.audiocrawl.ui.theme.SpectralCyan
import com.audiocrawl.ui.theme.TextMuted
import com.audiocrawl.ui.theme.TextPhosphor

@Composable
fun CopyScreen(
    onNavigateHome: () -> Unit,
    viewModel: CopyViewModel = hiltViewModel()
) {
    val copyState by viewModel.copyState.collectAsStateWithLifecycle()
    val deleteState by viewModel.deleteState.collectAsStateWithLifecycle()
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val intentSenderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        viewModel.onIntentSenderResult(result.resultCode == Activity.RESULT_OK)
    }

    Scaffold(
        containerColor = ObsidianVoid,
        topBar = {
            AudioCrawlTopAppBar(
                title = "VAULT COMMIT",
                subtitle = "SCOPED_TRANSFER // ACTIVE"
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
            when (val state = copyState) {
                is CopyState.Copying -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Central Radial Transfer Meter
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, ObsidianDelimiter, RoundedCornerShape(12.dp))
                                .background(ObsidianContainerLow)
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val progressFraction = if (state.total > 0) state.current.toFloat() / state.total.toFloat() else 0f

                                Box(
                                    modifier = Modifier.size(130.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        progress = { progressFraction },
                                        modifier = Modifier.fillMaxSize(),
                                        strokeWidth = 4.dp,
                                        color = SpectralCyan,
                                        trackColor = ObsidianDelimiter
                                    )
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "${state.progressPercentage}%",
                                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 28.sp),
                                            color = TextPhosphor,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${state.current} / ${state.total}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SpectralCyan,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "COMMITTING TRACKS TO VAULT",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextPhosphor,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Writing via Scoped Storage MediaStore API",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextMuted
                                )
                            }
                        }

                        // Current File In-Flight Card
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
                                    Text(
                                        text = "STREAM IN-FLIGHT",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = TextMuted
                                    )
                                    Text(
                                        text = "I/O SYNC",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = EngineEmerald
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = state.currentFileName.ifBlank { "Initializing writer…" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPhosphor,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                val progressFraction = if (state.total > 0) state.current.toFloat() / state.total.toFloat() else 0f
                                LinearProgressIndicator(
                                    progress = { progressFraction },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(2.dp),
                                    color = SpectralCyan,
                                    trackColor = ObsidianVoid
                                )
                            }
                        }

                        // Storage I/O Telemetry Status Matrix
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, ObsidianDelimiter, RoundedCornerShape(8.dp))
                                    .background(ObsidianContainerLow)
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "ID3v2 TAGS",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = TextMuted
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "PRESERVED",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = EngineEmerald,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, ObsidianDelimiter, RoundedCornerShape(8.dp))
                                    .background(ObsidianContainerLow)
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "MEDIA SCANNER",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = TextMuted
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "AUTO-INDEX",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SpectralCyan,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                is CopyState.Complete -> {
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
                            text = "COMMITTED TO VAULT",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPhosphor
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Successfully copied ${state.copiedCount} tracks with 0 errors.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.dp, ObsidianDelimiter, RoundedCornerShape(6.dp))
                                .background(ObsidianVoid)
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = state.targetPath,
                                style = MaterialTheme.typography.labelSmall,
                                color = SpectralCyan,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Delete / Trash Originals Option
                        if (deleteState is DeleteState.Success) {
                            val success = deleteState as DeleteState.Success
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, EngineEmerald.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .background(ObsidianVoid)
                                    .padding(14.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = EngineEmerald,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = if (success.isTrashed) "MOVED TO TRASH / RECENTLY DELETED" else "ORIGINALS CLEANED UP",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = EngineEmerald
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${success.deletedCount} source tracks removed. No duplicate files remain.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextMuted
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                        } else if (deleteState is DeleteState.InProgress) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = FilterRed
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Cleaning up original songs…",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextPhosphor
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                        } else if (state.originalFiles.isNotEmpty()) {
                            OutlinedButton(
                                onClick = { showDeleteConfirmDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, FilterRed.copy(alpha = 0.7f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = FilterRed)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = null,
                                    tint = FilterRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "DELETE ORIGINAL COPIES",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = FilterRed
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Moves source songs to Trash / Recently Deleted to prevent duplicate copies.",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                                textAlign = TextAlign.Center,
                                color = TextMuted
                            )

                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        if (showDeleteConfirmDialog) {
                            AlertDialog(
                                containerColor = ObsidianContainer,
                                titleContentColor = TextPhosphor,
                                textContentColor = TextMuted,
                                onDismissRequest = { showDeleteConfirmDialog = false },
                                title = { Text("Clean Up Original Songs?") },
                                text = {
                                    Column {
                                        Text(
                                            text = "You have safely copied ${state.copiedCount} songs to ${state.targetPath}.\n\nDo you want to remove the original files from their source folders to avoid having two copies on your phone?\n\nOn devices with 'Trash' or 'Recently Deleted', files are safely moved there so they can be restored if needed.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextPhosphor
                                        )
                                    }
                                },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            showDeleteConfirmDialog = false
                                            viewModel.requestDeleteOriginals(state.originalFiles) { intentSender ->
                                                intentSenderLauncher.launch(
                                                    IntentSenderRequest.Builder(intentSender).build()
                                                )
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = FilterRed)
                                    ) {
                                        Text("DELETE ORIGINALS", color = ObsidianVoid, fontWeight = FontWeight.Bold)
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDeleteConfirmDialog = false }) {
                                        Text("KEEP BOTH", color = TextMuted)
                                    }
                                }
                            )
                        }

                        Button(
                            onClick = onNavigateHome,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SpectralCyan)
                        ) {
                            Text(
                                text = "RETURN TO HOME",
                                fontWeight = FontWeight.Bold,
                                color = ObsidianVoid,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }

                is CopyState.Error -> {
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
                            text = "TRANSFER ERROR",
                            style = MaterialTheme.typography.headlineMedium,
                            color = FilterRed,
                            fontWeight = FontWeight.Bold
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
                            onClick = onNavigateHome,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("RETURN TO HOME")
                        }
                    }
                }

                is CopyState.Idle -> {
                    CircularProgressIndicator(color = SpectralCyan)
                }
            }
        }
    }
}
