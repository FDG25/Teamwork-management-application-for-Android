package com.polito.mad.teamtask.screens

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ListResult
import com.polito.mad.teamtask.Actions
import com.polito.mad.teamtask.R
import com.polito.mad.teamtask.Task
import com.polito.mad.teamtask.Team
import com.polito.mad.teamtask.components.TaskEntry
import com.polito.mad.teamtask.components.TeamCard
import com.polito.mad.teamtask.ui.theme.TeamTaskTypography
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HomeViewModel : ViewModel() {
    private val storage = FirebaseStorage.getInstance()
    private val _teamImages = MutableStateFlow<Map<String, Uri?>>(emptyMap())
    val teamImages: StateFlow<Map<String, Uri?>> = _teamImages

    fun fetchTeamImage(imageName: String, teamId: String) {
        viewModelScope.launch {
            try {
                val imageRef = storage.reference.child("teamImages/$imageName").downloadUrl.await()

                if (imageRef != null) {
                    _teamImages.value =
                        _teamImages.value.toMutableMap().apply { put(teamId, imageRef) }
                } else {
                    _teamImages.value = _teamImages.value.toMutableMap().apply { put(teamId, null) }
                }
            } catch (e: Exception) {
                _teamImages.value = _teamImages.value.toMutableMap().apply { put(teamId, null) }
            }
        }
    }
}

@Composable
fun HomeScreen(
    filteredTeams: List<Pair<String, Team>>,
    teams: List<Pair<String, Team>>,
    filteredTasks: List<Pair<String, Task>>,
    tasks: List<Pair<String, Task>>,
    goToTask: (String, String) -> Unit,
    homeViewModel: HomeViewModel = viewModel()
) {
    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography

    val currentDateTime = LocalDateTime.now()
    val formatter = DateTimeFormatter.ISO_DATE_TIME

    val localTasks = filteredTasks
        .filter { pair ->
            val taskDeadline = LocalDateTime.parse(pair.second.deadline, formatter)
            //Log.d("TaskDeadline", taskDeadline.toString())
            taskDeadline.isAfter(currentDateTime)
        }
        .sortedBy { it.second.deadline }
        //.take(10)

    //Log.d("localTasks", localTasks.toString())


    val groupedTasks = localTasks.groupBy { it.second.deadline.split("T")[0] }

    //Log.d("GroupedTasks", "GroupedTasks: $groupedTasks")

    LazyColumn {
        // Favourite teams
        item {
            Text(
                text = "Favourite Teams",
                style = typography.titleMedium,
                color = palette.secondary,
                modifier = Modifier.padding(horizontal = 15.dp, vertical = 8.dp)
            )
        }

        if (filteredTeams.isEmpty()) {
            item {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        "No teams added to favourite \uD83C\uDF1F",
                        style = typography.labelMedium,
                        color = palette.onSurfaceVariant
                    )
                }
            }
        } else {
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 15.dp)
                ) {
                    filteredTeams.forEach { pair ->
                        item {
                            val team = pair.second
                            val imageUri = homeViewModel.teamImages.collectAsState().value[pair.first]

                            homeViewModel.fetchTeamImage(pair.second.image, pair.first)

                            TeamCard(team, imageUri = imageUri, teamId = pair.first)

                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(10.dp)) }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 15.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Scheduled Tasks",
                    style = typography.titleMedium,
                    color = palette.secondary,
                    modifier = Modifier.padding(horizontal = 15.dp)
                )
                // Calendar option
                IconButton(
                    onClick = {
                        Actions.getInstance().goToHomeCalendar()
                    }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.outline_calendar_month_24),
                        contentDescription = "Calendar month",
                        modifier = Modifier.size(30.dp),
                        colorFilter = ColorFilter.tint(palette.secondary)
                    )
                }
            }
        }

        if (groupedTasks.isEmpty()) {
            item {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        "Currently you don't have any task \uD83E\uDD71",
                        style = typography.labelMedium,
                        color = palette.onSurfaceVariant
                    )
                }
            }
        } else {
            item {
                Column {
                    groupedTasks.forEach { (date, tasks) ->
                        Text(
                            text = date,
                            style = typography.labelMedium,
                            modifier = Modifier
                                .padding(horizontal = 15.dp)
                                .padding(top = 8.dp, bottom = 5.dp)
                        )

                        tasks.forEach { pair ->
                            val team = teams.firstOrNull { it.first == pair.second.teamId }

                            Button(
                                onClick = { goToTask(pair.second.teamId, pair.first) },
                                shape = RoundedCornerShape(5.dp),
                                colors = ButtonDefaults.buttonColors(
                                    contentColor = Color.Transparent,
                                    containerColor = Color.Transparent
                                ),
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.padding(horizontal = 15.dp)
                            ) {
                                val imageUri = homeViewModel.teamImages.collectAsState().value[team?.first]

                                homeViewModel.fetchTeamImage(team?.second?.image ?: "" ,pair.second.teamId)

                                TaskEntry(pair.second, team?.second, imageUri )
                            }

                            Spacer(Modifier.height(5.dp))
                        }
                    }
                }
            }
        }
    }
}
