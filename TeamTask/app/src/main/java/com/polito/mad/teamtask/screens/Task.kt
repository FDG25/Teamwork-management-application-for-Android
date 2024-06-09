package com.polito.mad.teamtask.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.polito.mad.teamtask.components.tasks.Replies
import com.polito.mad.teamtask.components.tasks.Tab4Screen

@Composable
fun ShowTaskDetails(
    teamId: String,
    taskId: String
    //task: Task
) {
    //val palette = MaterialTheme.colorScheme
    //val typography = TeamTaskTypography

    Scaffold {
            Box(modifier = Modifier.padding(it)) {
                Tab4Screen(
                    tabs = listOf("Comments", "Info", "Description", "People"),
                    teamId,
                    taskId,
                )
            }
    }
}