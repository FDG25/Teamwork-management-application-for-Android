package com.polito.mad.teamtask.components.tasks

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.polito.mad.teamtask.AppModel
import com.polito.mad.teamtask.ParametricFactory
import com.polito.mad.teamtask.chat.visualization.MemberTag
import com.polito.mad.teamtask.chat.visualization.UriCouple
import com.polito.mad.teamtask.components.tasks.components.CommentObject
import com.polito.mad.teamtask.components.tasks.components.Reply
import com.polito.mad.teamtask.components.tasks.components.ReplyObject
import com.polito.mad.teamtask.components.tasks.components.SendObject
import com.polito.mad.teamtask.components.tasks.components.WriteComment
import com.polito.mad.teamtask.ui.theme.TeamTaskTypography
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class RepliesViewModel(
    val model: AppModel,
    val teamId: String,
    val taskId: String,
    val commentId: String
) : ViewModel() {
    var recomposeParent by mutableStateOf(false)
        private set

    fun setRecomposeParent() {
        recomposeParent = !recomposeParent
    }

    private var membersStateFlow: StateFlow<List<MemberTag>> = model.getPeopleByTeamId(teamId)
        .map { peopleList ->
            peopleList
                .map {
                    var imageUri: Uri? = null

                    if (it.second.image.isNotEmpty() && isNetworkAvailable(model.applicationContext))
                        imageUri =
                            FirebaseStorage.getInstance().reference.child("profileImages/${it.second.image}").downloadUrl.await()

                    MemberTag(
                        it.first,
                        it.second.username,
                        imageUri ?: Uri.parse(""),
                    )
                }
        }
        .stateIn(
            scope = viewModelScope,
            initialValue = emptyList(),
            started = SharingStarted.WhileSubscribed(5000L)
        )

    var replies = model.getTaskRepliesByCommentId(commentId)
        .combine(membersStateFlow) { replies, members ->
            replies.map { reply ->
                val messageDateTime = LocalDateTime.parse(
                    reply.second.timestamp,
                    DateTimeFormatter.ISO_DATE_TIME
                )

                val user: MemberTag? = if (members.isNotEmpty())
                    members.first { member -> member.memberId == reply.second.senderId }
                else null

                ReplyObject(
                    id = reply.first,
                    commentId = reply.second.commentId,
                    profilePic = user?.profilePic ?: Uri.EMPTY,
                    username = user?.username ?: "",
                    role = "",
                    text = reply.second.body,
                    date = messageDateTime,
                    attachments = if (!reply.second.media.isNullOrBlank()) {
                        Gson().fromJson(reply.second.media, Array<String>::class.java)
                            .map { uriString ->
                                UriCouple(
                                    try {
                                        if (isNetworkAvailable(model.applicationContext)) {
                                            FirebaseStorage.getInstance().reference.child(
                                                uriString
                                            ).downloadUrl.await()
                                        } else null
                                    } catch (e: Exception) {
                                        null
                                    },
                                    uriString
                                )
                            }.toSet()
                    } else null,
                    clientReply = reply.second.senderId == (FirebaseAuth.getInstance().currentUser?.uid
                        ?: "")
                )
            }
        }
        .transform {
            emit(it.sortedBy { reply -> reply.date })
        }
        .stateIn(
            scope = viewModelScope,
            initialValue = emptyList(),
            started = SharingStarted.WhileSubscribed(5000L)
        )

    fun addReply(reply: SendObject) {
        reply.commentId = commentId
        model.addReply(teamId, taskId, reply)
    }

    fun deleteReply(replyId: String) {
        model.deleteReply(replyId)
    }

    fun editReply(reply: SendObject) {
        reply.commentId = commentId
        model.editReply(teamId, taskId, reply)
    }
}


@Composable
fun Replies(
    teamId: String,
    taskId: String,
    commentId: String,
    areRepliesOn: Boolean,
    vm: RepliesViewModel = viewModel(factory = ParametricFactory(LocalContext.current, teamId, taskId, commentId))
) {
    val replies by vm.replies.collectAsState()
    val recompose = vm.recomposeParent

    val listState = rememberLazyListState()

    LaunchedEffect(replies, recompose) {
        if (replies.isNotEmpty()) {
            listState.animateScrollToItem(index = replies.size - 1)
        }
    }

    Scaffold(
        bottomBar = {
            if (areRepliesOn) {
                WriteComment(
                    onSend = vm::addReply, onEdit = vm::editReply,
                    isComment = false, commentId = commentId,
                    senderId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                    taskId = taskId
                )
            }
        },
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.padding(innerPadding)
        ) {
            item { Spacer(modifier = Modifier.size(16.dp)) }

            replies.forEach { reply ->
                item {
                    Box(modifier = Modifier.padding(start = 16.dp, end = 16.dp)) {
                        Reply(reply, vm::deleteReply, recomposeParent = vm::setRecomposeParent)
                    }
                    Spacer(modifier = Modifier.size(16.dp))
                }
            }

            if(replies.isEmpty() && areRepliesOn)
                item {
                    Box(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "No Replies yet. Be the first to reply to this comment!",
                            style = TeamTaskTypography.displayLarge,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

            if (!areRepliesOn) {
                item {
                    Box(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Replies have been disabled by the owner of this comment.",
                            style = TeamTaskTypography.displayLarge,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

    }
}