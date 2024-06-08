package com.polito.mad.teamtask.components.tasks

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.polito.mad.teamtask.components.tasks.components.Reply
import com.polito.mad.teamtask.components.tasks.components.ReplyObject
import com.polito.mad.teamtask.components.tasks.components.SendObject
import com.polito.mad.teamtask.components.tasks.components.WriteComment
import com.polito.mad.teamtask.ui.theme.TeamTaskTypography
import kotlinx.coroutines.flow.MutableStateFlow


class RepliesViewModel : ViewModel() {
    var recomposeParent by mutableStateOf(false)
        private set

    fun setRecomposeParent() {
        recomposeParent = !recomposeParent
    }

    var commentValues by mutableStateOf(Pair("", true))
        private set

    fun setMyCommentValues(value: String, isRepliesView: Boolean) {
        commentValues = Pair(value, isRepliesView)
    }

    var replies = MutableStateFlow<Map<String, MutableSet<ReplyObject>>>(mapOf())
        private set

    fun addReply(reply: SendObject) {
        //todo: add reply to the database
    }

    fun deleteReply(commentId: String, replyId: String) {
        //todo: delete reply from the database
    }

    fun editReply(reply: SendObject) {
        //todo: edit reply in the database
    }
}


@Composable
fun Replies(
    teamId: String = "",
    taskId: String = "",
    commentId: String,
    vm: RepliesViewModel = viewModel()
) {
    //val typography = TeamTaskTypography

    val replies by vm.replies.collectAsState()

    val recompose = vm.recomposeParent

    Scaffold(
        bottomBar = {
            if (vm.commentValues.second) {
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
            modifier = Modifier.padding(innerPadding)
        ) {
            if (vm.commentValues.second || replies[vm.commentValues.first]?.isNotEmpty() == true) {
                item {
                    Text(
                        text = "Replies",
                        style = TeamTaskTypography.titleMedium,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            replies[vm.commentValues.first]?.toList()?.sortedBy { it.date }?.forEach { reply ->
                item {
                    Box(modifier = Modifier.padding(start = 16.dp, end = 16.dp)) {
                        Reply(reply, vm::deleteReply, recomposeParent = vm::setRecomposeParent)
                    }
                    Spacer(modifier = Modifier.size(16.dp))
                }
            }

            if (!vm.commentValues.second) {
                item {
                    Box(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Replies are disabled for this comment.",
                            style = TeamTaskTypography.titleMedium,
                            fontWeight = FontWeight.Normal,
                        )
                    }
                }
            }
        }

    }
}