package com.polito.mad.teamtask.screens

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat.startActivity
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.zxing.WriterException
import com.polito.mad.teamtask.Actions
import com.polito.mad.teamtask.Comment
import com.polito.mad.teamtask.Person
import com.polito.mad.teamtask.R
import com.polito.mad.teamtask.Task
import com.polito.mad.teamtask.Team
import com.polito.mad.teamtask.TeamParticipant
import com.polito.mad.teamtask.components.CustomSearchBar
import com.polito.mad.teamtask.components.ProfileInfoSection
import com.polito.mad.teamtask.components.ProfilePictureSection
import com.polito.mad.teamtask.components.ProgressBar
import com.polito.mad.teamtask.components.TaskEntry
import com.polito.mad.teamtask.components.tasks.Description
import com.polito.mad.teamtask.components.tasks.DescriptionViewOnly
import com.polito.mad.teamtask.components.tasks.TagsDropdownMenu
import com.polito.mad.teamtask.ui.theme.CaribbeanCurrent
import com.polito.mad.teamtask.ui.theme.Jet
import com.polito.mad.teamtask.ui.theme.Mulish
import com.polito.mad.teamtask.ui.theme.TeamTaskTypography
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

data class ToDoTask(
    val taskId: String,
    val taskName: String,
    val status: String,                     // 0 = scheduled, 1 = completed, 2 = expired
    val isNotPriority: Int,                 // 0 = priority, 1 = not priority
    val recurrence: String,                // "Never", "Weekly", "Monthly", "Yearly"
    val expirationTimestamp: String,
    val creationTimestamp: String,
    val taskpeople: List<PersonData>,
    val tags: List<String>
)

data class PersonData(
    val personId: String,
    val name: String,
    val surname: String,
    val username: String,
    val role: String,
    val permission: String,
    val image: String
)

enum class TaskCreationStep {
    Status,
    Description,
    People
}

class SpecificTeamViewModel : ViewModel() {
    val auth = FirebaseAuth.getInstance()

    fun init(
        toDoTasks: List<ToDoTask>
    ) {
        viewModelScope.launch {
            // Populate _taskpeople with all people from each task
            val allTaskPeople = toDoTasks.flatMap { it.taskpeople }
                .distinctBy { it.personId }
                .map {
                    if (it.image.isNotBlank()) {
                        val image =
                            FirebaseStorage.getInstance().reference.child("profileImages/${it.image}")
                        val url = image.downloadUrl.await()
                        it.copy(image = url.toString())
                    } else it
                }

            val updatedTaskPeople = _teampeople.value.filter { teamPerson ->
                allTaskPeople.any { it.personId == teamPerson.personId }
            }

            _taskpeople.value = updatedTaskPeople
        }
    }

    fun init(
        toDoTasks: List<ToDoTask>,
        teampeople: List<PersonData>,
        filteredPeople: List<PersonData>
    ) {
        _toDoTasks.value = toDoTasks
        //_teampeople.value = teampeople

        viewModelScope.launch {
            val updatedTeamPeople = teampeople.map {
                if (it.image.isNotBlank()) {
                    val image =
                        FirebaseStorage.getInstance().reference.child("profileImages/${it.image}")
                    val url = image.downloadUrl.await()
                    it.copy(image = url.toString())
                } else it
            }
            _teampeople.value = updatedTeamPeople
        }
    }

    var stringValueForDelete by mutableStateOf("")
        private set
    var stringErrorForDelete by mutableStateOf("")
        private set

    fun setStrinValueForDelete(a: String) {
        stringValueForDelete = a.trim()
        stringErrorForDelete = ""
    }

    suspend fun getUserPermission(teamId: String, userId: String?): String? {
        return try {
            val teamDoc = withContext(Dispatchers.IO) {
                db.collection("teams").document(teamId).get().await()
            }
            val admins = teamDoc.get("admins") as? List<String> ?: listOf()
            val members = teamDoc.get("members") as? List<String> ?: listOf()
            val ownerId = teamDoc.getString("ownerId")

            when {
                ownerId == userId -> "Owner"
                admins.contains(userId) -> "Admin"
                members.contains(userId) -> "Member"
                else -> null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun hasHigherPermission(teamId: String, userId: String?, personPermission: String): Boolean {
        var result = false
        runBlocking {
            launch(Dispatchers.IO) {
                val currentUserPermission = userId?.let { getUserPermission(teamId, it) }
                Log.e("prova", "$personPermission - $currentUserPermission")

                result = currentUserPermission != null && (
                        currentUserPermission == "Owner" && personPermission == "Owner" ||
                                currentUserPermission == "Owner" && personPermission == "Admin" ||
                                currentUserPermission == "Owner" && personPermission == "" ||
                                currentUserPermission == "Admin" && personPermission == "" //EMPTY STRING = MEMBER
                        )
            }.join()
        }
        return result
    }

    fun validateStringForDeleteTeam(teamId: String) {
        viewModelScope.launch {
            if (stringValueForDelete == auth.currentUser?.email) {
                deleteTeam(teamId)
                setShwDeleteTeamModal(false)
                setStrinValueForDelete("")
                stringErrorForDelete = ""
                //updateAccountBeenDeletedStatus(true)
            } else {
                stringErrorForDelete = "Inserted email is not correct!"
            }
        }
    }

    // Method to retrieve team information by invite hash
    suspend fun retrieveTeamInfoByInviteHash(hash: String): Pair<Team?, String> {
        return try {
            val currentUser = auth.currentUser
            val userStatus: String

            if (currentUser == null) {
                return Pair(null, "User not logged in")
            } else {
                val userId = currentUser.uid

                val teamQuery =
                    db.collection("teams").whereEqualTo("inviteLink", hash).get().await()
                if (teamQuery.documents.isNotEmpty()) {
                    val teamDocument = teamQuery.documents.first()
                    val teamData = teamDocument.data

                    if (teamData != null) {
                        // Process the team data
                        val teamMemberIds = teamData["members"] as? List<String> ?: emptyList()
                        val teamOwnerId = teamData["ownerId"] as? String ?: ""
                        val teamAdminIds = teamData["admins"] as? List<String> ?: emptyList()
                        val teamName = teamData["name"] as? String ?: ""
                        val teamImage = teamData["image"] as? String ?: ""
                        val inviteLink = teamData["inviteLink"] as? String ?: ""
                        val creationDate = teamData["creationDate"] as? String ?: ""
                        val category = teamData["category"] as? String ?: ""
                        val tasks = teamData["tasks"] as? List<String> ?: emptyList()

                        val team = Team(
                            name = teamName,
                            image = teamImage,
                            ownerId = teamOwnerId,
                            admins = teamAdminIds,
                            inviteLink = inviteLink,
                            creationDate = creationDate,
                            category = category,
                            members = teamMemberIds,
                            tasks = tasks
                        )

                        // Check if the current user is already a member of the team
                        val isUserInTeam =
                            teamMemberIds.contains(userId) || teamOwnerId == userId || teamAdminIds.contains(
                                userId
                            )

                        userStatus = if (isUserInTeam) {
                            "User already a member of the team"
                        } else {
                            "User not a member of the team"
                        }

                        val statusWithId = "$userStatus-${teamDocument.id}"
                        return Pair(team, statusWithId)
                    } else {
                        userStatus = "Team not found"
                    }
                } else {
                    userStatus = "Team not found"
                }
            }

            Pair(null, userStatus)
        } catch (e: Exception) {
            // Handle any errors that occur during the database operation
            Pair(null, "Error retrieving team information")
        }
    }

    // Function to add or remove a team from the favourite section in Home
    fun addOrRemoveTeamToFavourites(teamId: String, frequentlyAccessed: Boolean) {
        val currentUser = auth.currentUser ?: return

        val userId = currentUser.uid
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Query to find the team participant document
                val participantQuery = db.collection("team_participants")
                    .whereEqualTo("teamId", teamId)
                    .whereEqualTo("personId", userId)
                    .get()
                    .await()

                if (participantQuery.documents.isNotEmpty()) {
                    // Update the frequentlyAccessed field for the existing participant document
                    val participantDoc = participantQuery.documents[0]
                    val participantRef =
                        db.collection("team_participants").document(participantDoc.id)
                    participantRef.update("frequentlyAccessed", !frequentlyAccessed).await()
                } else {
                    // Handle the case where no participant document is found
                    Log.e(
                        "SpecificTeamViewModel",
                        "No participant document found for teamId: $teamId and userId: $userId"
                    )
                }
            } catch (e: Exception) {
                // Handle any errors that occur during the database operation
                Log.e("SpecificTeamViewModel", "Error updating favourite status", e)
            }
        }
    }

    // Function to add the current logged-in user to a team and navigate if successful
    fun joinTeam(teamId: String) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            return
        }

        val userId = currentUser.uid
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val teamRef = db.collection("teams").document(teamId)
                val teamDocument = teamRef.get().await()

