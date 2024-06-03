package com.polito.mad.teamtask.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.polito.mad.teamtask.R
import com.polito.mad.teamtask.ui.theme.TeamTaskTypography

@Composable
fun CustomSearchBar (
    modifier: Modifier = Modifier,
    placeholderText: String,
    searchQuery: String, onSearchQueryChanged: (String) -> Unit
) {
    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography

    Box (
        modifier = modifier
            .background(color = palette.surfaceVariant, shape = RoundedCornerShape(20.dp))
            .border(width = 1.dp, color = palette.secondary, shape = RoundedCornerShape(20.dp))
            .height(40.dp)
            .padding(horizontal = 15.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row (
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Image (
                painter = painterResource(R.drawable.outline_search_24),
                contentDescription = "Search team",
                modifier = Modifier.size(24.dp)
            )

            BasicTextField (
                value = searchQuery,
                onValueChange = onSearchQueryChanged,
                singleLine = true,
                textStyle = typography.labelMedium.copy(color = palette.onSurface),
                cursorBrush = SolidColor(palette.onSurface),
                decorationBox = { innerTextField ->
                    Box (
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 5.dp)
                    ) {
                        if (searchQuery.isEmpty()) {
                            Text (
                                text = placeholderText,
                                style = typography.headlineSmall.copy(
                                    color = palette.onSurfaceVariant
                                )
                            )
                        }

                        innerTextField()
                    }
                },
                modifier = Modifier
                    .padding(start = 8.dp)
                    .weight(1f)
            )

            Spacer(Modifier.width(8.dp))

            if (searchQuery.isNotEmpty()) {
                IconButton(
                    onClick = { onSearchQueryChanged("") },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon (
                        painter = painterResource(R.drawable.baseline_clear_24),
                        contentDescription = "Clear",
                        tint = palette.onSurfaceVariant
                    )
                }
            }
        }
    }
}
