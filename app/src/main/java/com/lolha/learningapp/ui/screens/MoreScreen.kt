package com.lolha.learningapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lolha.learningapp.ui.components.CompactCardPadding
import com.lolha.learningapp.ui.components.CompactListGap
import com.lolha.learningapp.ui.components.CompactPagePadding
import com.lolha.learningapp.ui.state.AppTab

@Composable
fun MoreScreen(
    selectedTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
) {
    Column(
        modifier = Modifier.padding(CompactPagePadding),
        verticalArrangement = Arrangement.spacedBy(CompactListGap),
    ) {
        Text("More", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        MoreItem(
            title = "Focus",
            body = "Timer and screen pinning",
            icon = Icons.Default.Timer,
            selected = selectedTab == AppTab.Focus,
            onClick = { onTabSelected(AppTab.Focus) },
        )
        MoreItem(
            title = "Materials",
            body = "Japanese, English, drawing links",
            icon = Icons.AutoMirrored.Filled.MenuBook,
            selected = selectedTab == AppTab.Materials,
            onClick = { onTabSelected(AppTab.Materials) },
        )
        MoreItem(
            title = "Journal",
            body = "Graded homework history",
            icon = Icons.Default.History,
            selected = selectedTab == AppTab.Journal,
            onClick = { onTabSelected(AppTab.Journal) },
        )
        MoreItem(
            title = "Profile",
            body = "Goals and availability",
            icon = Icons.Default.Person,
            selected = selectedTab == AppTab.Profile,
            onClick = { onTabSelected(AppTab.Profile) },
        )
        MoreItem(
            title = "Social",
            body = "Monthly art proof links",
            icon = Icons.Default.Image,
            selected = selectedTab == AppTab.Social,
            onClick = { onTabSelected(AppTab.Social) },
        )
    }
}

@Composable
private fun MoreItem(
    title: String,
    body: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Color(0xFFE0F2FE) else Color.White,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CompactCardPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(body, color = Color(0xFF475569))
            }
        }
    }
}