                if (teamDocument.exists()) {
                    val members =
                        teamDocument.get("members") as? MutableList<String> ?: mutableListOf()
                    if (!members.contains(userId)) {
                        members.add(userId)
                        teamRef.update("members", members).await()
                    }

                    // Add a new entry in the team_participants collection
                    val participantData = TeamParticipant(
                        teamId = teamId,
                        personId = userId,
                        frequentlyAccessed = false,
                        role = "",
                        completedTasks = 0L,
                        totalTasks = 0L
                    )
                    db.collection("team_participants").add(participantData).await()

                    // Update the user's teams field in the people collection
                    val userRef = db.collection("people").document(userId)
                    val userDocument = userRef.get().await()

                    if (userDocument.exists()) {
                        val userTeams = userDocument.get("teams") as? List<String> ?: emptyList()
                        val updatedTeams = userTeams.toMutableList()
                        if (!updatedTeams.contains(teamId)) {
                            updatedTeams.add(teamId)
                            userRef.update("teams", updatedTeams).await()
                        }
                    } else {
                        // If user document does not exist, create it
                        val newUserData = hashMapOf(
                            "teams" to listOf(teamId),
                            // Add other required fields with default values as necessary
                        )
                        userRef.set(newUserData).await()
                    }

                    withContext(Dispatchers.Main) {
                        Actions.getInstance().goToTeamTasks(teamId)
                    }
                }
            } catch (e: Exception) {
                // Handle any errors that occur during the database operation
                Log.e("SpecificTeamViewModel", "Error joining team", e)
            }
        }
    }

    var showExitFromTeamModal by mutableStateOf(false)
    fun setShwExitFromTeamModal(bool: Boolean) {
        showExitFromTeamModal = bool
    }

    // Function to remove the current logged-in user from a team and navigate if successful
    fun exitFromTeam(teamId: String) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            return
        }

        val userId = currentUser.uid
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Remove the user from the team's members
                val teamRef = db.collection("teams").document(teamId)
                val teamDocument = teamRef.get().await()

                if (teamDocument.exists()) {
                    val members =
                        teamDocument.get("members") as? MutableList<String> ?: mutableListOf()
                    if (members.contains(userId)) {
                        members.remove(userId)
                        teamRef.update("members", members).await()
                    }

                    // Remove the entry in the team_participants collection
                    val participantQuery = db.collection("team_participants")
                        .whereEqualTo("teamId", teamId)
                        .whereEqualTo("personId", userId)
                        .get()
                        .await()

                    for (participantDoc in participantQuery.documents) {
                        db.collection("team_participants").document(participantDoc.id).delete()
                            .await()
                    }

                    // Update the user's teams field in the people collection
                    val userRef = db.collection("people").document(userId)
                    val userDocument = userRef.get().await()

                    if (userDocument.exists()) {
                        val userTeams = userDocument.get("teams") as? List<String> ?: emptyList()
                        val updatedTeams = userTeams.toMutableList()
                        if (updatedTeams.contains(teamId)) {
                            updatedTeams.remove(teamId)
                            userRef.update("teams", updatedTeams).await()
                        }

                        // Remove tasks related to the team from the user's tasks field
                        val userTasks = userDocument.get("tasks") as? List<String> ?: emptyList()
                        val tasksToRemove = userTasks.filter { taskId ->
                            val taskRef = db.collection("tasks").document(taskId)
                            val taskDocument = taskRef.get().await()
                            taskDocument.exists() && taskDocument.getString("teamId") == teamId
                        }
                        val updatedTasks = userTasks.toMutableList()
                        updatedTasks.removeAll(tasksToRemove)
                        userRef.update("tasks", updatedTasks).await()
                    }

                    // Remove the user from the tasks associated with the team
                    val tasksQuery = db.collection("tasks")
                        .whereEqualTo("teamId", teamId)
                        .get()
                        .await()

                    for (taskDoc in tasksQuery.documents) {
                        val taskPeople =
                            taskDoc.get("people") as? MutableList<String> ?: mutableListOf()
                        if (taskPeople.contains(userId)) {
                            taskPeople.remove(userId)
                            db.collection("tasks").document(taskDoc.id).update("people", taskPeople)
                                .await()
                        }
                    }

                    withContext(Dispatchers.Main) {
                        Actions.getInstance().goToHome()
                    }
                }
            } catch (e: Exception) {
                // Handle any errors that occur during the database operation
                Log.e("SpecificTeamViewModel", "Error exiting team", e)
            }
        }
    }

    // Function to remove the current logged-in user from a task and navigate if successful
    fun exitFromTask(teamId: String, taskId: String) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            return
        }

        val userId = currentUser.uid
        viewModelScope.launch(Dispatchers.IO) {
            try {
                removeUserFromTask(teamId, taskId, userId)

                withContext(Dispatchers.Main) {
                    Actions.getInstance().navigateBack()
                }
            } catch (e: Exception) {
                Log.e("SpecificTeamViewModel", "Error exiting task", e)
            }
        }
    }


    private suspend fun removeUserFromTask(teamId: String, taskId: String, userId: String) {
        // Reference to the task document
        val taskRef = db.collection("tasks").document(taskId)
        val taskSnapshot = taskRef.get().await()

        if (taskSnapshot.exists()) {
            // Remove the user from the task's people field
            taskRef.update("people", FieldValue.arrayRemove(userId)).await()
        }

        // Reference to the user document
        val userRef = db.collection("people").document(userId)
        val userSnapshot = userRef.get().await()

        if (userSnapshot.exists()) {
            // Remove the task from the user's tasks field
            userRef.update("tasks", FieldValue.arrayRemove(taskId)).await()
        }
    }


    fun validateStringForDeleteTask(teamId: String, taskId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (stringValueForDelete == auth.currentUser?.email) {
                deleteTask(teamId, taskId)
                setShwDeleteTaskModal(false)
                setStrinValueForDelete("")
                stringErrorForDelete = ""
                //updateAccountBeenDeletedStatus(true)
            } else {
                stringErrorForDelete = "Inserted email is not correct!"
            }
        }
    }

    private suspend fun deleteTask(teamId: String, taskId: String) {
        val taskRef = db.collection("tasks").document(taskId)
        val taskSnapshot = taskRef.get().await()

        if (taskSnapshot.exists()) {
            // list of people assigned to the task
            val peopleIds = taskSnapshot.get("people") as? List<String> ?: emptyList()

            // Remove the taskId from each person's tasks field
            peopleIds.forEach { personId ->
                val personRef = db.collection("people").document(personId)
                personRef.update("tasks", FieldValue.arrayRemove(taskId)).await()
            }

            // Remove the taskId from the team's tasks field
            val teamRef = db.collection("teams").document(teamId)
            teamRef.update("tasks", FieldValue.arrayRemove(taskId)).await()

            // Delete the task document
            taskRef.delete().await()

            withContext(Dispatchers.Main) {
                Actions.getInstance().navigateBack()
            }
        } else {
            // case where the task document does not exist
        }
    }




    var showDeleteTeamModal by mutableStateOf(false)
    fun setShwDeleteTeamModal(bool: Boolean) {
        showDeleteTeamModal = bool
    }

    var showDeleteTaskModal by mutableStateOf(false)
    fun setShwDeleteTaskModal(bool: Boolean) {
        showDeleteTaskModal = bool
    }
    var showExitFromTaskModal by mutableStateOf(false)
    fun setShwExitFromTaskModal(bool: Boolean) {
        showExitFromTaskModal = bool
    }

    // Function to delete a team and navigate to the home screen if successful
    fun deleteTeam(teamId: String) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Fetch the team document
                val teamRef = db.collection("teams").document(teamId)
                val teamDocument = teamRef.get().await()

                if (teamDocument.exists()) {
                    // Remove all team members from the `people` collection
                    val members = teamDocument.get("members") as? List<String> ?: emptyList()
                    for (memberId in members) {
                        val userRef = db.collection("people").document(memberId)
                        val userDocument = userRef.get().await()
                        if (userDocument.exists()) {
                            val userTeams =
                                userDocument.get("teams") as? List<String> ?: emptyList()
                            val updatedTeams = userTeams.toMutableList()
                            if (updatedTeams.contains(teamId)) {
                                updatedTeams.remove(teamId)
                                userRef.update("teams", updatedTeams).await()
                            }

                            // Remove tasks related to the team from the user's tasks field
                            val userTasks =
                                userDocument.get("tasks") as? List<String> ?: emptyList()
                            val tasksToRemove = userTasks.filter { taskId ->
                                val taskRef = db.collection("tasks").document(taskId)
                                val taskDocument = taskRef.get().await()
                                taskDocument.exists() && taskDocument.getString("teamId") == teamId
                            }
                            val updatedTasks = userTasks.toMutableList()
                            updatedTasks.removeAll(tasksToRemove)
                            userRef.update("tasks", updatedTasks).await()
                        }
                    }

                    // Delete all entries in the `team_participants` collection
                    val participantQuery = db.collection("team_participants")
                        .whereEqualTo("teamId", teamId)
                        .get()
                        .await()

                    for (participantDoc in participantQuery.documents) {
                        db.collection("team_participants").document(participantDoc.id).delete()
                            .await()
                    }

                    // Delete all tasks associated with the team
                    val tasksQuery = db.collection("tasks")
                        .whereEqualTo("teamId", teamId)
                        .get()
                        .await()

                    for (taskDoc in tasksQuery.documents) {
                        db.collection("tasks").document(taskDoc.id).delete().await()
                    }

                    // Delete the team document
                    teamRef.delete().await()

                    withContext(Dispatchers.Main) {
                        Actions.getInstance().goToHome()
                    }
                }
            } catch (e: Exception) {
                // Handle any errors that occur during the database operation
                Log.e("SpecificTeamViewModel", "Error deleting team", e)
            }
        }
    }

    var isNotPriority by mutableIntStateOf(-1)           //look ToDoTasks data class for info on the values
    var isNotRecurrent by mutableIntStateOf(-1)          //1 if ToDoTask.recurrence="Never", 0 if ToDoTask.recurrence is one of ["Weekly", "Monthly", "Yearly"]
    var status by mutableIntStateOf(-1)
    var sortByCreationDate by mutableIntStateOf(-1) //false -> sort by expiration date, true -> sort by creation date
    fun setSortModality(value: Int) {
        sortByCreationDate = value
    }


    private val db = FirebaseFirestore.getInstance()

    // Method to promote or declass a person in the team
    fun promoteOrDeclassPersonInTeam(teamId: String, userId: String, currentPermission: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val teamRef = db.collection("teams").document(teamId)
                val document = teamRef.get().await()

                if (document.exists()) {
                    val admins = document.get("admins") as? MutableList<String> ?: mutableListOf()

                    if (currentPermission == "Admin") {
                        // Remove from admins
                        admins.remove(userId)
                    } else {
                        // Add to admins
                        if (!admins.contains(userId)) {
                            admins.add(userId)
                        }
                    }

                    teamRef.update("admins", admins).await()
                    // Update the local state
                    val updatedTeamPeople = _teampeople.value.map { person ->
                        if (person.personId == userId) {
                            person.copy(permission = if (currentPermission == "Admin") "" else "Admin")
                        } else {
                            person
                        }
                    }
                    _teampeople.value = updatedTeamPeople
                }
            } catch (e: Exception) {
                // Handle any errors that occur during the database operation
            }
        }
    }

    var selectedRole by mutableStateOf("")
        private set
    var selectedRoleError by mutableStateOf("")
    fun setSelectdRole(a: String) {
        selectedRole = a
    }

    suspend fun addOrUpdateRoleInTeamParticipants(teamId: String, userId: String, role: String) {
        withContext(Dispatchers.IO) {
            try {
                val participantsRef = db.collection("team_participants")
                val querySnapshot = participantsRef
                    .whereEqualTo("teamId", teamId)
                    .whereEqualTo("personId", userId)
                    .get()
                    .await()

                if (querySnapshot.documents.isNotEmpty()) {
                    val document = querySnapshot.documents[0]
                    val docRef = participantsRef.document(document.id)
                    docRef.update("role", role).await()
                } else {
                    // If the document does not exist, create a new one
                    val newParticipant = hashMapOf(
                        "teamId" to teamId,
                        "personId" to userId,
                        "role" to role,
                        "completedTasks" to 0,
                        "frequentlyAccessed" to false,
                        "totalTasks" to 0
                    )
                    participantsRef.add(newParticipant).await()
                }
            } catch (e: Exception) {
                // Handle any errors that occur during the database operation
                e.printStackTrace()
            }
        }
    }

    fun validateRole(teamId: String, userId: String, role: String) {
        viewModelScope.launch {
            addOrUpdateRoleInTeamParticipants(teamId, userId, role)
            setSelectdRole("")
        }
    }

    var showQrCodeDialog by mutableStateOf(false)
    fun setShowQrDialog(value: Boolean) {
        showQrCodeDialog = value
    }

    var showCalendarView by mutableStateOf(false)
    fun setShowCalendView(value: Boolean) {
        showCalendarView = value
    }

    var showCalendarEventsDialog by mutableStateOf(false)
    fun setShowCalendEventsDialog(value: Boolean) {
        showCalendarEventsDialog = value
    }


    var showingTeamLinkOrQrCode by mutableStateOf(false)
    fun setShowTeamLinkOrQrCode(value: Boolean) {
        showingTeamLinkOrQrCode = value
    }

    var taskTagsList by mutableStateOf(
        listOf(
            "#test1", "#test2", "#test3", "#test4", "#test5", "#test6",
            "#test7", "#test8", "#test9"
        ).sorted()
    )
        private set

    var selectedTags by mutableStateOf(emptyList<String>())

    /*fun addTag(tag: String) {
        // Check if the member is already in the list based on username
        if (selectedTags.none { it == tag }) {
            selectedTags = selectedTags + tag
        }
    }
    // Method to remove members

    fun removeTag(tag: String) {
        selectedTags = selectedTags - tag
    }
    */

    private fun clearSelectedTags() {
        selectedTags = emptyList()
    }

    fun setSelectedTags() {
        selectedTags = tempSelectedTags.toList()
    }

    var tempSelectedTags by mutableStateOf(emptyList<String>())

    fun addTempSelectedTags(tag: String) {
        // Check if the member is already in the list based on username
        if (tempSelectedTags.none { it == tag }) {
            tempSelectedTags = tempSelectedTags + tag
        }
    }
    // Method to remove members

    fun removeTempSelectedTags(tag: String) {
        tempSelectedTags = tempSelectedTags - tag
    }

    private fun clearTempSelectedTags() {
        tempSelectedTags = emptyList()
    }

    /*
    fun setTempSelectedTags() {
        tempSelectedTags = selectedTags.toList()
    }
     */

    var selectedTagsForNewTask by mutableStateOf(emptyList<String>())

    fun addTagForNewTask(tag: String) {
        // Check if the member is already in the list based on username
        if (selectedTagsForNewTask.none { it == tag }) {
            selectedTagsForNewTask = selectedTagsForNewTask + tag
        }
    }
    // Method to remove members

    fun removeTagForNewTask(tag: String) {
        selectedTagsForNewTask = selectedTagsForNewTask - tag
    }

    private fun clearSelectedTagsForNewTask() {
        selectedTagsForNewTask = emptyList()
    }

    var currentStep by mutableStateOf(TaskCreationStep.Status)

    private fun resetCurrentStep() {
        currentStep = TaskCreationStep.Status
    }

    fun goToPreviousStep(){
        when (currentStep) {
            TaskCreationStep.Status -> { }
            TaskCreationStep.Description -> currentStep = TaskCreationStep.Status
            TaskCreationStep.People -> currentStep = TaskCreationStep.Description
        }
    }

    // ----- Task name -----
    var showBackButtonModalCreateTask by mutableStateOf(false)

    var showingCreateTask by mutableStateOf(false)
    private fun setShowCreateTask(value: Boolean) {
        showingCreateTask = value
    }

    fun setBackButtModalCreateTask(bool: Boolean) {
        showBackButtonModalCreateTask = bool
    }

    fun createTask() {
        setShowCreateTask(true)
        clearSelectedTagsForNewTask()
    }

    private fun clearErrors() {
        taskNameError = ""
        taskDescriptionError = ""
        selectedDateTimeError = ""
    }

    fun cancelCreateTask() {
        setShowCreateTask(false)
        setTaskName("")
        setTaskDescription("")
        setDueDateDateTime("")
        setTaskRecurrency("Never")
        setTaskPriority(1)
        onSearchQueryForNewTaskChanged("")
        clearSelectedTagsForNewTask()
        clearErrors()
        resetCurrentStep()
    }

    var taskNameValue by mutableStateOf("")
        private set
    var taskNameError by mutableStateOf("")
        private set

    fun setTaskName(n: String) {
        taskNameValue = n
    }

    private suspend fun checkTaskName(teamId: String) {
        // Remove leading and trailing spaces
        val trimmedTaskName = taskNameValue.trim()

        // Check if a task with the same name exists in the specified team
        val taskExistsInTeam = db.collection("tasks")
            .whereEqualTo("teamId", teamId)
            .whereEqualTo("title", trimmedTaskName)
            .get()
            .await()
            .isEmpty

        taskNameError = when {
            trimmedTaskName.isBlank() -> {
                "Task name cannot be blank!"
            }
            !trimmedTaskName.matches(Regex("^(?=.*[a-zA-Z0-9])[a-zA-Z0-9 ]{1,50}\$")) -> {
                "Max 50 characters. Only letters, numbers and spaces are allowed!"
            }
            !taskExistsInTeam -> {
                "A task with this name already exists in this team!"
            }
            else -> {
                ""
            }
        }

        // Update the taskNameValue with the trimmed version if there are no errors
        if (taskNameError.isBlank()) {
            taskNameValue = trimmedTaskName
        }
    }


    var peopleOrTaskNameError by mutableStateOf("")
        private set

    private fun checkPeople() {
        val hasOwnerOrAdmin = selectedPeople.any { person ->
            person.permission == "Owner" || person.permission == "Admin"
        }

        peopleOrTaskNameError = if (!hasOwnerOrAdmin) {
            "Add at least one person who is either the owner or an admin!"
        } else {
            ""
        }
    }



    var selectedDateTime by mutableStateOf("")
    fun setDueDateDateTime(value: String) {
        selectedDateTime = value
    }

    var selectedDateTimeError by mutableStateOf("")
        private set

    private fun checkSelectedDateTimeError() {
        val iso8601Format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
        selectedDateTimeError = if (selectedDateTime.isBlank()) {
            "Please, select a due date!"
        } else {
            val selectedDate = iso8601Format.parse(selectedDateTime) //NOT NEEDED

            // Calendar instance for the current time plus one hour
            val calendar = Calendar.getInstance().apply {
                add(Calendar.HOUR_OF_DAY, 1)
            }

            // Check if the selected date is before the current time plus one hour
            if (selectedDate == null || selectedDate.before(calendar.time)) {
                "The selected date and time must be at least one hour in the future."
            } else {
                ""
            }
        }
    }

    var showDatePicker by mutableStateOf(false)
    fun setShowingDatePicker(value: Boolean) {
        showDatePicker = value
    }

    var showTimePicker by mutableStateOf(false)
    fun setShowingTimePicker(value: Boolean) {
        showTimePicker = value
    }

    // *** DATE RANGE ***
    var selectedTempStartDateTime by mutableStateOf("")
    fun setTempDueDateStartDateTime(value: String) {
        selectedTempStartDateTime = value
    }

    var selectedTempEndDateTime by mutableStateOf("")
    fun setTempDueDateEndDateTime(value: String) {
        selectedTempEndDateTime = value
    }

    var showStartDatePicker by mutableStateOf(false)
    fun setIsShowingStartDatePicker(value: Boolean) {
        showStartDatePicker = value
    }

    var showEndDatePicker by mutableStateOf(false)
    fun setIsShowingEndDatePicker(value: Boolean) {
        showEndDatePicker = value
    }

    var selectedDateRangeError by mutableStateOf("")
        private set

    fun clearSelectedDateRangeError() {
        selectedDateRangeError = ""
    }

    fun checkTempSelectedDateRangeError() {
        val iso8601Format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())

        val selectedStartDate = try {
            iso8601Format.parse(selectedTempStartDateTime)
        } catch (e: Exception) {
            null
        }

        val selectedEndDate = try {
            iso8601Format.parse(selectedTempEndDateTime)
        } catch (e: Exception) {
            null
        }

        selectedDateRangeError = when {
            selectedEndDate != null && selectedStartDate != null && selectedEndDate.before(
                selectedStartDate
            ) -> {
                "The end date must be after the start date."
            }

            else -> ""
        }
    }

    var selectedStartDateTime by mutableStateOf("")
    fun setDueDateStartDateTime(value: String) {
        selectedStartDateTime = value
    }

    var selectedEndDateTime by mutableStateOf("")
    fun setDueDateEndDateTime(value: String) {
        selectedEndDateTime = value
    }


    var recurrencyOptions by mutableStateOf(listOf("Never", "Weekly", "Monthly", "Yearly"))
    var expandedRecurrenceDropdown by mutableStateOf(false)
    fun setExpandedRecurrencDropdown(value: Boolean) {
        expandedRecurrenceDropdown = value
    }

    var selectedTextForRecurrence by mutableStateOf(recurrencyOptions[0])

    fun setTaskRecurrency(n: String) {
        selectedTextForRecurrence = n
    }

    var notPriorityValue by mutableIntStateOf(1) //1 = non priority, 0 priority

    fun setTaskPriority(n: Int) {
        notPriorityValue = n
    }

    var teamDescriptionValue by mutableStateOf("")
        private set

    /*
    var teamDescriptionError by mutableStateOf("")
        private set
     */
    fun setTeamDescription(desc: String) {
        teamDescriptionValue = desc
    }
    /*
    private fun checkTeamDescription() {
        teamDescriptionError = if (teamDescriptionValue.length > 200) {
            "Description must be at most 200 characters!"
        } else {
            ""
        }
    }
    */

    var showFilterMemberInFilters by mutableStateOf(false)
    fun setShowingFilterMemberInFilters(value: Boolean) {
        showFilterMemberInFilters = value
    }

    var listOfMembersForFilter by mutableStateOf(emptyList<PersonData>())

    private fun clearMembersInFilterPage() {
        listOfMembersForFilter = emptyList()
    }

    fun setMembersInFilterPage() {
        listOfMembersForFilter = tempListOfMembersForFilter.toList()
    }

    var tempListOfMembersForFilter by mutableStateOf(emptyList<PersonData>())

    // Method to add members
    fun addTempMemberToFilter(member: PersonData) {
        // Check if the member is already in the list based on username
        if (tempListOfMembersForFilter.none { it.username == member.username }) {
            tempListOfMembersForFilter = tempListOfMembersForFilter + member
        }
    }

    // Method to remove members
    fun removeTempMemberToFilter(member: PersonData) {
        tempListOfMembersForFilter = tempListOfMembersForFilter - member
    }

    private fun clearTempMembersInFilterPage() {
        tempListOfMembersForFilter = emptyList()
    }

    // ----- Task Description -----
    var taskDescriptionValue by mutableStateOf("")
        private set
    private var taskDescriptionError by mutableStateOf("")
    fun setTaskDescription(desc: String) {
        taskDescriptionValue = desc
    }

    private fun checkTaskDescription() {
        taskDescriptionError = if (taskDescriptionValue.length > 200) {
            "Description must be at most 200 characters!"
        } else {
            ""
        }
    }

    fun markAsCompletedOrScheduled(teamId: String, taskId: String) {
        viewModelScope.launch {
            try {
                updateTaskStatusToCompletedOrScheduled(teamId, taskId)
            } catch (e: Exception) {
                // Handle any exceptions that occur during the Firestore operations
                e.printStackTrace()
            }
        }
    }

    private suspend fun updateTaskStatusToCompletedOrScheduled(teamId: String, taskId: String) {
        val taskRef = db.collection("tasks").document(taskId)
        val taskSnapshot = taskRef.get().await()

        if (taskSnapshot.exists()) {
            val task = taskSnapshot.toObject(Task::class.java)
            val currentStatus = task?.status
            if (currentStatus == "Scheduled") {
                taskRef.update("status", "Completed").await()

                val completionTime = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

                // Create a notification (typology 4) for the task
                val notification = mapOf(
                    "body" to "*${task?.title}* has been completed successfully",
                    "fromGroup" to true,
                    "receivers" to (task?.people ?: ""),
                    "senderId" to teamId,
                    "taskId" to taskId,
                    "teamId" to "",
                    "timestamp" to completionTime,
                    "typology" to 4
                )

                // Add the notification to the 'notifications' collection
                val notificationRef = db.collection("notifications").add(notification).await()
                val notificationId = notificationRef.id

                // Add a document in the 'user_notifications' collection for each task member
                if (task != null) {
                    for (personId in task.people) {
                        //if (personId != auth.uid) {
                            val userNotification = mapOf(
                                "notificationId" to notificationId,
                                "read" to false,
                                "userId" to personId
                            )
                            db.collection("user_notifications").add(userNotification).await()
                        //}
                    }
                }

                val time = LocalDateTime.parse(completionTime, DateTimeFormatter.ISO_DATE_TIME)
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm")


                taskId?.let {
                    Comment(
                        it,
                        "",
                        LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
                        "Task completed at ${time.format(formatter)}",
                        null,
                        false,
                        emptyList(),
                        true
                    )
                }?.let {
                    db.collection("comments").add(
                        it
                    )
                }
            } else {
                taskRef.update("status", "Scheduled").await()
            }
        } else {
            // Handle the case where the task does not exist
        }
    }
    private suspend fun addTaskToFirestore(task: Task?, teamId: String) {
        try {
            // Add the task to the 'tasks' collection
            val taskRef = task?.let { db.collection("tasks").add(it).await() }
            val taskId = taskRef?.id

            // Add the task ID to the 'tasks' field in the corresponding team document
            db.collection("teams").document(teamId)
                .update("tasks", FieldValue.arrayUnion(taskId)).await()

            // Add the task ID to the 'tasks' field in each person document
            if (task != null) {
                for (personId in task.people) {
                    db.collection("people").document(personId)
                        .update("tasks", FieldValue.arrayUnion(taskId)).await()
                }
            }

            val creationTime = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

            // Create a notification (typology 1) for the task
            val notification = mapOf(
                "body" to "You have a new task",
                "fromGroup" to true,
                "receivers" to (task?.people ?: ""),
                "senderId" to teamId,
                "taskId" to taskId,
                "teamId" to "",
                "timestamp" to creationTime,
                "typology" to 1
            )

            // Add the notification to the 'notifications' collection
            val notificationRef = db.collection("notifications").add(notification).await()
            val notificationId = notificationRef.id

            // Add a document in the 'user_notifications' collection for each task member except the task creator
            if (task != null) {
                for (personId in task.people) {
                    if (personId != auth.uid) {
                        val userNotification = mapOf(
                            "notificationId" to notificationId,
                            "read" to false,
                            "userId" to personId
                        )
                        db.collection("user_notifications").add(userNotification).await()
                    }
                }
            }

            val time = LocalDateTime.parse(creationTime, DateTimeFormatter.ISO_DATE_TIME)
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm")


            taskId?.let {
                Comment(
                    it,
                    "",
                    LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
                    "Task created at ${time.format(formatter)}",
                    null,
                    false,
                    emptyList(),
                    true
                )
            }?.let {
                db.collection("comments").add(
                    it
                )
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    var isLoadingTaskCreation = mutableStateOf(false)

    fun validateCreateTask(teamId: String) {
        when (currentStep) {
            TaskCreationStep.Status -> {
                viewModelScope.launch {
                    checkTaskName(teamId)
                    checkSelectedDateTimeError()
                    if (taskNameError.isBlank() && selectedDateTimeError.isBlank()) {
                        currentStep = TaskCreationStep.Description
                        if(peopleOrTaskNameError == "A task with this name already exists in this team. Go back and insert another name!") {
                            peopleOrTaskNameError = ""
                        }
                    }
                }
            }
            TaskCreationStep.Description -> {
                checkTaskDescription()
                if(taskDescriptionError.isBlank()) {
                    currentStep = TaskCreationStep.People
                }
            }

            TaskCreationStep.People -> {
                checkPeople()
                if(peopleOrTaskNameError.isBlank()) {
                    viewModelScope.launch {
                        checkTaskName(teamId)
                        if (taskNameError.isBlank()) {
                            isLoadingTaskCreation.value = true
                            /*val newTask = ToDoTask(
                               "hardcoded", //TODO: HARDCODED
                               taskNameValue, "Scheduled", notPriorityValue,
                               selectedTextForRecurrence, selectedDateTime,
                               ZonedDateTime.now().format(
                                   DateTimeFormatter.ISO_OFFSET_DATE_TIME
                               ), selectedPeople,
                               selectedTagsForNewTask
                           )*/
                            val newTask = auth.uid?.let {
                                Task(
                                    teamId = teamId,
                                    title = taskNameValue,
                                    description = taskDescriptionValue,
                                    creatorId = it,
                                    creationDate = ZonedDateTime.now()
                                        .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                                    deadline = selectedDateTime,
                                    prioritized = notPriorityValue == 0,
                                    status = "Scheduled",
                                    tags = selectedTagsForNewTask,
                                    recurrence = selectedTextForRecurrence,
                                    people = selectedPeople.map { it.personId }
                                )
                            }

                            addTaskToFirestore(newTask, teamId)
                            cancelCreateTask()
                            Actions.getInstance().goToTeamTasks(teamId)
                            onSearchQueryChanged("")
                            currentStep = TaskCreationStep.Status
                        } else {
                            peopleOrTaskNameError = "A task with this name already exists in this team. Go back and insert another name!"
                        }
                        isLoadingTaskCreation.value = false
                    }
                }
            }
        }
    }

    fun setPrior(value: Int) {
        isNotPriority = value
    }

    fun setRec(value: Int) {
        isNotRecurrent = value
    }

    fun setStat(value: Int) {
        status = value
    }

    var tempIsSortByCreationDate by mutableIntStateOf(sortByCreationDate)
    var tempIsNotPriority by mutableIntStateOf(isNotPriority)
    var tempIsNotRecurrent by mutableIntStateOf(isNotRecurrent)
    var tempStatus by mutableIntStateOf(status)
    fun setTempPrior(value: Int) {
        tempIsNotPriority = value
    }

    fun setTempRec(value: Int) {
        tempIsNotRecurrent = value
    }

    fun setTempStat(value: Int) {
        tempStatus = value
    }

    fun setTempSortByCreation(value: Int) {
        tempIsSortByCreationDate = value
    }

    fun handleBackFilter() {
        tempIsSortByCreationDate = if (sortByCreationDate != -1) sortByCreationDate else -1
        tempIsNotPriority = if (isNotPriority != -1) isNotPriority else -1
        tempIsNotRecurrent = if (isNotRecurrent != -1) isNotRecurrent else -1
        tempStatus = if (status != -1) status else -1
        tempListOfMembersForFilter =
            listOfMembersForFilter.ifEmpty { emptyList() } //IS THE SAME OF if (listOfMembersForFilter.isNotEmpty()) listOfMembersForFilter else emptyList(), BUT THIS GIVES A WARNING!
        tempSelectedTags = selectedTags.ifEmpty { emptyList() }
        selectedTempStartDateTime = if (selectedStartDateTime != "") {
            selectedStartDateTime
        } else {
            ""
        }
        selectedTempEndDateTime = if (selectedEndDateTime != "") {
            selectedEndDateTime
        } else {
            ""
        }
    }

    fun clearTempState() {
        setTempSortByCreation(-1)
        setTempPrior(-1)
        setTempRec(-1)
        setTempStat(-1)
        clearTempMembersInFilterPage()
        clearTempSelectedTags()
        setSortModality(-1)
        setPrior(-1)
        setRec(-1)
        setStat(-1)
        clearMembersInFilterPage()
        clearSelectedTags()
        setTempDueDateStartDateTime("")
        setTempDueDateEndDateTime("")
        setDueDateStartDateTime("")
        setDueDateEndDateTime("")
    }

    fun applyTempState() {
        setSortModality(tempIsSortByCreationDate)
        setPrior(tempIsNotPriority)
        setRec(tempIsNotRecurrent)
        setStat(tempStatus)
        setMembersInFilterPage()
        setSelectedTags()
        setDueDateStartDateTime(selectedTempStartDateTime)
        setDueDateEndDateTime(selectedTempEndDateTime)
    }

    // Task people
    private val _taskpeople = mutableStateOf(listOf<PersonData>())
        /*
        PersonData("0", "Luca", "Bianchi", "luca_bianchi", "CEO", "Owner", ""),
        PersonData(
            "1",
            "Name1ejwnewjneees",
            "Surname1fskfsmkfnsk",
            "username1",
            "CTO",
            "Admin",
            ""
        ),
        PersonData("2", "Sofia", "Esposito", "sofia_esposito", "Marketing Director", "", ""),
        PersonData("3", "Giulia", "Ricci", "giulia_ricci", "HR Manager", "", ""),

    ).sortedBy { it.name })
         */

    // Provide an immutable view of the taskpeople to the UI
    val taskpeople: List<PersonData> get() = _taskpeople.value

    // Method to remove a person from taskpeople
    fun removePersonFromTask(teamId: String, personId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Update local state
                val updatedTaskPeople = _taskpeople.value.filter { it.personId != personId }
                _taskpeople.value = updatedTaskPeople

                val updatedTasks = _toDoTasks.value.map { task ->
                    task.copy(taskpeople = task.taskpeople.filter { it.personId != personId })
                }
                _toDoTasks.value = updatedTasks

                // Update Firestore tasks collection
                val tasksSnapshot =
                    db.collection("tasks").whereEqualTo("teamId", teamId).get().await()
                tasksSnapshot.documents.forEach { taskDoc ->
                    val taskPeople =
                        taskDoc.get("people") as? MutableList<String> ?: mutableListOf()
                    if (taskPeople.contains(personId)) {
                        taskPeople.remove(personId)
                        db.collection("tasks").document(taskDoc.id).update("people", taskPeople)
                            .await()
                    }
                }

                // Update the filters
                listOfMembersForFilter = listOfMembersForFilter.filter { it.personId != personId }
                tempListOfMembersForFilter =
                    tempListOfMembersForFilter.filter { it.personId != personId }
            } catch (e: Exception) {
                // Handle any errors that occur during the database operation
                e.printStackTrace()
            }
        }
    }

    fun removePersonFromTeam(teamId: String, personId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Remove person from Firestore team document
                val teamRef = db.collection("teams").document(teamId)
                val document = teamRef.get().await()

                if (document.exists()) {
                    val members = document.get("members") as? MutableList<String> ?: mutableListOf()
                    val admins = document.get("admins") as? MutableList<String> ?: mutableListOf()

                    if (members.contains(personId)) {
                        members.remove(personId)
                        teamRef.update("members", members).await()
                    }

                    if (admins.contains(personId)) {
                        admins.remove(personId)
                        teamRef.update("admins", admins).await()
                    }

                    // Remove the person from local state
                    val updatedTeamList = _teampeople.value.filter { it.personId != personId }
                    _teampeople.value = updatedTeamList

                    // Remove the person from task people in all tasks
                    val updatedTaskPeople = _taskpeople.value.filter { it.personId != personId }
                    _taskpeople.value = updatedTaskPeople
                    val updatedTasks = _toDoTasks.value.map { task ->
                        task.copy(taskpeople = task.taskpeople.filter { it.personId != personId })
                    }
                    _toDoTasks.value = updatedTasks

                    // Update Firestore tasks collection
                    val tasksSnapshot =
                        db.collection("tasks").whereEqualTo("teamId", teamId).get().await()
                    tasksSnapshot.documents.forEach { taskDoc ->
                        val taskPeople =
                            taskDoc.get("people") as? MutableList<String> ?: mutableListOf()
                        if (taskPeople.contains(personId)) {
                            taskPeople.remove(personId)
                            db.collection("tasks").document(taskDoc.id).update("people", taskPeople)
                                .await()
                        }
                    }

                    // Remove the person from the team_participants collection
                    val participantsSnapshot = db.collection("team_participants")
                        .whereEqualTo("teamId", teamId)
                        .whereEqualTo("personId", personId)
                        .get().await()
                    participantsSnapshot.documents.forEach { participantDoc ->
                        db.collection("team_participants").document(participantDoc.id).delete()
                            .await()
                    }

                    //Remove the team from people
                    db.collection("people").document(personId)
                        .update("teams", FieldValue.arrayRemove(teamId))
                        .await()

                    //Remove team tasks from person
                    document.get("tasks") as? List<String> ?: emptyList<String>().forEach {
                        db.collection("people").document(personId)
                            .update("tasks", FieldValue.arrayRemove(it))
                            .await()
                    }

                    // Update the filters
                    listOfMembersForFilter =
                        listOfMembersForFilter.filter { it.personId != personId }
                    tempListOfMembersForFilter =
                        tempListOfMembersForFilter.filter { it.personId != personId }
                }
            } catch (e: Exception) {
                // Handle any errors that occur during the database operation
                e.printStackTrace()
            }
        }
    }

    // Team people
    private val _teampeople = mutableStateOf(emptyList<PersonData>())

    // Provide an immutable view of the teampeople to the UI
    val teampeople: List<PersonData> get() = _teampeople.value

    var searchQuery = mutableStateOf("")

    // Computed list that filters people based on search query
    val filteredPeople: List<PersonData>
        get() = if (searchQuery.value.isEmpty()) {
            teampeople
        } else {
            teampeople.filter {
                it.name.contains(searchQuery.value, ignoreCase = true) ||
                        it.surname.contains(searchQuery.value, ignoreCase = true)
            }
        }

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }


    var searchQueryForNewTask = mutableStateOf("")
    fun onSearchQueryForNewTaskChanged(query: String) {
        searchQueryForNewTask.value = query
    }

    fun sortTasks() {
        _toDoTasks.value = when (sortByCreationDate) {
            1 -> {
                _toDoTasks.value.sortedBy { it.creationTimestamp }
            }

            0 -> {
                _toDoTasks.value.sortedBy { it.expirationTimestamp }
            }

            else -> {
                _toDoTasks.value.sortedBy { it.expirationTimestamp }
            }
        }
    }

    // Hardcoded list of scheduled tasks
    private val _toDoTasks = mutableStateOf(
        listOf(
            ToDoTask(
                "0",
                "Task 1",
                "Completed",
                1,
                "Weekly",
                "2024-04-30T12:53:00+02:00",
                "2024-04-01T09:00:00+02:00",
                listOf(
                    PersonData("0", "Luca", "Bianchi", "luca_bianchi", "CEO", "Owner", ""),
                    PersonData(
                        "1",
                        "Name1ejwnewjneees",
                        "Surname1fskfsmkfnsk",
                        "username1",
                        "CTO",
                        "Admin",
                        ""
                    ),
                    PersonData(
                        "2",
                        "Sofia",
                        "Esposito",
                        "sofia_esposito",
                        "Marketing Director",
                        "",
                        ""
                    ),
                    PersonData("3", "Giulia", "Ricci", "giulia_ricci", "HR Manager", "", ""),
                ).sortedBy { it.name },
                listOf("#test1", "#test2")
            ),
            ToDoTask(
                "1",
                "Task 2",
                "Expired",
                0,
                "Never",
                "2024-04-20T16:42:00+02:00",
                "2024-04-01T10:00:00+02:00",
                listOf(
                    PersonData("0", "Luca", "Bianchi", "luca_bianchi", "CEO", "Owner", ""),
                    PersonData("1", "Giulia", "Ricci", "giulia_ricci", "HR Manager", "", ""),
                ).sortedBy { it.name },
                listOf("#test1", "#test2")
            ),
            ToDoTask(
                "2",
                "Task 2.5",
                "Completed",
                0,
                "Never",
                "2024-04-20T16:42:00+02:00",
                "2024-04-01T10:00:00+02:00",
                listOf(
                    PersonData("0", "Luca", "Bianchi", "luca_bianchi", "CEO", "Owner", ""),
                    PersonData(
                        "1",
                        "Sofia",
                        "Esposito",
                        "sofia_esposito",
                        "Marketing Director",
                        "",
                        ""
                    ),
                ).sortedBy { it.name },
                listOf("#test1", "#test2")
            ),
            ToDoTask(
                "3",
                "Task 2.6",
                "Completed",
                0,
                "Never",
                "2024-04-20T16:42:00+02:00",
                "2024-04-01T10:00:00+02:00",
                listOf(
                    PersonData("0", "Luca", "Bianchi", "luca_bianchi", "CEO", "Owner", ""),
                    PersonData(
                        "1",
                        "Sofia",
                        "Esposito",
                        "sofia_esposito",
                        "Marketing Director",
                        "",
                        ""
                    ),
                ).sortedBy { it.name },
                listOf("#test1", "#test2")
            ),
            ToDoTask(
                "4",
                "Task 2.7",
                "Completed",
                0,
                "Never",
                "2024-04-20T16:42:00+02:00",
                "2024-04-01T10:00:00+02:00",
                listOf(
                    PersonData("0", "Luca", "Bianchi", "luca_bianchi", "CEO", "Owner", ""),
                    PersonData(
                        "1",
                        "Sofia",
                        "Esposito",
                        "sofia_esposito",
                        "Marketing Director",
                        "",
                        ""
                    ),
                ).sortedBy { it.name },
                listOf("#test1", "#test2")
            ),
            ToDoTask(
                "5",
                "Task 2.8",
                "Completed",
                0,
                "Never",
                "2024-04-20T16:42:00+02:00",
                "2024-04-01T10:00:00+02:00",
                listOf(
                    PersonData("0", "Luca", "Bianchi", "luca_bianchi", "CEO", "Owner", ""),
                    PersonData(
                        "1",
                        "Sofia",
                        "Esposito",
                        "sofia_esposito",
                        "Marketing Director",
                        "",
                        ""
                    ),
                ).sortedBy { it.name },
                listOf("#test1", "#test2")
            ),
            ToDoTask(
                "6",
                "Task 3",
                "Scheduled",
                1,
                "Never",
                "2024-05-07T13:36:00+02:00",
                "2024-04-02T11:00:00+02:00",
                listOf(
                    PersonData(
                        "0",
                        "Name1ejwnewjneees",
                        "Surname1fskfsmkfnsk",
                        "username1",
                        "CTO",
                        "Admin",
                        ""
                    ),
                    PersonData("1", "Giulia", "Ricci", "giulia_ricci", "HR Manager", "", ""),
                ).sortedBy { it.name },
                listOf("#test4", "#test5")
            ),
            ToDoTask(
                "7",
                "Task 4",
                "Scheduled",
                0,
                "Monthly",
                "2024-05-30T12:12:00+02:00",
                "2024-04-02T12:00:00+02:00",
                listOf(
                    PersonData(
                        "0",
                        "Sofia",
                        "Esposito",
                        "sofia_esposito",
                        "Marketing Director",
                        "",
                        ""
                    ),
                    PersonData("1", "Giulia", "Ricci", "giulia_ricci", "HR Manager", "", ""),
                ).sortedBy { it.name },
                listOf("#test1", "#test2")
            ),
            ToDoTask(
                "8",
                "Task 5",
                "Scheduled",
                1,
                "Yearly",
                "2024-05-07T22:21:00+02:00",
                "2024-04-03T08:00:00+02:00",
                listOf(
                    PersonData(
                        "0",
                        "Sofia",
                        "Esposito",
                        "sofia_esposito",
                        "Marketing Director",
                        "",
                        ""
                    ),
                    PersonData("1", "Giulia", "Ricci", "giulia_ricci", "HR Manager", "", ""),
                ).sortedBy { it.name },
                listOf("#test4", "#test5")
            )
        )
    )
    //TO INITIALIZE AS AN EMPTY LIST:
    //private val _toDoTasks = mutableStateOf(emptyList<toDoTask>())


    init {
        sortTasks() // Sort immediately after initialization
    }


    // Provide an immutable view of the messages to the UI
    val toDoTasks: List<ToDoTask> get() = _toDoTasks.value

    // Function to update the task statuses based on expiration time
    fun updateTaskStatuses() {
        // Date format for parsing ISO8601 strings
        val iso8601Format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())

        _toDoTasks.value = _toDoTasks.value.map { task ->
            val expirationDate = iso8601Format.parse(task.expirationTimestamp)

            // Calendar instance for the current time plus one hour
            var calendar = Calendar.getInstance().apply {
                add(Calendar.HOUR_OF_DAY, 0)
            }
            //Log.d("TAG", (calendar.time).toString())

            if (expirationDate != null && expirationDate.before(calendar.time)) {
                when (task.status) {
                    "Scheduled" -> {
                        // Handle expired recurring tasks
                        if (task.recurrence != "Never") {
                            calendar = Calendar.getInstance().apply {
                                when (task.recurrence) {
                                    "Weekly" -> add(Calendar.WEEK_OF_YEAR, 1)
                                    "Monthly" -> add(Calendar.MONTH, 1)
                                    "Yearly" -> add(Calendar.YEAR, 1)
                                }
                            }
                            task.copy(expirationTimestamp = iso8601Format.format(calendar.time))//IT REMAINS SCHEDULED
                        } else {
                            // If the task isn't recurring, just mark it as expired
                            task.copy(status = "Expired")
                        }
                    }

                    "Completed" -> {
                        // For completed recurring tasks, reset to "Scheduled" and update the expiration timestamp
                        if (task.recurrence != "Never") {
                            calendar = Calendar.getInstance().apply {
                                when (task.recurrence) {
                                    "Weekly" -> add(Calendar.WEEK_OF_YEAR, 1)
                                    "Monthly" -> add(Calendar.MONTH, 1)
                                    "Yearly" -> add(Calendar.YEAR, 1)
                                }
                            }
                            task.copy(
                                status = "Scheduled",
                                expirationTimestamp = iso8601Format.format(calendar.time)
                            )
                        } else {
                            task
                        }
                    }

                    else -> task
                }
            } else {
                task
            }
        }
        sortTasks()
    }


    private fun addTask(toDoTask: ToDoTask) {
        val newTask = ToDoTask(
            taskId = "hardocded1", //TODO: HARDCODED
            taskName = toDoTask.taskName,
            status = toDoTask.status,
            isNotPriority = toDoTask.isNotPriority,
            recurrence = toDoTask.recurrence,
            expirationTimestamp = toDoTask.expirationTimestamp,
            creationTimestamp = toDoTask.creationTimestamp,
            taskpeople = toDoTask.taskpeople.toList(), // Ensure a new list is created,
            tags = toDoTask.tags.toList()
        )
        val updatedTasks = _toDoTasks.value + newTask
        _toDoTasks.value =
            updatedTasks.sortedBy { if (sortByCreationDate == 1) it.creationTimestamp else it.expirationTimestamp }
    }

    var searchQueryForTasks = mutableStateOf("")


    private val statusMapping = mapOf(
        "Scheduled" to 0,
        "Completed" to 1,
        "Expired" to 2
    )

    // Computed list that filters tasks based on search query and other conditions
    val filteredTasks: List<ToDoTask>
        get() {
            // Start with all tasks or only those matching the search query
            val initialFilter = if (searchQueryForTasks.value.isEmpty()) {
                toDoTasks
            } else {
                toDoTasks.filter {
                    it.taskName.contains(
                        searchQueryForTasks.value,
                        ignoreCase = true
                    )
                }
            }

            // Sort tasks based on some criteria (not defined in the provided code, assuming it exists)
            sortTasks()

            // Further filter based on set conditions for priority, recurrence, status, members, tags, and date range
            return initialFilter.filter { task ->
                val meetsPriorityCondition =
                    if (isNotPriority != -1) task.isNotPriority == isNotPriority else true
                val meetsRecurrentCondition = if (isNotRecurrent != -1) {
                    if (isNotRecurrent == 0) task.recurrence != "Never"
                    else task.recurrence == "Never" // isNotRecurrent == 1
                } else true
                val meetsStatusCondition =
                    if (status != -1) statusMapping[task.status] == status else true

                // Check if all members in listOfMembersForFilter are in the task's taskpeople list
                val membersCondition = listOfMembersForFilter.all { filterMember ->
                    task.taskpeople.any { taskMember ->
                        taskMember.username == filterMember.username
                    }
                }

                // Check if all tags in selectedTags are in the task's tags list
                val tagsCondition = selectedTags.all { filterTag ->
                    task.tags.contains(filterTag)
                }

                // Check if the task's date falls within the selected date range if either date is provided
                val datePart =
                    if (sortByCreationDate == 1) task.creationTimestamp.split("T")[0] else task.expirationTimestamp.split(
                        "T"
                    )[0]
                val dateCondition = when {
                    selectedTempStartDateTime.isNotEmpty() && selectedTempEndDateTime.isNotEmpty() -> {
                        val startDate = selectedTempStartDateTime.split("T")[0]
                        val endDate = selectedTempEndDateTime.split("T")[0]
                        datePart in startDate..endDate
                    }

                    selectedTempStartDateTime.isNotEmpty() -> {
                        val startDate = selectedTempStartDateTime.split("T")[0]
                        datePart >= startDate
                    }

                    selectedTempEndDateTime.isNotEmpty() -> {
                        val endDate = selectedTempEndDateTime.split("T")[0]
                        datePart <= endDate
                    }

                    else -> true // No date range filtering if both dates are empty
                }

                meetsPriorityCondition && meetsRecurrentCondition && meetsStatusCondition && membersCondition && tagsCondition && dateCondition
            }
        }

    fun onSearchQueryForTasksChanged(query: String) {
        searchQueryForTasks.value = query
    }


    //SELECTED PEOPLE THAT YOU WANT TO ADD
    // Use mutableStateListOf to directly create an observable and mutable list
    private val _selectedPeople = mutableStateListOf<PersonData>()

    // Expose an immutable view of the list
    val selectedPeople: List<PersonData> get() = _selectedPeople

    fun clearSelectedPeople() {
        _selectedPeople.clear()
    }

    fun addPerson(person: PersonData) {
        // Ensure no duplicate person is added based on a unique attribute, e.g., username
        if (_selectedPeople.none { it.username == person.username }) {
            _selectedPeople.add(person)
        }
    }

    fun removePerson(person: PersonData) {
        _selectedPeople.remove(person)
    }

    // Function to add all unique selected teampeople to taskpeople
    fun addSelectedTeamPeopleToTask() {
        // Retrieve the current list of task people, make it mutable for modification
        val currentTaskPeople = _taskpeople.value.toMutableList()

        // Iterate over selected people
        _selectedPeople.forEach { selected ->
            // Check if the selected person is already in the task people list based on username
            if (currentTaskPeople.none { it.username == selected.username }) {
                // If not present, add to the task people list
                currentTaskPeople.add(selected)
            }
        }

        // Sort the updated list by name
        currentTaskPeople.sortBy { it.name }

        // Update the state of task people with the new list
        _taskpeople.value = currentTaskPeople

        // Optionally, clear selected people after adding them to the task
        _selectedPeople.clear()
    }
}


