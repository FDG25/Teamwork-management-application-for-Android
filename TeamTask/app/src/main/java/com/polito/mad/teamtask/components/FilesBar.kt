package com.polito.mad.teamtask.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.polito.mad.teamtask.R
import com.polito.mad.teamtask.ui.theme.TeamTaskTypography

private data class DoubleUri(
    val originalUri: Uri,
    val downloadedUri: Uri?
)

@Composable
fun FilesBar(files: Set<Uri>, removeAttachment: (Uri) -> Unit) {
    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography
    val context = LocalContext.current

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 8.dp, start = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        files.forEach { file ->
            item {
                if ((getFileNameWithExtension(
                        file,
                        context.contentResolver
                    )?.substringAfterLast('.', "") ?: "") in listOf(
                        "jpg",
                        "png",
                        "webp",
                        "gif",
                        "jpeg"
                    )
                ) {
                    // Image
                    if (file.toString().startsWith("content:/"))
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(palette.surfaceVariant),
                            contentAlignment = Alignment.Center,
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(file)
                                    .crossfade(true)
                                    .build(),
                                placeholder = painterResource(R.drawable.outline_camera_alt_24),
                                contentDescription = "Team Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .width(40.dp)
                            )

                            // Close button
                            IconButton(
                                onClick = {
                                    removeAttachment(file)
                                },
                                modifier = Modifier
                                    .size(30.dp)
                                    .align(Alignment.TopEnd)
                            ) {
                                Icon(
                                    tint = palette.surface,
                                    painter = painterResource(id = R.drawable.baseline_clear_24),
                                    contentDescription = "options",
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(palette.onSurface)
                                )
                            }
                        }
                    else {
                        // Other files
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(palette.surfaceVariant)
                                .padding(1.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(
                                modifier = Modifier
                                    .width(80.dp)
                                    .aspectRatio(1f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                // File icon
                                Icon(
                                    tint = palette.onSurface,
                                    painter = painterResource(
                                        id = formatToIcon(
                                            getFileNameWithExtension(
                                                file,
                                                context.contentResolver
                                            )
                                                ?: "File Not Found"
                                        )
                                    ),
                                    contentDescription = "options",
                                    modifier = Modifier.size(40.dp)
                                )

                                Spacer(modifier = Modifier.height(5.dp))

                                // File name
                                Text(
                                    text = getFileNameWithExtension(
                                        file,
                                        context.contentResolver
                                    )!!,
                                    style = typography.labelSmall.copy(
                                        color = palette.onSurface,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Normal,
                                        lineHeight = 10.sp
                                    ),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Close button
                            IconButton(
                                onClick = {
                                    removeAttachment(file)
                                },
                                modifier = Modifier
                                    .size(30.dp)
                                    .align(Alignment.TopEnd)
                            ) {
                                Icon(
                                    tint = palette.surface,
                                    painter = painterResource(id = R.drawable.baseline_clear_24),
                                    contentDescription = "options",
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(palette.onSurface)
                                )
                            }
                        }
                    }
                } else {
                    // Other files
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(palette.surfaceVariant)
                            .padding(1.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            modifier = Modifier
                                .width(80.dp)
                                .aspectRatio(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // File icon
                            Icon(
                                tint = palette.onSurface,
                                painter = painterResource(
                                    id = formatToIcon(
                                        getFileNameWithExtension(
                                            file,
                                            context.contentResolver
                                        )
                                            ?: "File Not Found"
                                    )
                                ),
                                contentDescription = "options",
                                modifier = Modifier.size(40.dp)
                            )

                            Spacer(modifier = Modifier.height(5.dp))

                            // File name
                            Text(
                                text = getFileNameWithExtension(
                                    file,
                                    context.contentResolver
                                )!!,
                                style = typography.labelSmall.copy(
                                    color = palette.onSurface,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Normal,
                                    lineHeight = 10.sp
                                ),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Close button
                        IconButton(
                            onClick = {
                                removeAttachment(file)
                            },
                            modifier = Modifier
                                .size(30.dp)
                                .align(Alignment.TopEnd)
                        ) {
                            Icon(
                                tint = palette.surface,
                                painter = painterResource(id = R.drawable.baseline_clear_24),
                                contentDescription = "options",
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(palette.onSurface)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}