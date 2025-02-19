package com.polito.mad.teamtask

import android.net.Uri
import android.util.Log
import com.polito.mad.teamtask.screens.PersonData
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.firebase.storage.FirebaseStorage
import com.polito.mad.teamtask.components.ProgressBar
import com.polito.mad.teamtask.ui.theme.TeamTaskTypography
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.math.truncate

data class StatisticsDataPerson(
    val person: PersonData,
    val totalTasksAssigned: Int,
    val totalTasksCompleted: Int
)

data class teamData(
    val totalPeople: Int,
    val totalTasks: Int,
    val totalTasksCompleted: Int,
    val people: List<StatisticsDataPerson>
)

class TeamStatisticsViewModel : ViewModel() {
    /*
    var teamData by mutableStateOf(
        teamData(
            totalPeople = 4,
            totalTasks = 20,
            totalTasksCompleted = 10,
            people = listOf(
                StatisticsDataPerson(
                    PersonData("0", "Luca", "Bianchi", "luca_bianchi", "CEO", "Owner", ""),
                    totalTasksAssigned = 5,
                    totalTasksCompleted = 3
                ),
                StatisticsDataPerson(
                    PersonData("1", "Mario", "Rossi", "username1", "CTO", "Admin", ""),
                    totalTasksAssigned = 6,
                    totalTasksCompleted = 5
                ),
                StatisticsDataPerson(
                    PersonData("2", "Sofia", "Esposito", "sofia_esposito", "Marketing Director", "", ""),
                    totalTasksAssigned = 4,
                    totalTasksCompleted = 4
                ),
                StatisticsDataPerson(
                    PersonData("3", "Giulia", "Ricci", "giulia_ricci", "HR Manager", "", ""),
                    totalTasksAssigned = 5,
                    totalTasksCompleted = 2
                )
            )
        )
    )
    */

    private val storage = FirebaseStorage.getInstance()
    private val _personImages = MutableStateFlow<Map<String, Uri?>>(emptyMap())
    val personImages: StateFlow<Map<String, Uri?>> = _personImages

    fun fetchPersonImage(imageName: String, personId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val imageRef = storage.reference.child("profileImages/$imageName").downloadUrl.await()

                if (imageRef != null) {
                    _personImages.value =
                        _personImages.value.toMutableMap().apply { put(personId, imageRef) }
                } else {
                    _personImages.value = _personImages.value.toMutableMap().apply { put(personId, null) }
                }
            } catch (e: Exception) {
                _personImages.value = _personImages.value.toMutableMap().apply { put(personId, null) }
            }
        }
    }
}


