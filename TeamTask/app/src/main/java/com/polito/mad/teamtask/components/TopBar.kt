package com.polito.mad.teamtask.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.polito.mad.teamtask.Actions
import com.polito.mad.teamtask.Person
import com.polito.mad.teamtask.R
import com.polito.mad.teamtask.Task
import com.polito.mad.teamtask.Team
import com.polito.mad.teamtask.TeamParticipant
import com.polito.mad.teamtask.ui.theme.TeamTaskTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    navController: NavHostController,
    userId: String,
    setShwDeleteAccountModal: (Boolean) -> Unit,
    deleteAccount: () -> Unit,
    setShwLogoutModal: (Boolean) -> Unit,
    setShwDeleteTeamModal: (Boolean) -> Unit,
    deleteTeam: (String) -> Unit,
    setShwExitFromTeamModal: (Boolean) -> Unit,
    exitFromTeam: (String) -> Unit,
    addOrRemoveTeamToFavourites: (String, Boolean) -> Unit,
    showMenu: Boolean,
    setShowMen: (Boolean) -> Unit,
    showBackButtonModal: Boolean,
    setBackButtModal: (Boolean) -> Unit = {},
    goBackToPresentation: () -> Unit,
    editProfile: () -> Unit,
    validate: (String) -> Unit,
    cancelEditProfile: () -> Unit,
    goToPreviousStep: () -> Unit,
    cancelCreateTask: () -> Unit,
    people: List<Pair<String, Person>>,
    teams: List<Pair<String, Team>>,
    tasks: List<Pair<String, Task>>,
    teamParticipants: List<TeamParticipant>
) {
    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography

    val auth = FirebaseAuth.getInstance()


    val currentRoute = Actions.getInstance().getCurrentRoute()

    when (currentRoute) {
        "firstScreen" -> {}
        "home" -> {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.teamtasklogo),
                            contentDescription = "Logo",
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("TeamTask", style = typography.titleLarge)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(palette.primary)
            )
        }

        "homeCalendar" -> {
            CenterAlignedTopAppBar(
                // Back button
                navigationIcon = {
                    BackButton(onClick = { Actions.getInstance().navigateBack() })
                },

                // Title
                title = { Text("Personal Agenda", style = typography.titleLarge) },

                colors = TopAppBarDefaults.topAppBarColors(palette.primary),
            )
        }

        "teams" -> {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Teams", style = typography.titleLarge)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(palette.primary)
            )
        }

        "chats" -> {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Chats", style = typography.titleLarge)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(palette.primary)
            )
        }

        "notifications" -> {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Notifications", style = typography.titleLarge)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(palette.primary)
            )
        }

        "profile" -> {
            TopAppBar(
                // Title
                title = { Text("Profile", style = typography.titleLarge) },
                actions = {
                    // Dropdown
                    DropDownButton(onClick = { setShowMen(true) })

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { setShowMen(false) },
                        modifier = Modifier.background(palette.background)
                    ) {
                        // Edit profile
                        DropdownMenuItem(
                            text = { Text("Edit Profile", style = typography.headlineSmall) },
                            onClick = {
                                editProfile()
                                setShowMen(false)
                            },
                            leadingIcon = {
                                Image(
                                    painter = painterResource(id = R.drawable.outline_edit_24),
                                    contentDescription = "Edit Profile"
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Delete Account",
                                    style = typography.headlineSmall,
                                    color = palette.error
                                )
                            },
                            onClick = {
                                setShwDeleteAccountModal(true)
                                setShowMen(false)
                            },
                            leadingIcon = {
                                Image(
                                    painter = painterResource(id = R.drawable.outline_delete_outline_24),
                                    contentDescription = "Delete Account",
                                    colorFilter = ColorFilter.tint(palette.error)
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Logout",
                                    style = typography.headlineSmall,
                                    color = palette.inverseSurface
                                )
                            },
                            onClick = {
                                setShwLogoutModal(true)
                                setShowMen(false)
                            },
                            leadingIcon = {
                                Image(
                                    painter = painterResource(id = R.drawable.outline_logout_24),
                                    contentDescription = "Logout",
                                    colorFilter = ColorFilter.tint(palette.inverseSurface)
                                )
                            }
                        )

                        //TODO: Add the other dropdown options
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(palette.primary),
            )

            if (showMenu) {
                Box(
                    modifier = Modifier
                        .clickable(onClick = { setShowMen(false) })
                )
            }
        }

        "profile/edit" -> {
            CenterAlignedTopAppBar(
                // Back button
                navigationIcon = {
                    BackButton(onClick = { goBackToPresentation() })
                },

                // Title
                title = { Text("Edit Profile", style = typography.titleLarge) },

                // Confirm button
                actions = {
                    DoneButton(onClick = { validate(userId) })
                },
                colors = TopAppBarDefaults.topAppBarColors(palette.primary),
            )

            if (showBackButtonModal) {
                // Alert popup
                AlertDialog(
                    onDismissRequest = { setBackButtModal(false) },
                    // Title
                    title = {
                        Text(
                            text = "Attention",
                            style = typography.bodyMedium,
                            color = palette.secondary,
                            fontWeight = FontWeight.Bold
                        )
                    },

                    // Text
                    text = {
                        Text(
                            "Are you sure you want to go back? Changes might not be saved.",
                            style = typography.bodySmall,
                            color = palette.onSurfaceVariant
                        )
                    },

                    // Confirm button
                    confirmButton = {
                        Button(
                            onClick = {
                                setBackButtModal(false)
                                cancelEditProfile()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = palette.secondary)
                        ) {
                            Text(
                                "Confirm",
                                style = typography.bodySmall,
                                color = palette.background
                            )
                        }
                    },

                    // Dismiss button
                    dismissButton = {
                        Button(
                            onClick = {
                                setBackButtModal(false)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = palette.primary)
                        ) {
                            Text("Cancel", style = typography.bodySmall, color = palette.secondary)
                        }
                    },

                    containerColor = palette.background
                )
            }
        }

        "teams/{teamId}/edit/description" -> {
            CenterAlignedTopAppBar(
                // Back button
                navigationIcon = {
                    BackButton(onClick = { Actions.getInstance().navigateBack() })
                },

                // Title
                title = { Text("Edit Team Description", style = typography.titleLarge) },

                colors = TopAppBarDefaults.topAppBarColors(palette.primary),
            )
        }

        "teams/newTeam/info" -> {
            CenterAlignedTopAppBar(
                // Back button
                navigationIcon = {
                    BackButton(onClick = { Actions.getInstance().navigateBack() })
                },

                // Title
                title = { Text("New Team", style = typography.titleLarge) },

                colors = TopAppBarDefaults.topAppBarColors(palette.primary),
            )

            if (showBackButtonModal) {
                // Alert popup
                AlertDialog(
                    onDismissRequest = { setBackButtModal(false) },
                    // Title
                    title = {
                        Text(
                            text = "Attention",
                            style = typography.bodyMedium,
                            color = palette.secondary,
                            fontWeight = FontWeight.Bold
                        )
                    },

                    // Text
                    text = {
                        Text(
                            "Are you sure you want to go back? Changes might not be saved.",
                            style = typography.bodySmall,
                            color = palette.onSurfaceVariant
                        )
                    },

                    // Confirm button
                    confirmButton = {
                        Button(
                            onClick = {
                                setBackButtModal(false)
                                /* TODO: Handle confirmation */
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = palette.secondary)
                        ) {
                            Text(
                                "Confirm",
                                style = typography.bodySmall,
                                color = palette.background
                            )
                        }
                    },

                    // Dismiss button
                    dismissButton = {
                        Button(
                            onClick = {
                                setBackButtModal(false)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = palette.primary)
                        ) {
                            Text("Cancel", style = typography.bodySmall, color = palette.secondary)
                        }
                    },

                    containerColor = palette.background
                )
            }
        }

        "teams/newTeam/share/{teamId}/{teamName}", "teams/newTeam/qr" -> {
            val teamId = Actions.getInstance().getStringParameter("teamId")

            CenterAlignedTopAppBar(
                // Back button
                navigationIcon = {
                    BackButton(onClick = { Actions.getInstance().navigateBack() })
                },

                // Title
                title = { Text("Add people", style = typography.titleLarge) },

                actions = {
                    DoneButton(onClick = {
                        if (teamId != null) {
                            Actions.getInstance().goToTeamTasks(teamId)
                        }
                    })
                },

                colors = TopAppBarDefaults.topAppBarColors(palette.primary),
            )
        }

        "teams/{teamId}/tasks", "teams/{teamId}/description", "teams/{teamId}/people",
        "teams/{teamId}/tasksCalendar" -> {
            val teamId = Actions.getInstance().getStringParameter("teamId")

            val team = teams.find { it.first == teamId }

            val frequentlyAccessed = teamParticipants.find {
                it.teamId == teamId && it.personId == userId
            }?.frequentlyAccessed ?: false

            val userPermissionInTeam =
                if(teams.find { it.first == teamId }?.second?.ownerId == auth.uid) {
                    "Owner"
                } else if(teams.find { it.first == teamId }?.second?.admins?.contains(auth.uid) == true) {
                    "Admin"
                } else if(teams.find { it.first == teamId }?.second?.members?.contains(auth.uid) == true) {
                    ""
                } else {
                    "Not Member of this team"
                }



            TopAppBar(
                // Back button
                navigationIcon = {
                    IconButton(onClick = {
                        Actions.getInstance().navigateBack()
                    }) {
                        Image(
                            painter = painterResource(id = R.drawable.outline_arrow_back_24),
                            contentDescription = "Back",
                            modifier = Modifier.scale(1.2f)
                        )
                    }
                },

                // Title
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.teamtasklogo),  // TODO: Adapt team image
                            contentDescription = "Team logo",
                            modifier = Modifier.size(38.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        if (team?.second?.name != null) {
                            Column(
                                modifier = Modifier.weight(1f)
                                    .padding(bottom = 4.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    team.second.name,
                                    style = typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row{
                                    Text(
                                        team.second.category,
                                        style = typography.labelSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        " | " + team.second.creationDate.split("T")[0],
                                        style = typography.labelSmall,
                                        maxLines = 1,
                                    )
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(palette.primary),
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        // Chat
                        IconButton(onClick = {
                            if (teamId != null) {
                                Actions.getInstance().goToChat(teamId, true)
                            }
                        }) {
                            Image(
                                painter = painterResource(id = R.drawable.outline_chat_24),
                                contentDescription = "Chat",
                                modifier = Modifier.scale(1.2f).padding(top = 3.dp)
                            )
                        }
                        // Dropdown
                        DropDownButton(onClick = { setShowMen(true) })

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { setShowMen(false) },
                            modifier = Modifier.background(palette.background)
                        ) {
                            // Edit profile
                            if(userPermissionInTeam == "Owner") {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "Edit Team Info",
                                            style = typography.headlineSmall
                                        )
                                    },
                                    onClick = {
                                        setShowMen(false)
                                        if (teamId != null) {
                                            Actions.getInstance().goToEditTeamStatus(teamId)
                                        }
                                    },
                                    leadingIcon = {
                                        Image(
                                            painter = painterResource(id = R.drawable.outline_edit_24),
                                            contentDescription = "Edit Team Info"
                                        )
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        if (!frequentlyAccessed) "Mark as favourite" else "Remove from favourites",
                                        style = typography.headlineSmall
                                    )
                                },
                                onClick = {
                                    setShowMen(false)
                                    if (teamId != null) {
                                        // Add/remove team to/from frequentlyAccessed
                                        addOrRemoveTeamToFavourites(teamId, frequentlyAccessed)
                                    }
                                },
                                leadingIcon = {
                                    Box {
                                        Image(
                                            painter = painterResource(id = R.drawable.outline_star_24),
                                            contentDescription = "Mark as favourite",
                                            colorFilter = ColorFilter.tint(palette.secondary)
                                        )
                                        if (frequentlyAccessed) {
                                            Image(
                                                painter = painterResource(id = R.drawable.outline_remove_24),
                                                contentDescription = "Remove from favourites",
                                                colorFilter = ColorFilter.tint(palette.error),
                                                modifier = Modifier
                                                    .align(Alignment.Center)
                                                    .rotate(45f) // Rotate the remove icon to make it diagonal
                                            )
                                        }
                                    }
                                }
                            )

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "Show Statistics",
                                        style = typography.headlineSmall
                                    )
                                },
                                onClick = {
                                    setShowMen(false)
                                    if (teamId != null) {
                                        Actions.getInstance().goToTeamStatistics(teamId)
                                    }
                                },
                                leadingIcon = {
                                    Image(
                                        painter = painterResource(id = R.drawable.outline_query_stats_24),
                                        contentDescription = "Show Statistics",
                                        colorFilter = ColorFilter.tint(palette.secondary)
                                    )
                                }
                            )
                            if(userPermissionInTeam != "Owner") {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "Leave Team",
                                            style = typography.headlineSmall,
                                            color = palette.inverseSurface
                                        )
                                    },
                                    onClick = {
                                        setShwExitFromTeamModal(true)
                                        setShowMen(false)
                                    },
                                    leadingIcon = {
                                        Image(
                                            painter = painterResource(id = R.drawable.outline_logout_24),
                                            contentDescription = "Leave Team",
                                            colorFilter = ColorFilter.tint(palette.inverseSurface)
                                        )
                                    }
                                )
                            }
                            if(userPermissionInTeam == "Owner") {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "Delete Team",
                                            style = typography.headlineSmall,
                                            color = palette.error
                                        )
                                    },
                                    onClick = {
                                        setShwDeleteTeamModal(true)
                                        setShowMen(false)
                                    },
                                    leadingIcon = {
                                        Image(
                                            painter = painterResource(id = R.drawable.outline_delete_outline_24),
                                            contentDescription = "Delete Team",
                                            colorFilter = ColorFilter.tint(palette.error)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }

        "teams/{teamId}/statistics" -> {
            val teamId = Actions.getInstance().getStringParameter("teamId")

            val team = teams.find { it.first == teamId }

            TopAppBar(
                // Back button
                navigationIcon = {
                    IconButton(onClick = {
                        Actions.getInstance().navigateBack()
                    }) {
                        Image(
                            painter = painterResource(id = R.drawable.outline_arrow_back_24),
                            contentDescription = "Back",
                            modifier = Modifier.scale(1.2f)
                        )
                    }
                },

                // Title
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.teamtasklogo),  // TODO: Adapt team image
                            contentDescription = "Team logo",
                            modifier = Modifier.size(38.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        if (team?.second?.name != null) {
                            Column(
                                modifier = Modifier.weight(1f)
                                    .padding(bottom = 4.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    team.second.name,
                                    style = typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row{
                                    Text(
                                        team.second.category,
                                        style = typography.labelSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        " | " + team.second.creationDate.split("T")[0],
                                        style = typography.labelSmall,
                                        maxLines = 1,
                                    )
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(palette.primary),
                /*
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        // Chat
                        IconButton(onClick = {
                            if (teamId != null) {
                                Actions.getInstance().goToChat(teamId, true)
                            }
                        }) {
                            Image(
                                painter = painterResource(id = R.drawable.outline_chat_24),
                                contentDescription = "Chat",
                                modifier = Modifier.scale(1.2f).padding(top = 3.dp)
                            )
                        }
                    }
                }
                */
            )
        }

        "teams/{teamId}/filterTasks", "teams/filter" -> {
            CenterAlignedTopAppBar(
                // Back button
                navigationIcon = {
                    BackButton(onClick = { Actions.getInstance().navigateBack() })
                },

                // Title
                title = {
                    Text(
                        if (currentRoute != "teams/filter") "Filter Tasks" else "Filter Teams",
                        style = typography.titleLarge
                    )
                },

                colors = TopAppBarDefaults.topAppBarColors(palette.primary),
            )

            if (showBackButtonModal) {
                // Alert popup
                AlertDialog(
                    onDismissRequest = { setBackButtModal(false) },
                    // Title
                    title = {
                        Text(
                            text = "Attention",
                            style = typography.bodyMedium,
                            color = palette.secondary,
                            fontWeight = FontWeight.Bold
                        )
                    },

                    // Text
                    text = {
                        Text(
                            "Are you sure you want to go back? Changes might not be saved.",
                            style = typography.bodySmall,
                            color = palette.onSurfaceVariant
                        )
                    },

                    // Confirm button
                    confirmButton = {
                        Button(
                            onClick = {
                                setBackButtModal(false)
                                cancelEditProfile()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = palette.secondary)
                        ) {
                            Text(
                                "Confirm",
                                style = typography.bodySmall,
                                color = palette.background
                            )
                        }
                    },

                    // Dismiss button
                    dismissButton = {
                        Button(
                            onClick = {
                                setBackButtModal(false)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = palette.primary)
                        ) {
                            Text("Cancel", style = typography.bodySmall, color = palette.secondary)
                        }
                    },

                    containerColor = palette.background
                )
            }
        }

        "teams/{teamId}/tasks/{taskId}/comments/{commentId}/{areRepliesOn}" -> {
            CenterAlignedTopAppBar(
                // Back button
                navigationIcon = {
                    BackButton(onClick = { Actions.getInstance().navigateBack() })
                },

                // Title
                title = {
                    Text(
                        "Replies",
                        style = typography.titleLarge
                    )
                },

                colors = TopAppBarDefaults.topAppBarColors(palette.primary),
            )
        }

        "teams/{teamId}/edit/status" -> {
            CenterAlignedTopAppBar(
                // Back button
                navigationIcon = {
                    BackButton(onClick = { Actions.getInstance().navigateBack() })
                },

                // Title
                title = { Text("Edit Team Info", style = typography.titleLarge) },

                colors = TopAppBarDefaults.topAppBarColors(palette.primary),
            )
        }

        "teams/{teamId}/edit/description" -> {
            CenterAlignedTopAppBar(
                // Back button
                navigationIcon = {
                    BackButton(onClick = { /* TODO: Handle back */ })
                },

                // Title
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(R.drawable.teamtasklogo), // TODO: Adapt team image
                            contentDescription = "Team image",
                            modifier = Modifier.size(30.dp)
                        )

                        Spacer(Modifier.width(10.dp))

                        Text(
                            "Team name", // TODO: Adapt team name
                            style = typography.titleLarge
                        )
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(palette.primary),
            )
        }

        "teams/{teamId}/edit/people" -> {
            CenterAlignedTopAppBar(
                // Back button
                navigationIcon = {
                    BackButton(onClick = { Actions.getInstance().navigateBack() })
                },

                // Title
                title = { Text("Add people", style = typography.titleLarge) },

                colors = TopAppBarDefaults.topAppBarColors(palette.primary),
            )
        }

        "teams/{teamId}/newTask/status"//, "teams/{teamId}/newTask/description", "teams/{teamId}/newTask/people"
        -> {
            CenterAlignedTopAppBar(
                // Back button
                navigationIcon = {
                    BackButton(onClick = {
                        //if(currentRoute == "teams/{teamId}/newTask/status") {
                            Actions.getInstance().navigateBack()
                            cancelCreateTask()
                        /*} else {
                            goToPreviousStep
                        }*/
                    })
                },

                // Title
                title = {
                    Text(
                        "New Task",
                        style = typography.titleLarge
                    )
                },

                colors = TopAppBarDefaults.topAppBarColors(palette.primary),
            )
        }

        "teams/{teamId}/tasks/{taskId}/comments", "teams/{teamId}/tasks/{taskId}/comments/{commentId}", "teams/{teamId}/tasks/{taskId}/info",
        "teams/{teamId}/tasks/{taskId}/description", "teams/{teamId}/tasks/{taskId}/people" -> {
            val auth = FirebaseAuth.getInstance()

            val teamId = Actions.getInstance().getStringParameter("teamId")
            val taskId = Actions.getInstance().getStringParameter("taskId")

            val teamName = teams.find { it.first == teamId }?.second?.name
            val taskName = tasks.find { it.first == taskId }?.second?.title

            CenterAlignedTopAppBar(
                // Back button
                navigationIcon = {
                    BackButton(onClick = { Actions.getInstance().navigateBack() })
                },

                // Title
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        /*
                        Image(
                            painter = painterResource(R.drawable.teamtasklogo), // TODO: Adapt team image
                            contentDescription = "Team image",
                            modifier = Modifier.size(30.dp)
                        )
                        */

                        //Spacer(Modifier.width(10.dp))

                        if (taskName != null) {
                            Text(
                                taskName,
                                style = typography.titleLarge
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(palette.primary),
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        // Dropdown
                        DropDownButton(onClick = { setShowMen(true) })

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { setShowMen(false) },
                            modifier = Modifier.background(palette.background)
                        ) {
                            // Edit profile
                            DropdownMenuItem(
                                text = { Text("Mark as Completed", style = typography.headlineSmall) },
                                onClick = {
                                    setShowMen(false)

                                },
                                leadingIcon = {
                                    Image(
                                        painter = painterResource(id = R.drawable.outline_edit_24),
                                        contentDescription = "Mark as Completed"
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "Leave Task",
                                        style = typography.headlineSmall,
                                        color = palette.inverseSurface
                                    )
                                },
                                onClick = {
                                    //
                                    setShowMen(false)
                                },
                                leadingIcon = {
                                    Image(
                                        painter = painterResource(id = R.drawable.outline_logout_24),
                                        contentDescription = "Leave Task",
                                        colorFilter = ColorFilter.tint(palette.inverseSurface)
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "Delete Task",
                                        style = typography.headlineSmall,
                                        color = palette.error
                                    )
                                },
                                onClick = {
                                    //
                                    setShowMen(false)
                                },
                                leadingIcon = {
                                    Image(
                                        painter = painterResource(id = R.drawable.outline_delete_outline_24),
                                        contentDescription = "Delete Task",
                                        colorFilter = ColorFilter.tint(palette.error)
                                    )
                                }
                            )
                        }
                    }
                }
            )
        }

        "teams/{teamId}/tasks/{taskId}/edit/info", "teams/{teamId}/tasks/{taskId}/edit/description", "teams/{teamId}/tasks/{taskId}/edit/people" -> {
            CenterAlignedTopAppBar(
                // Back button
                navigationIcon = {
                    BackButton(onClick = { /* TODO: Handle back */ })
                },

                // Title
                title = {
                    Text(
                        "Edit Task",
                        style = typography.titleLarge
                    )
                },

                colors = TopAppBarDefaults.topAppBarColors(palette.primary),
            )
        }

        "chats/{isGroupChat}/{chatId}" -> {
            val isGroupChat = Actions.getInstance().getBooleanParameter("isGroupChat")

            val id = Actions.getInstance().getStringParameter("chatId")

            val topAppBarLabel: String?
            var teamCategory: String? = null

            if (isGroupChat) {
                val team = teams.find { it.first == id }
                topAppBarLabel = team?.second?.name
                teamCategory = team?.second?.category
            } else {
                val person = people.find { it.first == id }
                topAppBarLabel = person?.second?.name + " " + person?.second?.surname
            }

            TopAppBar(
                // Back button
                navigationIcon = {
                    IconButton(onClick = {
                        Actions.getInstance().navigateBack()
                    }) {
                        Image(
                            painter = painterResource(id = R.drawable.outline_arrow_back_24),
                            contentDescription = "Back",
                            modifier = Modifier.scale(1.2f)
                        )
                    }
                },

                // Title
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.teamtasklogo),  // TODO: Adapt team image
                            contentDescription = "Team logo",
                            modifier = if (!isGroupChat) {
                                Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                            } else {
                                Modifier.size(38.dp)
                            }
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Column(
                            modifier = Modifier.weight(1f)
                                .padding(bottom = 4.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                topAppBarLabel ?: "",
                                style = if (!isGroupChat) typography.titleLarge else typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (isGroupChat)
                                Text(
                                    teamCategory?:"",
                                    style = typography.labelSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(palette.primary),
                /*actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (isGroupChat) {
                            // Dropdown
                            DropDownButton(onClick = { setShowMen(true) })

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { setShowMen(false) },
                                modifier = Modifier.background(palette.background)
                            ) {
                                // Edit profile
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "Edit Team Info",
                                            style = typography.headlineSmall
                                        )
                                    },
                                    onClick = {
                                        setShowMen(false)
                                        if (id != null) {
                                            Actions.getInstance().goToEditTeamStatus(id)
                                        }
                                    },
                                    leadingIcon = {
                                        Image(
                                            painter = painterResource(id = R.drawable.outline_edit_24),
                                            contentDescription = "Edit Team Info"
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "Show Statistics",
                                            style = typography.headlineSmall
                                        )
                                    },
                                    onClick = {
                                        setShowMen(false)
                                        if (id != null) {
                                            Actions.getInstance().goToTeamStatistics(id)
                                        }
                                    },
                                    leadingIcon = {
                                        Image(
                                            painter = painterResource(id = R.drawable.outline_query_stats_24),
                                            contentDescription = "Show Statistics",
                                            colorFilter = ColorFilter.tint(palette.secondary)
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "Leave Team",
                                            style = typography.headlineSmall,
                                            color = palette.inverseSurface
                                        )
                                    },
                                    onClick = {
                                        //
                                        setShowMen(false)
                                    },
                                    leadingIcon = {
                                        Image(
                                            painter = painterResource(id = R.drawable.outline_logout_24),
                                            contentDescription = "Leave Team",
                                            colorFilter = ColorFilter.tint(palette.inverseSurface)
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "Delete Team",
                                            style = typography.headlineSmall,
                                            color = palette.error
                                        )
                                    },
                                    onClick = {
                                        //
                                        setShowMen(false)
                                    },
                                    leadingIcon = {
                                        Image(
                                            painter = painterResource(id = R.drawable.outline_delete_outline_24),
                                            contentDescription = "Delete Team",
                                            colorFilter = ColorFilter.tint(palette.error)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }*/
            )
        }

        "accounts/{accountId}" -> {
            val accountId = Actions.getInstance().getStringParameter("accountId")

            val auth = FirebaseAuth.getInstance()


            CenterAlignedTopAppBar(
                // Back button
                navigationIcon = {
                    BackButton(onClick = { Actions.getInstance().navigateBack() })
                },

                // Title
                title = {
                    Text(
                        "Contact name", // TODO: Adapt contact name
                        style = typography.titleLarge
                    )
                },

                actions = {
                    if(auth.uid != accountId){
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Chat
                            IconButton(onClick = {
                                if (accountId != null) {
                                    Actions.getInstance().goToChat(accountId, false)
                                }
                            }) {
                                Image(
                                    painter = painterResource(id = R.drawable.outline_chat_24),
                                    contentDescription = "Chat",
                                    modifier = Modifier.scale(1.2f).padding(top = 3.dp)
                                )
                            }
                            DropDownButton(onClick = { /* TODO: Handle click */ })
                        }
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(palette.primary),
            )
        }

        else -> {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.teamtasklogo),
                            contentDescription = "Logo",
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("TeamTask", style = typography.titleLarge)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(palette.primary)
            )
        }
    }
}


@Composable
fun BackButton(
    onClick: () -> Unit
) {
    val palette = MaterialTheme.colorScheme

    IconButton(
        onClick = onClick
    ) {
        Image(
            painter = painterResource(id = R.drawable.outline_arrow_back_24),
            contentDescription = "Back",
            colorFilter = ColorFilter.tint(palette.secondary),
            modifier = Modifier.size(30.dp)
        )
    }
}


@Composable
private fun DoneButton(
    onClick: () -> Unit
) {
    val palette = MaterialTheme.colorScheme

    IconButton(
        onClick = onClick
    ) {
        Image(
            painter = painterResource(id = R.drawable.outline_done_24),
            contentDescription = "Done",
            colorFilter = ColorFilter.tint(palette.secondary),
            modifier = Modifier.size(30.dp)
        )
    }
}


@Composable
private fun ChatButton(
    onClick: () -> Unit
) {
    val palette = MaterialTheme.colorScheme

    IconButton(
        onClick = onClick
    ) {
        Image(
            painter = painterResource(id = R.drawable.outline_chat_24),
            contentDescription = "Chat",
            colorFilter = ColorFilter.tint(palette.secondary),
            modifier = Modifier.size(30.dp)
        )
    }
}


@Composable
private fun DropDownButton(
    onClick: () -> Unit
) {
    val palette = MaterialTheme.colorScheme

    IconButton(
        onClick = onClick
    ) {
        Image(
            painter = painterResource(id = R.drawable.outline_more_vert_24),
            contentDescription = "Dropdown menu",
            colorFilter = ColorFilter.tint(palette.secondary),
            modifier = Modifier.size(30.dp)
        )
    }
}
