package com.audiocrawl.ui.results

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.audiocrawl.ui.components.AudioCrawlTopAppBar
import com.audiocrawl.ui.theme.EngineEmerald
import com.audiocrawl.ui.theme.ObsidianContainer
import com.audiocrawl.ui.theme.ObsidianContainerLow
import com.audiocrawl.ui.theme.ObsidianDelimiter
import com.audiocrawl.ui.theme.ObsidianSurface
import com.audiocrawl.ui.theme.ObsidianVoid
import com.audiocrawl.ui.theme.SpectralCyan
import com.audiocrawl.ui.theme.TextBody
import com.audiocrawl.ui.theme.TextMuted
import com.audiocrawl.ui.theme.TextPhosphor

@Composable
fun ResultsScreen(
    onNavigateToCopy: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: ResultsViewModel = hiltViewModel()
) {
    val filteredFiles by viewModel.filteredFiles.collectAsStateWithLifecycle()
    val allFiles by viewModel.scannedFiles.collectAsStateWithLifecycle()
    val selectedIds by viewModel.selectedIds.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val targetFolderName by viewModel.targetFolderName.collectAsStateWithLifecycle()
    val playerState by viewModel.playerState.collectAsStateWithLifecycle()

    val selectedCount = selectedIds.size
    val allSelected = allFiles.isNotEmpty() && selectedCount == allFiles.size

    Scaffold(
        containerColor = ObsidianVoid,
        topBar = {
            AudioCrawlTopAppBar(
                title = "VAULT STAGING",
                subtitle = "${allFiles.size} ISOLATED",
                onBackClick = onNavigateBack,
                actions = {
                    TextButton(onClick = {
                        if (allSelected) viewModel.deselectAll() else viewModel.selectAll()
                    }) {
                        Icon(
                            imageVector = if (allSelected) Icons.Default.Deselect else Icons.Default.SelectAll,
                            contentDescription = null,
                            tint = SpectralCyan
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (allSelected) "DESELECT" else "SELECT ALL",
                            color = SpectralCyan,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            )
        },
        bottomBar = {
            Column {
                // Mini audio preview player
                AnimatedVisibility(
                    visible = playerState.currentSong != null,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    MiniPlayer(
                        playerState = playerState,
                        onTogglePlayPause = { viewModel.togglePlayPause() },
                        onClose = { viewModel.stopPreview() }
                    )
                }

                // Sticky Vault Batch Commit Footer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                        .border(1.dp, ObsidianDelimiter, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                        .background(ObsidianContainerLow)
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = SpectralCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = targetFolderName,
                                onValueChange = { viewModel.updateTargetFolderName(it) },
                                label = { Text("Vault Target (/Music/…)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
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
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                val copied = viewModel.copySelectedFiles()
                                if (copied.isNotEmpty()) {
                                    onNavigateToCopy()
                                }
                            },
                            enabled = selectedCount > 0,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SpectralCyan,
                                disabledContainerColor = ObsidianDelimiter
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = null,
                                tint = if (selectedCount > 0) ObsidianVoid else TextMuted
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "COMMIT TO VAULT ($selectedCount TRACKS)",
                                fontWeight = FontWeight.Bold,
                                color = if (selectedCount > 0) ObsidianVoid else TextMuted,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar in Obsidian Pod style
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text("Search by title, artist, format…", color = TextMuted) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = SpectralCyan)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPhosphor),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SpectralCyan,
                    unfocusedBorderColor = ObsidianDelimiter,
                    focusedContainerColor = ObsidianContainerLow,
                    unfocusedContainerColor = ObsidianContainerLow
                )
            )

            if (filteredFiles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.MusicOff,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = TextMuted
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "No files match \"$searchQuery\"" else "No music files isolated",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredFiles, key = { it.id }) { audioFile ->
                        AudioFileItem(
                            audioFile = audioFile,
                            isSelected = selectedIds.contains(audioFile.id),
                            isPlaying = playerState.currentSong?.id == audioFile.id && playerState.isPlaying,
                            onToggleSelect = { viewModel.toggleSelection(audioFile) },
                            onPlayPauseClick = { viewModel.playPreview(audioFile) }
                        )
                    }
                }
            }
        }
    }
}
