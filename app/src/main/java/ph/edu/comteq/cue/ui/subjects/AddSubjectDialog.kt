package ph.edu.comteq.cue.ui.subjects

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.clickable

/** The subject icon colors from our mockups. Add more here later if you want. */
const val DEFAULT_SUBJECT_COLOR_KEY = "default"
val subjectColorKeys = listOf(DEFAULT_SUBJECT_COLOR_KEY, "teal", "coral", "purple", "pink")

fun colorForKey(key: String, defaultColor: Color = Color.Gray): Color {
    val preset = when (key) {
        "teal" -> Color(0xFF1D9E75)
        "coral" -> Color(0xFFD85A30)
        "purple" -> Color(0xFF7F77DD)
        "pink" -> Color(0xFFD4537E)
        DEFAULT_SUBJECT_COLOR_KEY -> defaultColor
        else -> null
    }
    if (preset != null) return preset

    val hex = key.removePrefix("#")
    val value = hex.takeIf { it.length == 6 && it.all { char -> char.digitToIntOrNull(16) != null } }
        ?.toLongOrNull(16)
        ?: return defaultColor
    return Color(0xFF000000L or value)
}

@Composable
fun AddSubjectDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, colorKey: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(DEFAULT_SUBJECT_COLOR_KEY) }
    var nameTouched by remember { mutableStateOf(false) }
    val isNameBlank = name.isBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add subject") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameTouched = true
                    },
                    placeholder = { Text("Subject name") },
                    singleLine = true,
                    isError = nameTouched && isNameBlank,
                    supportingText = if (nameTouched && isNameBlank) {
                        { Text("Subject name can't be blank") }
                    } else {
                        null
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.padding(top = 16.dp))
                SubjectColorPicker(
                    selectedColor = selectedColor,
                    onColorSelected = { selectedColor = it }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name.trim(), selectedColor) },
                enabled = !isNameBlank
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SubjectColorPicker(selectedColor: String, onColorSelected: (String) -> Unit) {
    var showCustomPicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Subject color",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            subjectColorKeys.forEach { key ->
                ColorDot(
                    colorKey = key,
                    selected = key == selectedColor,
                    onClick = { onColorSelected(key) }
                )
            }
        }
        OutlinedButton(
            onClick = { showCustomPicker = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .height(48.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            val isCustomSelected = selectedColor.startsWith("#")
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(
                        if (isCustomSelected) colorForKey(
                            selectedColor,
                            MaterialTheme.colorScheme.primary
                        ) else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isCustomSelected) Icons.Default.Check else Icons.Default.Colorize,
                    contentDescription = null,
                    tint = if (isCustomSelected) Color.White
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(if (isCustomSelected) "Change custom color" else "Choose custom color")
        }
    }

    if (showCustomPicker) {
        CustomSubjectColorDialog(
            initialColor = colorForKey(selectedColor, MaterialTheme.colorScheme.primary),
            onDismiss = { showCustomPicker = false },
            onConfirm = { color ->
                onColorSelected(color.toSubjectColorKey())
                showCustomPicker = false
            }
        )
    }
}

@Composable
private fun CustomSubjectColorDialog(
    initialColor: Color,
    onDismiss: () -> Unit,
    onConfirm: (Color) -> Unit
) {
    val hsv = remember(initialColor) { initialColor.toHsvComponents() }
    var hue by remember(initialColor) { mutableStateOf(hsv[0]) }
    var saturation by remember(initialColor) { mutableStateOf(hsv[1]) }
    var brightness by remember(initialColor) { mutableStateOf(hsv[2]) }
    val selectedColor = Color.hsv(hue, saturation, brightness)
    val hueColor = Color.hsv(hue, 1f, 1f)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Custom color") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .pointerInput(hue) {
                            fun updateSelection(position: Offset) {
                                saturation = (position.x / size.width).coerceIn(0f, 1f)
                                brightness = (1f - position.y / size.height).coerceIn(0f, 1f)
                            }
                            detectTapGestures { updateSelection(it) }
                        }
                        .pointerInput(hue) {
                            detectDragGestures { change, _ ->
                                saturation = (change.position.x / size.width).coerceIn(0f, 1f)
                                brightness = (1f - change.position.y / size.height).coerceIn(0f, 1f)
                                change.consume()
                            }
                        }
                ) {
                    drawRect(
                        brush = Brush.horizontalGradient(listOf(Color.White, hueColor)),
                        size = size
                    )
                    drawRect(
                        brush = Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black)
                        ),
                        size = size
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 9.dp.toPx(),
                        center = Offset(
                            saturation * size.width,
                            (1f - brightness) * size.height
                        ),
                        style = Stroke(width = 3.dp.toPx())
                    )
                    drawCircle(
                        color = Color.Black.copy(alpha = 0.75f),
                        radius = 10.5.dp.toPx(),
                        center = Offset(
                            saturation * size.width,
                            (1f - brightness) * size.height
                        ),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
                Text(
                    text = "Hue",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = hue,
                    onValueChange = { hue = it },
                    valueRange = 0f..360f
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(selectedColor)
                    )
                    Text(
                        text = "Preview",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedColor) }) {
                Text("Use color")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun Color.toHsvComponents(): List<Float> {
    val max = maxOf(red, green, blue)
    val min = minOf(red, green, blue)
    val delta = max - min
    val hue = when {
        delta == 0f -> 0f
        max == red -> (60f * ((green - blue) / delta) + 360f) % 360f
        max == green -> 60f * ((blue - red) / delta + 2f)
        else -> 60f * ((red - green) / delta + 4f)
    }
    val saturation = if (max == 0f) 0f else delta / max
    return listOf(hue, saturation, max)
}

private fun Color.toSubjectColorKey(): String {
    val rgb = toArgb() and 0x00FFFFFF
    return "#${rgb.toString(16).padStart(6, '0').uppercase()}"
}

@Composable
private fun ColorDot(colorKey: String, selected: Boolean, onClick: () -> Unit) {
    val label = when (colorKey) {
        DEFAULT_SUBJECT_COLOR_KEY -> "Default"
        else -> colorKey.replaceFirstChar { it.uppercase() }
    }
    val swatch = colorForKey(colorKey, MaterialTheme.colorScheme.primary)
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = "$label subject color"
                this.selected = selected
                role = Role.RadioButton
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .border(
                    width = if (selected) 2.dp else 0.dp,
                    color = if (selected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                    shape = CircleShape
                )
                .padding(4.dp)
                .size(28.dp)
                .background(swatch, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AddSubjectDialogPreview() {
    MaterialTheme {
        AddSubjectDialog(onDismiss = {}, onConfirm = { _, _ -> })
    }
}