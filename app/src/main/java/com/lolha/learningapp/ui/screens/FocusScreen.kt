package com.lolha.learningapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lolha.learningapp.ui.components.formatSeconds
import com.lolha.learningapp.ui.state.MainUiState
import kotlin.math.roundToInt

@Composable
fun FocusScreen(
    state: MainUiState,
    onMinutesChanged: (Int) -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(16.dp))
        Text(formatSeconds(state.remainingSeconds), fontSize = 56.sp, fontWeight = FontWeight.Bold)
        Text("Focus session with Android screen pinning")
        Spacer(Modifier.height(28.dp))
        Text("${state.focusMinutes} minutes", fontWeight = FontWeight.Bold)
        Slider(
            value = state.focusMinutes.toFloat(),
            onValueChange = { onMinutesChanged(it.roundToInt()) },
            valueRange = 5f..180f,
            steps = 34,
            enabled = !state.focusRunning,
        )
        Spacer(Modifier.height(16.dp))
        if (state.focusRunning) {
            Button(onClick = onStop) {
                Icon(Icons.Default.Stop, contentDescription = null)
                Text(" Stop")
            }
        } else {
            Button(onClick = onStart) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Text(" Start focus")
            }
        }
    }
}

