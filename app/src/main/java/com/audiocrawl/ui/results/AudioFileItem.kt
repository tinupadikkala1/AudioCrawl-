package com.audiocrawl.ui.results

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.audiocrawl.model.AudioFile
import com.audiocrawl.ui.components.SdCardBadge
import com.audiocrawl.ui.theme.AtmosphereBlue
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
fun AudioFileItem(
    audioFile: AudioFile,
    isSelected: Boolean,
    isPlaying: Boolean,
    onToggleSelect: (Boolean) -> Unit,
    onPlayPauseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = when {
        isPlaying -> SpectralCyan
        isSelected -> SpectralCyan.copy(alpha = 0.5f)
        else -> ObsidianDelimiter
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .background(if (isPlaying) ObsidianContainer else ObsidianContainerLow)
            .clickable { onToggleSelect(!isSelected) }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggleSelect(it) },
                colors = CheckboxDefaults.colors(
                    checkedColor = SpectralCyan,
                    checkmarkColor = ObsidianVoid,
                    uncheckedColor = TextMuted
                )
            )

            Spacer(modifier = Modifier.width(6.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = audioFile.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPhosphor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (audioFile.isFromSdCard) {
                        Spacer(modifier = Modifier.width(6.dp))
                        SdCardBadge()
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                val artistAlbumText = buildString {
                    audioFile.artist?.let { append(it) }
                    if (audioFile.artist != null && audioFile.album != null) append(" • ")
                    audioFile.album?.let { append(it) }
                    if (isEmpty()) append(audioFile.parentFolder.substringAfterLast('/'))
                }

                Text(
                    text = artistAlbumText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Telemetry Spec Tag Pills
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Duration
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(ObsidianVoid)
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = audioFile.formattedDuration,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = SpectralCyan,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Format
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(ObsidianVoid)
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = audioFile.fileName.substringAfterLast('.', "").uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = EngineEmerald,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Size
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(ObsidianVoid)
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = audioFile.formattedSize,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = TextMuted,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            FilledIconButton(
                onClick = onPlayPauseClick,
                modifier = Modifier.size(38.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (isPlaying) SpectralCyan else ObsidianVoid
                )
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play Preview",
                    tint = if (isPlaying) ObsidianVoid else SpectralCyan,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
