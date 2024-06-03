package com.polito.mad.teamtask.tasks

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.polito.mad.teamtask.tasks.components.Comment
import com.polito.mad.teamtask.tasks.components.CommentObject
import com.polito.mad.teamtask.tasks.components.WriteComment
import kotlinx.coroutines.flow.MutableStateFlow


class CommentsViewModel : ViewModel() {
    var comments = MutableStateFlow<List<CommentObject>>(
        listOf()
    )
        private set

    fun addComment(comment: CommentObject) {
        if(comment.text.isNotBlank() || comment.attachments.isNotEmpty()) {
            comments.value += comment
        }
    }

    fun deleteComment(commentId: String) {
        comments.value.toMutableList().apply {
            removeAt(indexOfFirst { it.id == commentId })
        }.let { updatedComments ->
            comments.value = updatedComments
        }
    }

    fun changeAreRepliesOn(commentId: String, value: Boolean) {
        comments.value.toMutableList().apply {
            set(indexOfFirst { it.id == commentId }, get(indexOfFirst { it.id == commentId }).copy(areRepliesOn = value))
        }.let { updatedComments ->
            comments.value = updatedComments
        }
    }

    fun editComment(comment: CommentObject) {
        comments.value.toMutableList().apply {
            set(indexOfFirst { it.id == comment.id }, comment)
        }.let { updatedComments ->
            comments.value = updatedComments
        }
    }
}


@Preview
@Composable
fun Comments(
    vm: CommentsViewModel = viewModel()
) {
    val palette = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    val comments by vm.comments.collectAsState()

    Scaffold(
        bottomBar = {
            WriteComment(
                onSend = vm::addComment,
                onEdit = vm::editComment,
                isComment = true, commentId = null
            )
        },
    ) { innerPadding ->
        if(comments.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp)
            ) {
                comments.forEach { comment ->
                    item {
                        Comment(comment, onDelete = vm::deleteComment, editAreRepliesOn = vm::changeAreRepliesOn)
                        Spacer(modifier = Modifier.size(16.dp))
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(),
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
