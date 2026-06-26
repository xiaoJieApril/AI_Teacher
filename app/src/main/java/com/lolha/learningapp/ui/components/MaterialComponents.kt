package com.lolha.learningapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.lolha.learningapp.domain.LearningMaterial
import com.lolha.learningapp.domain.LearningMaterialCatalog

@Composable
fun MaterialCard(
    material: LearningMaterial,
    onOpenUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = { onOpenUrl(material.url) },
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Row(
            modifier = Modifier.padding(CompactCardPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CompactListGap),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(material.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("${material.subject} · ${material.level}", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                Text(material.description, color = Color(0xFF475569))
                if (material.notes.isNotBlank()) {
                    Text(material.notes, color = Color(0xFF64748B), fontSize = 12.sp)
                }
            }
            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "Open material")
        }
    }
}

@Composable
fun RecommendedMaterials(
    subject: String,
    onOpenUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val materials = LearningMaterialCatalog.forSubject(subject).take(2)
    if (materials.isEmpty()) return
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(CompactListGap),
    ) {
        Text("Recommended materials", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        materials.forEach { material ->
            MaterialCard(material = material, onOpenUrl = onOpenUrl)
        }
    }
}
