package com.polito.mad.teamtask

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.polito.mad.teamtask.components.ProgressBar
import com.polito.mad.teamtask.ui.theme.TeamTaskTypography
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
    var teamData by mutableStateOf(teamData(
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
    ))
}
@Composable
fun TeamPerformances(
    teamStatisticsVM: TeamStatisticsViewModel = viewModel()
) {
    val typography = TeamTaskTypography
    val palette = MaterialTheme.colorScheme

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
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.people),
                    contentDescription = "Team people",
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Text(
                    text = "Total members: ${teamStatisticsVM.teamData.totalPeople}",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    style = typography.bodySmall
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

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
                        text = "Total Tasks: ${teamStatisticsVM.teamData.totalTasks}",
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
                        text = "Tasks Completed: ${teamStatisticsVM.teamData.totalTasksCompleted}",
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        style = typography.bodySmall
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Total number of tasks completed:",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    style = typography.bodySmall
                )
                Spacer(modifier = Modifier.height(7.dp))
                ProgressBar(
                    teamStatisticsVM.teamData.totalTasksCompleted,
                    teamStatisticsVM.teamData.totalTasks
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

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
                        Image(
                            painter = painterResource(id = R.drawable.person_1), // TODO: Adapt to actual person's image
                            contentDescription = "Account Image",
                            modifier = Modifier.size(48.dp)
                        )
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
                            "Mario Rossi", // TODO: Adapt to actual person's name
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
                teamStatisticsVM.teamData.people
                    .forEach { p -> TeamPeopleEntry(p) }
            }
        }
    }
}

@Composable
private fun TeamPeopleEntry (
    data: StatisticsDataPerson
) {
    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography

    val performance: Double = truncate(data.totalTasksCompleted.toDouble()/data.totalTasksAssigned.toDouble() * 100)

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
        Image( // TODO: Adapt to actual person's image
            painter = painterResource(id = when (data.person.name.length % 5) {
                1 -> userImages[0]
                2 -> userImages[1]
                3 -> userImages[2]
                4 -> userImages[3]
                else -> userImages[2]
            }),
            contentDescription = "Account Image",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
        )

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
                    text = data.person.role,
                    style = typography.bodySmall,
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