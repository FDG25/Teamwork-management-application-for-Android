package com.polito.mad.teamtask.components

import androidx.compose.foundation.Image
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import com.polito.mad.teamtask.Actions
import com.polito.mad.teamtask.R
import com.polito.mad.teamtask.ui.theme.TeamTaskTypography

@Composable
fun FloatingButton (
    navController: NavController
) {
    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography

    val currentRoute = Actions.getInstance().getCurrentRoute()

    when (currentRoute) {
        "teams" -> {
            // Floating Action Button at the bottom end
            FloatingActionButton(
                onClick = Actions.getInstance().goToCreateTeamInfo,
                containerColor = palette.secondary
            ) {
                Image(
                    painter = painterResource(id = R.drawable.outline_add_24),
                    contentDescription = "Add",
                    colorFilter = ColorFilter.tint(palette.background)
                )
            }
        }
        "teams/{teamId}/tasks" -> {
            val teamId = Actions.getInstance().getStringParameter("teamId")

            FloatingActionButton(
                onClick = {
                    if (teamId != null) {
                        Actions.getInstance().goToEditTeamPeople(teamId)
                        //Actions.getInstance().goToCreateTaskStatus(teamId)
                    }
                },
                containerColor = palette.secondary
            ) {
                Image(
                    painter = painterResource(id = R.drawable.outline_person_add_24),
                    contentDescription = "Add",
                    colorFilter = ColorFilter.tint(palette.background)
                )
            }
        }
        "chats" -> {
            FloatingActionButton(
                onClick = {},
                containerColor = palette.secondary
            ) {
                Image(
                    painter = painterResource(id = R.drawable.outline_add_24),
                    contentDescription = "New message",
                    colorFilter = ColorFilter.tint(palette.background)
                )
            }
        }
        else -> {


        }
    }
}
