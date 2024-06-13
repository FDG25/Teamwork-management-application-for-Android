package com.polito.mad.teamtask.components

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.polito.mad.teamtask.Notification
import com.polito.mad.teamtask.R
import com.polito.mad.teamtask.ui.theme.TeamTaskTypography

@Composable
fun NotificationEntry (
    notification: Notification,
    senderName: String,
    read: Boolean,
    imageUri: Uri?
) {
    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography

    val notificationIcon = when (notification.typology.toInt()) {
        1 -> R.drawable.outline_work_outline_24
        2 -> R.drawable.outline_newspaper_24
        3 -> R.drawable.outline_mark_chat_unread_24
        4 -> R.drawable.outline_done_24
        5 -> R.drawable.outline_access_time_24
        else -> R.drawable.outline_camera_alt_24
    }

    Row (
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(
                if (!read) palette.surfaceVariant else palette.background,
                RoundedCornerShape(5.dp)
            )
            .border(1.dp, palette.secondary, RoundedCornerShape(5.dp))
            .padding(8.dp)
    ) {
        // Team or Account image
        Box {
            Box (
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(45.dp)
                    .then(
                        if (!notification.fromGroup) Modifier.clip(CircleShape)
                        else Modifier
                    )
            ) {
                if (imageUri != null) { // User set an image
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUri)
                            .crossfade(true)
                            //.error()
                            .build(),
                        contentDescription = "User or Team image",
                        contentScale = ContentScale.Crop,
                        modifier =
                        if(!notification.fromGroup){
                            Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .border(1.dp, palette.secondary, CircleShape)
                        } else {
                            Modifier
                                .size(45.dp)
                                .border(1.dp, palette.secondary)
                                .fillMaxSize()
                        }
                        //.padding(4.dp)
                    )
                } else { // No image set
                    Image (
                        painter = painterResource(id = if(!notification.fromGroup){
                            R.drawable.baseline_person_24
                        } else{
                            R.drawable.baseline_groups_24
                        }), // TODO: Replace with placeholder for teams
                        contentDescription = "User or Team image",
                        contentScale = ContentScale.Crop,
                        modifier =
                        if(!notification.fromGroup) {
                            Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .border(1.dp, palette.secondary, CircleShape)
                        } else {
                            Modifier
                                .size(45.dp)
                                .border(1.dp, palette.secondary)
                                .fillMaxSize()
                        }
                    )
                }
            }

            IconButton (
                onClick = {},
                modifier = Modifier
                    .clip(CircleShape)
                    .border(1.dp, palette.secondary, CircleShape)
                    .align(Alignment.BottomEnd)
                    .size(18.dp)
                    .background(
                        color = when (notification.typology.toInt()) {
                            1 -> palette.primary
                            2 -> palette.inverseOnSurface
                            3 -> palette.tertiary
                            4 -> palette.inversePrimary
                            5 -> palette.error
                            else -> palette.primary
                        }
                    )
            ) {
                Image (
                    painter = painterResource(notificationIcon),
                    contentDescription = "Notification typology",
                    colorFilter = ColorFilter.tint(palette.onSurface),
                    modifier = Modifier.fillMaxSize(0.7f)
                )
            }
        }

        Spacer(Modifier.width(8.dp))

        Column (
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            // Sender
            Row (
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text (
                    senderName,
                    style = typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(5.dp))

            // Notification body
            // Notification body
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (notification.typology.toInt() == 3) {
                    Column(
                        modifier = Modifier.padding(end = 10.dp)
                    ) {
                        Row {
                            val parts = notification.body.split("*")
                            parts.forEachIndexed { index, part ->
                                if (index == 1) {
                                    Text(
                                        text = part,
                                        style = typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                if (index == 2) {
                                    Text(
                                        text = part,
                                        style = typography.bodySmall,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                        val parts = notification.body.split("*")
                        if (parts.size > 3) {
                            Text(
                                text = parts[3],
                                style = typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }  else if(notification.typology.toInt() == 2) {
                    Row (
                        modifier = Modifier.padding(end = 10.dp)
                    ) {
                        val parts = notification.body.split("*")
                        parts.forEachIndexed { index, part ->
                            if (index == 1) {
                                Text(
                                    text = part,
                                    style = typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if(index == 2) {
                                Text(
                                    text = part,
                                    style = typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if(index == 3){
                                Text(
                                    text = part,
                                    style = typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
                else if(notification.typology.toInt() == 4) {
                    Column(
                    ) {
                        val parts = notification.body.split("*")
                        parts.forEachIndexed { index, part ->
                            if (index == 1) {
                                Text(
                                    text = part,
                                    style = typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    maxLines = Int.MAX_VALUE, // Allow wrapping to multiple lines
                                )
                            }
                            if (index == 2){
                                Text(
                                    text = part.trim(),
                                    style = typography.bodySmall,
                                    maxLines = Int.MAX_VALUE, // Allow wrapping to multiple lines
                                )
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.padding(end = 10.dp)
                    ) {
                        val parts = notification.body.split("*")
                        parts.forEachIndexed { index, part ->
                            if (index == 1) {
                                Text(
                                    text = part,
                                    style = typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            } else {
                                Text(
                                    text = part,
                                    style = typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }



            // Hour
            Row (
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text (
                    notification.timestamp.split("T")[1].split("+")[0].slice(IntRange(0,4)),
                    style = typography.bodySmall,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}
