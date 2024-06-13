package com.polito.mad.teamtask.components.tasks.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.polito.mad.teamtask.R
import com.polito.mad.teamtask.chat.visualization.UriCouple
import com.polito.mad.teamtask.chat.visualization.isMessageDateDifferentFromToday
import com.polito.mad.teamtask.components.FileElement
import com.polito.mad.teamtask.components.FileToDownload
import com.polito.mad.teamtask.components.tasks.CommentsViewModel
import com.polito.mad.teamtask.ui.theme.TeamTaskTypography
import com.polito.mad.teamtask.utils.isFileAlreadyDownloaded
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

interface TaskInteraction


data class CommentObject(
    val id: String,
    val profilePic: Uri,
    val username: String,
    val role: String,
    val text: String,
    val date: LocalDateTime,
    val attachments: Set<UriCouple>?,
    val repliesNumber: Int,
    val areRepliesOn: Boolean,
    val clientComment: Boolean = false,
    val isInformation: Boolean = false
) : TaskInteraction


@Composable
fun Comment(
    comment: CommentObject =
        CommentObject(
            "421fs",
            Uri.parse("android.resource://com.polito.mad.teamtask/drawable/image1"),
            "john.delafuente",
            "Owner",
            "This is a comment",
            LocalDateTime.now(),
            attachments = null,
            3,
            true
        ),
//    taskVM: TaskViewModel = viewModel(),
//    repliesVM: RepliesViewModel = viewModel(),
    writeCommentVM: WTViewModel = viewModel(),
    onDelete: (String) -> Unit = {},
    editAreRepliesOn: (String) -> Unit = {},
    recomposeParent: () -> Unit = {},
    commentsVM: CommentsViewModel
) {
    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography

    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .clip(RoundedCornerShape(5.dp))
            .fillMaxWidth()
    ) {
        if (!comment.isInformation) {
            Column {
                // Comment header
                Box(
                    modifier = Modifier
                        .height(55.dp)
                        .fillMaxWidth()
                        .background(
                            color = palette.primaryContainer
                        )
                        .padding(9.dp),
                    contentAlignment = Alignment.CenterStart
                )
                {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(comment.profilePic)
                                .error(R.drawable.avatar)
                                .crossfade(true)
                                .build(),
                            placeholder = painterResource(R.drawable.avatar),
                            contentDescription = "Team Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .fillMaxHeight()
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            // Author
                            Row {
                                Text(
                                    modifier = Modifier
                                        .wrapContentWidth(Alignment.Start, unbounded = false)
                                        .widthIn(max = 100.dp, min = 0.dp),
                                    text = comment.username.ifEmpty { "Deleted user" },
                                    style = typography.labelSmall.copy(
                                        color = palette.onSurface,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Normal
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.width(5.dp))

                                // Role
//                            Text(
//                                text = "(${comment.role})",
//                                style = typography.labelSmall.copy(
//                                    color = palette.onSurfaceVariant,
//                                    fontSize = 12.sp,
//                                    fontWeight = FontWeight.Light
//                                ),
//                                maxLines = 1,
//                                overflow = TextOverflow.Ellipsis
//                            )
                            }

                            //Date
                            Text(
                                text = comment.date.format(
                                    DateTimeFormatter.ofPattern(
                                        if (isMessageDateDifferentFromToday(
                                                comment.date
                                            )
                                        ) "dd/MM/yyyy HH:mm" else "HH:mm"
                                    )
                                ),
                                style = typography.labelSmall.copy(
                                    color = palette.onSurfaceVariant,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Light
                                )
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Forward button
                        IconButton(modifier = Modifier.size(30.dp),
                            onClick = { /*TODO*/ }) {
                            Icon(
                                tint = palette.onSurface,
                                painter = painterResource(id = R.drawable.baseline_arrow_forward_24),
                                contentDescription = "options",
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(5.dp))

                        // Options button
                        if (comment.clientComment)
                            IconButton(modifier = Modifier.size(30.dp),
                                onClick = { expanded = true }) {
                                Icon(
                                    tint = palette.onSurface,
                                    painter = painterResource(id = R.drawable.outline_more_vert_24),
                                    contentDescription = "options",
                                    modifier = Modifier.size(24.dp)
                                )

                                // Dropdown menu
                                DropdownMenu(
                                    modifier = Modifier.background(palette.background),
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false },
                                ) {
                                    // Edit
                                    DropdownMenuItem(onClick = {
                                        expanded = false
                                        writeCommentVM.goEditing(
                                            EditableObject(
                                                comment.id,
                                                null,
                                                comment.profilePic,
                                                comment.username,
                                                comment.role,
                                                comment.text,
                                                comment.date,
                                                comment.attachments ?: emptySet(),
                                                comment.repliesNumber
                                            )
                                        )
                                    },
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    tint = palette.onSurface,
                                                    painter = painterResource(id = R.drawable.outline_edit_24),
                                                    contentDescription = "edit",
                                                    modifier = Modifier.size(25.dp)
                                                )
                                                Spacer(modifier = Modifier.size(5.dp))
                                                Text(
                                                    "Edit",
                                                    style = typography.labelSmall.copy(
                                                        fontSize = 15.sp
                                                    )
                                                )
                                            }
                                        }
                                    )

                                    // Turn Off/On Replies
                                    DropdownMenuItem(onClick = {
                                        expanded = false
                                        editAreRepliesOn(comment.id)
                                    },
                                        text = {
                                            if (comment.areRepliesOn) {
                                                // Turn off replies
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        tint = palette.onSurface,
                                                        painter = painterResource(id = R.drawable.baseline_near_me_disabled_24),
                                                        contentDescription = "Turn off replies",
                                                        modifier = Modifier.size(25.dp)
                                                    )
                                                    Spacer(modifier = Modifier.size(5.dp))
                                                    Text(
                                                        "Turn Off Replies",
                                                        style = typography.labelSmall.copy(
                                                            fontSize = 15.sp
                                                        )
                                                    )
                                                }
                                            } else {
                                                // Turn on replies
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        tint = palette.onSurface,
                                                        painter = painterResource(id = R.drawable.baseline_reply_24),
                                                        contentDescription = "Turn on replies",
                                                        modifier = Modifier.size(25.dp)
                                                    )
                                                    Spacer(modifier = Modifier.size(5.dp))
                                                    Text(
                                                        "Turn On Replies",
                                                        style = typography.labelSmall.copy(
                                                            fontSize = 15.sp
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    )

                                    // Delete
                                    DropdownMenuItem(onClick = {
                                        onDelete(comment.id)
                                        expanded = false
                                    },
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    tint = palette.error,
                                                    painter = painterResource(id = R.drawable.outline_delete_outline_24),
                                                    contentDescription = "delete",
                                                    modifier = Modifier.size(25.dp)
                                                )

                                                Spacer(modifier = Modifier.size(5.dp))

                                                Text(
                                                    "Delete",
                                                    style = typography.labelSmall.copy(
                                                        fontSize = 15.sp
                                                    ),
                                                    color = palette.error
                                                )
                                            }
                                        }
                                    )
                                }
                            }
                    }
                }

                // Comment body
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = palette.surfaceVariant)
                        .padding(10.dp)
                ) {
                    // Comment body
                    if (comment.text.isNotEmpty()) {
                        Text(
                            text = comment.text,
                            style = typography.labelSmall.copy(
                                color = palette.onSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal
                            )
                        )
                    }

                    if (!comment.attachments.isNullOrEmpty() && comment.text.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    //Files
                    if (comment.attachments != null) {
                        //Spacer
                        Spacer(modifier = Modifier.height(5.dp))
                        //File
                        comment.attachments.forEachIndexed { index, uri ->
                            val isThereUri =
                                isFileAlreadyDownloaded(
                                    uri.firebaseRelativePath,
                                    LocalContext.current
                                )
                            if (isThereUri != null) {
                                //File is already downloaded
                                FileElement(isThereUri, recomposeParent)
                                if (index != (comment.attachments.size - 1)) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                }
                            } else {
                                //File should be downloaded
                                FileToDownload(uri.firebaseRelativePath, recomposeParent)

                                if (index != (comment.attachments.size - 1)) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Comment footer
            Box(
                modifier = Modifier
                    .height(50.dp)
                    .fillMaxWidth()
                    .background(color = palette.primaryContainer)
                    .padding(9.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${comment.repliesNumber} Replies",
                        style = typography.labelSmall.copy(
                            color = palette.onSurface,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal
                        ),
                    )

                    //Reply button
                    IconButton(modifier = Modifier.size(30.dp),
                        onClick = {
                            commentsVM.goToReplies(comment.id, comment.areRepliesOn)
                        }
                    ) {
                        Icon(
                            tint = palette.onSurface,
                            painter = painterResource(id = R.drawable.baseline_reply_24),
                            contentDescription = "options",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        } else {
            // Comment information body
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = palette.surfaceVariant)
                    .padding(10.dp),
                contentAlignment = Alignment.Center,
            ) {
                // Comment body
                if (comment.text.isNotEmpty()) {
                    Text(
                        text = comment.text,
                        style = typography.labelSmall.copy(
                            color = palette.onSurface,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            lineHeight = 20.sp
                        )
                    )
                }
            }
        }
    }
}