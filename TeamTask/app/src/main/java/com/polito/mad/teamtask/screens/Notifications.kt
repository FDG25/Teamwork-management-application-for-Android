package com.polito.mad.teamtask.screens

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ListResult
import com.polito.mad.teamtask.Notification
import com.polito.mad.teamtask.Person
import com.polito.mad.teamtask.Team
import com.polito.mad.teamtask.UserNotification
import com.polito.mad.teamtask.components.NotificationEntry
import com.polito.mad.teamtask.components.TeamCard
import com.polito.mad.teamtask.ui.theme.TeamTaskTypography
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class NotificationsViewModel : ViewModel() {
    private val storage = FirebaseStorage.getInstance()
    private val _images = MutableStateFlow<Map<String, Uri?>>(emptyMap())
    val images: StateFlow<Map<String, Uri?>> = _images

    fun fetchImage(teamIdOrUserId: String, fromGroup: Boolean, typology: Int) {
        viewModelScope.launch {
            try {
                val imageRef = if (fromGroup){
                    storage.reference.child("teamImages")
                } else {
                    storage.reference.child("profileImages")
                    /*
                    if(typology == 2){
                        storage.reference.child("profileImages")
                    } else {
                        storage.reference.child("profileImages")
                    }
                    */
                }
                val result: ListResult = imageRef.listAll().await()
                val matchingItem = result.items.firstOrNull { it.name.startsWith(teamIdOrUserId) }

                if (matchingItem != null) {
                    val uri = matchingItem.downloadUrl.await()
                    _images.value = _images.value.toMutableMap().apply { put(teamIdOrUserId, uri) }
                } else {
                    _images.value = _images.value.toMutableMap().apply { put(teamIdOrUserId, null) }
                }
            } catch (e: Exception) {
                _images.value = _images.value.toMutableMap().apply { put(teamIdOrUserId, null) }
            }
        }
    }
}

@Composable
fun NotificationsScreen (
    filteredNotifications: List<String>,
    notifications: List<Pair<String, Notification>>,
    userNotifications: List<UserNotification>,
    teams: List<Pair<String, Team>>,
    people: List<Pair<String, Person>>,
    goToTask: (String, String) -> Unit,
    notificationsVm: NotificationsViewModel = viewModel()
) {
    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography

    val localNotifications = filteredNotifications.mapNotNull { id ->
        val n = notifications.firstOrNull { t -> t.first.equals(id) }?.second
        if (n != null) Pair(id, n)
        else null
    }

    val localUserNotifications = mutableMapOf<String, UserNotification>()
    filteredNotifications.forEach { id ->
        val tmp = userNotifications.firstOrNull { un -> un.notificationId.equals(id) }
        if (tmp != null) localUserNotifications[id] = tmp
    }

    val localTeams = mutableMapOf<String, Team>()
    teams.forEach { (index, team) -> localTeams[index] = team }
    val localPeople = mutableMapOf<String, Person>()
    people.forEach { (index, person) -> localPeople[index] = person }

    val groupedNotifications = localNotifications
        .sortedByDescending { n -> n.second.timestamp }
        .groupBy { n -> n.second.timestamp.split("T")[0] }

    LazyColumn {
        if (groupedNotifications.isEmpty()) {
            item{Spacer(modifier = Modifier.height(40.dp)) }
            item {
                Column(verticalArrangement = Arrangement.Center) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            "Everything is quiet \uD83D\uDCA4\uD83D\uDCA4",
                            style = typography.labelMedium,
                            color = palette.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            item {
                Column {
                    groupedNotifications.forEach { (date, notifications) ->
                        Text(
                            date,
                            style = typography.labelMedium,
                            modifier = Modifier
                                .padding(horizontal = 15.dp)
                                .padding(top = 8.dp, bottom = 5.dp)
                        )

                        notifications.forEach { (id, n) ->
                            val senderName = if (n.fromGroup) {
                                localTeams[n.senderId]?.name ?: ""
                            } else {
                                if(n.typology.toInt()==2 || n.typology.toInt()==3) {
                                    localTeams[n.teamId]?.name ?: ""
                                } else {
                                    (localPeople[n.senderId]?.name
                                        ?: "") + " " + (localPeople[n.senderId]?.surname ?: "")
                                }
                            }
                            val read = localUserNotifications[id]?.read ?: false
                            val imageUri = notificationsVm.images.collectAsState().value[n.senderId]
                            notificationsVm.fetchImage(n.senderId, n.fromGroup, n.typology.toInt())

                            Button(
                                onClick = {
                                    if(n.typology.toInt()==2 || n.typology.toInt()==3) {
                                        goToTask(n.teamId, n.taskId)
                                    } else {
                                        goToTask(n.senderId, n.taskId)
                                    }
                                },
                                shape = RoundedCornerShape(5.dp),
                                colors = ButtonDefaults.buttonColors(
                                    contentColor = Color.Transparent,
                                    containerColor = Color.Transparent
                                ),
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.padding(horizontal = 15.dp)
                            ) {
                                NotificationEntry(n, senderName, read, imageUri)
                            }

                            Spacer(Modifier.height(5.dp))
                        }
                    }
                }
            }
        }
    }
}
