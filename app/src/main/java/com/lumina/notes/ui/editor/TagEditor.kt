package com.lumina.notes.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

/** Inline tag chips with an "add" affordance, shown under the editor title. */
@OptIn(ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun TagEditor(
    tags: List<String>,
    onAdd: (String) -> Unit,
    onRemove: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var adding by remember { mutableStateOf(false) }
    var draft by remember { mutableStateOf("") }

    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        tags.forEach { tag ->
            InputChip(
                selected = false,
                onClick = { onRemove(tag) },
                label = { Text(tag) },
                trailingIcon = {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Rimuovi $tag",
                        modifier = Modifier.size(16.dp),
                    )
                },
            )
        }

        if (adding) {
            androidx.compose.material3.TextField(
                value = draft,
                onValueChange = { draft = it.replace("\n", "") },
                singleLine = true,
                placeholder = { Text("nuovo tag") },
                modifier = Modifier.size(width = 160.dp, height = 56.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    val t = draft.trim()
                    if (t.isNotEmpty()) onAdd(t)
                    draft = ""
                    adding = false
                }),
            )
        } else {
            AssistChip(
                onClick = { adding = true },
                label = { Text("Aggiungi tag") },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(AssistChipDefaults.IconSize),
                    )
                },
            )
        }
    }
}
