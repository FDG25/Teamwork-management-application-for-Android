package com.polito.mad.teamtask.components.tasks.components

import android.content.Context
import android.content.Intent
//import android.graphics.Bitmap
//import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
//import coil.size.Size
//import coil.transform.Transformation
import com.polito.mad.teamtask.R
import com.polito.mad.teamtask.ui.theme.TeamTaskTypography
//import java.io.ByteArrayOutputStream


@Composable
fun CommentFile(
    myFile: Uri
) {
    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography

    val context = LocalContext.current
    val fileName = getFileNameWithExtension(myFile, context.contentResolver) ?: "File not found"

    Log.d("CommentFile", "File: $fileName")

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(palette.surface)
            .clickable { openFile(context, myFile) }
            .padding(6.dp)
            .fillMaxWidth(0.5f)
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
                        placeholder = painterResource(R.drawable.outline_camera_alt_24),
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
                            color = palette.onSurface,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            } else -> {
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
    }
}


fun openFile(
    context: Context,
    file: Uri
) {
    /*val uri = FileProvider.getUriForFile(
        context,
        context.applicationContext.packageName + ".provider",
        file
    )*/

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