// ----- Button to add a new task -----
@Composable
fun CustomFloatingButton(
    modifier: Modifier,
    type: String,
    setAddMembersModality: (Boolean) -> Unit,
    createTask: () -> Unit,
    addSelectedTeamPeopleToTask: () -> Unit,
    clearSelectedPeople: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    setTaskDescription: (String) -> Unit,
    teamId: String = ""
) {
    val palette = MaterialTheme.colorScheme

    val icon = when (type) {
        //"Filter Add Members" -> R.drawable.outline_done_24
        "Share Link or Qr" -> R.drawable.outline_add_24
        "New Task" -> R.drawable.outline_add_24
        "Add Members" -> R.drawable.outline_person_add_24
        "Confirm Add Members" -> R.drawable.outline_done_24
        else -> R.drawable.teamtasklogo
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomEnd
    ) {
        FloatingActionButton(
            onClick = {
                when (type) {
                    /*
                    "Filter Add Members" -> {
                        onSearchQueryChanged("")
                        setAddMembersModality(false)
                    }*/
                    "New Task" -> {
                        setTaskDescription("")
                        createTask()
                        clearSelectedPeople()
                        Actions.getInstance().goToCreateTaskStatus(teamId)
                    }

                    "Add Members" -> {
                        clearSelectedPeople()
                        setAddMembersModality(true)
                    }

                    "Confirm Add Members" -> {
                        onSearchQueryChanged("")
                        addSelectedTeamPeopleToTask()
                        setAddMembersModality(false)
                        clearSelectedPeople()
                    }

                    else -> {}
                }
            },
            containerColor = palette.secondary,
            modifier = Modifier.padding(25.dp)
        ) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = "Add a new task",
                modifier = Modifier.size(30.dp),
                colorFilter = ColorFilter.tint(palette.background)
            )
        }
    }
}

@Composable
fun FilterBadge(
    label: String, onRemove: () -> Unit
) {
    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(color = palette.surfaceVariant, shape = CircleShape)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.outline_close_24),
            contentDescription = "Remove",
            modifier = Modifier
                .size(30.dp)
                .clickable(onClick = onRemove)
                .padding(4.dp),
            colorFilter = ColorFilter.tint(palette.onSurface)
        )
        Text(
            text = label,
            style = typography.labelSmall,
            color = palette.onSurface,
            modifier = Modifier.padding(4.dp),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}


@Composable
fun NewTask(
    teamId: String,
    vm: SpecificTeamViewModel = viewModel()
) {
    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography

    if(vm.isLoadingTaskCreation.value){
        LoadingScreen()
    }
    Scaffold(
        bottomBar = {
            BottomAppBar(
                containerColor = palette.background,
                contentColor = palette.background,
                modifier = Modifier.height(65.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = if (vm.currentStep == TaskCreationStep.Status) Arrangement.End else Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (vm.currentStep != TaskCreationStep.Status) {
                        // Back button
                        Button(
                            onClick = { vm.goToPreviousStep() },
                            enabled = true, // Disable back button on first step
                            modifier = Modifier.width(110.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = palette.primary, contentColor = palette.secondary)
                        ) {
                            Text("Back")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    // Next/Create button
                    Button(
                        onClick = {
                            vm.validateCreateTask(teamId)
                        },
                        modifier = Modifier.width(110.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = palette.secondary, contentColor = palette.background)
                    ) {
                        Text(if (vm.currentStep == TaskCreationStep.People) { "Create" } else { "Next" })
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (vm.currentStep) {
                TaskCreationStep.Status -> StatusStep(
                    vm.taskNameValue,
                    vm.taskNameError,
                    vm::setTaskName,
                    vm.selectedDateTime,
                    vm.selectedDateTimeError,
                    vm::setDueDateDateTime,
                    vm.showDatePicker,
                    vm::setShowingDatePicker,
                    vm.showTimePicker,
                    vm::setShowingTimePicker,
                    vm.recurrencyOptions,
                    vm.expandedRecurrenceDropdown,
                    vm::setExpandedRecurrencDropdown,
                    vm.selectedTextForRecurrence,
                    vm::setTaskRecurrency,
                    vm.notPriorityValue,
                    vm::setTaskPriority,
                    vm.taskTagsList,
                    vm.selectedTags,
                    vm::addTagForNewTask,
                    vm::removeTagForNewTask,
                )

                TaskCreationStep.Description -> DescriptionStep(
                    vm.taskDescriptionValue,
                    vm::goToPreviousStep,
                    vm::setTaskDescription
                )

                TaskCreationStep.People -> PeopleStepCreation(
                    teamId,
                    vm::goToPreviousStep,
                    vm.taskpeople, vm.teampeople, vm.selectedPeople, vm::clearSelectedPeople,
                    vm::addPerson, vm::removePerson,
                    vm::addSelectedTeamPeopleToTask, vm::removePersonFromTask,
                    vm.filteredPeople,
                    //isInAddMode, setAddMode,
                    vm.searchQueryForNewTask.value, vm::onSearchQueryForNewTaskChanged,
                    vm.peopleOrTaskNameError
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}


@Composable
fun DateRangePicker(
    selectedTempStartDateTime: String,
    selectedTempEndDateTime: String,
    setTempDueDateStartDateTime: (String) -> Unit,
    setTempDueDateEndDateTime: (String) -> Unit,
    showStartDatePicker: Boolean,
    setIsShowingStartDatePicker: (Boolean) -> Unit,
    showEndDatePicker: Boolean,
    setIsShowingEndDatePicker: (Boolean) -> Unit,
    selectedDateRangeError: String,
    checkTempSelectedDateRangeError: () -> Unit
) {
    val context = LocalContext.current

    // Format for the ISO 8601 string
    val iso8601Format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())

    var tempStartCalendar by remember(selectedTempStartDateTime) {
        mutableStateOf(
            if (selectedTempStartDateTime.isNotEmpty()) {
                Calendar.getInstance().apply {
                    try {
                        time = iso8601Format.parse(selectedTempStartDateTime)
                            ?: Calendar.getInstance().time
                    } catch (e: Exception) {
                        Calendar.getInstance().time // Fallback to current time on parsing failure
                    }
                }
            } else {
                Calendar.getInstance() // Default to current time if no date is set
            }
        )
    }

    var tempEndCalendar by remember(selectedTempEndDateTime) {
        mutableStateOf(
            if (selectedTempEndDateTime.isNotEmpty()) {
                Calendar.getInstance().apply {
                    try {
                        time = iso8601Format.parse(selectedTempEndDateTime)
                            ?: Calendar.getInstance().time
                    } catch (e: Exception) {
                        Calendar.getInstance().time // Fallback to current time on parsing failure
                    }
                }
            } else {
                Calendar.getInstance() // Default to current time if no date is set
            }
        )
    }

    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography

    Column {
        Text(
            text = "Date Range",
            style = typography.labelMedium,
            modifier = Modifier.padding(start = 10.dp, top = 8.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Start Date TextField
            Box(
                modifier = Modifier
                    .weight(0.25f)
                    .padding(8.dp)
            ) {
                TextField(
                    modifier = Modifier
                        .clickable { setIsShowingStartDatePicker(true) }
                        .fillMaxWidth(),
                    enabled = false,
                    value = if (selectedTempStartDateTime.isEmpty()) "" else selectedTempStartDateTime.split(
                        'T'
                    )[0],
                    onValueChange = {},
                    label = { Text("Start Date") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = palette.surfaceVariant,
                        unfocusedContainerColor = palette.surfaceVariant,
                        disabledContainerColor = palette.surfaceVariant,
                        cursorColor = palette.secondary,
                        focusedIndicatorColor = palette.secondary,
                        unfocusedIndicatorColor = palette.onSurfaceVariant,
                        errorIndicatorColor = palette.error,
                        focusedLabelColor = palette.secondary,
                        unfocusedLabelColor = palette.onSurfaceVariant,
                        errorLabelColor = palette.error,
                        selectionColors = TextSelectionColors(palette.primary, palette.surface)
                    ),
                    isError = selectedDateRangeError.isNotBlank()
                )
                if (selectedTempStartDateTime.isNotEmpty()) {
                    IconButton(
                        onClick = { setTempDueDateStartDateTime("") },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(30.dp)
                            .padding(end = 9.dp, bottom = 9.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_clear_24),
                            contentDescription = "Clear",
                            tint = palette.onSurfaceVariant
                        )
                    }
                }
            }

            Text(" - ")

            // End Date TextField
            Box(
                modifier = Modifier
                    .weight(0.25f)
                    .padding(8.dp)
            ) {
                TextField(
                    modifier = Modifier
                        .clickable { setIsShowingEndDatePicker(true) }
                        .fillMaxWidth(),
                    enabled = false,
                    value = if (selectedTempEndDateTime.isEmpty()) "" else selectedTempEndDateTime.split(
                        'T'
                    )[0],
                    onValueChange = {},
                    label = { Text("End Date") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = palette.surfaceVariant,
                        unfocusedContainerColor = palette.surfaceVariant,
                        disabledContainerColor = palette.surfaceVariant,
                        cursorColor = palette.secondary,
                        focusedIndicatorColor = palette.secondary,
                        unfocusedIndicatorColor = palette.onSurfaceVariant,
                        errorIndicatorColor = palette.error,
                        focusedLabelColor = palette.secondary,
                        unfocusedLabelColor = palette.onSurfaceVariant,
                        errorLabelColor = palette.error,
                        selectionColors = TextSelectionColors(palette.primary, palette.surface)
                    ),
                    isError = selectedDateRangeError.isNotBlank()
                )
                if (selectedTempEndDateTime.isNotEmpty()) {
                    IconButton(
                        onClick = { setTempDueDateEndDateTime("") },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(30.dp)
                            .padding(end = 9.dp, bottom = 9.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_clear_24),
                            contentDescription = "Clear",
                            tint = palette.onSurfaceVariant
                        )
                    }
                }
            }

            // Start Date picker dialog
            if (showStartDatePicker) {
                DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        tempStartCalendar.set(year, month, dayOfMonth)
                        setTempDueDateStartDateTime(iso8601Format.format(tempStartCalendar.time))
                        setIsShowingStartDatePicker(false)
                        checkTempSelectedDateRangeError()  // Check error after setting start date
                    },
                    tempStartCalendar.get(Calendar.YEAR),
                    tempStartCalendar.get(Calendar.MONTH),
                    tempStartCalendar.get(Calendar.DAY_OF_MONTH)
                ).apply {
                    setOnCancelListener {
                        setIsShowingStartDatePicker(false)
                        tempStartCalendar = if (selectedTempStartDateTime.isNotEmpty()) {
                            Calendar.getInstance().apply {
                                time = iso8601Format.parse(selectedTempStartDateTime)
                                    ?: Calendar.getInstance().time
                            }
                        } else {
                            Calendar.getInstance()
                        }
                    }
                    show()
                }
            }

            // End Date picker dialog
            if (showEndDatePicker) {
                DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        tempEndCalendar.set(year, month, dayOfMonth)
                        setTempDueDateEndDateTime(iso8601Format.format(tempEndCalendar.time))
                        setIsShowingEndDatePicker(false)
                        checkTempSelectedDateRangeError()  // Check error after setting end date
                    },
                    tempEndCalendar.get(Calendar.YEAR),
                    tempEndCalendar.get(Calendar.MONTH),
                    tempEndCalendar.get(Calendar.DAY_OF_MONTH)
                ).apply {
                    setOnCancelListener {
                        setIsShowingEndDatePicker(false)
                        tempEndCalendar = if (selectedTempEndDateTime.isNotEmpty()) {
                            Calendar.getInstance().apply {
                                time = iso8601Format.parse(selectedTempEndDateTime)
                                    ?: Calendar.getInstance().time
                            }
                        } else {
                            Calendar.getInstance()
                        }
                    }
                    if (selectedTempStartDateTime.isNotEmpty()) {
                        datePicker.minDate =
                            tempStartCalendar.timeInMillis // Ensure end date is after start date
                    }
                    show()
                }
            }
        }
        if (selectedDateRangeError.isNotBlank()) {
            Text(
                text = selectedDateRangeError,
                color = palette.error,
                style = typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                maxLines = 3
            )
        }
    }
}


@Composable
fun DateTimePicker(
    selectedDateTime: String, selectedDateTimeError: String, setDueDateDateTime: (String) -> Unit,
    showDatePicker: Boolean, setShowingDatePicker: (Boolean) -> Unit,
    showTimePicker: Boolean, setShowingTimePicker: (Boolean) -> Unit,

    ) {
    val context = LocalContext.current

    // Format for the ISO 8601 string
    val iso8601Format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())

    var tempCalendar by remember(selectedDateTime) {
        mutableStateOf(
            if (selectedDateTime.isNotEmpty()) {
                Calendar.getInstance().apply {
                    try {
                        time = iso8601Format.parse(selectedDateTime) ?: Calendar.getInstance().time
                    } catch (e: Exception) {
                        Calendar.getInstance().time // Fallback to current time on parsing failure
                    }
                }
            } else {
                Calendar.getInstance() // Default to current time if no date is set
            }
        )
    }

    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography

    // ----- Date and time picker -----
    Box() {
        TextField(
            modifier = Modifier
                .clickable { setShowingDatePicker(true) }
                .fillMaxWidth(),
            enabled = false,
            value = if (selectedDateTime == "") {
                ""
            } else {
                "${selectedDateTime.split('T')[0]}, ${
                    selectedDateTime.split('T')[1].split('+')[0].split(
                        ':'
                    )[0]
                }:${selectedDateTime.split('T')[1].split('+')[0].split(':')[1]}"
            },
            onValueChange = {},
            label = { Text("Due Date and Hour") },
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = palette.surfaceVariant,
                unfocusedContainerColor = palette.surfaceVariant,
                disabledContainerColor = palette.surfaceVariant,
                cursorColor = palette.secondary,
                focusedIndicatorColor = palette.secondary,
                unfocusedIndicatorColor = palette.onSurfaceVariant,
                errorIndicatorColor = palette.error,
                focusedLabelColor = palette.secondary,
                unfocusedLabelColor = palette.onSurfaceVariant,
                errorLabelColor = palette.error,
                selectionColors = TextSelectionColors(palette.primary, palette.surface)
            ),
            isError = selectedDateTimeError.isNotBlank()
        )
        if (selectedDateTime.isNotEmpty()) {
            IconButton(
                onClick = { setDueDateDateTime("") },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(30.dp)
                    .padding(end = 9.dp, bottom = 9.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_clear_24),
                    contentDescription = "Clear",
                    tint = palette.onSurfaceVariant
                )
            }
        }
    }

    if (selectedDateTimeError.isNotBlank()) {
        Text(
            text = selectedDateTimeError,
            color = palette.error,
            style = typography.bodySmall,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            maxLines = 3
        )
    }

    // Date picker dialog

    // Date picker dialog
    if (showDatePicker) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                tempCalendar.set(year, month, dayOfMonth)
                setShowingDatePicker(false)
                setShowingTimePicker(true)  // Move to time selection
            },
            tempCalendar.get(Calendar.YEAR),
            tempCalendar.get(Calendar.MONTH),
            tempCalendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            setOnCancelListener {
                setShowingDatePicker(false)
                tempCalendar = if (selectedDateTime.isNotEmpty()) {
                    Calendar.getInstance().apply {
                        time = iso8601Format.parse(selectedDateTime) ?: Calendar.getInstance().time
                    }
                } else {
                    Calendar.getInstance()
                }
            }
            datePicker.minDate = System.currentTimeMillis()
            show()
        }
    }

    // Time picker dialog
    if (showTimePicker) {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                tempCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                tempCalendar.set(Calendar.MINUTE, minute)
                setDueDateDateTime(iso8601Format.format(tempCalendar.time))  // Format and set the selected date-time
                setShowingTimePicker(false)
            },
            tempCalendar.get(Calendar.HOUR_OF_DAY),
            tempCalendar.get(Calendar.MINUTE),
            true  // Use 24-hour format
        ).apply {
            setOnCancelListener {
                setShowingTimePicker(false)
                tempCalendar = if (selectedDateTime.isNotEmpty()) {
                    Calendar.getInstance().apply {
                        time = iso8601Format.parse(selectedDateTime) ?: Calendar.getInstance().time
                    }
                } else {
                    Calendar.getInstance()
                }
            }
            show()
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurrencyDropdownMenu(
    recurrencyOptions: List<String>,
    expandedRecurrenceDropdown: Boolean,
    setExpandedRecurrencDropdown: (Boolean) -> Unit,
    selectedTextForRecurrence: String,
    setTaskRecurrency: (String) -> Unit,
) {
    val palette = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
        //.padding(top = 16.dp, start = 16.dp, end = 16.dp),
        //verticalArrangement = Arrangement.SpaceBetween
    ) {
        ExposedDropdownMenuBox(
            expanded = expandedRecurrenceDropdown,
            onExpandedChange = {
                setExpandedRecurrencDropdown(!expandedRecurrenceDropdown)
            },
        ) {
            TextField(
                value = selectedTextForRecurrence,
                label = {
                    Text(
                        "Recurrence",
                        color = if (expandedRecurrenceDropdown) palette.secondary else palette.onSurface
                    )
                },
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRecurrenceDropdown) },
                modifier = Modifier
                    .menuAnchor()
                    .background(palette.background),
                placeholder = { Text("Recurrence") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = palette.surfaceVariant,
                    unfocusedContainerColor = palette.surfaceVariant,
                    disabledContainerColor = palette.surfaceVariant,
                    cursorColor = palette.secondary,
                    focusedIndicatorColor = palette.secondary,
                    unfocusedIndicatorColor = palette.onSurfaceVariant,
                    errorIndicatorColor = palette.error,
                    focusedLabelColor = palette.secondary,
                    unfocusedLabelColor = palette.onSurfaceVariant,
                    errorLabelColor = palette.error,
                    selectionColors = TextSelectionColors(palette.primary, palette.surface)
                )
            )
            ExposedDropdownMenu(
                expanded = expandedRecurrenceDropdown,
                onDismissRequest = { setExpandedRecurrencDropdown(false) },
                modifier = Modifier.background(palette.background)
            ) {
                recurrencyOptions.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(text = item) },
                        onClick = {
                            setTaskRecurrency(item)
                            setExpandedRecurrencDropdown(false)
                            //Toast.makeText(context, item, Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.background(
                            if (item == selectedTextForRecurrence) palette.surfaceVariant else palette.background
                        )
                    )
                }
            }
        }
    }
}


