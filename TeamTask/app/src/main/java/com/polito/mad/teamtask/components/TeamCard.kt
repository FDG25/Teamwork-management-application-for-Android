package com.polito.mad.teamtask.components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.polito.mad.teamtask.Actions
import com.polito.mad.teamtask.R
import com.polito.mad.teamtask.Team
import com.polito.mad.teamtask.ui.theme.TeamTaskTypography


@Composable
fun TeamCard (
    team: Team,
    imageUri: Uri?,
    teamId: String
) {
    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography

    Card (
        modifier = Modifier
            .fillMaxWidth()
            .width(90.dp)
            .height(110.dp)
            .clickable { Actions.getInstance().goToTeamTasks(teamId) }, // Make the card clickable
        colors = CardDefaults.cardColors(
            containerColor = palette.background,
            contentColor = palette.background
        ),
    ) {
        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (imageUri != null) { // User set an image
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUri)
                        .crossfade(true)
                        //.error()
                        .build(),
                    contentDescription = "Team Image",
                    modifier =
                    Modifier
                        .padding(horizontal = 2.dp, vertical = 4.dp)
                        .size(80.dp)
                        .border(1.dp, palette.secondary),
                        //.padding(4.dp)
                    contentScale = ContentScale.Crop
                )
            } else { // No image set
                Image (
                    painter = painterResource(id = R.drawable.baseline_groups_24), // TODO: Replace with placeholder for teams
                    contentDescription = "Default Team image",
                    modifier =  Modifier
                        .padding(horizontal = 2.dp, vertical = 4.dp)
                        .size(80.dp)
                        .border(1.dp, palette.secondary, RoundedCornerShape(5.dp))
                        .padding(4.dp)
                )
            }
            Text (
                text = team.name,
                style = typography.bodySmall,
                maxLines = 1,
                overflow = Ellipsis,
                color = palette.onSurface
            )
        }
    }
}
