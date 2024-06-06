package com.polito.mad.teamtask.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.polito.mad.teamtask.ui.theme.TeamTaskTypography


@Composable
fun ProgressBar(
    completedNumber: Int = 5,
    totalNumber: Int = 20
) {
    val typography = TeamTaskTypography
    val palette = MaterialTheme.colorScheme
    val ratio = if(totalNumber>0) completedNumber.toFloat() / totalNumber else 0f

    Box (
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 25.dp)
            //.aspectRatio(13f)
            .border(1.dp, palette.onSurfaceVariant, shape = RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(palette.background),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(ratio)
                .clip(RoundedCornerShape(12.dp))
                .background(palette.secondary)
        )
        Text(
            text = "$completedNumber/$totalNumber",
            style = typography.bodySmall,
            color = if (ratio>0.6) palette.background else if (ratio in 0.4..0.6) palette.onSurface else palette.secondary,
            textAlign = TextAlign.Center,
            fontWeight = if (ratio in 0.4..0.6) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}
