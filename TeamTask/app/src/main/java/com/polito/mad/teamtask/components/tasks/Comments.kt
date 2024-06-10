package com.polito.mad.teamtask.components.tasks

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.polito.mad.teamtask.Actions
import com.polito.mad.teamtask.AppModel
import com.polito.mad.teamtask.ParametricFactory
import com.polito.mad.teamtask.chat.visualization.MemberTag
import com.polito.mad.teamtask.chat.visualization.UriCouple
import com.polito.mad.teamtask.components.tasks.components.CommentObject
import com.polito.mad.teamtask.components.tasks.components.Comment
import com.polito.mad.teamtask.components.tasks.components.SendObject
import com.polito.mad.teamtask.components.tasks.components.WriteComment
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class CommentsViewModel(
    model: AppModel,
    teamId: String,
    taskId: String,
) : ViewModel() {
    private val myModel = model
    private val myTeamId = teamId
    private val myTaskId = taskId

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

                    if (it.second.image.isNotEmpty() && isNetworkAvailable(myModel.applicationContext))
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

    val commentsStateFlow: StateFlow<List<CommentObject>> = model.getCommentsByTask(taskId)
        .combine(membersStateFlow) { comments, members ->
            comments.map { comment ->
                val messageDateTime = LocalDateTime.parse(
                    comment.second.timestamp,
                    DateTimeFormatter.ISO_DATE_TIME
                )

                val user: MemberTag? = if (members.isNotEmpty())
                    members.firstOrNull { member -> member.memberId == comment.second.senderId }
                else null

                CommentObject(
                    id = comment.first,
                    profilePic = user?.profilePic ?: Uri.EMPTY,
                    username = user?.username ?: "",
                    role = "",
                    text = comment.second.body ?: "",
                    date = messageDateTime,
                    attachments = if (!comment.second.media.isNullOrBlank()) {
                        Gson().fromJson(comment.second.media, Array<String>::class.java)
                            .map { uriString ->
                                UriCouple(
                                    try {
                                        if (isNetworkAvailable(myModel.applicationContext)) {
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
                    repliesNumber = comment.second.replies.size,
                    areRepliesOn = comment.second.repliesAllowed,
                    clientComment = comment.second.senderId == (FirebaseAuth.getInstance().currentUser?.uid
                        ?: ""),
                    isInformation = comment.second.information,
                )
            }
        }
        .transform {
            emit(it.sortedBy { comment -> comment.date })
        }
        .stateIn(
            scope = viewModelScope,
            initialValue = emptyList(),
            started = SharingStarted.WhileSubscribed(5000L)
        )

    fun addComment(comment: SendObject) {
        myModel.addComment(teamId = myTeamId, taskId = myTaskId, comment = comment)
    }

    fun deleteComment(commentId: String) {
        myModel.deleteComment(commentId)
    }

    fun changeAreRepliesOn(commentId: String) {
        myModel.changeRepliesOnComment(commentId)
    }

    fun editComment(comment: SendObject) {
        if (comment.id != null) {
            myModel.editComment(teamId = myTeamId, taskId = myTaskId, comment = comment)
        }
    }

    fun goToReplies(commentId: String, areRepliesOn: Boolean) {
        Actions.getInstance().goToTaskReplies(myTeamId, myTaskId, commentId, areRepliesOn)
    }
}


@Preview
@Composable
fun Comments(
    teamId: String = "",
    taskId: String = "",
    vm: CommentsViewModel = viewModel(
        factory = ParametricFactory(
            LocalContext.current,
            teamId,
            taskId,
            ""
        )
    )
) {
    val palette = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    val comments by vm.commentsStateFlow.collectAsState()
    val recompose = vm.recomposeParent

    val listState = rememberLazyListState()

    LaunchedEffect(comments, recompose) {
        if (comments.isNotEmpty()) {
            listState.animateScrollToItem(index = comments.size - 1)
        }
    }

    Scaffold(
        bottomBar = {
            WriteComment(
                onSend = vm::addComment,
                onEdit = vm::editComment,
                isComment = true,
                commentId = null,
                senderId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                taskId = taskId
            )
        },
    ) { innerPadding ->
        if (comments.isNotEmpty()) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp)
            ) {
                comments.forEach { comment ->
                    item {
                        Comment(
                            comment,
                            onDelete = vm::deleteComment,
                            editAreRepliesOn = vm::changeAreRepliesOn,
                            recomposeParent = vm::setRecomposeParent,
                            commentsVM = vm
                        )
                        Spacer(modifier = Modifier.size(16.dp))
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Nothing here, start writing a comment",
                    style = typography.bodyLarge,
                    color = palette.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || networkCapabilities.hasTransport(
        NetworkCapabilities.TRANSPORT_CELLULAR
    )
}
