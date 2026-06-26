package com.lolha.learningapp.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val CompactPagePadding: Dp = 12.dp
val CompactCardPadding: Dp = 12.dp
val CompactListGap: Dp = 8.dp
val CompactButtonHeight: Dp = 44.dp

@Composable
fun EmptyState(title: String, body: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(modifier = Modifier.padding(CompactCardPadding)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(4.dp))
            Text(body, color = Color(0xFF475569))
        }
    }
}

@Composable
fun TeacherThinkingCard(message: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Row(
            modifier = Modifier.padding(CompactCardPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 2.dp,
            )
            Spacer(Modifier.width(10.dp))
            Column {
                Text(message, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("請等一下，老師正在整理回覆。", color = Color(0xFF475569), fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun ErrorText(message: String, modifier: Modifier = Modifier) {
    Text(message, color = Color(0xFFB91C1C), modifier = modifier)
}

@Composable
fun SyncWarningText(message: String, modifier: Modifier = Modifier) {
    Text("Sync warning: $message", color = Color(0xFFB45309), modifier = modifier)
}

@Composable
fun FeedbackSection(title: String, text: String) {
    if (text.isBlank()) return
    Spacer(Modifier.height(10.dp))
    Text(title, fontWeight = FontWeight.Bold)
    text.lines().filter { it.isNotBlank() }.forEach { item ->
        Text("- $item")
    }
}

fun formatSeconds(seconds: Int): String {
    val minutes = seconds / 60
    val remaining = seconds % 60
    return "%02d:%02d".format(minutes, remaining)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompactDropdownField(
    value: String,
    options: List<String>,
    label: String,
    onValueSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            singleLine = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onValueSelected(option)
                        expanded = false
                    },
                )
            }
        }
    }
}
