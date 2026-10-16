package com.audiocrawl.ui.results

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.audiocrawl.model.AudioFile
import com.audiocrawl.ui.components.SdCardBadge
import com.audiocrawl.ui.theme.AccentWarm
import com.audiocrawl.ui.theme.CardSurface
import com.audiocrawl.ui.theme.CardSurfaceElevated
import com.audiocrawl.ui.theme.DividerSubtle
import com.audiocrawl.ui.theme.EngineEmerald
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
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onToggleSelect(!isSelected) },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPlaying) CardSurfaceElevated else CardSurface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                if (isPlaying) listOf(AccentWarm.copy(alpha = 0.6f), AccentWarm.copy(alpha = 0.6f))
                else if (isSelected) listOf(DividerSubtle.copy(alpha = 0.8f), DividerSubtle.copy(alpha = 0.8f))
                else listOf(DividerSubtle.copy(alpha = 0.4f), DividerSubtle.copy(alpha = 0.4f))
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggleSelect(it) },
                colors = CheckboxDefaults.colors(
                    checkedColor = AccentWarm,
                    checkmarkColor = Color.White,
                    uncheckedColor = TextMuted.copy(alpha = 0.7f)
                )
            )

            Spacer(modifier = Modifier.width(4.dp))

            // Music note / artwork avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(
                            colors = if (isPlaying) {
                                listOf(AccentWarm, Color(0xFFD4381C))
                            } else {
                                listOf(CardSurfaceElevated, CardSurface)
                            }
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = if (isPlaying) Color.White else AccentWarm,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Song Info Column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = audioFile.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPhosphor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                val artistAlbumText = buildString {
                    audioFile.artist?.let { append(it) }
                    if (!audioFile.artist.isNullOrBlank() && !audioFile.album.isNullOrBlank()) append(" • ")
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

                Spacer(modifier = Modifier.height(4.dp))

                // Tags Row: Duration, Format, Size, SD Card
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Duration Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CardSurfaceElevated)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = audioFile.formattedDuration,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextPhosphor.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Format Pill
                    val extension = audioFile.fileName.substringAfterLast('.', "").uppercase()
                    if (extension.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(CardSurfaceElevated)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = extension,
                                style = MaterialTheme.typography.labelSmall,
                                color = EngineEmerald,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Size
                    Text(
                        text = audioFile.formattedSize,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )

                    if (audioFile.isFromSdCard) {
                        SdCardBadge()
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Play / Pause Preview Button
            FilledIconButton(
                onClick = onPlayPauseClick,
                modifier = Modifier.size(38.dp),
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (isPlaying) AccentWarm else CardSurfaceElevated,
                    contentColor = if (isPlaying) Color.White else TextPhosphor
                )
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play Preview",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
