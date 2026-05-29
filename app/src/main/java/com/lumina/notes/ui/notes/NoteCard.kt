package com.lumina.notes.ui.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lumina.notes.data.local.NoteEntity
import com.lumina.notes.ui.theme.NoteAccents

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun NoteCard(
    note: NoteEntity,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = NoteAccents[note.colorSeed % NoteAccents.size]
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.surfaceColorAtElevationCompat(),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 6.dp else 1.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(50))
                        .background(accent)
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    text = note.title.ifBlank { "Senza titolo" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (note.isPinned) {
                    Icon(
                        Icons.Filled.PushPin, contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp),
                    )
                }
                if (note.isFavorite) {
                    Spacer(Modifier.size(4.dp))
                    Icon(
                        Icons.Filled.Star, contentDescription = null,
                        tint = accent, modifier = Modifier.size(16.dp),
                    )
                }
            }
            if (note.preview.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = note.preview,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (note.hasInk) {
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Brush, contentDescription = null,
                        tint = accent, modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.size(6.dp))
                    Text(
                        "Disegno a mano",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun androidx.compose.material3.ColorScheme.surfaceColorAtElevationCompat(): Color =
    surfaceContainer