@Composable
fun StatusStep(
    taskNameValue: String,
    taskNameError: String,
    setTaskName: (String) -> Unit,
    selectedDateTime: String,
    selectedDateTimeError: String,
    setDueDateDateTime: (String) -> Unit,
    showDatePicker: Boolean,
    setShowingDatePicker: (Boolean) -> Unit,
    showTimePicker: Boolean,
    setShowingTimePicker: (Boolean) -> Unit,
    recurrencyOptions: List<String>,
    expandedRecurrenceDropdown: Boolean,
    setExpandedRecurrencDropdown: (Boolean) -> Unit,
    selectedTextForRecurrence: String,
    setTaskRecurrency: (String) -> Unit,
    notPriorityValue: Int,
    setTaskPriority: (Int) -> Unit,
    taskTagsList: List<String>,
    selectedTags: List<String>,
    addTag: (String) -> Unit,
    removeTag: (String) -> Unit
) {
    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp, horizontal = 16.dp)
    ) {
        ProgressBar(1, 3)
    }

    Text("Status", style = typography.titleMedium)

    LazyColumn {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                //horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ----- Task Name -----
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = taskNameValue,
                    onValueChange = setTaskName,
                    label = { Text("Task Name") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = palette.surfaceVariant,
                        unfocusedContainerColor = palette.surfaceVariant,
                        disabledContainerColor = palette.surfaceVariant,
                        cursorColor = palette.secondary,
                        focusedIndicatorColor = palette.secondary,
                        unfocusedIndicatorColor = palette.onSurfaceVariant,
                        errorIndicatorColor = palette.error,
                        focusedLabelColor = palette.secondary,
                        unfocusedLabelColor = palette.onSurfaceVariant,
                        errorLabelColor = palette.error,
                        selectionColors = TextSelectionColors(palette.primary, palette.surface)
                    ),
                    isError = taskNameError.isNotBlank()
                )
                if (taskNameError.isNotBlank()) {
                    Text(
                        text = taskNameError,
                        color = palette.error,
                        style = typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        maxLines = 3
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                DateTimePicker(
                    selectedDateTime, selectedDateTimeError, setDueDateDateTime,
                    showDatePicker, setShowingDatePicker,
                    showTimePicker, setShowingTimePicker
                )

                Spacer(modifier = Modifier.height(16.dp))

                RecurrencyDropdownMenu(
                    recurrencyOptions, expandedRecurrenceDropdown, setExpandedRecurrencDropdown,
                    selectedTextForRecurrence, setTaskRecurrency
                )

                Spacer(modifier = Modifier.height(16.dp))

                TagsDropdownMenu(
                    taskTagsList, selectedTags,
                    addTag, removeTag
                )

                //Spacer(modifier = Modifier.height(16.dp))

                CustomToggle(
                    "",
                    listOf("Priority", "Non priority"),
                    notPriorityValue,
                    setTaskPriority,
                    true
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun DescriptionStep(
    taskDescriptionValue: String, goToPreviousStep: () -> Unit,
    setTaskDescription: (String) -> Unit
) {
    //val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography

    BackHandler {
        goToPreviousStep()
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp, horizontal = 16.dp)
    ) {
        ProgressBar(2, 3)
    }

    Text("Description", style = typography.titleMedium)

    Description(
        taskDescriptionValue, setTaskDescription
    )

    /*LazyColumn(
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ----- Description -----
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = taskDescriptionValue,
                    onValueChange = { newValue ->
                        if (newValue.length <= 200) { // Prevent input past max length
                            setTaskDescription(newValue)
                        }
                    },
                    label = { Text("Description (${200 - taskDescriptionValue.length} characters left)") },
                    placeholder = {
                        Text(
                            "Insert at most ${200 - taskDescriptionValue.length} characters",
                            style = typography.bodySmall,
                            color = palette.onSurfaceVariant
                        )
                    },
                    singleLine = false,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = palette.surfaceVariant,
                        unfocusedContainerColor = palette.surfaceVariant,
                        disabledContainerColor = palette.surfaceVariant,
                        cursorColor = palette.secondary,
                        focusedIndicatorColor = palette.secondary,
                        unfocusedIndicatorColor = palette.onSurfaceVariant,
                        errorIndicatorColor = palette.error,
                        focusedLabelColor = palette.secondary,
                        unfocusedLabelColor = palette.onSurfaceVariant,
                        errorLabelColor = palette.error,
                        selectionColors = TextSelectionColors(palette.primary, palette.surface)
                    )
                )
                if (taskDescriptionError.isNotBlank()) {
                    Text(
                        text = taskDescriptionError,
                        color = palette.error,
                        style = typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        maxLines = 8
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }*/
}

/*
@Composable
fun PeopleStep(
    teamId: String,
    goToPreviousStep: () -> Unit,
    taskpeople: List<PersonData>,
    teampeople: List<PersonData>,
    selectedPeople: List<PersonData>,
    clearSelectedPeople: () -> Unit,
    addPerson: (PersonData) -> Unit,
    removePerson: (PersonData) -> Unit,
    addSelectedTeamPeopleToTask: () -> Unit,
    removePersonFromTask: (String, String) -> Unit,
    filteredPeople: List<PersonData>,
    //isInAddMode: Boolean, setAddMode: (Boolean) -> Unit,
    searchQueryForNewTask: String,
    onSearchQueryChangedForNewTask: (String) -> Unit,
    peopleOrTaskNameError: String
) {
    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography

    BackHandler {
        goToPreviousStep()
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp, horizontal = 16.dp)
    ) {
        ProgressBar(3, 3)
    }

    Text("People", style = typography.titleMedium)

    Text(
        text = "If you are interested in monitoring the work, add yourself " +
                "to the list of people for this task. \uD83D\uDC40\uD83D\uDC40",
        color = palette.onSurface,
        style = typography.bodySmall,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        maxLines = 3
    )

    PeopleSection(
        teamId,
        taskpeople, teampeople, selectedPeople, clearSelectedPeople,
        addPerson, removePerson,
        addSelectedTeamPeopleToTask, removePersonFromTask,
        filteredPeople,
        searchQueryForNewTask, onSearchQueryChangedForNewTask, {}, isInTeamPeople = false,
        peopleOrTaskNameError,
    )
}
 */

@Composable
fun PeopleStepCreation(
    teamId: String,
    goToPreviousStep: () -> Unit,
    taskpeople: List<PersonData>,
    teampeople: List<PersonData>,
    selectedPeople: List<PersonData>,
    clearSelectedPeople: () -> Unit,
    addPerson: (PersonData) -> Unit,
    removePerson: (PersonData) -> Unit,
    addSelectedTeamPeopleToTask: () -> Unit,
    removePersonFromTask: (String, String) -> Unit,
    filteredPeople: List<PersonData>,
    //isInAddMode: Boolean, setAddMode: (Boolean) -> Unit,
    searchQueryForNewTask: String,
    onSearchQueryChangedForNewTask: (String) -> Unit,
    peopleOrTaskNameError: String
) {
    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography

    BackHandler {
        goToPreviousStep()
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp, horizontal = 16.dp)
    ) {
        ProgressBar(3, 3)
    }

    Text("People", style = typography.titleMedium)

    Text(
        text = "If you are interested in monitoring the work, add yourself " +
                "to the list of people for this task. \uD83D\uDC40\uD83D\uDC40",
        color = palette.onSurface,
        style = typography.bodySmall,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        maxLines = 3
    )

    PeopleSectionCreation(
        teamId,
        taskpeople, teampeople, selectedPeople, clearSelectedPeople,
        addPerson, removePerson,
        addSelectedTeamPeopleToTask, removePersonFromTask,
        filteredPeople,
        searchQueryForNewTask, onSearchQueryChangedForNewTask, {}, isInTeamPeople = false,
        peopleOrTaskNameError
    )
}

// ----- Section for tasks -----
@Composable
private fun TaskList(
    teamId: String,
    teamDocument: Team,
    toDoTasks: List<ToDoTask>,
    showingCreateTask: Boolean,
    createTask: () -> Unit,
    //currentStep: TaskCreationStep,
    sortByCreationDate: Int,
    setSortModality: (Int) -> Unit,
    //tempIsSortByCreationDate: Int,
    setTempSortByCreation: (Int) -> Unit,
    isNotPriority: Int,
    setPrior: (Int) -> Unit,
    isNotRecurrent: Int,
    setRec: (Int) -> Unit,
    status: Int,
    setStat: (Int) -> Unit,
    //tempIsNotPriority: Int,
    setTempPrior: (Int) -> Unit,
    //tempIsNotRecurrent: Int,
    setTempRec: (Int) -> Unit,
    //tempStatus: Int,
    setTempStat: (Int) -> Unit,
    setTaskDescription: (String) -> Unit,
    filteredTasks: List<ToDoTask>,
    searchQueryForTask: String,
    onSearchQueryForTask: (String) -> Unit,
    listOfMembersForFilter: List<PersonData>,
    setMembersInFilterPage: () -> Unit,
    //selectedPeople: List<PersonData>, removePerson: (PersonData) -> Unit,
    clearSelectedPeople: () -> Unit,
    //tempListOfMembersForFilter: List<PersonData>, addTempMemberToFilter: (PersonData) -> Unit,
    removeTempMemberToFilter: (PersonData) -> Unit, //clearTempMembersInFilterPage: () -> Unit,
    selectedTags: List<String>,
    setSelectedTags: () -> Unit,
    removeTempSelectedTags: (String) -> Unit,
    clearSelectedDateRangeError: () -> Unit,
    //selectedTempStartDateTime: String,
    //selectedTempEndDateTime: String,
    setTempDueDateStartDateTime: (String) -> Unit,
    setTempDueDateEndDateTime: (String) -> Unit,
    selectedStartDateTime: String,
    selectedEndDateTime: String,
    setDueDateStartDateTime: (String) -> Unit,
    setDueDateEndDateTime: (String) -> Unit,
    showCalendarView: Boolean,
    setShowCalendView: (Boolean) -> Unit,
    showCalendarEventsDialog: Boolean,
    setShowCalendEventsDialog: (Boolean) -> Unit
) {
    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography
    val auth = FirebaseAuth.getInstance()

    val groupedtoDoTasks = if (sortByCreationDate == 1) {
        filteredTasks.groupBy { it.creationTimestamp.split("T")[0] }
    } else {
        filteredTasks.groupBy { it.expirationTimestamp.split("T")[0] }
    } //IT IS A MAP

    if (showCalendarView == false) {
        Box {
            // List of tasks
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 15.dp)
                ) {
                    if (toDoTasks.isNotEmpty()) {
                        // Search bar}
                        Column(
                            modifier = Modifier
                                .weight(0.6f)
                                .padding(end = 10.dp)
                        ) {
                            CustomSearchBar(
                                modifier = Modifier.padding(vertical = 10.dp),
                                placeholderText = "Search for a task",
                                searchQueryForTask,
                                onSearchQueryForTask
                            )
                        }
                    }

                    if (toDoTasks.isNotEmpty()) {
                        // Filtering button
                        Column(
                            modifier = Modifier.padding(horizontal = 5.dp)
                        ) {
                            // Filtering option
                            IconButton(
                                onClick = {
                                    clearSelectedDateRangeError()
                                    Actions.getInstance().goToFilterTeamTasks(teamId)
                                }
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.outline_tune_24),
                                    contentDescription = "Edit preferences",
                                    modifier = Modifier.size(30.dp),
                                    colorFilter = ColorFilter.tint(palette.secondary)
                                )
                            }
                        }
                        Column(
                            modifier = Modifier.padding(horizontal = 5.dp)
                        ) {
                            // Filtering option
                            IconButton(
                                onClick = {
                                    Actions.getInstance().goToTeamTasksCalendar(teamId)
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
                }
                if (sortByCreationDate != -1 || isNotPriority != -1 || isNotRecurrent != -1 || status != -1 ||
                    listOfMembersForFilter.isNotEmpty() || selectedStartDateTime != "" || selectedEndDateTime != "" ||
                    selectedTags.isNotEmpty()
                ) {
                    if (toDoTasks.isNotEmpty()) {
                        LazyVerticalGrid(
                            columns = GridCells.FixedSize(160.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .heightIn(max = 80.dp)
                                .padding(start = 10.dp, bottom = 15.dp)
                        ) {
                            if (sortByCreationDate != -1) {
                                item {
                                    FilterBadge(
                                        label = if (sortByCreationDate == 1) "Sorted by Creation" else if (sortByCreationDate == 0) "Sorted by Expiration" else "",
                                        onRemove = {
                                            setSortModality(-1)
                                            setTempSortByCreation(-1)
                                        }
                                    )
                                }
                            }
                            if (isNotPriority != -1) {
                                item {
                                    FilterBadge(
                                        label = if (isNotPriority == 1) "Non Priority" else if (isNotPriority == 0) "Priority" else "",
                                        onRemove = {
                                            setPrior(-1)
                                            setTempPrior(-1)
                                        }
                                    )
                                }
                            }
                            if (isNotRecurrent != -1) {
                                item {
                                    FilterBadge(
                                        label = if (isNotRecurrent == 1) "Non Recurrent" else if (isNotRecurrent == 0) "Recurrent" else "",
                                        onRemove = {
                                            setRec(-1)
                                            setTempRec(-1)
                                        }
                                    )
                                }
                            }
                            if (status != -1) {
                                item {
                                    FilterBadge(
                                        label = if (status == 1) "Completed" else if (status == 2) "Expired" else if (status == 0) "Scheduled" else "",
                                        onRemove = {
                                            setStat(-1)
                                            setTempStat(-1)
                                        }
                                    )
                                }
                            }
                            // Handling listOfMembersForFilter
                            items(listOfMembersForFilter) { person ->
                                FilterBadge(
                                    label = "${person.name} ${person.surname}",
                                    onRemove = {
                                        removeTempMemberToFilter(person)
                                        setMembersInFilterPage()
                                    }
                                )
                            }
                            if (selectedStartDateTime != "" || selectedEndDateTime != "") {
                                item {
                                    FilterBadge(
                                        label = if (selectedStartDateTime.isNotEmpty() && selectedEndDateTime.isNotEmpty()) "${
                                            selectedStartDateTime.split(
                                                "T"
                                            )[0]
                                        } - ${selectedEndDateTime.split("T")[0]}" else if (selectedStartDateTime.isNotEmpty()) "Start date: ${
                                            selectedStartDateTime.split(
                                                "T"
                                            )[0]
                                        }" else if (selectedEndDateTime.isNotEmpty()) "End date: ${
                                            selectedEndDateTime.split(
                                                "T"
                                            )[0]
                                        }" else "",
                                        onRemove = {
                                            if (selectedStartDateTime.isNotEmpty() && selectedEndDateTime.isNotEmpty()) {
                                                setDueDateStartDateTime("")
                                                setDueDateEndDateTime("")
                                                setTempDueDateStartDateTime("")
                                                setTempDueDateEndDateTime("")
                                            }
                                            if (selectedStartDateTime.isNotEmpty()) {
                                                setDueDateStartDateTime("")
                                                setTempDueDateStartDateTime("")
                                            }
                                            if (selectedEndDateTime.isNotEmpty()) {
                                                setTempDueDateEndDateTime("")
                                                setDueDateEndDateTime("")
                                            }
                                        }
                                    )
                                }
                            }
                            items(selectedTags.sorted()) { tag ->
                                FilterBadge(
                                    label = tag,
                                    onRemove = {
                                        removeTempSelectedTags(tag)
                                        setSelectedTags()
                                    }
                                )
                            }
                        }
                    }
                }

                // List of tasks
                LazyColumn(
                    modifier = Modifier.fillMaxHeight()
                ) {
                    item {
                        groupedtoDoTasks.forEach { (label, schedtasks) ->
                            Text(
                                text = label,
                                style = typography.labelMedium,
                                fontSize = 18.sp,
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .padding(top = 12.dp, bottom = 8.dp)
                            )
                            for (task in schedtasks) {
                                ToDoTaskEntry(
                                    scheduledtask = task,
                                    viewOnlyMode = false,
                                    teamId = teamId,
                                    taskId = task.taskId
                                )
                                Spacer(modifier = Modifier.height(5.dp))
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    item {
                        if (groupedtoDoTasks.isEmpty()) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = if (toDoTasks.isEmpty()) "Currently you don't have any task" else "No results found",
                                    style = typography.labelMedium,
                                    color = palette.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }


            if(teamDocument.ownerId == auth.uid || teamDocument.admins.contains(auth.uid)) {
                CustomFloatingButton(
                    modifier = Modifier.align(Alignment.BottomEnd),
                    "New Task",
                    {}, createTask, {}, clearSelectedPeople, {}, setTaskDescription, teamId
                )
            }
        }
    } else {
        /*CalendarWithEvents(
            toDoTasks
        )*/
    }
}


@Composable
fun ReadOnlyTextFieldWithCopyToClipboard(textState: String) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .background(palette.surfaceVariant, shape = RoundedCornerShape(5.dp))
    ) {
        BasicTextField(
            value = textState,
            onValueChange = { }, // No action on value change
            readOnly = true, // Make the text field read-only
            textStyle = typography.bodySmall,
            modifier = Modifier
                .weight(1f)
                //.background(Color.Transparent)
                .padding(8.dp)
        )

        IconButton(onClick = {
            clipboardManager.setText(AnnotatedString(textState))
            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        }) {
            Image(
                painter = painterResource(id = R.drawable.outline_content_copy_24),
                contentDescription = "Copy",
                modifier = Modifier.size(30.dp),
                colorFilter = ColorFilter.tint(palette.onSurfaceVariant)
            )
        }
    }
}

fun generateHash(input: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
    val fullHash = bytes.joinToString("") { "%02x".format(it) }
    //ONLY 8 CHARACTERS OF THE HASH, BUT IN THIS WAY WE INCREASE POSSIBILITY OF COLLISIONS
    // --> WE can generate approximately 77,145 hashes before the probability of
    //     a collision reaches 50%
    return fullHash.take(8)
}

/*FOR GENERATING THE QR CODE WE ADDED THE FOLLOWING DEPENDENCIES IN build.gradle:
implementation 'com.google.zxing:core:3.4.1'
implementation 'androidx.compose.ui:ui-graphics:1.0.0'
implementation 'androidx.compose.ui:ui-tooling:1.0.0'
*/
fun generateQRCode(text: String): Bitmap? {
    val qrCodeWriter = QRCodeWriter()
    return try {
        val bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 200, 200)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bmp.setPixel(
                    x,
                    y,
                    if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                )
            }
        }
        bmp
    } catch (e: WriterException) {
        e.printStackTrace()
        null
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AddMemberTeamPresentationScreen(
    showSnackbar: Boolean,
    setShowQrDialog: (Boolean) -> Unit,
    textState: String,
    teamName: String
) {
    val palette = MaterialTheme.colorScheme
    var currentRoute = Actions.getInstance().getCurrentRoute()
    var showBanner by remember { mutableStateOf(showSnackbar) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (showBanner) {
                Snackbar(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = palette.inversePrimary,
                    contentColor = palette.background
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    ) {
                        Text("Team created successfully", color = palette.onPrimary)
                        IconButton(
                            onClick = { showBanner = false }
                        ) {
                            Image(
                                painter = painterResource(R.drawable.outline_close_24),
                                contentDescription = "Close banner",
                                colorFilter = ColorFilter.tint(palette.surfaceVariant)
                            )
                        }
                    }
                }
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(palette.background)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            item {
                Column {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.group_2),
                            contentDescription = "Team Image",
                            modifier = Modifier
                                .border(1.dp, palette.secondary, RoundedCornerShape(5))
                                .width(120.dp)
                                .height(120.dp),
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Help",
                            fontSize = 24.sp,
                            color = palette.onSurface
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = teamName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = palette.onSurface
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "to expand!",
                            fontSize = 24.sp,
                            color = palette.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    ReadOnlyTextFieldWithCopyToClipboard(textState)

                    Spacer(modifier = Modifier.height(16.dp))

                    val context = LocalContext.current

                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        putExtra(Intent.EXTRA_TEXT, textState)
                        type = "text/plain"
                        addCategory(Intent.CATEGORY_DEFAULT) // Ensure it targets appropriate apps
                    }

                    val packageManager = context.packageManager
                    val activities = packageManager.queryIntentActivities(sendIntent, 0)

                    if (activities.isNotEmpty()) {
                        val shareIntent = Intent.createChooser(sendIntent, null)

                        Button(
                            onClick = { startActivity(context, shareIntent, null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = palette.primary,
                                contentColor = palette.secondary
                            )
                        ) {
                            Text("Share Link")
                        }
                    } else {
                        Toast.makeText(
                            context,
                            "No suitable apps found to share the link.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { setShowQrDialog(true) },  // Opens the QR Code dialog
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.secondary,
                            contentColor = palette.background
                        )
                    ) {
                        Text("Show QR Code")
                    }
                }
            }
        }
    }
}


