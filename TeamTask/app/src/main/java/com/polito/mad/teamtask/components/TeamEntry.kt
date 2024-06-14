package com.polito.mad.teamtask.components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.rememberImagePainter
import coil.request.ImageRequest
import com.polito.mad.teamtask.R
import com.polito.mad.teamtask.Team
import com.polito.mad.teamtask.ui.theme.TeamTaskTypography


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamEntry (
    team: Team,
    unreadNotifications: Int,
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
        // Team image
        if (imageUri != null) { // User set an image
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUri)
                    .crossfade(true)
                    .error(R.drawable.baseline_groups_24)
                    .build(),
                contentDescription = "Team Image",
                Modifier
                    .size(48.dp)
                    .border(1.dp, palette.secondary),
                contentScale = ContentScale.Crop
            )
        } else { // No image set
            Image (
                painter = painterResource(id = R.drawable.baseline_groups_24), // TODO: Replace with placeholder for teams
                contentDescription = "Default Team image",
                Modifier
                    .size(48.dp)
                    .border(1.dp, palette.secondary, RoundedCornerShape(5.dp))
                    .padding(4.dp)
            )
        }

        Spacer(Modifier.width(8.dp))

        Row (
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Team name
            Text (
                text = team.name,
                modifier = Modifier.weight(1f),
                style = typography.bodyMedium,
                color = palette.onSurface,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Unread messages
            if (unreadNotifications > 0) {
                Badge (
                    modifier = Modifier
                        .clip(CircleShape)
                        .padding(end = 10.dp),
                    contentColor = palette.background,
                    containerColor = palette.secondary
                ) {
                    Text (
                        text = unreadNotifications.toString(),
                        modifier = Modifier.padding(5.dp),
                        style = typography.bodySmall,
                        color = palette.background
                    )
                }
            }
        }
    }
}
