package com.audiocrawl.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.audiocrawl.ui.theme.EngineEmerald
import com.audiocrawl.ui.theme.ObsidianDelimiter
import com.audiocrawl.ui.theme.ObsidianSurface
import com.audiocrawl.ui.theme.SpectralCyan
import com.audiocrawl.ui.theme.TextMuted
import com.audiocrawl.ui.theme.TextPhosphor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioCrawlTopAppBar(
    title: String,
    subtitle: String? = null,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {}
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPhosphor
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .border(1.dp, ObsidianDelimiter, RoundedCornerShape(4.dp))
                            .background(ObsidianSurface)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = SpectralCyan
                        )
                    }
                }
            }
        },
        navigationIcon = {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPhosphor
                    )
                }
            }
        },
        actions = { actions() },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = ObsidianSurface,
            titleContentColor = TextPhosphor
        )
    )
}

@Composable
fun SdCardBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .border(1.dp, EngineEmerald.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
            .background(EngineEmerald.copy(alpha = 0.12f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.SdCard,
                contentDescription = "SD Card",
                modifier = Modifier.size(12.dp),
                tint = EngineEmerald
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = "SD CARD",
                style = MaterialTheme.typography.labelSmall,
                color = EngineEmerald
            )
        }
    }
}
