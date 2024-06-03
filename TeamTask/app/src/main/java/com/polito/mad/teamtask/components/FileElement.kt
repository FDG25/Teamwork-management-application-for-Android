package com.polito.mad.teamtask.components

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.input.pointer.pointerInput
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
import com.polito.mad.teamtask.utils.deleteFileFromUri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun FileElement(
    myFile: Uri,
    recomposeParent: () -> Unit
) {
    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography

    val context = LocalContext.current
    val fileName = getFileNameWithExtension(myFile, context.contentResolver) ?: "File not found"

    var dropdownMenuExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(palette.secondary)
            .padding(6.dp)
            .fillMaxWidth(0.5f)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        dropdownMenuExpanded = true
                    },
                    onTap = {
                        openFile(context, myFile)
                    }
                )
            }
    ) {
        when (fileName.substringAfterLast('.', "")) {
            //Images
            in listOf("jpg", "png", "webp") -> {
                Column {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(myFile)
                            .crossfade(true)
                            .build(),
                        placeholder = painterResource(R.drawable.image_placeholder),
                        contentDescription = "Team Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .aspectRatio(1f)
                            .width(40.dp)
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = fileName,
                        style = typography.labelSmall.copy(
                            color = palette.surface,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            else -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        tint = palette.onSurface,
                        painter = painterResource(id = formatToIcon(fileName)),
                        contentDescription = "file",
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
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        //DropDownMenu
            DropdownMenu(
                expanded = dropdownMenuExpanded,
                onDismissRequest = { dropdownMenuExpanded = false },
                modifier = Modifier.background(palette.background)
            ) {
                DropdownMenuItem(onClick = {
                    CoroutineScope(Dispatchers.IO).launch {
                        deleteFileFromUri(myFile, context)
                        recomposeParent()
                    }
                    dropdownMenuExpanded = false
                },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                tint = palette.error,
                                painter = painterResource(R.drawable.outline_delete_outline_24),
                                contentDescription = "Delete File from Device Storage",
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Delete File from Device Storage",
                                style = typography.labelSmall.copy(
                                    color = palette.error,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            )
                        }
                    }
                )
            }
    }
}


fun openFile(
    context: Context,
    file: Uri
) {
    val intent = Intent(Intent.ACTION_VIEW)

    intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

    intent.setDataAndType(
        file,
        when (getFileNameWithExtension(file, context.contentResolver)?.substringAfterLast(
            '.',
            ""
        )) {
            "jpg", "png", "webp" -> "image/*"
            "mp4", "3gp", "mkv", "avi" -> "video/*"
            "mp3", "wav", "aac", "flac" -> "audio/*"
            "pdf" -> "application/pdf"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            else -> "*/*"
        }
    )

    context.startActivity(Intent.createChooser(intent, "Open File"))
}

fun formatToIcon(fileName: String): Int {
    return when (fileName.substringAfterLast('.', "")) {
        "jpg", "png", "webp" -> R.drawable.outline_camera_alt_24
        "mp4", "3gp", "mkv", "avi" -> R.drawable.baseline_video_file_24
        "mp3", "wav", "aac", "flac", "opus" -> R.drawable.baseline_music_note_24
        "pdf" -> R.drawable.baseline_picture_as_pdf_24
        //"docx" -> R.drawable.outline_insert_drive_file_24
        //"xlsx" -> R.drawable.outline_insert_drive_file_24
        else -> R.drawable.baseline_insert_drive_file_24
    }
}

/*
class DownscaleImage(
    override val cacheKey: String = "Downscale",
    private val factor: Float,
    private val quality: Int
) : Transformation {
    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val targetWidth = (input.width * factor).toInt()
        val targetHeight = (input.height * factor).toInt()
        val scaledBitmap = Bitmap.createScaledBitmap(input, targetWidth, targetHeight, true)

        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        val compressedBytes = outputStream.toByteArray()

        return BitmapFactory.decodeByteArray(compressedBytes, 0, compressedBytes.size)
    }
}
 */

fun getFileNameWithExtension(
    uri: Uri,
    contentResolver: ContentResolver
): String? {
    var name: String? = null

    if (uri.scheme.equals("content")) {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor.use {
            if (it != null && it.moveToFirst()) {
                name =
                    it.getString(it.getColumnIndexOrThrow(android.provider.OpenableColumns.DISPLAY_NAME))
            }
        }
    }

    if (name == null) {
        name = uri.path
        val cut = name?.lastIndexOf('/')
        if (cut != -1) {
            if (cut != null) {
                name = name?.substring(cut + 1)
            }
        }
    }

    return name
}