@Composable
fun ShowQRCodeDialog(
    showQrCodeDialog: Boolean,
    //teamName: String,
    setShowQrDialog: (Boolean) -> Unit,
    bitmap: Bitmap?
) {
    val palette = MaterialTheme.colorScheme
    //val typography = TeamTaskTypography

    if (showQrCodeDialog) {
        Dialog(onDismissRequest = { setShowQrDialog(false) }) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                LazyColumn(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Close button
                    item {
                        Box(
                            contentAlignment = Alignment.TopEnd,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(onClick = { setShowQrDialog(false) }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.outline_close_24),
                                    contentDescription = "Close"
                                )
                            }
                        }
                    }

                    item {

                        //QR CODE
                        bitmap?.let {
                            val imageBitmap = bitmap.asImageBitmap()
                            Image(
                                bitmap = imageBitmap,
                                contentDescription = "QR Code",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp, bottom = 10.dp)
                                    .size(180.dp),
                                contentScale = ContentScale.Fit
                            )
                        }

                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    /*item {
                        Row (
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.teamtasklogo),
                                contentDescription = "Team Image",
                                modifier = Modifier
                                    .size(40.dp)
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(text = teamName)
                        }
                    }*/

                    item {
                        Spacer(modifier = Modifier.height(5.dp))
                    }

                    /*
                    item {
                        Button(
                            onClick = { /* Handle sharing logic here */ },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = palette.secondary, contentColor = palette.background)
                        ) {
                            Text("Share QR Code")
                        }
                    }
                    */
                }
            }
        }
    }
}

@Composable
fun AddMemberToTeamScreen(
    showSnackbar: Boolean,
    teamId: String,
    teamName: String,
    vm: SpecificTeamViewModel = viewModel(),
) {
    val urlPrefix = "https://teamtask.com/invite/"
    val hashedString = generateHash(teamId)
    val inviteLink = urlPrefix + hashedString
    val textState = remember { mutableStateOf(inviteLink) }

    val bitmap = generateQRCode(textState.value)

    AddMemberTeamPresentationScreen(
        showSnackbar,
        //showQrCodeDialog,
        vm::setShowQrDialog, textState.value, teamName
    )
    if (vm.showQrCodeDialog) {
        ShowQRCodeDialog(
            showQrCodeDialog = true,
            //"TeamName",
            vm::setShowQrDialog, bitmap
        )
    }
}

