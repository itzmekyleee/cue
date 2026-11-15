package ph.edu.comteq.cue.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/** Avoids flashing a spinner for data loads that finish almost immediately. */
@Composable
fun LoadingState(
    modifier: Modifier = Modifier,
    message: String = "Loading"
) {
    var showIndicator by remember(message) { mutableStateOf(false) }
    LaunchedEffect(message) {
        delay(350)
        showIndicator = true
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = showIndicator,
            enter = fadeIn()
        ) {
            CircularProgressIndicator(
                modifier = Modifier.semantics { contentDescription = message }
            )
        }
    }
}
