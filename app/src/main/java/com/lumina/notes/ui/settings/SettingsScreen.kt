package com.lumina.notes.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.lumina.notes.data.settings.AppSettings
import com.lumina.notes.util.FontScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    showBackButton: Boolean,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()

    Column(modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Impostazioni") },
            navigationIcon = {
                if (showBackButton) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                }
            },
        )
        Column(Modifier.padding(horizontal = 8.dp)) {
            SectionTitle("Aspetto")
            SwitchRow(
                title = "Colore dinamico (Material You)",
                subtitle = "Usa i colori dello sfondo del sistema",
                checked = state.dynamicColor,
                onChange = viewModel::setDynamicColor,
            )
            ThemeRow(state = state, onChange = viewModel::setDarkTheme)
            FontScaleRow(value = state.fontScale, onChange = viewModel::setFontScale)

            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            SectionTitle("S Pen")
            SwitchRow(
                title = "Rifiuto del palmo",
                subtitle = "Mentre disegni con la S Pen, ignora i tocchi delle dita",
                checked = state.palmRejection,
                onChange = viewModel::setPalmRejection,
            )
            PressureRow(
                value = state.pressureSensitivity,
                onChange = viewModel::setPressureSensitivity,
            )
            LineSpacingRow(
                value = state.lineSpacingDp,
                onChange = viewModel::setLineSpacing,
            )

            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            SectionTitle("Libreria")
            val stats by viewModel.libraryStats.collectAsState()
            Text(
                stats.summary(),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
            Text(
                "${stats.favorites} preferite · ${stats.pinned} fissate",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            val ctx = LocalContext.current
            TextButton(
                onClick = {
                    viewModel.exportAll { name, content ->
                        val send = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/markdown"
                            putExtra(android.content.Intent.EXTRA_SUBJECT, name)
                            putExtra(android.content.Intent.EXTRA_TEXT, content)
                        }
                        ctx.startActivity(
                            android.content.Intent.createChooser(send, "Esporta note")
                        )
                    }
                },
                modifier = Modifier.padding(horizontal = 8.dp),
            ) {
                Icon(
                    Icons.Filled.Share, contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text("Esporta tutte le note (.md)")
            }

            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            SectionTitle("Info")
            Text(
                "Lumina • Note per Galaxy Tab S10 Ultra\n" +
                    "Penna a pressione, evidenziatore, gomma per tratto, autosave.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp),
    )
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun FontScaleRow(value: Float, onChange: (Float) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text("Dimensione testo", style = MaterialTheme.typography.titleMedium)
            Text(
                "Scala il testo dell'editor",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Slider(
                value = value,
                onValueChange = onChange,
                valueRange = FontScale.MIN..FontScale.MAX,
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(FontScale.label(value), style = MaterialTheme.typography.titleMedium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LineSpacingRow(value: Float, onChange: (Float) -> Unit) {
    Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text("Spaziatura righe", style = MaterialTheme.typography.titleMedium)
        Text(
            "Altezza delle righe del quaderno",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Stretta" to 26f, "Normale" to 36f, "Larga" to 48f).forEach { (label, dp) ->
                FilterChip(
                    selected = kotlin.math.abs(value - dp) < 2f,
                    onClick = { onChange(dp) },
                    label = { Text(label) },
                )
            }
        }
    }
}

@Composable
private fun PressureRow(value: Float, onChange: (Float) -> Unit) {
    Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text("Sensibilità pressione", style = MaterialTheme.typography.titleMedium)
        Text(
            "Quanto la pressione della S Pen cambia lo spessore del tratto",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Slider(
                value = value,
                onValueChange = onChange,
                valueRange = 0f..1f,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(12.dp))
            Text("${(value * 100).toInt()}%", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeRow(state: AppSettings, onChange: (Boolean?) -> Unit) {
    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text("Tema", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier.selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = state.darkTheme == null,
                onClick = { onChange(null) },
                label = { Text("Sistema") },
            )
            FilterChip(
                selected = state.darkTheme == false,
                onClick = { onChange(false) },
                label = { Text("Chiaro") },
            )
            FilterChip(
                selected = state.darkTheme == true,
                onClick = { onChange(true) },
                label = { Text("Scuro") },
            )
        }
    }
}
