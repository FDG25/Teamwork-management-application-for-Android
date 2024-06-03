package com.polito.mad.teamtask.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.polito.mad.teamtask.R
import com.polito.mad.teamtask.ui.theme.TeamTaskTypography
import com.polito.mad.teamtask.utils.downloadFileFromFirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun FileToDownload(
    myFile: String,
    recomposeParent: () -> Unit,
) {
    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography

    val context = LocalContext.current
    val fileName = myFile.substringAfterLast('/')

    var downloading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(palette.surface)
            .clickable {
                CoroutineScope(Dispatchers.IO).launch {
                    downloading = true
                    try {
                        downloadFileFromFirebaseStorage(myFile, context)
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast
                                .makeText(
                                    context,
                                    "${e.message}",
                                    Toast.LENGTH_SHORT
                                )
                                .show()
                        }
                    }
                    downloading = false
                    recomposeParent()
                }
            }
            .padding(6.dp)
            .fillMaxWidth(0.5f)
    ) {
//        when (fileName.substringAfterLast('.', "")) {
//            //Images
//            in listOf("jpg", "png", "webp") -> {
//                Column {
//                    AsyncImage(
//                        model = ImageRequest.Builder(LocalContext.current)
//                            .data(myFile)
//                            .crossfade(true)
//                            .build(),
//                        placeholder = painterResource(R.drawable.outline_camera_alt_24),
//                        contentDescription = "Team Image",
//                        contentScale = ContentScale.Crop,
//                        modifier = Modifier
//                            .aspectRatio(1f)
//                            .width(40.dp)
//                    )
//
//                    Spacer(modifier = Modifier.height(5.dp))
//
//                    Text(
//                        text = fileName,
//                        style = typography.labelSmall.copy(
//                            color = palette.onSurface,
//                            fontSize = 11.sp,
//                            fontWeight = FontWeight.Normal
//                        ),
//                        maxLines = 1,
//                        overflow = TextOverflow.Ellipsis
//                    )
//                }
//            } else -> {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (!downloading)
                Icon(
                    tint = palette.onSurface,
                    painter = painterResource(id = R.drawable.baseline_file_download_24),
                    contentDescription = "file",
                    modifier = Modifier.size(38.dp)
                )
            else
                CircularProgressIndicator(
                    color = palette.secondary,
                    modifier = Modifier.size(38.dp)
                )

            Spacer(modifier = Modifier.width(5.dp))

            Text(
                text = fileName,
                style = typography.labelSmall.copy(
                    color = palette.onSurface,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal
                ),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
//    }
//}