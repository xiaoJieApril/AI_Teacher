package com.lolha.learningapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.lolha.learningapp.domain.LearningMaterialCatalog
import com.lolha.learningapp.ui.components.CompactListGap
import com.lolha.learningapp.ui.components.CompactPagePadding
import com.lolha.learningapp.ui.components.MaterialCard

@Composable
fun MaterialsScreen(
    onOpenUrl: (String) -> Unit,
) {
    val groupedMaterials = LearningMaterialCatalog.materials.groupBy { it.subject }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(CompactPagePadding),
        verticalArrangement = Arrangement.spacedBy(CompactListGap),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(CompactListGap)) {
                Text("Materials", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(
                    "Curated links the AI teacher can assign for Japanese, English, drawing, and fitness.",
                    fontSize = 13.sp,
                )
            }
        }

        groupedMaterials.forEach { (subject, materials) ->
            item {
                Text(subject.subjectTitle(), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            items(materials) { material ->
                MaterialCard(material = material, onOpenUrl = onOpenUrl)
            }
        }
    }
}

private fun String.subjectTitle(): String = when (this) {
    "japanese" -> "Japanese"
    "english" -> "English"
    "drawing" -> "Drawing"
    "fitness" -> "Fitness"
    else -> replaceFirstChar { it.uppercase() }
}