@Composable
fun InviteConfirmationScreen(
    hash: String,
    vm: SpecificTeamViewModel = viewModel()
) {
    val palette = MaterialTheme.colorScheme

    // State to hold the team information and status
    var team by remember { mutableStateOf<Team?>(null) }
    var status by remember { mutableStateOf("") }
    var teamId by remember { mutableStateOf("") }

    // Fetch the team information when the screen is rendered
    LaunchedEffect(hash) {
        val (teamInfo, statusMessage) = vm.retrieveTeamInfoByInviteHash(hash)
        team = teamInfo
        status = statusMessage.split("-")[0]
        if (statusMessage.contains("-")) {
            teamId = statusMessage.split("-")[1]
        }
        Log.e("InviteConfirmationScreen", status)
    }

    if (LocalConfiguration.current.orientation != Configuration.ORIENTATION_LANDSCAPE) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(palette.background)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            //Spacer(modifier = Modifier.height(16.dp))
            item {
                Column {
                    if (status == "User not logged in") {
                        Actions.getInstance().goToFirstScreen()
                    }
                    if (status == "Team not found") {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.group_2),
                                contentDescription = "Team Not Found",
                                modifier = Modifier
                                    .border(1.dp, palette.secondary, RoundedCornerShape(5))
                                    .width(120.dp)
                                    .height(120.dp),
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Ops, this team does ",
                                fontSize = 20.sp,
                                color = palette.onSurface
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "not exist...",
                                fontSize = 20.sp,
                                color = palette.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = { Actions.getInstance().goToHome() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = palette.primary,
                                contentColor = palette.secondary
                            )
                        ) {
                            Text("Go to home")
                        }
                    }
                    if (status == "User already a member of the team") {
                        Actions.getInstance().goToTeamTasks(teamId)
                    }
                    if (status == "User not a member of the team") {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.group_2),
                                contentDescription = "Team Image",
                                modifier = Modifier
                                    .border(1.dp, palette.secondary, RoundedCornerShape(5))
                                    .width(120.dp)
                                    .height(120.dp),
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "You have been invited to join ",
                                fontSize = 20.sp,
                                color = palette.onSurface
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            team?.let {
                                Text(
                                    text = it.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    color = palette.onSurface
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "",
                                fontSize = 24.sp,
                                color = palette.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { vm.joinTeam(teamId) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = palette.primary,
                                contentColor = palette.secondary
                            )
                        ) {
                            Text("Join team")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { Actions.getInstance().goToHome() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = palette.secondary,
                                contentColor = palette.background
                            )
                        ) {
                            Text("Reject invite")
                        }
                    }
                }
            }
        }
    } else {
        if (status == "User not logged in") {
            Actions.getInstance().goToFirstScreen()
        }
        if (status == "Team not found") {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Spacer(modifier = Modifier.height(40.dp))

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.group_2),
                            contentDescription = "Team Not Found",
                            modifier = Modifier
                                .border(1.dp, palette.secondary, RoundedCornerShape(5))
                                .width(120.dp)
                                .height(120.dp),
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Ops, this team does ",
                            fontSize = 20.sp,
                            color = palette.onSurface
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "not exist...",
                            fontSize = 20.sp,
                            color = palette.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 30.dp)
                ) {
                    Box {
                        Column {
                            Button(
                                onClick = { Actions.getInstance().goToHome() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = palette.primary,
                                    contentColor = palette.secondary
                                )
                            ) {
                                Text("Go to home")
                            }
                        }
                    }
                }
            }
        }
        if (status == "User already a member of the team") {
            Actions.getInstance().goToTeamTasks(teamId)
        }
        if (status == "User not a member of the team") {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Spacer(modifier = Modifier.height(40.dp))

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.group_2),
                            contentDescription = "Team Image",
                            modifier = Modifier
                                .border(1.dp, palette.secondary, RoundedCornerShape(5))
                                .width(120.dp)
                                .height(120.dp),
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "You have been invited to join ",
                            fontSize = 20.sp,
                            color = palette.onSurface
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        team?.let {
                            Text(
                                text = it.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = palette.onSurface
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "",
                            fontSize = 24.sp,
                            color = palette.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 30.dp)
                ) {
                    Box {
                        Column {
                            Button(
                                onClick = { vm.joinTeam(teamId) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = palette.primary,
                                    contentColor = palette.secondary
                                )
                            ) {
                                Text("Join team")
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { Actions.getInstance().goToHome() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = palette.secondary,
                                    contentColor = palette.background
                                )
                            ) {
                                Text("Reject invite")
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----- Bar to select section -----
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Tab3Screen(
    teamId: String,
    teamDocument: Team,
    showFilterMemberInFilters: Boolean,
    setShowingFilterMemberInFilters: (Boolean) -> Unit,
    toDoTasks: List<ToDoTask>,
    updateTaskStatuses: () -> Unit,
    listOfMembersForFilter: List<PersonData>,
    setMembersInFilterPage: () -> Unit,
    taskpeople: List<PersonData>,
    teampeople: List<PersonData>,
    selectedPeople: List<PersonData>,
    clearSelectedPeople: () -> Unit,
    addPerson: (PersonData) -> Unit,
    removePerson: (PersonData) -> Unit,
    tempListOfMembersForFilter: List<PersonData>,
    addTempMemberToFilter: (PersonData) -> Unit,
    removeTempMemberToFilter: (PersonData) -> Unit,
    //clearTempMembersInFilterPage: () -> Unit,
    addSelectedTeamPeopleToTask: () -> Unit,
    removePersonFromTask: (String, String) -> Unit,
    removePersonFromTeam: (String, String) -> Unit,
    filteredPeople: List<PersonData>,
    tabs: List<String>,
    pagerState: PagerState,
    showingCreateTask: Boolean,
    createTask: () -> Unit,
    currentStep: TaskCreationStep,
    sortByCreationDate: Int,
    setSortModality: (Int) -> Unit,
    tempIsSortByCreationDate: Int,
    setTempSortByCreation: (Int) -> Unit,
    isNotPriority: Int,
    setPrior: (Int) -> Unit,
    isNotRecurrent: Int,
    setRec: (Int) -> Unit,
    status: Int,
    setStat: (Int) -> Unit,
    tempIsNotPriority: Int,
    setTempPrior: (Int) -> Unit,
    tempIsNotRecurrent: Int,
    setTempRec: (Int) -> Unit,
    tempStatus: Int,
    setTempStat: (Int) -> Unit,
    //showingTeamLinkOrQrCode: Boolean,
    setShowTeamLinkOrQrCode: (Boolean) -> Unit,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    searchQueryForNewTask: String,
    onSearchQueryChangedForNewTask: (String) -> Unit,
    filteredTasks: List<ToDoTask>,
    searchQueryForTask: String,
    onSearchQueryForTask: (String) -> Unit,
    taskNameValue: String,
    taskNameError: String,
    setTaskName: (String) -> Unit,
    taskDescriptionValue: String,
    //taskDescriptionError: String,
    setTaskDescription: (String) -> Unit,
    selectedDateTime: String,
    selectedDateTimeError: String,
    setDueDateDateTime: (String) -> Unit,
    showDatePicker: Boolean,
    setShowingDatePicker: (Boolean) -> Unit,
    showTimePicker: Boolean,
    setShowingTimePicker: (Boolean) -> Unit,
    recurrencyOptions: List<String>,
    expandedRecurrenceDropdown: Boolean,
    setExpandedRecurrencDropdown: (Boolean) -> Unit,
    selectedTextForRecurrence: String,
    setTaskRecurrency: (String) -> Unit,
    notPriorityValue: Int,
    setTaskPriority: (Int) -> Unit,
    teamDescriptionValue: String,
    //teamDescriptionError: String,
    setTeamDescription: (String) -> Unit,
    taskTagsList: List<String>,
    selectedTags: List<String>,
    setSelectedTags: () -> Unit,
    //addTag: (String) -> Unit,
    //removeTag: (String) -> Unit,
    tempSelectedTags: List<String>,
    addTempSelectedTags: (String) -> Unit,
    removeTempSelectedTags: (String) -> Unit,
    selectedTagsForNewTask: List<String>,
    addTagForNewTask: (String) -> Unit,
    removeTagForNewTask: (String) -> Unit,
    selectedTempStartDateTime: String,
    selectedTempEndDateTime: String,
    setTempDueDateStartDateTime: (String) -> Unit,
    setTempDueDateEndDateTime: (String) -> Unit,
    selectedStartDateTime: String,
    selectedEndDateTime: String,
    setDueDateStartDateTime: (String) -> Unit,
    setDueDateEndDateTime: (String) -> Unit,
    showStartDatePicker: Boolean,
    setIsShowingStartDatePicker: (Boolean) -> Unit,
    showEndDatePicker: Boolean,
    setIsShowingEndDatePicker: (Boolean) -> Unit,
    selectedDateRangeError: String,
    clearSelectedDateRangeError: () -> Unit,
    checkTempSelectedDateRangeError: () -> Unit,
    showCalendarView: Boolean,
    setShowCalendView: (Boolean) -> Unit,
    showCalendarEventsDialog: Boolean,
    setShowCalendEventsDialog: (Boolean) -> Unit,
    teamDescription: String,
) {
    val palette = MaterialTheme.colorScheme

    val animationScope = rememberCoroutineScope()

    updateTaskStatuses()

    // --- Tab row ---
    TabRow(
        selectedTabIndex = pagerState.currentPage,
        containerColor = palette.background,
        contentColor = palette.secondary,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                color = palette.secondary
            )
        }
    ) {
        tabs.forEachIndexed { index, currentTab ->
            Tab(
                text = { Text(currentTab) },
                selected = pagerState.currentPage == index,
                onClick = {
                    animationScope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                }
            )
        }
    }

    // --- Swipeable main content ---
    HorizontalPager(
        state = pagerState,
    ) { page ->
        when (page) {
            0 -> TaskList(
                teamId,
                teamDocument,
                toDoTasks,
                showingCreateTask, createTask,
                sortByCreationDate, setSortModality,
                setTempSortByCreation,
                isNotPriority, setPrior,
                isNotRecurrent, setRec,
                status, setStat,
                setTempPrior,
                setTempRec,
                setTempStat,
                setTaskDescription,
                filteredTasks, searchQueryForTask, onSearchQueryForTask,
                listOfMembersForFilter, setMembersInFilterPage, clearSelectedPeople,
                removeTempMemberToFilter,
                selectedTags, setSelectedTags, removeTempSelectedTags,
                clearSelectedDateRangeError,
                setTempDueDateStartDateTime,
                setTempDueDateEndDateTime,
                selectedStartDateTime,
                selectedEndDateTime,
                setDueDateStartDateTime,
                setDueDateEndDateTime,
                showCalendarView, setShowCalendView,
                showCalendarEventsDialog, setShowCalendEventsDialog
            )

            1 -> DescriptionViewOnly(descriptionValue = teamDescription, teamId = teamId)

            2 -> PeopleSectionForTeam(
                teamId,
                teampeople, teampeople, selectedPeople, clearSelectedPeople,
                addPerson, removePerson,
                addSelectedTeamPeopleToTask, removePersonFromTeam,
                filteredPeople,
                searchQuery, onSearchQueryChanged, setShowTeamLinkOrQrCode, isInTeamPeople = true, peopleOrTaskNameError = "",
            )
        }
    }
}


@Composable
fun MembersSelectorForFilter(
    showFilterMemberInFilters: Boolean, setShowingFilterMemberInFilters: (Boolean) -> Unit,
    //people: List<PersonData>,
    //onRemovePerson: (PersonData) -> Unit,
    //listOfMembersForFilter: List<PersonData>,
    setMembersInFilterPage: () -> Unit,
    //teampeople: List<PersonData>,
    selectedPeople: List<PersonData>,
    //clearSelectedPeople: () -> Unit,
    addPerson: (PersonData) -> Unit, removePerson: (PersonData) -> Unit,
    //addSelectedTeamPeopleToTask: () -> Unit,
    //removePersonFromTask: (String) -> Unit,
    filteredPeople: List<PersonData>,
    searchQuery: String, onSearchQueryChanged: (String) -> Unit
) {
    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography


    if (selectedPeople.isNotEmpty()) {
        Column {
            Row {
                Text(
                    text = "Members",
                    style = typography.labelMedium,
                    modifier = Modifier.padding(start = 10.dp, top = 8.dp)
                )
            }

            // Wraps the badges in a container with borders
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(palette.background)
                    .border(
                        BorderStroke(1.dp, palette.onSurfaceVariant),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp)
                    .clickable { setShowingFilterMemberInFilters(true) }
            ) {
                // List of placeholders selected people
                LazyHorizontalGrid(
                    rows = GridCells.FixedSize(30.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .heightIn(max = 50.dp)
                        .widthIn(min = 800.dp)
                ) {
                    items(selectedPeople) { person ->
                        PersonBadge(
                            person = person,
                            onRemove = {
                                removePerson(person)
                                setMembersInFilterPage()
                            }
                        )
                    }
                }
            }
        }
    } else {
        Column {
            Row {
                Text(
                    text = "Members",
                    style = typography.labelMedium,
                    modifier = Modifier.padding(start = 10.dp, top = 8.dp)
                )
            }
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(palette.background)
                    .border(BorderStroke(1.dp, palette.onSurfaceVariant), RoundedCornerShape(50)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                //
                Box(
                    modifier = Modifier
                        .weight(0.3f)
                        .background(palette.primary)
                        .padding(10.dp)
                        .clickable { setShowingFilterMemberInFilters(true) },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.baseline_groups_24),
                            contentDescription = "Member",
                            modifier = Modifier.scale(1.2f),
                            colorFilter = ColorFilter.tint(palette.onSurface)
                        )
                        Text(
                            text = "Select members",
                            style = typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }

            }
        }
    }
    if (showFilterMemberInFilters) {
        PeopleSectionForFilters(
            //listOfMembersForFilter, setMembersInFilterPage,
            //teampeople,
            selectedPeople,
            //clearSelectedPeople,
            addPerson, removePerson,
            filteredPeople,
            searchQuery, onSearchQueryChanged,
            //showFilterMemberInFilters,
            setShowingFilterMemberInFilters
        )
    }
}

// ----- Section to filter tasks -----
@Composable
fun FilterTasksScreen(
    vm: SpecificTeamViewModel = viewModel(),
) {
    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography

    Box {
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
            }
            item {
                CustomToggle(
                    "Sort by",
                    listOf("Expiration Date", "Creation Date"),
                    vm.tempIsSortByCreationDate,
                    vm::setTempSortByCreation,
                    false
                )
            }
            item {
                Column {
                    Spacer(modifier = Modifier.height(20.dp))
                    Divider(color = palette.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
            item {
                CustomToggle(
                    "Priority",
                    listOf("Priority", "Non priority"),
                    vm.tempIsNotPriority,
                    vm::setTempPrior,
                    false
                )
            }
            item {
                CustomToggle(
                    "Recurrence",
                    listOf("Recurrent", "Non Recurrent"),
                    vm.tempIsNotRecurrent,
                    vm::setTempRec,
                    false
                )
            }
            item {
                CustomToggle(
                    "Task Status",
                    listOf("Scheduled", "Completed", "Expired"),
                    vm.tempStatus,
                    vm::setTempStat,
                    false
                )
            }
            item {
                if (vm.teampeople.isNotEmpty()) {
                    MembersSelectorForFilter(
                        vm.showFilterMemberInFilters, vm::setShowingFilterMemberInFilters,
                        //listOfMembersForFilter,
                        vm::setMembersInFilterPage,
                        //teampeople,
                        vm.selectedPeople,
                        //clearSelectedPeople,
                        vm::addPerson, vm::removePerson,
                        vm.filteredPeople,
                        vm.searchQuery.value, vm::onSearchQueryChanged
                    )
                }
            }
            item {
                DateRangePicker(
                    vm.selectedTempStartDateTime,
                    vm.selectedTempEndDateTime,
                    vm::setTempDueDateStartDateTime,
                    vm::setTempDueDateEndDateTime,
                    vm.showStartDatePicker,
                    vm::setIsShowingStartDatePicker,
                    vm.showEndDatePicker,
                    vm::setIsShowingEndDatePicker,
                    vm.selectedDateRangeError, vm::checkTempSelectedDateRangeError
                )
            }
            item {
                Column(
                    modifier = Modifier.padding(
                        start = 10.dp,
                        end = 10.dp,
                        top = 8.dp,
                        bottom = 8.dp
                    )
                ) {
                    Row {
                        Text(
                            text = "Tags",
                            style = typography.labelMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    TagsDropdownMenu(
                        tags = vm.taskTagsList, selectedTags = vm.tempSelectedTags,
                        addTag = vm::addTempSelectedTags, removeTag = vm::removeTempSelectedTags
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(25.dp))
            }

        }
    }
}


// CALENDAR VIEW
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarWithEvents(
    filteredTasks: List<Pair<String, Task>>,
    filteredTeams: List<Pair<String, Team>>,
    teams: List<Pair<String, Team>>,
) {
    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography

    // MutableState for current month
    var currentMonth by remember { mutableStateOf(Calendar.getInstance()) }

    // MutableState for current month
    var showCalendarEventsDialog by remember { mutableStateOf(false) }


    // MutableState for selected date events
    val selectedDateEvents = remember { mutableStateOf<List<ToDoTask>>(listOf()) }

    // MutableState for selected day
    var selectedDay by remember { mutableStateOf<Int?>(null) }

    val currentDateTime = LocalDateTime.now()
    val formatter = DateTimeFormatter.ISO_DATE_TIME

    val localTasks = filteredTasks
        /*
        .filter { pair ->
            val taskDeadline = LocalDateTime.parse(pair.second.deadline, formatter)
            //Log.d("TaskDeadline", taskDeadline.toString())
            taskDeadline.isAfter(currentDateTime)
        }
        */
        .sortedBy { it.second.deadline }
    //.take(10)

    //Log.d("localTasks", localTasks.toString())


    val groupedTasks = localTasks.groupBy { it.second.deadline.split("T")[0] }

    //Log.d("GroupedTasks", "GroupedTasks: $groupedTasks")

    val tasks: List<ToDoTask> = filteredTasks.map { pair ->
        ToDoTask(
            taskId = pair.first,
            taskName = pair.second.title,
            status = pair.second.status,
            isNotPriority = if (pair.second.prioritized) 0 else 1,
            recurrence = pair.second.recurrence,
            expirationTimestamp = pair.second.deadline,
            creationTimestamp = pair.second.creationDate,
            taskpeople = emptyList(), // Assuming you have a list of PersonData to convert to
            tags = pair.second.tags
        )
    }


    // UI Layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    16.dp
                } else {
                    0.dp
                }
            )
    ) {
        /*
        Button(
            onClick = { Actions.getInstance().goToHome() },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Exit from calendar")
        }
        */
        // Calendar controls
        CalendarControls(currentMonth) { updatedMonth ->
            currentMonth = updatedMonth
            selectedDay = null // Reset selected day when the month changes
        }

        // Days of the week header
        DaysOfWeekHeader()

        // Calendar grid with events
        CalendarGrid(tasks, currentMonth, selectedDateEvents, selectedDay) { day ->
            showCalendarEventsDialog = true
            selectedDay = day
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Divider(
                color = palette.surfaceVariant,
                thickness = 2.dp,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Display events for selected date at the bottom
        if (selectedDay != null) {
            if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) {
                var selectedDateTextual = "${
                    currentMonth.getDisplayName(
                        Calendar.MONTH,
                        Calendar.LONG,
                        Locale.getDefault()
                    )
                } ${selectedDay.toString()}, ${currentMonth.get(Calendar.YEAR)}"
                Column {
                    Text(
                        selectedDateTextual,
                        modifier = Modifier.padding(top = 10.dp, start = 10.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    // Define the input format
                    val inputFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                    // Define the output format
                    val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                    val date = inputFormat.parse(selectedDateTextual)
                    val finalDate = date?.let { outputFormat.format(it) }

                    if (finalDate != null) {
                        EventList(selectedDateEvents.value, groupedTasks, teams, finalDate)
                    }
                }
            } else {
                if (showCalendarEventsDialog) {
                    var selectedDateTextual = "${
                        currentMonth.getDisplayName(
                            Calendar.MONTH,
                            Calendar.LONG,
                            Locale.getDefault()
                        )
                    } ${selectedDay.toString()}, ${currentMonth.get(Calendar.YEAR)}"
                    Dialog(onDismissRequest = { showCalendarEventsDialog = false }) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                                .background(Color.White)
                        ) {
                            Text(selectedDateTextual,
                                modifier = Modifier.padding(top = 10.dp, start = 10.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            // Define the input format
                            val inputFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                            // Define the output format
                            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                            val date = inputFormat.parse(selectedDateTextual)
                            val finalDate = date?.let { outputFormat.format(it) }

                            if (finalDate != null) {
                                EventList(selectedDateEvents.value, groupedTasks, teams, finalDate)
                                Spacer(modifier = Modifier.height(30.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarControls(currentMonth: Calendar, onMonthChange: (Calendar) -> Unit) {
    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        IconButton(onClick = {
            val newMonth = currentMonth.clone() as Calendar
            newMonth.add(Calendar.MONTH, -1)
            onMonthChange(newMonth)
        }) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Previous Month",
                tint = palette.onSurfaceVariant
            )
        }
        // Display month and year
        Text(
            text = "${
                currentMonth.getDisplayName(
                    Calendar.MONTH,
                    Calendar.LONG,
                    Locale.getDefault()
                )
            } ${currentMonth.get(Calendar.YEAR)}",
            style = MaterialTheme.typography.bodyLarge
        )

        IconButton(onClick = {
            val newMonth = currentMonth.clone() as Calendar
            newMonth.add(Calendar.MONTH, 1)
            onMonthChange(newMonth)
        }) {
            Icon(
                Icons.Default.ArrowForward,
                contentDescription = "Next Month",
                tint = palette.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DaysOfWeekHeader() {
    val daysOfWeek = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        daysOfWeek.forEach { day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                color = Color.Gray,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun CalendarGrid(
    events: List<ToDoTask>,
    currentMonth: Calendar,
    selectedDateEvents: MutableState<List<ToDoTask>>,
    selectedDay: Int?,
    onDaySelected: (Int) -> Unit
) {
    val typography = TeamTaskTypography
    val palette = MaterialTheme.colorScheme

    val daysInMonth = currentMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfMonth = currentMonth.clone() as Calendar
    firstDayOfMonth.set(Calendar.DAY_OF_MONTH, 1)
    val startingDayOfWeek =
        (firstDayOfMonth.get(Calendar.DAY_OF_WEEK) + 5) % 7 // EACH ROW starts from Monday
    val days = (1..daysInMonth).toList()
    val paddingDaysBefore = List(startingDayOfWeek) { -1 }
    val paddingDaysAfter = List((7 - (startingDayOfWeek + daysInMonth) % 7) % 7) { -1 }
    val allDays = paddingDaysBefore + days + paddingDaysAfter

    // Get today's date for comparison
    val today = Calendar.getInstance()

    LazyVerticalGrid(columns = GridCells.Fixed(7)) {
        items(allDays.size) { index ->
            val day = allDays[index]
            val isCurrentDay =
                day > 0 && currentMonth.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                        currentMonth.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                        day == today.get(Calendar.DAY_OF_MONTH)
            val isSelectedDay = day > 0 && day == selectedDay

            val textColor = when {
                day <= 0 -> Color.Transparent
                isCurrentDay -> palette.inverseSurface
                else -> palette.onSurface
            }
            val currentDay = if (day <= 0) "" else day.toString()
            val eventsForDay = if (day > 0) {
                events.filter { event ->
                    val eventDate = Calendar.getInstance()
                    eventDate.time =
                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault()).parse(
                            event.expirationTimestamp
                        )!!
                    eventDate.get(Calendar.DAY_OF_MONTH) == day && eventDate.get(Calendar.MONTH) == currentMonth.get(
                        Calendar.MONTH
                    ) && eventDate.get(Calendar.YEAR) == currentMonth.get(Calendar.YEAR)
                }
            } else {
                emptyList()
            }

            val eventDots = if (eventsForDay.isNotEmpty()) "•" else ""

            Column(
                modifier = Modifier
                    .padding(2.dp)
                    .background(
                        if (isSelectedDay) palette.primaryContainer else Color.Transparent,
                        CircleShape
                    )
                    .clickable {
                        if (day > 0) {
                            onDaySelected(day)
                            selectedDateEvents.value = eventsForDay
                        }
                    }
                    .padding(0.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = currentDay,
                    color = textColor,
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp
                )
                Text(
                    text = eventDots,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun EventList(
    events: List<ToDoTask>,
    groupedTasks: Map<String, List<Pair<String, Task>>>,
    teams: List<Pair<String, Team>>,
    chosenDate: String,
    homeViewModel: HomeViewModel = viewModel()
) {
    val typography = TeamTaskTypography
    val palette = MaterialTheme.colorScheme

    if (events.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No tasks are expiring on this day",
                style = typography.bodySmall,
                modifier = Modifier.align(Alignment.Center),
                color = palette.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            groupedTasks.forEach { (date, tasks) ->
                item {
                    Column {
                        /*
                        Text(
                            text = date,
                            style = typography.bodySmall,
                            color = palette.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 15.dp, vertical = 5.dp)
                        )
                        */

                        tasks.filter { taskPair ->
                            // Filter tasks based on the specific date
                            val expirationDate = taskPair.second.deadline.split("T").first() // Assuming ISO date format
                            expirationDate == date
                            taskPair.second.deadline.split("T")[0] == chosenDate
                        }.forEach { pair ->
                            val team = teams.firstOrNull { it.first == pair.second.teamId }

                            when (Actions.getInstance().getCurrentRoute()) {
                                "homeCalendar" -> {
                                    Box(
                                        modifier = Modifier.clickable(
                                                onClick = { Actions.getInstance().goToTaskComments(pair.second.teamId, pair.first) },
                                            )
                                    ) {
                                        val imageUri =
                                            homeViewModel.teamImages.collectAsState().value[team?.first]

                                        //Log.e("imageuriCalendar", imageUri.toString()) --> TODO: IMAGEURI IS EMPTY HERE
                                        homeViewModel.fetchTeamImage(team?.second?.image ?: "", pair.first)

                                        TaskEntry(pair.second, team?.second, imageUri)
                                    }
                                }

                                "teams/{teamId}/tasksCalendar" -> {
                                    val teamId = Actions.getInstance().getStringParameter("teamId")

                                    if(pair.second.teamId == teamId) {
                                        val tempToDoTask = ToDoTask(
                                            taskId = pair.first,
                                            taskName = pair.second.title,
                                            status = pair.second.status,
                                            isNotPriority = if (pair.second.prioritized) 0 else 1,
                                            recurrence = pair.second.recurrence,
                                            expirationTimestamp = pair.second.deadline,
                                            creationTimestamp = pair.second.creationDate,
                                            taskpeople = emptyList(), // Replace with actual people data if available
                                            tags = pair.second.tags
                                        )
                                        ToDoTaskEntry(
                                            scheduledtask = tempToDoTask,
                                            viewOnlyMode = false,
                                            teamId = pair.second.teamId,
                                            taskId = pair.first
                                        )

                                        Spacer(Modifier.height(5.dp))
                                    }
                                }

                                else -> {
                                    // Handle other routes if necessary
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}



@Composable
fun ToDoTaskEntry(
    scheduledtask: ToDoTask,
    viewOnlyMode: Boolean,
    teamId: String,
    taskId: String
) {
    val typography = TeamTaskTypography
    val palette = MaterialTheme.colorScheme

    Box(
        modifier = if (!viewOnlyMode) {
            Modifier.clickable(
                onClick = { Actions.getInstance().goToTaskComments(teamId, taskId) },
            )
        } else {
            Modifier
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
                .background(palette.surfaceVariant, RoundedCornerShape(5.dp))
                .border(1.dp, palette.secondary, RoundedCornerShape(5.dp))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                // Task name
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        scheduledtask.taskName,
                        style = typography.bodyMedium,
                        color = palette.onSurface,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Priority
                        if (scheduledtask.isNotPriority == 0) {
                            // Triangle
                            Image(
                                painter = painterResource(id = R.drawable.outline_warning_amber_24),
                                contentDescription = "Warning",
                                modifier = Modifier.size(20.dp),
                                colorFilter = ColorFilter.tint(palette.error)
                            )

                            // Label
                            Text(
                                text = "Prior",
                                fontWeight = FontWeight.Bold,
                                color = palette.error,
                                modifier = Modifier.padding(start = 8.dp),
                                style = typography.bodyMedium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Task state
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        // Icon
                        when (scheduledtask.status) {
                            "Completed" -> {
                                Image(
                                    painter = painterResource(id = R.drawable.outline_done_24),
                                    contentDescription = "Status",
                                    modifier = Modifier
                                        .size(25.dp)
                                        .background(palette.inversePrimary, shape = CircleShape)
                                        .scale(0.8f),
                                    colorFilter = ColorFilter.tint(palette.background)
                                )
                            }

                            "Expired" -> {
                                Image(
                                    painter = painterResource(id = R.drawable.outline_calendar_month_24),
                                    contentDescription = "Status",
                                    modifier = Modifier
                                        .size(25.dp)
                                        .background(palette.error, shape = CircleShape)
                                        .scale(0.8f),
                                    colorFilter = ColorFilter.tint(palette.background)
                                )
                            }

                            else -> {
                                Image(
                                    painter = painterResource(id = R.drawable.outline_access_time_24),
                                    contentDescription = "Status",
                                    modifier = Modifier
                                        .size(25.dp)
                                        .background(palette.inverseSurface, shape = CircleShape)
                                        .scale(0.8f),
                                    colorFilter = ColorFilter.tint(palette.background)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(5.dp))

                        // Label
                        Text(text = scheduledtask.status, style = typography.bodySmall)

                        when (scheduledtask.recurrence) {
                            "Weekly" -> {
                                Text(
                                    text = " (Weekly) ",
                                    fontWeight = FontWeight.Bold,
                                    //color = palette.error,
                                    modifier = Modifier.padding(end = 8.dp),
                                    style = typography.bodySmall
                                )
                            }

                            "Monthly" -> {
                                Text(
                                    text = " (Monthly) ",
                                    fontWeight = FontWeight.Bold,
                                    //color = palette.error,
                                    modifier = Modifier.padding(end = 8.dp),
                                    style = typography.bodySmall
                                )
                            }

                            "Yearly" -> {
                                Text(
                                    text = " (Yearly) ",
                                    fontWeight = FontWeight.Bold,
                                    //color = palette.error,
                                    modifier = Modifier.padding(end = 8.dp),
                                    style = typography.bodySmall
                                )
                            }
                        }
                    }

                    // Expiration date
                    Text(
                        text = when (scheduledtask.status) {
                            "Completed" -> {
                                ""
                            }

                            "Expired" -> {
                                "at " + scheduledtask.expirationTimestamp.split('T')[1].split('+')[0].slice(
                                    IntRange(0, 4)
                                )
                            }

                            "Scheduled" -> {
                                "Expires at ${
                                    scheduledtask.expirationTimestamp.split('T')[1].split('+')[0].slice(
                                        IntRange(0, 4)
                                    )
                                }"
                            }

                            else -> {
                                ""
                            }
                        },
                        style = typography.bodySmall,
                        fontSize = if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) {
                            13.sp
                        } else {
                            10.sp
                        },
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SpecificTeamScreen(
    teamId: String,
    teamDocument: Team,
    teamDescription: String,
    rawToDoTasks: List<Pair<String, Task>>,
    rawTeamParticipants: List<Pair<String, PersonData>>,
    rawPeople: List<Pair<String, Person>>,
    vm: SpecificTeamViewModel
) {
    // Log rawToDoTasks for debugging
    Log.d("SpecificTeamScreen", "rawToDoTasks: $rawToDoTasks")

    val typography = TeamTaskTypography
    val palette = MaterialTheme.colorScheme
    val auth = FirebaseAuth.getInstance()


    // Convert raw data to required types
    val toDoTasks = rawToDoTasks.map { (id, task) ->
        ToDoTask(
            taskId = id,
            taskName = task.title,
            status = task.status,
            isNotPriority = if (task.prioritized) 0 else 1,
            recurrence = task.recurrence,
            expirationTimestamp = task.deadline,
            creationTimestamp = task.creationDate,
            taskpeople = task.people.map { personId ->
                val person = rawPeople.find { it.first == personId }?.second ?: Person()
                PersonData(
                    personId = personId,
                    name = person.name,
                    surname = person.surname,
                    username = person.username,
                    role = "", // Assuming role is not available in your current data structure
                    permission = "", // Assuming permission is not available in your current data structure
                    image = ""
                )
            },
            tags = task.tags
        )
    }

    val teampeople = rawTeamParticipants.map { (id, person) ->
        PersonData(
            personId = id,
            name = person.name,
            surname = person.surname,
            username = person.username,
            role = person.role,
            permission = person.permission,
            image = person.image
        )
    }

    val filteredPeople = rawPeople.map { (id, person) ->
        PersonData(
            personId = id,
            name = person.name,
            surname = person.surname,
            username = person.username,
            role = "", // Assuming role is not available in your current data structure
            permission = "", // Assuming permission is not available in your current data structure
            image = ""
        )
    }

    // Log the converted data for debugging
    Log.d("SpecificTeamScreen", "toDoTasks: $toDoTasks")
    Log.d("SpecificTeamScreen", "teampeople: $teampeople")
    Log.d("SpecificTeamScreen", "filteredPeople: $filteredPeople")

    // Initialize ViewModel state with the converted data using LaunchedEffect
    LaunchedEffect(key1 = teamId, key2 = teampeople) {
        vm.init(toDoTasks, teampeople, filteredPeople)
    }

    val tabs = listOf("Tasks", "Description", "People")
    val pagerState = rememberPagerState { tabs.size }

    if (vm.showExitFromTeamModal) {
        AlertDialog(
            onDismissRequest = {
                vm.setShwExitFromTeamModal(false)
            },
            title = { Text(text = "Exit From Team") },
            text = { Text(text = "Are you sure that you want to exit from this team?") },
            confirmButton = {
                Button(onClick = {
                    vm.exitFromTeam(teamId)
                    vm.setShwExitFromTeamModal(false)
                }) {
                    Text(
                        text = "Yes",
                        style = typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = CaribbeanCurrent
                    )
                }
            },
            dismissButton = {
                Button(onClick = {
                    vm.setShwExitFromTeamModal(false)
                }) {
                    Text(
                        text = "Cancel",
                        style = typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = CaribbeanCurrent
                    )
                }
            }
        )
    }
    if (vm.showDeleteTeamModal) {
        AlertDialog(
            onDismissRequest = {
                vm.setStrinValueForDelete("")
                vm.setShwDeleteTeamModal(false)
            },
            title = { Text(text = "Delete Team", color = palette.error) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Write \"" + (auth.currentUser?.email
                            ?: "your email") + "\" and press \"Delete\" to permanently delete this team:"
                    )
                    Spacer(modifier = Modifier.height(15.dp))
                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = vm.stringValueForDelete,
                        onValueChange = vm::setStrinValueForDelete,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Done
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = palette.surfaceVariant,
                            unfocusedContainerColor = palette.surfaceVariant,
                            disabledContainerColor = palette.surfaceVariant,
                            cursorColor = palette.secondary,
                            focusedIndicatorColor = palette.secondary,
                            unfocusedIndicatorColor = palette.onSurfaceVariant,
                            errorIndicatorColor = palette.error,
                            focusedLabelColor = palette.secondary,
                            unfocusedLabelColor = palette.onSurfaceVariant,
                            errorLabelColor = palette.error,
                            selectionColors = TextSelectionColors(palette.primary, palette.surface)
                        ),
                        isError = vm.stringErrorForDelete.isNotBlank()
                    )
                    if (vm.stringErrorForDelete.isNotBlank()) {
                        Text(
                            text = vm.stringErrorForDelete,
                            color = palette.error,
                            style = typography.bodySmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            maxLines = 3
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    vm.validateStringForDeleteTeam(teamId)
                }) {
                    Text(
                        text = "Delete",
                        style = typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = CaribbeanCurrent
                    )
                }
            },
            dismissButton = {
                Button(onClick = {
                    vm.setStrinValueForDelete("")
                    vm.setShwDeleteTeamModal(false)
                }) {
                    Text(
                        text = "Cancel",
                        style = typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = CaribbeanCurrent
                    )
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Tab3Screen(
            teamId,
            teamDocument,
            vm.showFilterMemberInFilters,
            vm::setShowingFilterMemberInFilters,
            vm.toDoTasks,
            vm::updateTaskStatuses,
            vm.listOfMembersForFilter,
            vm::setMembersInFilterPage,
            vm.taskpeople,
            vm.teampeople,
            vm.selectedPeople,
            vm::clearSelectedPeople,
            vm::addPerson,
            vm::removePerson,
            vm.tempListOfMembersForFilter,
            vm::addTempMemberToFilter,
            vm::removeTempMemberToFilter,
            vm::addSelectedTeamPeopleToTask,
            vm::removePersonFromTask,
            vm::removePersonFromTeam,
            vm.filteredPeople,
            tabs,
            pagerState,
            vm.showingCreateTask,
            vm::createTask,
            vm.currentStep,
            vm.sortByCreationDate,
            vm::setSortModality,
            vm.tempIsSortByCreationDate,
            vm::setTempSortByCreation,
            vm.isNotPriority,
            vm::setPrior,
            vm.isNotRecurrent,
            vm::setRec,
            vm.status,
            vm::setStat,
            vm.tempIsNotPriority,
            vm::setTempPrior,
            vm.tempIsNotRecurrent,
            vm::setTempRec,
            vm.tempStatus,
            vm::setTempStat,
            vm::setShowTeamLinkOrQrCode,
            vm.searchQuery.value,
            vm::onSearchQueryChanged,
            vm.searchQueryForNewTask.value,
            vm::onSearchQueryForNewTaskChanged,
            vm.filteredTasks,
            vm.searchQueryForTasks.value,
            vm::onSearchQueryForTasksChanged,
            vm.taskNameValue,
            vm.taskNameError,
            vm::setTaskName,
            vm.taskDescriptionValue,
            vm::setTaskDescription,
            vm.selectedDateTime,
            vm.selectedDateTimeError,
            vm::setDueDateDateTime,
            vm.showDatePicker,
            vm::setShowingDatePicker,
            vm.showTimePicker,
            vm::setShowingTimePicker,
            vm.recurrencyOptions,
            vm.expandedRecurrenceDropdown,
            vm::setExpandedRecurrencDropdown,
            vm.selectedTextForRecurrence,
            vm::setTaskRecurrency,
            vm.notPriorityValue,
            vm::setTaskPriority,
            vm.teamDescriptionValue,
            vm::setTeamDescription,
            vm.taskTagsList,
            vm.selectedTags,
            vm::setSelectedTags,
            vm.tempSelectedTags,
            vm::addTempSelectedTags,
            vm::removeTempSelectedTags,
            vm.selectedTagsForNewTask,
            vm::addTagForNewTask,
            vm::removeTagForNewTask,
            vm.selectedTempStartDateTime,
            vm.selectedTempEndDateTime,
            vm::setTempDueDateStartDateTime,
            vm::setTempDueDateEndDateTime,
            vm.selectedStartDateTime,
            vm.selectedEndDateTime,
            vm::setDueDateStartDateTime,
            vm::setDueDateEndDateTime,
            vm.showStartDatePicker,
            vm::setIsShowingStartDatePicker,
            vm.showEndDatePicker,
            vm::setIsShowingEndDatePicker,
            vm.selectedDateRangeError,
            vm::clearSelectedDateRangeError,
            vm::checkTempSelectedDateRangeError,
            vm.showCalendarView,
            vm::setShowCalendView,
            vm.showCalendarEventsDialog,
            vm::setShowCalendEventsDialog,
            teamDescription
        )
    }
}

@Composable
fun CustomToggle(
    label: String,
    opt: List<String>,
    customTempState: Int,
    customSetTempState: (Int) -> Unit,
    isInCreateTaskOrTaskInfoEdit: Boolean
) {
    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography

    Column {
        Row {
            Text(
                text = label,
                style = typography.labelMedium,
                modifier = Modifier.padding(start = 10.dp, top = 8.dp)
            )
        }

        // Toggle options row
        Row(
            modifier = Modifier
                .height(65.dp)
                .padding(8.dp)
                .clip(RoundedCornerShape(50))
                .background(palette.background)
                .border(BorderStroke(1.dp, palette.onSurfaceVariant), RoundedCornerShape(50)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            for (index in opt.indices) {
                Box(
                    modifier = Modifier
                        .weight(0.3f)
                        .background(if (customTempState == index) palette.primary else palette.background)
                        .clickable {
                            if (isInCreateTaskOrTaskInfoEdit) {
                                customSetTempState(index)
                            } else {
                                // Deselect if the option is already selected
                                if (customTempState == index) {
                                    customSetTempState(-1)
                                } else {
                                    customSetTempState(index)
                                }
                            }
                        }
                        .padding(15.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Tick
                        if (customTempState == index) {
                            Image(
                                painter = painterResource(id = R.drawable.outline_done_24),
                                contentDescription = "Selected",
                                modifier = Modifier.scale(0.8f),
                                colorFilter = ColorFilter.tint(palette.onSurface)
                            )
                        }

                        // Text
                        Text(
                            text = opt[index],
                            fontFamily = Mulish,
                            fontWeight = FontWeight.Normal,
                            fontSize = 11.6.sp,
                            lineHeight = 16.sp,
                            letterSpacing = 0.sp,
                            color = Jet
                        )
                    }
                }
                if (index != opt.size - 1) {
                    Spacer(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(1.dp)
                            .background(palette.onSurfaceVariant)
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PeopleEntry(
    teamId: String,
    person: PersonData,
    selectedPeople: List<PersonData>,
    addPerson: (PersonData) -> Unit,
    removePerson: (PersonData) -> Unit,
    removePersonFromTask: (String, String) -> Unit,
    taskpeople: List<PersonData>,
    showingCreateTask: Boolean,
    isInTeamPeople: Boolean,
    vm: SpecificTeamViewModel = viewModel()
) {
    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography
    val currentRoute = Actions.getInstance().getCurrentRoute()
    val auth = FirebaseAuth.getInstance()


    var isSelected = selectedPeople.any { it.username == person.username }
    val isAlreadyInTask =
        remember(taskpeople) { taskpeople.any { it.username == person.username } && !showingCreateTask }
    var showMenu by remember { mutableStateOf(false) }
    var showMenuAssignRole by remember { mutableStateOf(false) }
    var showOwnerMenu by remember { mutableStateOf(false) }

    val backgroundColor = if (isSelected &&
        (currentRoute == "teams/{teamId}/edit/people" || currentRoute == "teams/{teamId}/filterTasks" || currentRoute == "teams/{teamId}/newTask/status")
    ) palette.primaryContainer else palette.surfaceVariant
    val textColor = palette.onSurface

    val userImages = listOf(
        R.drawable.person_1,
        R.drawable.person_2,
        R.drawable.person_3,
        R.drawable.person_4
    )

    if(currentRoute != "teams/{teamId}/newTask/status") {
        // Modal menu
        if (showMenu) {
            AlertDialog(
                onDismissRequest = { showMenu = false },
                title = {
                    Text(
                        "${person.name} ${person.surname}",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center  // Center the title text
                    )
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,  // Align text and buttons to the center
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                if (isInTeamPeople) {
                                    vm.removePersonFromTeam(teamId, person.personId)
                                } else {
                                    removePersonFromTask(teamId, person.personId)

                                }
                                showMenu = false  // Close the dialog after action
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = palette.primary,
                                contentColor = palette.secondary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            Text(
                                text = if (isInTeamPeople) "Remove from Team" else "Remove from Task",
                                style = typography.bodySmall
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showMenu = false }) {
                        Text(
                            "Close",
                            color = palette.secondary
                        )
                    }
                }
            )
        }

        if (showMenuAssignRole) {
            LaunchedEffect(showMenuAssignRole) {
                if (person.role.isNotEmpty()) {
                    vm.setSelectdRole(person.role)
                } else {
                    vm.setSelectdRole("")
                }
            }
            AlertDialog(
                onDismissRequest = {
                    showMenuAssignRole = false
                },
                title = { Text(text = if (person.role == "") "Assign Role" else "Edit Role") },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Assign a role to " + person.name + " " + person.surname + ":"
                        )
                        Spacer(modifier = Modifier.height(15.dp))
                        TextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = vm.selectedRole,
                            onValueChange = { if (it.length <= 30) vm.setSelectdRole(it) },
                            singleLine = true,
                            label = {
                                Row {
                                    Text("Role")
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("(${30 - vm.selectedRole.length} characters left)")
                                }
                            },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Done
                            ),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = palette.surfaceVariant,
                                unfocusedContainerColor = palette.surfaceVariant,
                                disabledContainerColor = palette.surfaceVariant,
                                cursorColor = palette.secondary,
                                focusedIndicatorColor = palette.secondary,
                                unfocusedIndicatorColor = palette.onSurfaceVariant,
                                errorIndicatorColor = palette.error,
                                focusedLabelColor = palette.secondary,
                                unfocusedLabelColor = palette.onSurfaceVariant,
                                errorLabelColor = palette.error,
                                selectionColors = TextSelectionColors(
                                    palette.primary,
                                    palette.surface
                                )
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        vm.validateRole(teamId, person.personId, vm.selectedRole)
                        showMenuAssignRole = false
                    }) {
                        Text(
                            text = "Assign",
                            style = typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = CaribbeanCurrent
                        )
                    }
                },
                dismissButton = {
                    Button(onClick = {
                        vm.selectedRoleError = ""
                        showMenuAssignRole = false
                    }) {
                        Text(
                            text = "Cancel",
                            style = typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = CaribbeanCurrent
                        )
                    }
                }
            )
        }
    }


    // Person details
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .background(backgroundColor, RoundedCornerShape(5.dp))
            .padding(8.dp)
            .then(
                if ((currentRoute == "teams/{teamId}/edit/people" || currentRoute == "teams/{teamId}/filterTasks" || currentRoute == "teams/{teamId}/newTask/status")
                    && !isAlreadyInTask
                ) Modifier.clickable {
                    isSelected = !isSelected
                    if (isSelected) {
                        if (!selectedPeople.any { it.username == person.username }) {
                            addPerson(person)
                        }
                    } else {
                        removePerson(person)
                    }
                } else if ((currentRoute != "teams/{teamId}/edit/people" && currentRoute != "teams/{teamId}/filterTasks" && currentRoute != "teams/{teamId}/newTask/status")) Modifier.combinedClickable(
                    onLongClick = {
                        if (vm.hasHigherPermission(
                                teamId,
                                auth.uid,
                                person.permission
                            )
                        ) if (person.permission == "Owner") {

                        } else {
                            showMenu = true
                        }
                    },
                    onClick = {
                        Actions
                            .getInstance()
                            .goToAccount(person.personId)
                    }
                ) else Modifier
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {


        // Account image
//        Image(
//            painter = painterResource(id = when (person.name.length%5) {
//                1 -> userImages[0]
//                2 -> userImages[1]
//                3 -> userImages[2]
//                4 -> userImages[3]
//                else -> userImages[2]
//            }),
//            contentDescription = "Task Image",
//            modifier = Modifier
//                .size(48.dp)
//                .clip(CircleShape)
//        )

        if (person.image.isNotEmpty()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(person.image)
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
                    .background(palette.surface),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (person.name.isNotEmpty() && person.surname.isNotEmpty()) "${person.name[0]}${person.surname[0]}"
                    else if (person.name.isNotEmpty()) "${person.name[0]}"
                    else "",
                    style = typography.bodyLarge,
                    color = palette.onSurface
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.width(220.dp)
                ) {
                    // Account name
                    Text(
                        text = "${person.name} ${person.surname}",
                        style = typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = textColor
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
                    text = if (person.personId != auth.uid) {
                        person.username
                    } else {
                        person.username + " (you)"
                    },
                    style = typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = textColor
                )
                Column(
                    modifier = Modifier.width(80.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    // Permission for the task
                    Text(
                        text = person.permission,
                        style = typography.bodySmall,
                        maxLines = 1,
                        color = palette.secondary
                    )
                }
            }

            // Role inside the team
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = person.role,
                    style = typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = textColor
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(10.dp))
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PeopleEntryForTask(
    teamId: String,
    person: PersonData,
    selectedPeople: List<PersonData>,
    addPerson: (PersonData) -> Unit,
    removePerson: (PersonData) -> Unit,
    removePersonFromTask: (String, String) -> Unit,
    taskpeople: List<PersonData>,
    showingCreateTask: Boolean,
    isInTeamPeople: Boolean,
    vm: SpecificTeamViewModel = viewModel()
) {
    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography
    val auth = FirebaseAuth.getInstance()


    var isSelected = selectedPeople.any { it.username == person.username }
    val isAlreadyInTask =
        remember(taskpeople) { taskpeople.any { it.username == person.username }}
    var showMenu by remember { mutableStateOf(false) }
    var showMenuAssignRole by remember { mutableStateOf(false) }
    var showOwnerMenu by remember { mutableStateOf(false) }

    val backgroundColor = if (isSelected) palette.primaryContainer else palette.surfaceVariant
    val textColor = palette.onSurface

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
            .background(backgroundColor, RoundedCornerShape(5.dp))
            .padding(8.dp)
            .then(
                if (!isAlreadyInTask) Modifier.clickable {
                    isSelected = !isSelected
                    if (isSelected) {
                        if (!selectedPeople.any { it.username == person.username }) {
                            addPerson(person)
                        }
                    } else {
                        removePerson(person)
                    }
                } else Modifier
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Selected icon
        if (isSelected && !isAlreadyInTask) {
            Image(
                painter = painterResource(id = R.drawable.outline_done_24),
                contentDescription = "Selected",
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape),
                colorFilter = ColorFilter.tint(palette.onSurface)
            )
        }
        if (isAlreadyInTask) {
            Image(
                painter = painterResource(id = R.drawable.outline_done_24),
                contentDescription = "Selected",
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape),
                colorFilter = ColorFilter.tint(palette.onSurface)
            )
        }

        // Account image
//        Image(
//            painter = painterResource(id = when (person.name.length%5) {
//                1 -> userImages[0]
//                2 -> userImages[1]
//                3 -> userImages[2]
//                4 -> userImages[3]
//                else -> userImages[2]
//            }),
//            contentDescription = "Task Image",
//            modifier = Modifier
//                .size(48.dp)
//                .clip(CircleShape)
//        )

        if (person.image.isNotEmpty()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(person.image)
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
                    .background(palette.surface),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (person.name.isNotEmpty() && person.surname.isNotEmpty()) "${person.name[0]}${person.surname[0]}"
                    else if (person.name.isNotEmpty()) "${person.name[0]}"
                    else "",
                    style = typography.bodyLarge,
                    color = palette.onSurface
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.width(220.dp)
                ) {
                    // Account name
                    Text(
                        text = "${person.name} ${person.surname}",
                        style = typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = textColor
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
                    text = if (person.personId != auth.uid) {
                        person.username
                    } else {
                        person.username + " (you)"
                    },
                    style = typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = textColor
                )
                Column(
                    modifier = Modifier.width(80.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    // Permission for the task
                    Text(
                        text = person.permission,
                        style = typography.bodySmall,
                        maxLines = 1,
                        color = palette.secondary
                    )
                }
            }

            // Role inside the team
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = person.role,
                    style = typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = textColor
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(10.dp))
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PeopleEntryForTeam(
    teamId: String,
    person: PersonData,
    selectedPeople: List<PersonData>,
    addPerson: (PersonData) -> Unit,
    removePerson: (PersonData) -> Unit,
    removePersonFromTask: (String, String) -> Unit,
    taskpeople: List<PersonData>,
    showingCreateTask: Boolean,
    isInTeamPeople: Boolean,
    vm: SpecificTeamViewModel = viewModel()
) {
    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography
    val currentRoute = Actions.getInstance().getCurrentRoute()
    val auth = FirebaseAuth.getInstance()

    val isAlreadyInTask =
        remember(taskpeople) { taskpeople.any { it.username == person.username } && !showingCreateTask }
    var showMenu by remember { mutableStateOf(false) }
    var showMenuAssignRole by remember { mutableStateOf(false) }
    var showOwnerMenu by remember { mutableStateOf(false) }

    val backgroundColor = palette.surfaceVariant
    val textColor = palette.onSurface

    val userImages = listOf(
        R.drawable.person_1,
        R.drawable.person_2,
        R.drawable.person_3,
        R.drawable.person_4
    )

    if(currentRoute != "teams/{teamId}/newTask/status") {
        // Modal menu
        if (showMenu) {
            AlertDialog(
                onDismissRequest = { showMenu = false },
                title = {
                    Text(
                        "${person.name} ${person.surname}",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center  // Center the title text
                    )
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,  // Align text and buttons to the center
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Text("Set Role/Edit Role", textAlign = TextAlign.Center)
                        Button(
                            onClick = {
                                vm.promoteOrDeclassPersonInTeam(
                                    teamId,
                                    person.personId,
                                    person.permission
                                )
                                showMenu = false  // Close the dialog after action
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = palette.primary,
                                contentColor = palette.secondary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            Text(
                                text = if (person.permission == "Admin") "Declass to Member" else "Set as Admin",
                                style = typography.bodySmall
                            )
                        }
                        // Text("Set Role/Edit Role", textAlign = TextAlign.Center)
                        Button(
                            onClick = {
                                showMenuAssignRole = true
                                showMenu = false  // Close the dialog after action
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = palette.primary,
                                contentColor = palette.secondary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            Text(
                                text = if (person.role == "") "Assign Role" else "Edit Role",
                                style = typography.bodySmall
                            )
                        }
                        Button(
                            onClick = {
                                if (isInTeamPeople) {
                                    vm.removePersonFromTeam(teamId, person.personId)
                                } else {
                                    removePersonFromTask(teamId, person.personId)

                                }
                                showMenu = false  // Close the dialog after action
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = palette.primary,
                                contentColor = palette.secondary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            Text(
                                text = if (isInTeamPeople) "Remove from Team" else "Remove from Task",
                                style = typography.bodySmall
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showMenu = false }) {
                        Text(
                            "Close",
                            color = palette.secondary
                        )
                    }
                }
            )
        }

        if (showMenuAssignRole) {
            LaunchedEffect(showMenuAssignRole) {
                if (person.role.isNotEmpty()) {
                    vm.setSelectdRole(person.role)
                } else {
                    vm.setSelectdRole("")
                }
            }
            AlertDialog(
                onDismissRequest = {
                    showMenuAssignRole = false
                },
                title = { Text(text = if (person.role == "") "Assign Role" else "Edit Role") },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Assign a role to " + person.name + " " + person.surname + ":"
                        )
                        Spacer(modifier = Modifier.height(15.dp))
                        TextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = vm.selectedRole,
                            onValueChange = { if (it.length <= 30) vm.setSelectdRole(it) },
                            singleLine = true,
                            label = {
                                Row {
                                    Text("Role")
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("(${30 - vm.selectedRole.length} characters left)")
                                }
                            },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Done
                            ),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = palette.surfaceVariant,
                                unfocusedContainerColor = palette.surfaceVariant,
                                disabledContainerColor = palette.surfaceVariant,
                                cursorColor = palette.secondary,
                                focusedIndicatorColor = palette.secondary,
                                unfocusedIndicatorColor = palette.onSurfaceVariant,
                                errorIndicatorColor = palette.error,
                                focusedLabelColor = palette.secondary,
                                unfocusedLabelColor = palette.onSurfaceVariant,
                                errorLabelColor = palette.error,
                                selectionColors = TextSelectionColors(
                                    palette.primary,
                                    palette.surface
                                )
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        vm.validateRole(teamId, person.personId, vm.selectedRole)
                        showMenuAssignRole = false
                    }) {
                        Text(
                            text = "Assign",
                            style = typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = CaribbeanCurrent
                        )
                    }
                },
                dismissButton = {
                    Button(onClick = {
                        vm.selectedRoleError = ""
                        showMenuAssignRole = false
                    }) {
                        Text(
                            text = "Cancel",
                            style = typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = CaribbeanCurrent
                        )
                    }
                }
            )
        }
        if (showOwnerMenu) {
            AlertDialog(
                onDismissRequest = { showOwnerMenu = false },
                title = {
                    Text(
                        "${person.name} ${person.surname}",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Text("Set Role/Edit Role", textAlign = TextAlign.Center)
                        Button(
                            onClick = {
                                showMenuAssignRole = true
                                showOwnerMenu = false  // Close the dialog after action
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = palette.primary,
                                contentColor = palette.secondary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            Text(
                                text = if (person.role == "") "Assign Role" else "Edit Role",
                                style = typography.bodySmall
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showOwnerMenu = false }) {
                        Text(
                            "Close",
                            color = palette.secondary
                        )
                    }
                }
            )
        }
    }


    // Person details
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .background(backgroundColor, RoundedCornerShape(5.dp))
            .padding(8.dp)
            .then(
                if ((currentRoute == "teams/{teamId}/edit/people" || currentRoute == "teams/{teamId}/filterTasks" || currentRoute == "teams/{teamId}/newTask/status")
                    && !isAlreadyInTask
                ) Modifier.clickable {
                } else if ((currentRoute != "teams/{teamId}/edit/people" && currentRoute != "teams/{teamId}/filterTasks" && currentRoute != "teams/{teamId}/newTask/status")) Modifier.combinedClickable(
                    onLongClick = {
                        if (vm.hasHigherPermission(
                                teamId,
                                auth.uid,
                                person.permission
                            )
                        ) if (person.permission == "Owner") {
                            showOwnerMenu = true
                        } else {
                            showMenu = true
                        }
                    },
                    onClick = {
                        Actions
                            .getInstance()
                            .goToAccount(person.personId)
                    }
                ) else Modifier
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Account image
//        Image(
//            painter = painterResource(id = when (person.name.length%5) {
//                1 -> userImages[0]
//                2 -> userImages[1]
//                3 -> userImages[2]
//                4 -> userImages[3]
//                else -> userImages[2]
//            }),
//            contentDescription = "Task Image",
//            modifier = Modifier
//                .size(48.dp)
//                .clip(CircleShape)
//        )

        if (person.image.isNotEmpty()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(person.image)
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
                    .background(palette.surface),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (person.name.isNotEmpty() && person.surname.isNotEmpty()) "${person.name[0]}${person.surname[0]}"
                    else if (person.name.isNotEmpty()) "${person.name[0]}"
                    else "",
                    style = typography.bodyLarge,
                    color = palette.onSurface
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.width(220.dp)
                ) {
                    // Account name
                    Text(
                        text = "${person.name} ${person.surname}",
                        style = typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = textColor
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
                    text = if (person.personId != auth.uid) {
                        person.username
                    } else {
                        person.username + " (you)"
                    },
                    style = typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = textColor
                )
                Column(
                    modifier = Modifier.width(80.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    // Permission for the task
                    Text(
                        text = person.permission,
                        style = typography.bodySmall,
                        maxLines = 1,
                        color = palette.secondary
                    )
                }
            }

            // Role inside the team
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = person.role,
                    style = typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = textColor
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(10.dp))
}


@Composable
fun PersonBadge(
    person: PersonData, onRemove: () -> Unit
) {
    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(color = palette.surfaceVariant, shape = CircleShape)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.outline_close_24),
            contentDescription = "Remove",
            modifier = Modifier
                .size(30.dp)
                .clickable(onClick = onRemove)
                .padding(4.dp),
            colorFilter = ColorFilter.tint(palette.onSurface)
        )
        Text(
            text = "${person.name} ${person.surname}",
            style = typography.labelSmall,
            color = palette.onSurface,
            modifier = Modifier.padding(4.dp),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}


// ----- Section for team members -----
@Composable
fun PeopleSection(
    teamId: String,
    taskId: String,
    taskpeople: List<PersonData>,
    teampeople: List<PersonData>,
    selectedPeople: List<PersonData>,
    clearSelectedPeople: () -> Unit,
    addPerson: (PersonData) -> Unit,
    removePerson: (PersonData) -> Unit,
    addSelectedTeamPeopleToTask: () -> Unit,
    removePersonFromTask: (String, String) -> Unit,
    filteredPeople: List<PersonData>,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    setShowTeamLinkOrQrCode: (Boolean) -> Unit,
    isInTeamPeople: Boolean,
    peopleOrTaskNameError: String
) {
    val typography = TeamTaskTypography
    val palette = MaterialTheme.colorScheme

    val currentRoute = Actions.getInstance().getCurrentRoute()

    BoxWithConstraints {
        val maxHeight = this.maxHeight
        val maxWidth = this.maxWidth

        Column {
            if ((currentRoute == "teams/{teamId}/edit/people" || currentRoute == "teams/{teamId}/filterTasks" || currentRoute == "teams/{teamId}/newTask/status"
                        || currentRoute == "teams/{teamId}/tasks/{taskId}/comments")) {
                /*
                if (taskpeople.isNotEmpty()) {
                    // Search bar
                    CustomSearchBar(
                        modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp),
                        placeholderText = "Who would you like to add?",
                        searchQuery,
                        onSearchQueryChanged
                    )
                }
                if(currentRoute == "teams/{teamId}/newTask/status" && peopleOrTaskNameError.isNotEmpty()){
                    Text(
                        text = peopleOrTaskNameError,
                        color = palette.error,
                        style = typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 5.dp),
                        maxLines = 3
                    )
                }

                // List of placeholders selected people
                LazyVerticalGrid(
                    columns = GridCells.FixedSize(120.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .heightIn(max = if (maxHeight > maxWidth) 140.dp else 40.dp)
                        .padding(start = 10.dp, bottom = 15.dp)
                ) {
                    items(selectedPeople) { person ->
                        PersonBadge(
                            person = person,
                            onRemove = {
                                removePerson(person)
                            }
                        )
                    }
                }
                 */
            }

            // List of members of the task
            LazyColumn(
                modifier = Modifier.fillMaxHeight()
            ) {
                if ((currentRoute != "teams/{teamId}/edit/people" && currentRoute != "teams/{teamId}/filterTasks" && currentRoute != "teams/{teamId}/newTask/status")) {
                    item {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 15.dp, vertical = 15.dp)
                        ) {
                            if (taskpeople.isNotEmpty()) {
                                Text(
                                    "${taskpeople.size} members",
                                    style = typography.bodySmall
                                )
                            }
                            if ((isInTeamPeople && teampeople.isEmpty() && taskpeople.isEmpty())
                                || (teampeople.isNotEmpty() && taskpeople.isEmpty())
                            ) {
                                Text(
                                    "Press on the button below to add members to this task!",
                                    style = typography.bodyMedium,
                                    modifier = Modifier.align(Alignment.Center)
                                )

                            }
                        }
                    }
                }

                items(taskpeople) {
                    PeopleEntry(
                        teamId,
                        person = it,
                        selectedPeople,
                        addPerson,
                        removePerson,
                        removePersonFromTask,
                        listOf(),
                        false,
                        isInTeamPeople
                    )
                }
                if (!isInTeamPeople && teampeople.isEmpty()) {
                    item {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 15.dp, vertical = 15.dp)
                        ) {
                            Text(
                                text = "To assign members to a task, \nyou should add members to your teams before!",
                                style = typography.bodyMedium,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }

                item {
                    if (filteredPeople.isEmpty() && taskpeople.isNotEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No results for $searchQuery",
                                style = typography.labelMedium,
                                color = palette.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        if ((currentRoute == "teams/{teamId}/edit/people" || currentRoute == "teams/{teamId}/filterTasks" || currentRoute == "teams/{teamId}/newTask/status") && currentRoute != "teams/{teamId}/newTask/status") {
            CustomFloatingButton(modifier = Modifier.align(Alignment.BottomEnd),
                "Confirm Add Members",
                {},
                {},
                addSelectedTeamPeopleToTask,
                clearSelectedPeople,
                onSearchQueryChanged,
                {}
            )
        }
        if ((currentRoute != "teams/{teamId}/edit/people" && currentRoute != "teams/{teamId}/filterTasks" && currentRoute != "teams/{teamId}/newTask/status")) {
            if (isInTeamPeople) {
            } else {
                if (teampeople.isNotEmpty()) {
                    CustomFloatingButton(modifier = Modifier.align(Alignment.BottomEnd),
                        "Add Members",
                        {},
                        {},
                        {},
                        clearSelectedPeople,
                        {}, {}
                    )
                }
            }
        }

        //invite people to team
        if(currentRoute != "teams/{teamId}/newTask/status"){
            FloatingActionButton(
                onClick = {
                    Actions.getInstance().goToEditTeamPeople(teamId)
                    //Actions.getInstance().goToCreateTaskStatus(teamId)
                },
                containerColor = palette.secondary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(25.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.outline_person_add_24),
                    contentDescription = "Add",
                    colorFilter = ColorFilter.tint(palette.background)
                )
            }
        }

        //invite people to task
        if(currentRoute == "teams/{teamId}/tasks/{taskId}/comments"){
            FloatingActionButton(
                onClick = {

                      clearSelectedPeople()
                      Actions.getInstance().goToEditTaskPeople(teamId, taskId)

                      //onSearchQueryChanged("")
                      //addSelectedTeamPeopleToTask()
                      //setAddingPeopleInTask(false)
                      //clearSelectedPeople()
                },
                containerColor = palette.secondary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(25.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.outline_person_add_24),
                    contentDescription = "Add",
                    colorFilter = ColorFilter.tint(palette.background)
                )
            }
        }
    }
}

@Composable
fun AddPeopleInTaskSection(
    teamId: String,
    taskId: String,
    taskpeople: List<PersonData>,
    teampeople: List<PersonData>,
    selectedPeople: List<PersonData>,
    clearSelectedPeople: () -> Unit,
    addPerson: (PersonData) -> Unit,
    removePerson: (PersonData) -> Unit,
    addSelectedTeamPeopleToTask: () -> Unit,
    removePersonFromTask: (String, String) -> Unit,
    filteredPeople: List<PersonData>,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    setShowTeamLinkOrQrCode: (Boolean) -> Unit,
    isInTeamPeople: Boolean,
    peopleOrTaskNameError: String
){
    val typography = TeamTaskTypography
    val palette = MaterialTheme.colorScheme

    val currentRoute = Actions.getInstance().getCurrentRoute()

    BoxWithConstraints {
        val maxHeight = this.maxHeight
        val maxWidth = this.maxWidth

        Column {
            if (teampeople.isNotEmpty() || !isInTeamPeople) {
                // Search bar
                CustomSearchBar(
                    modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp),
                    placeholderText = "Who would you like to add?",
                    searchQuery,
                    onSearchQueryChanged
                )
            }

            // List of placeholders selected people
            LazyVerticalGrid(
                columns = GridCells.FixedSize(120.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .heightIn(max = if (maxHeight > maxWidth) 140.dp else 40.dp)
                    .padding(start = 10.dp, bottom = 15.dp)
            ) {
                items(selectedPeople) { person ->
                    PersonBadge(
                        person = person,
                        onRemove = {
                            removePerson(person)
                        }
                    )
                }
            }

            // List of members of the task
            LazyColumn(
                modifier = Modifier.fillMaxHeight()
            ) {
                items(filteredPeople) {
                    PeopleEntryForTask(
                        teamId,
                        person = it,
                        selectedPeople,
                        addPerson,
                        removePerson,
                        removePersonFromTask,
                        taskpeople,
                        showingCreateTask = true,
                        isInTeamPeople
                    )
                }
                item {
                    if (filteredPeople.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No results for $searchQuery",
                                style = typography.labelMedium,
                                color = palette.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = {

                clearSelectedPeople()
                Actions.getInstance().navigateBack()

                //onSearchQueryChanged("")
                //addSelectedTeamPeopleToTask()
                //setAddingPeopleInTask(false)
                //clearSelectedPeople()
            },
            containerColor = palette.secondary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(25.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.outline_done_24),
                contentDescription = "Add",
                colorFilter = ColorFilter.tint(palette.background)
            )
        }
    }
}

@Composable
fun PeopleSectionForTeam(
    teamId: String,
    taskpeople: List<PersonData>,
    teampeople: List<PersonData>,
    selectedPeople: List<PersonData>,
    clearSelectedPeople: () -> Unit,
    addPerson: (PersonData) -> Unit,
    removePerson: (PersonData) -> Unit,
    addSelectedTeamPeopleToTask: () -> Unit,
    removePersonFromTask: (String, String) -> Unit,
    filteredPeople: List<PersonData>,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    setShowTeamLinkOrQrCode: (Boolean) -> Unit,
    isInTeamPeople: Boolean,
    peopleOrTaskNameError: String
) {
    val typography = TeamTaskTypography
    val palette = MaterialTheme.colorScheme

    val currentRoute = Actions.getInstance().getCurrentRoute()

    BoxWithConstraints {
        val maxHeight = this.maxHeight
        val maxWidth = this.maxWidth

        Column {
            // List of members of the task
            LazyColumn(
                modifier = Modifier.fillMaxHeight()
            ) {
                item {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 15.dp, vertical = 15.dp)
                    ) {
                        if (taskpeople.isNotEmpty()) {
                            Text(
                                "${taskpeople.size} members",
                                style = typography.bodySmall
                            )
                        }
                        if ((isInTeamPeople && teampeople.isEmpty() && taskpeople.isEmpty())
                            || (teampeople.isNotEmpty() && taskpeople.isEmpty())
                        ) {
                            if (currentRoute != "teams/{teamId}/newTask/status") {
                                Text(
                                    "Press on the button below to add members!",
                                    style = typography.bodyMedium,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }
                }


                items(taskpeople) {
                    PeopleEntryForTeam(
                        teamId,
                        person = it,
                        selectedPeople,
                        addPerson,
                        removePerson,
                        removePersonFromTask,
                        listOf(),
                        false,
                        isInTeamPeople
                    )
                }
            }
        }

        if ((currentRoute == "teams/{teamId}/edit/people" || currentRoute == "teams/{teamId}/filterTasks" || currentRoute == "teams/{teamId}/newTask/status") && currentRoute != "teams/{teamId}/newTask/status") {
            CustomFloatingButton(modifier = Modifier.align(Alignment.BottomEnd),
                "Confirm Add Members",
                {},
                {},
                addSelectedTeamPeopleToTask,
                clearSelectedPeople,
                onSearchQueryChanged,
                {}
            )
        }
        if ((currentRoute != "teams/{teamId}/edit/people" && currentRoute != "teams/{teamId}/filterTasks" && currentRoute != "teams/{teamId}/newTask/status")) {
            if (isInTeamPeople) {
            } else {
                if (teampeople.isNotEmpty()) {
                    CustomFloatingButton(modifier = Modifier.align(Alignment.BottomEnd),
                        "Add Members",
                        {},
                        {},
                        {},
                        clearSelectedPeople,
                        {}, {}
                    )
                }
            }
        }

        //invite people to team
        if(currentRoute != "teams/{teamId}/newTask/status"){
            FloatingActionButton(
                onClick = {
                    Actions.getInstance().goToEditTeamPeople(teamId)
                    //Actions.getInstance().goToCreateTaskStatus(teamId)
                },
                containerColor = palette.secondary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(25.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.outline_person_add_24),
                    contentDescription = "Add",
                    colorFilter = ColorFilter.tint(palette.background)
                )
            }
        }

        //invite people to ta
        if(currentRoute == "teams/{teamId}/tasks/{taskId}/comments"){
            FloatingActionButton(
                onClick = {

                    clearSelectedPeople()
                    //NAVIGA AD AGGIUNGI MEMBRI

                    //onSearchQueryChanged("")
                    //addSelectedTeamPeopleToTask()
                    //setAddingPeopleInTask(false)
                    //clearSelectedPeople()
                },
                containerColor = palette.secondary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(25.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.teamtasklogo),
                    contentDescription = "Add",
                    colorFilter = ColorFilter.tint(palette.background)
                )
            }
        }
    }
}

@Composable
fun PeopleSectionCreation(
    teamId: String,
    taskpeople: List<PersonData>,
    teampeople: List<PersonData>,
    selectedPeople: List<PersonData>,
    clearSelectedPeople: () -> Unit,
    addPerson: (PersonData) -> Unit,
    removePerson: (PersonData) -> Unit,
    addSelectedTeamPeopleToTask: () -> Unit,
    removePersonFromTask: (String, String) -> Unit,
    filteredPeople: List<PersonData>,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    setShowTeamLinkOrQrCode: (Boolean) -> Unit,
    isInTeamPeople: Boolean,
    peopleOrTaskNameError: String
) {
    val typography = TeamTaskTypography
    val palette = MaterialTheme.colorScheme

    val currentRoute = Actions.getInstance().getCurrentRoute()

    BoxWithConstraints {
        val maxHeight = this.maxHeight
        val maxWidth = this.maxWidth

        Column {
            if (teampeople.isNotEmpty() || !isInTeamPeople && taskpeople.isNotEmpty()) {
                // Search bar
                CustomSearchBar(
                    modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp),
                    placeholderText = "Who would you like to add?",
                    searchQuery,
                    onSearchQueryChanged
                )
            }
            if(peopleOrTaskNameError.isNotEmpty()){
                Text(
                    text = peopleOrTaskNameError,
                    color = palette.error,
                    style = typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 5.dp),
                    maxLines = 3
                )
            }

            // List of placeholders selected people
            LazyVerticalGrid(
                columns = GridCells.FixedSize(120.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .heightIn(max = if (maxHeight > maxWidth) 140.dp else 40.dp)
                    .padding(start = 10.dp, bottom = 15.dp)
            ) {
                items(selectedPeople) { person ->
                    PersonBadge(
                        person = person,
                        onRemove = {
                            removePerson(person)
                        }
                    )
                }
            }

            // List of members of the task
            LazyColumn(
                modifier = Modifier.fillMaxHeight()
            ) {
                items(filteredPeople) {
                    PeopleEntry(
                        teamId,
                        person = it,
                        selectedPeople,
                        addPerson,
                        removePerson,
                        removePersonFromTask,
                        taskpeople,
                        showingCreateTask = true,
                        isInTeamPeople
                    )
                }
                //NEVER VISUALIZED SINCE WE HAVE AT LEAST ONE OWNER WHEN WE CREATE A NEW TASK!
                if (!isInTeamPeople && teampeople.isEmpty()) {
                    item {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 15.dp, vertical = 15.dp)
                        ) {
                            Text(
                                text = "To assign members to a task, \nyou should add members to your teams before!",
                                style = typography.bodyMedium,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }

                item {
                    if (filteredPeople.isEmpty() && taskpeople.isNotEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No results for $searchQuery",
                                style = typography.labelMedium,
                                color = palette.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PeopleEntryForFilters(
    person: PersonData, selectedPeople: List<PersonData>,
    addPerson: (PersonData) -> Unit, removePerson: (PersonData) -> Unit,
) {
    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography

    var isSelected = selectedPeople.any { it.username == person.username }

    val backgroundColor = if (isSelected) palette.primaryContainer else palette.surfaceVariant
    val textColor = palette.onSurface

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
            .background(backgroundColor, RoundedCornerShape(5.dp))
            .padding(8.dp)
            .clickable {
                isSelected = !isSelected
                if (isSelected) {
                    if (!selectedPeople.any { it.username == person.username }) {
                        addPerson(person)
                    }
                } else {
                    removePerson(person)
                }
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSelected) {
            // Selected icon
            Image(
                painter = painterResource(id = R.drawable.outline_done_24),
                contentDescription = "Selected",
                modifier = Modifier
                    .size(25.dp)
                    .clip(CircleShape),
                colorFilter = ColorFilter.tint(palette.onSurface)
            )
        }

        // Account image
        Image(
            painter = painterResource(
                id = when (person.name.length % 5) {
                    1 -> userImages[0]
                    2 -> userImages[1]
                    3 -> userImages[2]
                    4 -> userImages[3]
                    else -> userImages[2]
                }
            ),
            contentDescription = "Task Image",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.width(220.dp)
                ) {
                    // Account name
                    Text(
                        text = "${person.name} ${person.surname}",
                        style = typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = textColor
                    )
                }

                Column(
                    modifier = Modifier.width(80.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    // Role for the task
                    Text(
                        text = person.permission,
                        style = typography.bodySmall,
                        maxLines = 1,
                        color = palette.secondary
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
                    text = person.username,
                    style = typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = textColor
                )
            }

            // Role inside the team
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = person.role,
                    style = typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = textColor
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(10.dp))
}

@Composable
fun PeopleSectionForFilters(
    //listOfMembersForFilter: List<PersonData>, setMembersInFilterPage: () -> Unit,
    //teampeople: List<PersonData>,
    selectedPeople: List<PersonData>,
    //clearSelectedPeople: () -> Unit,
    addPerson: (PersonData) -> Unit, removePerson: (PersonData) -> Unit,
    //addSelectedTeamPeopleToTask: () -> Unit,
    filteredPeople: List<PersonData>,
    searchQuery: String, onSearchQueryChanged: (String) -> Unit,
    //showFilterMemberInFilters: Boolean,
    setShowingFilterMemberInFilters: (Boolean) -> Unit,
) {
    val typography = TeamTaskTypography
    val palette = MaterialTheme.colorScheme

    val copyOfselectedPeople = selectedPeople.toList()

    Dialog(onDismissRequest = {
        setShowingFilterMemberInFilters(false)
        onSearchQueryChanged("")

        // Create a set of usernames for quick lookup from listOfMembersForFilter
        val filterSet = copyOfselectedPeople.map { it.username }.toSet()

        // Create a set from selectedPeople for comparison
        val selectedSet = selectedPeople.map { it.username }.toSet()

        // Check if the sets are different to decide if updates are necessary
        if (selectedSet != filterSet) {
            // Prepare a new list of people based on listOfMembersForFilter
            val updatedSelectedPeople = copyOfselectedPeople.filter { person ->
                person.username in filterSet
            }.toList()

            updatedSelectedPeople.forEach { person ->
                addPerson(person) // Adds each person to the state if not already present
            }
        }
    }) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .heightIn(max = 500.dp)
        ) {
            Column(
                modifier = Modifier
                    .background(palette.surface)
                    .padding(16.dp)
            ) {
                Column {
                    // Search bar
                    CustomSearchBar(
                        modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp),
                        placeholderText = "Search members",
                        searchQuery,
                        onSearchQueryChanged
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // List of members of the task
                    LazyColumn(
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        items(filteredPeople) {
                            PeopleEntryForFilters(
                                person = it,
                                selectedPeople,
                                addPerson,
                                removePerson
                            )
                        }

                        item {
                            if (filteredPeople.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No results for $searchQuery",
                                        style = typography.labelMedium,
                                        color = palette.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
                /*
                CustomFloatingButton(Modifier.align(Alignment.BottomEnd), "Filter Add Members",
                    setShowingFilterMemberInFilters, {}, {}, clearSelectedPeople,
                    onSearchQueryChanged, {}
                ) */
            }
        }
    }
}


@Composable
fun ExpandableContainer(
    groupedtoDoTasks: Map<String, List<ToDoTask>>
) {
    val typography = TeamTaskTypography
    val palette = MaterialTheme.colorScheme
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        border = BorderStroke(1.dp, Color.Gray)
    ) {
        Column {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.teamtasklogo),
                    contentDescription = "Team Image",
                    modifier = Modifier.size(50.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = "team name", color = Color.Black)
                    Text(text = "CEO", color = Color.Gray)
                }
                Spacer(modifier = Modifier.weight(1f))
                Image(
                    painter = painterResource(if (expanded) R.drawable.baseline_arrow_drop_up_24 else R.drawable.baseline_arrow_drop_down_24),
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }

            if (expanded) {
                groupedtoDoTasks.forEach { (label, schedtasks) ->
                    Text(
                        text = label,
                        style = typography.labelMedium,
                        fontSize = 18.sp,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(top = 12.dp, bottom = 8.dp)
                    )
                    for (task in schedtasks) {
                        ToDoTaskEntry(
                            scheduledtask = task,
                            viewOnlyMode = true,
                            teamId = "",
                            taskId = ""
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
    Spacer(modifier = Modifier.height(5.dp))
}

@Composable
fun ShowProfile(
    filteredTeamParticipant: Pair<String, Person>
) {
    // Hardcoded list of scheduled tasks
    var toDoTasks = listOf(
        ToDoTask(
            "0",
            "Task 1",
            "Completed",
            1,
            "Weekly",
            "2024-04-30T12:53:00+02:00",
            "2024-04-01T09:00:00+02:00",
            listOf(
                PersonData("0", "Luca", "Bianchi", "luca_bianchi", "CEO", "Owner", ""),
                PersonData(
                    "1",
                    "Name1ejwnewjneees",
                    "Surname1fskfsmkfnsk",
                    "username1",
                    "CTO",
                    "Admin",
                    ""
                ),
                PersonData(
                    "2",
                    "Sofia",
                    "Esposito",
                    "sofia_esposito",
                    "Marketing Director",
                    "",
                    ""
                ),
                PersonData("3", "Giulia", "Ricci", "giulia_ricci", "HR Manager", "", ""),
            ).sortedBy { it.name },
            listOf("#test1", "#test2")
        ),
        ToDoTask(
            "1",
            "Task 2",
            "Expired",
            0,
            "Never",
            "2024-04-20T16:42:00+02:00",
            "2024-04-01T10:00:00+02:00",
            listOf(
                PersonData("0", "Luca", "Bianchi", "luca_bianchi", "CEO", "Owner", ""),
                PersonData("1", "Giulia", "Ricci", "giulia_ricci", "HR Manager", "", ""),
            ).sortedBy { it.name },
            listOf("#test1", "#test2")
        ),
        ToDoTask(
            "2",
            "Task 2.5",
            "Completed",
            0,
            "Never",
            "2024-04-20T16:42:00+02:00",
            "2024-04-01T10:00:00+02:00",
            listOf(
                PersonData("0", "Luca", "Bianchi", "luca_bianchi", "CEO", "Owner", ""),
                PersonData(
                    "1",
                    "Sofia",
                    "Esposito",
                    "sofia_esposito",
                    "Marketing Director",
                    "",
                    ""
                ),
            ).sortedBy { it.name },
            listOf("#test1", "#test2")
        ),
        ToDoTask(
            "3",
            "Task 2.6",
            "Completed",
            0,
            "Never",
            "2024-04-20T16:42:00+02:00",
            "2024-04-01T10:00:00+02:00",
            listOf(
                PersonData("0", "Luca", "Bianchi", "luca_bianchi", "CEO", "Owner", ""),
                PersonData(
                    "1",
                    "Sofia",
                    "Esposito",
                    "sofia_esposito",
                    "Marketing Director",
                    "",
                    ""
                ),
            ).sortedBy { it.name },
            listOf("#test1", "#test2")
        ),
        ToDoTask(
            "4",
            "Task 2.7",
            "Completed",
            0,
            "Never",
            "2024-04-20T16:42:00+02:00",
            "2024-04-01T10:00:00+02:00",
            listOf(
                PersonData("0", "Luca", "Bianchi", "luca_bianchi", "CEO", "Owner", ""),
                PersonData(
                    "1",
                    "Sofia",
                    "Esposito",
                    "sofia_esposito",
                    "Marketing Director",
                    "",
                    ""
                ),
            ).sortedBy { it.name },
            listOf("#test1", "#test2")
        ),
        ToDoTask(
            "5",
            "Task 2.8",
            "Completed",
            0,
            "Never",
            "2024-04-20T16:42:00+02:00",
            "2024-04-01T10:00:00+02:00",
            listOf(
                PersonData("0", "Luca", "Bianchi", "luca_bianchi", "CEO", "Owner", ""),
                PersonData(
                    "1",
                    "Sofia",
                    "Esposito",
                    "sofia_esposito",
                    "Marketing Director",
                    "",
                    ""
                ),
            ).sortedBy { it.name },
            listOf("#test1", "#test2")
        ),
        ToDoTask(
            "6",
            "Task 3",
            "Scheduled",
            1,
            "Never",
            "2024-05-07T13:36:00+02:00",
            "2024-04-02T11:00:00+02:00",
            listOf(
                PersonData(
                    "0",
                    "Name1ejwnewjneees",
                    "Surname1fskfsmkfnsk",
                    "username1",
                    "CTO",
                    "Admin",
                    ""
                ),
                PersonData("1", "Giulia", "Ricci", "giulia_ricci", "HR Manager", "", ""),
            ).sortedBy { it.name },
            listOf("#test4", "#test5")
        ),
        ToDoTask(
            "7",
            "Task 4",
            "Scheduled",
            0,
            "Monthly",
            "2024-05-30T12:12:00+02:00",
            "2024-04-02T12:00:00+02:00",
            listOf(
                PersonData(
                    "0",
                    "Sofia",
                    "Esposito",
                    "sofia_esposito",
                    "Marketing Director",
                    "",
                    ""
                ),
                PersonData("1", "Giulia", "Ricci", "giulia_ricci", "HR Manager", "", ""),
            ).sortedBy { it.name },
            listOf("#test1", "#test2")
        ),
        ToDoTask(
            "8",
            "Task 5",
            "Scheduled",
            1,
            "Yearly",
            "2024-05-07T22:21:00+02:00",
            "2024-04-03T08:00:00+02:00",
            listOf(
                PersonData(
                    "0",
                    "Sofia",
                    "Esposito",
                    "sofia_esposito",
                    "Marketing Director",
                    "",
                    ""
                ),
                PersonData("1", "Giulia", "Ricci", "giulia_ricci", "HR Manager", "", ""),
            ).sortedBy { it.name },
            listOf("#test4", "#test5")
        )
    )

    val groupedtoDoTasks = toDoTasks.groupBy { it.expirationTimestamp.split("T")[0] }

    val person = filteredTeamParticipant.second
    var imageUri = "teamtasklogo".toUri()

    var teamList = listOf(1, 2, 3)

    BoxWithConstraints {
        if (this.maxHeight >= this.maxWidth) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Spacer(modifier = Modifier.height(25.dp))
                }

                // Image and username
                item {
                    ProfilePictureSection(
                        person.name,
                        person.surname,
                        person.username,
                        false,
                        imageUri
                    )
                }

                item { Spacer(modifier = Modifier.height(40.dp)) }

                item {
                    ProfileInfoSection(
                        person.name, "", {},
                        person.surname, "", {},
                        person.email, "", {},
                        person.username, "", {},
                        person.location, "", {},
                        person.bio, "", {},
                        false
                    )
                }

                item { Spacer(modifier = Modifier.height(40.dp)) }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 5.dp),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Teams and Tasks in common")
                    }
                }
                items(teamList) {
                    ExpandableContainer(groupedtoDoTasks)
                    Spacer(modifier = Modifier.height(2.dp))
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        } else {
            Row(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp)
            ) {
                // Image and username
                Column(
                    modifier = Modifier
                        .fillMaxWidth(fraction = 0.33f)
                        .fillMaxHeight()
                        .padding(end = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    ProfilePictureSection(
                        person.name,
                        person.surname,
                        person.username,
                        false,
                        imageUri
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item { Spacer(modifier = Modifier.height(20.dp)) }

                    item {
                        ProfileInfoSection(
                            person.name, "", {},
                            person.surname, "", {},
                            person.email, "", {},
                            person.username, "", {},
                            person.location, "", {},
                            person.bio, "", {},
                            false
                        )
                    }

                    item { Spacer(modifier = Modifier.height(20.dp)) }

                    items(teamList) {
                        ExpandableContainer(groupedtoDoTasks)
                        Spacer(modifier = Modifier.height(2.dp))
                    }

                    item { Spacer(modifier = Modifier.height(20.dp)) }
                }
            }
        }
    }

}

