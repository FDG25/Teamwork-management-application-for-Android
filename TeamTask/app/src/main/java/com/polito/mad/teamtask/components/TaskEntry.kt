package com.polito.mad.teamtask.components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.polito.mad.teamtask.R
import com.polito.mad.teamtask.Task
import com.polito.mad.teamtask.Team
import com.polito.mad.teamtask.ui.theme.TeamTaskTypography


@Composable
fun TaskEntry (
    task: Task,
    team: Team?,
    imageUri: Uri?
) {
    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography

    Row (
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(palette.surfaceVariant, RoundedCornerShape(5.dp))
            .border(1.dp, palette.secondary, RoundedCornerShape(5.dp))
            .padding(8.dp)
    ) {
        if (imageUri != null) { // User set an image
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUri)
                    .crossfade(true)
                    //.error()
                    .build(),
                contentDescription = "Team Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(50.dp)
                    .border(1.dp, palette.secondary)
                    //.padding(4.dp)
            )
        } else { // No image set
            Image (
                painter = painterResource(id = R.drawable.baseline_groups_24), // TODO: Replace with placeholder for teams
                contentDescription = "Default Team image",
                modifier = Modifier
                    .size(50.dp)
                    .border(1.dp, palette.secondary, RoundedCornerShape(5.dp))
                    .padding(4.dp)
            )
        }

        Spacer(Modifier.width(8.dp))

        Column (
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Row (
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text (
                    text = task.title,
                    style = typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = palette.onSurface
                )

                if (task.prioritized) {
                    Row (
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image (
                            painter = painterResource(id = R.drawable.outline_warning_amber_24),
                            contentDescription = "Warning",
                            modifier = Modifier.size(20.dp),
                            colorFilter = ColorFilter.tint(palette.error)
                        )

                        Text (
                            text = "Prior",
                            style = typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = palette.error,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(5.dp))

            Row (
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (team != null) {
                    Text (
                        text = team.name,
                        style = typography.bodySmall,
                        color = palette.onSurface
                    )
                }

                Text (
                    text = "Expires at " + task.deadline.split("T")[1].split("+")[0].slice(IntRange(0,4)),
                    style = typography.bodySmall,
                    modifier = Modifier.padding(start = 8.dp),
                    color = palette.onSurface
                )
            }
        }
    }
}