@Composable
fun TeamPerformances(
    teamParticipants: List<TeamParticipant>,
    people: List<Pair<String, Person>>,
    tasks: List<Pair<String, Task>>,
    teamStatisticsVM: TeamStatisticsViewModel = viewModel()
) {
    val typography = TeamTaskTypography
    val palette = MaterialTheme.colorScheme

    val totalMembers = teamParticipants
        .map { tp -> tp.personId }
        .distinct().count()
    val totalTasks = tasks.count()
    val completedTasks = tasks
        .filter { t -> t.second.status.equals("Completed") }
        .distinct().count()

    val members = mutableListOf<StatisticsDataPerson>()
    teamParticipants.forEach { tp ->
        val personId = tp.personId
        val role = tp.role
        val permission = ""
        val image = tp.personId
        val totalTasksAssigned = tp.totalTasks
        val totalTasksCompleted = tp.completedTasks
        val person = people.firstOrNull { p -> p.first.equals(tp.personId) }

        if (person!=null) {
            val name = person.second.name
            val surname = person.second.surname
            val username = person.second.username

            val pd = PersonData(personId, name, surname, username, role, permission, image)
            val sdp = StatisticsDataPerson(pd, totalTasksAssigned.toInt(), totalTasksCompleted.toInt())
            members.add(sdp)
        }
    }

    val allMembersHaveZeroCompleted = members.all { it.totalTasksCompleted == 0 }

    val bestMember = if (allMembersHaveZeroCompleted) {
        members.toList().maxByOrNull { p ->
            p.totalTasksAssigned.toDouble()
        }!!
    } else {
        members.toList().maxByOrNull { p ->
            if (p.totalTasksAssigned == 0) {
                0.0
            } else {
                p.totalTasksCompleted.toDouble() / p.totalTasksAssigned
            }
        }!!
    }


    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
            .padding(horizontal = 16.dp)
    ) {
        item{Spacer(Modifier.height(10.dp))}
        item {
            Row (
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "Overall Team Statistics",
                    style = typography.titleMedium
                )
            }
        }

        item { Spacer(modifier = Modifier.height(6.dp)) }

        item {
            Row (
                horizontalArrangement = Arrangement.SpaceBetween,
                //verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1.0f)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.people),
                        contentDescription = "Team people",
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Text(
                        //text = "Total members: ${teamStatisticsVM.teamData.totalPeople}",
                        text = "Total members: $totalMembers",
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        style = typography.bodySmall
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .weight(1f)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.total_tasks),
                        contentDescription = "Total Tasks",
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Text(
                        //text = "Total Tasks: ${teamStatisticsVM.teamData.totalTasks}",
                        text = "Total Tasks: $totalTasks",
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        style = typography.bodySmall
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        /*
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .weight(1f)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.total_tasks),
                        contentDescription = "Total Tasks",
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Text(
                        //text = "Total Tasks: ${teamStatisticsVM.teamData.totalTasks}",
                        text = "Total Tasks: $totalTasks",
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        style = typography.bodySmall
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .weight(1f)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.tasks_completed),
                        contentDescription = "Tasks Completed",
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Text(
                        //text = "Tasks Completed: ${teamStatisticsVM.teamData.totalTasksCompleted}",
                        text = "Tasks Completed: $completedTasks",
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        style = typography.bodySmall
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
        */

        item {
            Row(
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "Best Member",
                    textAlign = TextAlign.Center,
                    style = typography.titleMedium
                )

            }
        }

        item { Spacer(modifier = Modifier.height(6.dp)) }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .background(palette.background, RoundedCornerShape(5.dp))
                    .border(1.dp, palette.secondary, RoundedCornerShape(5.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    // Account image
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .border(1.dp, palette.primary, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Log.e("propic", bestMember.person.personId)
                        Log.e("propic", bestMember.person.image)
                        val imageUri = teamStatisticsVM.personImages.collectAsState().value[bestMember.person.personId]

                        LaunchedEffect(bestMember.person.personId) {
                            teamStatisticsVM.fetchPersonImage(bestMember.person.image, bestMember.person.personId)
                        }

                        Log.e("propic", imageUri.toString())

                        if (imageUri != null) { // User set an image
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(imageUri)
                                    .error(R.drawable.avatar)
                                    .crossfade(true)
                                    .placeholder(R.drawable.avatar)
                                    //.error()
                                    .build(),
                                contentDescription = "Member Pic",
                                Modifier
                                    .size(48.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, palette.secondary, CircleShape)
                                    .background(palette.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (bestMember.person.name.isNotEmpty() && bestMember.person.surname.isNotEmpty()) "${bestMember.person.name[0].uppercaseChar()}${bestMember.person.surname[0].uppercaseChar()}"
                                    else if (bestMember.person.name.isNotEmpty()) "${bestMember.person.name[0].uppercaseChar()}"
                                    else "",
                                    color = palette.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Notification icon
                    IconButton(
                        onClick = { /* Nothing to do */ },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(color = palette.surfaceVariant)
                            .border(1.dp, palette.secondary, shape = CircleShape)
                            .align(Alignment.BottomEnd)
                            .size(18.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.trophy),
                            contentDescription = "Profile",
                            modifier = Modifier.fillMaxSize(0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Team or account name
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "${bestMember.person.name} ${bestMember.person.surname}",
                            style = typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        item {
            Row (
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "Members",
                    style = typography.titleMedium
                )
            }
        }

        item { Spacer(modifier = Modifier.height(6.dp)) }

        item {
            Column {
                //teamStatisticsVM.teamData.people
                members.sortedBy { it.person.name }
                    .forEach { p ->
                        val person = p.person
                        val imageUri = teamStatisticsVM.personImages.collectAsState().value[person.personId]

                        LaunchedEffect(person.personId) {
                            teamStatisticsVM.fetchPersonImage(person.image, person.personId)
                        }

                        TeamPeopleEntry(p, imageUri)
                    }
            }
        }
    }
}

@Composable
private fun TeamPeopleEntry (
    data: StatisticsDataPerson,
    imageUri: Uri?,
) {
    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography

    val performance: Double =
        if (data.totalTasksAssigned==0) truncate(0.toDouble())
        else truncate(data.totalTasksCompleted.toDouble()/data.totalTasksAssigned.toDouble() * 100)

    val userImages = listOf(
        R.drawable.person_1,
        R.drawable.person_2,
        R.drawable.person_3,
        R.drawable.person_4
    )

    // Person details
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .background(palette.background, RoundedCornerShape(5.dp))
            .border(1.dp, palette.secondary, RoundedCornerShape(5.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Account image
        if (imageUri != null) { // User set an image
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUri)
                    .error(R.drawable.avatar)
                    .crossfade(true)
                    .placeholder(R.drawable.avatar)
                    //.error()
                    .build(),
                contentDescription = "Member Pic",
                Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .border(1.dp, palette.secondary, CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .border(1.dp, palette.secondary, CircleShape)
                    .background(palette.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (data.person.name.isNotEmpty() && data.person.surname.isNotEmpty()) "${data.person.name[0].uppercaseChar()}${data.person.surname[0].uppercaseChar()}"
                    else if (data.person.name.isNotEmpty()) "${data.person.name[0].uppercaseChar()}"
                    else "",
                    color = palette.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(
            modifier = Modifier
                .weight(2f)
                .padding(4.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column (
                    modifier = Modifier.width(220.dp)
                ) {
                    // Account name
                    Text(
                        text = "${data.person.name} ${data.person.surname}",
                        style = typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = palette.onSurface
                    )
                }
            }

            // Username
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = data.person.username,
                    style = typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = palette.onSurface
                )
            }

            // Role inside the team
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text =
                    if (data.person.role!="") data.person.role
                    else "Without a specific role",
                    style = typography.bodySmall,
                    fontStyle =
                    if (data.person.role!="") FontStyle.Normal
                    else FontStyle.Italic,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = palette.onSurface
                )
            }

            // Assigned Tasks
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Tasks assigned: ${data.totalTasksAssigned}",
                    style = typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = palette.onSurface
                )
            }

            // Completed Tasks
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Tasks completed: ${data.totalTasksCompleted}",
                    style = typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = palette.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Rocket performance icon
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Image(
                painter = painterResource(R.drawable.rocket),
                contentDescription = "Performance Image",
                colorFilter = ColorFilter.tint(palette.secondary),
                modifier = Modifier
                    .size(25.dp)
                    .align(Alignment.CenterHorizontally)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Performance $performance %",
                    style = typography.bodySmall,
                    color = palette.onSurface
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(10.dp))
}