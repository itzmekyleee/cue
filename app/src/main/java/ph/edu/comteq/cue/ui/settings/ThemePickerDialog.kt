package ph.edu.comteq.cue.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

private val THEME_OPTIONS = listOf("system", "light", "dark")

@Composable
fun ThemePickerDialog(
    currentTheme: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Theme") },
        text = {
            Column {
                THEME_OPTIONS.forEach { option ->
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.selectable(
                            selected = option == currentTheme,
                            onClick = { onSelect(option) }
                        ),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        RadioButton(selected = option == currentTheme, onClick = { onSelect(option) })
                        Text(option.replaceFirstChar { it.uppercase() })
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun ThemePickerDialogPreview() {
    MaterialTheme {
        ThemePickerDialog(currentTheme = "system", onDismiss = {}, onSelect = {})
    }
}