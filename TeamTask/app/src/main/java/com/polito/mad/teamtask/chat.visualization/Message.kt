package com.polito.mad.teamtask.chat.visualization

import android.content.res.Configuration
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.polito.mad.teamtask.R
import com.polito.mad.teamtask.components.FileElement
import com.polito.mad.teamtask.components.FileToDownload
import com.polito.mad.teamtask.utils.isFileAlreadyDownloaded
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.random.Random

data class TaskTag(
    val taskId: String,
    val taskTitle: String,
    val isClickable: Boolean = true
)

data class UriCouple(
    val firebaseUri: Uri?,
    val firebaseRelativePath: String //it is the same path used for save files in the app cache from context.filesDir
)

abstract class Message(
    open val id: String,
    open val body: AnnotatedString,
    open val date: LocalDateTime,
    open val files: Set<UriCouple>? = null,
    open val taskTag: TaskTag? = null
)

data class GroupMessage(
    override val id: String,
    override val body: AnnotatedString,
    override val date: LocalDateTime,
    val profilePic: Uri,
    val username: String,
    val usernameColor: Color,
    override val files: Set<UriCouple>? = null,
    override val taskTag: TaskTag? = null,
) : Message(
    id,
    body,
    date,
    files,
    taskTag
)

data class ClientMessage(
    override val id: String,
    override val body: AnnotatedString,
    override val date: LocalDateTime,
    override val files: Set<UriCouple>? = null,
    override val taskTag: TaskTag? = null,
) : Message(
    id,
    body,
    date,
    files
)

data class InterlocutorMessage(
    override val id: String,
    override val body: AnnotatedString,
    override val date: LocalDateTime,
    override val files: Set<UriCouple>? = null,
    override val taskTag: TaskTag? = null
) : Message(
    id,
    body,
    date,
    files,
    taskTag
)

@Preview
@Composable
fun <T : Message> Message(
    message: T =
        GroupMessage(
            Random.nextInt(100).toString(),
            AnnotatedString("Hi, I'm Luca. What are you doing?, please let me know it is important for me. :)))))"),
            LocalDateTime.now(),
            Uri.parse("android.resource://com.polito.mad.teamtask/drawable/person_4"),
            "luca_bianchi",
            Color.Blue,
            taskTag = TaskTag(
                "1",
                "Task 1 superLong name that should be cutted in the UI Task 1 superLong name that should be cutted in the UI Task 1 superLong name that should be cutted in the UI"
            )
        ) as T,
    recomposeParent: () -> Unit = {}
) {

    val palette = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography



    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = if (message is ClientMessage) Arrangement.End else Arrangement.Start,
    ) {

        if (message is GroupMessage) {
            Log.d("ProfilePic", "ProfilePic: ${message.profilePic}")
            //Profile Pic
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(if(message.profilePic.toString().isNotBlank()) message.profilePic else null)
                    .error(R.drawable.avatar)
                    .crossfade(true)
                    .build(),
                placeholder = painterResource(R.drawable.avatar),
                contentDescription = "Team Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth(
                        (if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE)
                            0.05f else 0.1f)
                    )
                    .aspectRatio(1f)
                    .clip(CircleShape)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        //Message Box
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .clip(
                    shape =
                    if (message is ClientMessage)
                        RoundedCornerShape(15.dp, 15.dp, 0.dp, 15.dp)
                    else
                        RoundedCornerShape(0.dp, 15.dp, 15.dp, 15.dp)
                )
                .background(
                    if (message is ClientMessage) palette.primaryContainer
                    else palette.surfaceVariant
                )
                .padding(10.dp)
        ) {
            Column {
                //Author
                if (message is GroupMessage) {
                    // Author
                    Text(
                        modifier = Modifier
                            .wrapContentWidth(Alignment.Start, unbounded = false)
                            .widthIn(max = 100.dp, min = 0.dp),
                        text = message.username.ifEmpty { "Deleted user" },
                        style = typography.labelSmall.copy(
                            color = if(message.username.isNotEmpty()) message.usernameColor else palette.error,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(5.dp))
                }

                //TagSpace
                if (message.taskTag != null) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(shape = RoundedCornerShape(10.dp, 10.dp, 10.dp, 10.dp))
//                            .border(
//                                width = 1.dp,
//                                color = palette.onSurface,
//                                shape = RoundedCornerShape(10.dp, 10.dp, 10.dp, 10.dp)
//                            )
                            //todo routing to task page
                            .clickable {
                                if (message.taskTag!!.isClickable) { /*onTagClick(message.taskTag) */
                                }
                            }
                            .padding(5.dp)
                    ) {
                        Icon(
                            tint = palette.onSurface,
                            painter = painterResource(id = R.drawable.outline_work_outline_24),
                            contentDescription = "taskTag",
                            modifier = Modifier.size(25.dp)
                        )

                        Spacer(modifier = Modifier.width(5.dp))

                        //Tag Name
                        Text(
                            modifier = Modifier
                                .wrapContentWidth(Alignment.Start, unbounded = false),
                            text = message.taskTag!!.taskTitle,
                            style = typography.labelSmall.copy(
                                color = palette.onSurface,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal,
                                fontStyle = FontStyle.Italic
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    //Another Spacer
                    Spacer(modifier = Modifier.height(5.dp))
                }

                if (message.body.isNotEmpty())
                //Body Message
                    ClickableText(
                        text = message.body,
                        style = typography.labelSmall.copy(
                            color = palette.onSurface,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        onClick = { offset ->
                            message.body.getStringAnnotations("TAG", offset, offset)
                                .firstOrNull()?.let {
                                    //onTagClick(it.item)
                                }
                        }
                    )

                //Files
                if (message.files != null) {
                    //Spacer
                    Spacer(modifier = Modifier.height(5.dp))
                    //File
                    message.files!!.forEachIndexed { index, uri ->
                        val isThereUri =
                            isFileAlreadyDownloaded(uri.firebaseRelativePath, LocalContext.current)
                        if (isThereUri != null) {
                            //File is already downloaded
                            FileElement(isThereUri, recomposeParent)
                            if (index != (message.files!!.size - 1)) {
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                        } else {
                            //File should be downloaded
                            FileToDownload(uri.firebaseRelativePath, recomposeParent)

                            if (index != (message.files!!.size - 1)) {
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                //Date
                Box(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Text(
                        text = message.date.format(
                            DateTimeFormatter.ofPattern(
                                if (isMessageDateDifferentFromToday(
                                        message
                                    )
                                ) "dd/MM/yyyy HH:mm" else "HH:mm"
                            )
                        ),
                        style = typography.labelSmall.copy(
                            color = palette.onSurfaceVariant,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Light
                        ),
                    )
                }
            }
        }
    }
}

fun isMessageDateDifferentFromToday(message: Message): Boolean {
    val messageDate =
        Instant.ofEpochMilli(message.date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

    val currentDate = LocalDate.now()

    return messageDate != currentDate
}