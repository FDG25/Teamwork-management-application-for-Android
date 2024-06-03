package com.polito.mad.teamtask.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.polito.mad.teamtask.R
import com.polito.mad.teamtask.Team
import com.polito.mad.teamtask.TeamParticipant
import com.polito.mad.teamtask.ui.theme.TeamTaskTypography


data class StatisticsData(
    val numberOfTeams: Int,
    val totalTasksCompleted: Int,
    val totalTasks: Int,
    val tasksCompletedPerTeam: List<TeamTaskData>
)

data class TeamTaskData(
    val teamName: String,
    val teamImage: Int,
    val teamRole: String,
    val tasksCompleted: Int,
    val totalTasks: Int
)

@Composable
fun Statistics(
    numTeams: Int,
    teams: List<Pair<String, Team>>, teamParticipants: List<TeamParticipant>,
    completedTasks: Int, totalTasks: Int,
    completedTasksPerTeam: List<Pair<String, Int>>, totalTasksPerTeam: List<Pair<String, Int>>
) {
    val typography = TeamTaskTypography
    val palette = MaterialTheme.colorScheme

    val ttdata = mutableListOf<TeamTaskData>()
    completedTasksPerTeam.forEach { (id, v) ->
        val t = teams.firstOrNull { t -> t.first.equals(id) }
        val tp = teamParticipants.firstOrNull { tp -> tp.teamId.equals(id) }
        val totTasks = totalTasksPerTeam.firstOrNull { pair -> pair.first.equals(id) }?.second
        if (t!=null && tp!=null && totTasks!=null) {
            ttdata.add(
                TeamTaskData(
                    t.second.name, R.drawable.teamtasklogo, // t.image.toInt(),
                    tp.role, v, totTasks
                )
            )
        }
    }

    val data = StatisticsData(numTeams, completedTasks, totalTasks, ttdata)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(palette.primaryContainer)
            .padding(top = 14.dp, start = 10.dp, end = 10.dp, bottom = 14.dp)
    ) {
        Text(
            "Your Statistics",
            style = typography.titleMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Number of teams: ${data.numberOfTeams}",
            style = typography.bodySmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                "Completed tasks:",
                style = typography.bodySmall
            )

            Spacer(modifier = Modifier.height(7.dp))

            ProgressBar(completedTasks, totalTasks)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                "Tasks completed per team: ${data.tasksCompletedPerTeam.size}",
                style = typography.bodySmall
            )
            data.tasksCompletedPerTeam.filter { it.totalTasks > 0 }.forEach {
                Spacer(modifier = Modifier.height(10.dp))
                TeamStatistics(
                    teamImage = it.teamImage,
                    teamName = it.teamName,
                    teamRole = it.teamRole,
                    taskCompleted = it.tasksCompleted,
                    totalTask = it.totalTasks
                )
            }
        }
    }
}


@Composable
fun TeamStatistics(
    teamImage: Int = R.drawable.teamtasklogo,
    teamName: String = "Team Rocket",
    teamRole: String = "Admin",
    taskCompleted: Int = 18, totalTask: Int = 30
) {
    val typography = TeamTaskTypography
    val palette = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(4.46f)
            .clip(RoundedCornerShape(8.dp))
            .background(palette.surface)
            .border(1.dp, palette.secondary, RoundedCornerShape(8.dp))
            .padding(6.dp)
    ) {
        Row (
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image (
                painter = painterResource(R.drawable.teamtasklogo), // painterResource(teamImage),
                contentDescription = "Team image"
            )

            /*
            AsyncImage (
                model = ImageRequest.Builder(LocalContext.current)
                    .data(teamImage)
                    .crossfade(true)
                    .build(),
                placeholder = painterResource(R.drawable.outline_camera_alt_24),
                contentDescription = "Team Image",
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
            )
            */

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Team name
                    Text(
                        teamName,
                        modifier = Modifier.weight(0.65f),
                        style = typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Role inside the team
                    Text(
                        teamRole,
                        modifier = Modifier.weight(0.25f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.End,
                        style = typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(15.dp))

                ProgressBar(taskCompleted, totalTask)
            }
        }
    }
}
