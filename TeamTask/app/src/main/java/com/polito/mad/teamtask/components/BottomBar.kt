package com.polito.mad.teamtask.components

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import com.polito.mad.teamtask.ui.theme.TeamTaskTypography
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.polito.mad.teamtask.Actions
import com.polito.mad.teamtask.R


@Composable
fun BottomBar (
    navController: NavHostController,
    goToPreviousStep: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    validateCreateTask: (String) -> Unit,
    clearTempState: () -> Unit, applyTempState: () -> Unit,
    sortTasks: () -> Unit,
    areThereNotificationsToRead: Boolean
) {
    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography
    val auth = FirebaseAuth.getInstance()
    val currentRoute = Actions.getInstance().getCurrentRoute()

    Log.e("prova", currentRoute.toString())
    auth.currentUser?.let { Log.e("prova", it.uid) }

    when (currentRoute) {
        "firstScreen" -> {}

        "profile/edit" -> {}

        "invite/{hash}" -> {}

        "teams/filter" -> {}

        "teams/{teamId}/filterTasks" -> {
            val teamId = Actions.getInstance().getStringParameter("teamId")

            BottomAppBar(
                containerColor = palette.background,
                contentColor = palette.secondary,
                modifier = Modifier.height(65.dp)
            ) {
                Row (
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Clear button
                    Column (
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Button(
                            onClick = {
                                clearTempState()
                                if (teamId != null) {
                                    Actions.getInstance().navigateBack()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = palette.primary,
                                contentColor = palette.secondary
                            )
                        ) {
                            Text(
                                "Clear",
                                style = typography.bodyMedium,
                                color = palette.secondary
                            )
                        }
                    }

                    // Apply button
                    Column (
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Button(
                            onClick = {
                                applyTempState()
                                sortTasks()
                                if (teamId != null) {
                                    Actions.getInstance().navigateBack()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = palette.secondary,
                                contentColor = palette.background
                            )
                        ) {
                            Text(
                                "Apply",
                                style = typography.bodyMedium,
                                color = palette.background
                            )
                        }
                    }
                }
            }
        }

        "teams/{teamId}/edit/status" -> {

        }


        "teams/{teamId}/newTask/status" -> {
            /*
            val teamId = Actions.getInstance().getStringParameter("teamId")


            Log.d("ciau", teamId.toString())

            BottomAppBar(
                containerColor = palette.background,
                contentColor = palette.background,
                modifier = Modifier.height(65.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Next/Create button
                    Button(
                        onClick = {
                            if (teamId != null) {
                                validateCreateTask(teamId)
                            }
                        },
                        modifier = Modifier.width(110.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = palette.secondary, contentColor = palette.background)
                    ) {
                        Text("Next")
                    }
                }
            }
            */
        }

        /*
        "teams/{teamId}/newTask/description" -> {
            val teamId = Actions.getInstance().getStringParameter("teamId")
            BottomAppBar(
                containerColor = palette.background,
                contentColor = palette.background,
                modifier = Modifier.height(65.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back button
                    Button(
                        onClick = {
                            if (teamId != null) {
                                goToPreviousStep()
                            }
                        },
                        enabled = true, // Disable back button on first step
                        modifier = Modifier.width(110.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = palette.primary, contentColor = palette.secondary)
                    ) {
                        Text("Back")
                    }
                    Spacer(modifier = Modifier.width(8.dp))


                    // Next/Create button
                    Button(
                        onClick = {
                            if (teamId != null) {
                                validateCreateTask(teamId, "people")
                            }
                        },
                        modifier = Modifier.width(110.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = palette.secondary, contentColor = palette.background)
                    ) {
                        Text("Next")
                    }
                }
            }
        }

        "teams/{teamId}/newTask/people" -> {
            val teamId = Actions.getInstance().getStringParameter("teamId")

            BottomAppBar(
                containerColor = palette.background,
                contentColor = palette.background,
                modifier = Modifier.height(65.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back button
                    Button(
                        onClick = {
                            if (teamId != null) {
                                goToPreviousStep()
                            }
                        },
                        enabled = true, // Disable back button on first step
                        modifier = Modifier.width(110.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = palette.primary, contentColor = palette.secondary)
                    ) {
                        Text("Back")
                    }
                    Spacer(modifier = Modifier.width(8.dp))


                    // Next/Create button
                    Button(
                        onClick = {
                            if (teamId != null) {
                                validateCreateTask(teamId, currentRoute)
                            }
                            onSearchQueryChanged("")
                        },
                        modifier = Modifier.width(110.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = palette.secondary, contentColor = palette.background)
                    ) {
                        Text("Create" )
                    }
                }
            }
        }
        */
        "chats/{isGroupChat}/{chatId}" -> {

        }

        else -> {
            BottomAppBar(
                containerColor = palette.primary,
                contentColor = palette.secondary,
                modifier = Modifier.height(65.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Home
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(onClick = Actions.getInstance().goToHome) {
                            Image(
                                painter = when (currentRoute) {
                                    "home" -> painterResource(id = R.drawable.baseline_home_24)
                                    else -> painterResource(id = R.drawable.outline_home_24)
                                },
                                contentDescription = "Home",
                                modifier = Modifier.size(32.dp),
                                colorFilter = ColorFilter.tint(palette.secondary),
                            )
                        }
                        Text("Home", style = typography.titleSmall,
                            fontWeight = when (currentRoute) {
                                "home" -> FontWeight.ExtraBold
                                else -> FontWeight.Light
                            },
                            color = palette.secondary,
                            modifier = Modifier.offset(y = (-10).dp))
                    }

                    // Teams
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(onClick = Actions.getInstance().goToTeams) {
                            Image(
                                painter = painterResource(
                                    id = when (currentRoute) {
                                        "teams",
                                        "teams/newTeam/info", "teams/newTeam/share", "teams/newTeam/qr",
                                        "teams/{teamId}/tasks", "teams/{teamId}/description", "teams/{teamId}/people",
                                        "teams/{teamId}/filterTasks", "teams/{teamId}/edit/description", "teams/{teamId}/edit/people",
                                        "teams/{teamId}/newTask/status", "teams/{teamId}/newTask/description", "teams/{teamId}/newTask/people",
                                        "teams/{teamId}/tasks/{taskId}/comments", "teams/{teamId}/tasks/{taskId}/comments/{commentId}", "teams/{teamId}/tasks/{taskId}/info", "teams/{teamId}/tasks/{taskId}/description", "teams/{teamId}/tasks/{taskId}/people",
                                        "teams/{teamId}/tasks/{taskId}/edit/info", "teams/{teamId}/tasks/{taskId}/edit/description", "teams/{teamId}/tasks/{taskId}/edit/people",
                                        "teams/{teamId}/statistics",
                                        -> R.drawable.baseline_groups_24

                                        else -> R.drawable.outline_groups_24
                                    }
                                ),
                                contentDescription = "Teams",
                                modifier = Modifier.size(34.dp),
                                colorFilter = ColorFilter.tint(palette.secondary),
                            )
                        }
                        Text("Teams",
                            style = typography.titleSmall,
                            fontWeight = when (currentRoute) {
                                "teams",
                                "teams/newTeam/info", "teams/newTeam/share", "teams/newTeam/qr",
                                "teams/{teamId}/tasks", "teams/{teamId}/description", "teams/{teamId}/people",
                                "teams/{teamId}/filterTasks", "teams/{teamId}/edit/description", "teams/{teamId}/edit/people",
                                "teams/{teamId}/newTask/status", "teams/{teamId}/newTask/description", "teams/{teamId}/newTask/people",
                                "teams/{teamId}/tasks/{taskId}/comments", "teams/{teamId}/tasks/{taskId}/comments/{commentId}", "teams/{teamId}/tasks/{taskId}/info", "teams/{teamId}/tasks/{taskId}/description", "teams/{teamId}/tasks/{taskId}/people",
                                "teams/{teamId}/tasks/{taskId}/edit/info", "teams/{teamId}/tasks/{taskId}/edit/description", "teams/{teamId}/tasks/{taskId}/edit/people"
                                -> FontWeight.Bold

                                else -> FontWeight.Light
                            },
                            color = palette.secondary,
                            modifier = Modifier.offset(y = (-10).dp))
                    }

                    // Chat
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(onClick = Actions.getInstance().goToChats) {
                            Image(
                                painter = painterResource(
                                    id = when (currentRoute) {
                                        "chats", "chats/{chatId}" -> R.drawable.baseline_chat_24
                                        else -> R.drawable.outline_chat_24
                                    }
                                ),
                                contentDescription = "Chat",
                                modifier = Modifier.size(28.dp),
                                colorFilter = ColorFilter.tint(palette.secondary),
                            )
                        }
                        Text("Chat", style = typography.titleSmall,
                            fontWeight = when (currentRoute) {
                                "chats", "chats/{chatId}" -> FontWeight.Bold
                                else -> FontWeight.Light
                            },
                            color = palette.secondary,
                            modifier = Modifier.offset(y = (-10).dp))
                    }

                    // Notifications
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box {
                            IconButton(onClick = Actions.getInstance().goToNotifications) {
                                Image(
                                    painter = painterResource(
                                        id =
                                        if (currentRoute == "notifications") R.drawable.baseline_notifications_24
                                        else R.drawable.outline_notifications_24
                                    ),
                                    contentDescription = "Notifications",
                                    modifier = Modifier.size(32.dp),
                                    colorFilter = ColorFilter.tint(palette.secondary),
                                )
                            }
                            if(areThereNotificationsToRead)
                                Box(
                                    modifier = Modifier
                                        .offset(x = 28.dp, y = (10).dp)
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(palette.error)
                                )
                        }
                        Text("Notifications", style = typography.titleSmall,
                            fontWeight = if (currentRoute == "notifications") FontWeight.Bold else FontWeight.Light,
                            color = palette.secondary,
                            modifier = Modifier.offset(y = (-10).dp))
                    }

                    // Profile
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(onClick = Actions.getInstance().goToProfile) {
                            Image(
                                painter = painterResource(
                                    id =
                                    if (currentRoute == "profile") R.drawable.baseline_person_24
                                    else R.drawable.outline_person_outline_24
                                ),
                                contentDescription = "Profile",
                                modifier = Modifier.size(32.dp),
                                colorFilter = ColorFilter.tint(palette.secondary),
                            )
                        }
                        Text("Profile", style = typography.titleSmall,
                            fontWeight = if (currentRoute == "profile") FontWeight.Bold else FontWeight.Light,
                            color = palette.secondary,
                            modifier = Modifier.offset(y = (-10).dp))
                    }
                }
            }
        }
    }
}
