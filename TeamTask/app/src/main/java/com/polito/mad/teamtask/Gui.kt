package com.polito.mad.teamtask

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.navigation
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.firestore
import com.google.gson.Gson
import com.polito.mad.teamtask.chat.visualization.ClientMessage
import com.polito.mad.teamtask.chat.visualization.SingleChatScreen
import com.polito.mad.teamtask.chat.visualization.SingleChatViewModel
import com.polito.mad.teamtask.components.BottomBar
import com.polito.mad.teamtask.components.CategoryFilterScreen
import com.polito.mad.teamtask.components.NewTeam
import com.polito.mad.teamtask.components.TopBar
import com.polito.mad.teamtask.screens.AddMemberToTeamScreen
import com.polito.mad.teamtask.screens.CalendarWithEvents
import com.polito.mad.teamtask.screens.ChatScreen
import com.polito.mad.teamtask.screens.HomeScreen
import com.polito.mad.teamtask.screens.LoadingScreen
import com.polito.mad.teamtask.screens.NotificationsScreen
import com.polito.mad.teamtask.screens.ProfileFormViewModel
import com.polito.mad.teamtask.screens.ProfileScreen
import com.polito.mad.teamtask.screens.EditProfilePane
import com.polito.mad.teamtask.screens.FilterTasksScreen
import com.polito.mad.teamtask.screens.InviteConfirmationScreen
import com.polito.mad.teamtask.screens.NewTask
import com.polito.mad.teamtask.screens.PersonData
import com.polito.mad.teamtask.screens.ShowProfile
import com.polito.mad.teamtask.screens.SpecificTeamScreen
import com.polito.mad.teamtask.screens.SpecificTeamViewModel
import com.polito.mad.teamtask.screens.TeamsScreen
import com.polito.mad.teamtask.screens.TeamsViewModel
import com.polito.mad.teamtask.tasks.ShowTaskDetails
import com.polito.mad.teamtask.ui.theme.CaribbeanCurrent
import com.polito.mad.teamtask.ui.theme.TeamTaskTypography
import com.polito.mad.teamtask.utils.uploadFilesToFirebaseStorage
import com.polito.mad.teamtask.utils.uploadTeamImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


data class Person(
    val name: String,
    val surname: String,
    val email: String,
    val username: String,
    val loginMethod: String,
    val image: String,
    val emailVerified: Boolean,
    val location: String,
    val bio: String,
    val teams: List<String>,
    val tasks: List<String>
) {
    constructor() : this("", "", "", "", "", "", false, "", "", emptyList(), emptyList())
}

data class Task(
    val teamId: String,
    val title: String,
    val description: String,
    val creatorId: String,
    val creationDate: String,
    val deadline: String,
    val prioritized: Boolean,
    val status: String,
    val tags: List<String>,
    val recurrence: String,
    val people: List<String>
) {
    constructor() : this(
        "Jolly team 1",
        "",
        "",
        "Jolly person 1",
        "1970-01-01T00:00:00+00:00",
        "1970-01-01T00:00:00+00:00",
        false,
        "",
        emptyList(),
        "",
        emptyList()
    )
}

data class Comment(
    val taskId: String,
    val senderId: String,
    val timestamp: String,
    val body: String?,
    val media: String?,
    val repliesAllowed: Boolean,
    val replies: List<String>
) {
    constructor() : this(
        "Jolly task 1",
        "Jolly person 1",
        "1970-01-01T00:00:00+00:00",
        "",
        "",
        false,
        emptyList()
    )
}

data class PrivateMessage(
    val senderId: String,
    val receiverId: String,
    val timestamp: String,
    val body: String?,
    val media: String?,
    val read: Boolean
) {
    constructor() : this(
        "Jolly person 1",
        "Jolly person 2",
        "1970-01-01T00:00:00+00:00",
        "",
        "",
        false
    )
}

data class TaskReply(
    val commentId: String,
    val senderId: String,
    val timestamp: String,
    val body: String,
    val media: String?
) {
    constructor() : this("Jolly comment 1", "Jolly person 1", "1970-01-01T00:00:00+00:00", "", null)
}

data class Team(
    val name: String,
    val image: String,
    val ownerId: String,
    val admins: List<String>,
    val inviteLink: String,
    val creationDate: String,
    val category: String,
    val members: List<String>,
    val tasks: List<String>
) {
    constructor() : this(
        "",
        "",
        "Jolly person 1",
        emptyList(),
        "",
        "1970-01-01T00:00:00+00:00",
        "",
        emptyList(),
        emptyList()
    )
}

data class Notification(
    val senderId: String,
    val taskId: String,
    val body: String,
    val timestamp: String,
    val typology: Long,
    val teamId: String,
    val fromGroup: Boolean,
    val receivers: List<String>
) {
    constructor() : this(
        "Jolly person 1",
        "Jolly task 1",
        "",
        "1970-01-01T00:00:00+00:00",
        0L,
        "",
        false,
        emptyList()
    )
}

data class TeamMessage(
    val teamId: String,
    val senderId: String,
    val timestamp: String,
    val body: String,
    val media: String?
) {
    constructor() : this("Jolly team 1", "Jolly person 1", "1970-01-01T00:00:00+00:00", "", null)
}

data class TeamParticipant(
    val teamId: String,
    val personId: String,
    val frequentlyAccessed: Boolean,
    val role: String,
    val completedTasks: Long,
    val totalTasks: Long
) {
    constructor() : this("Jolly team 1", "Jolly person 1", false, "", 0L, 0L)
}

data class UserNotification(
    val notificationId: String,
    val userId: String,
    val read: Boolean
) {
    constructor() : this("Jolly notification 1", "Jolly person 1", false)
}

data class TeamMemberInfo(
    val id: String,
    val username: String,
    val name: String,
    val surname: String,
    val image: String
)

sealed class ChatMessage {
    data class TeamChatMessage(val id: String, val message: TeamMessage) : ChatMessage()
    data class PrivateChatMessage(val id: String, val message: PrivateMessage) : ChatMessage()
}


class TeamTask : Application() {
    lateinit var model: AppModel

    override fun onCreate() {
        super.onCreate()
        model = AppModel(this)
    }
}


class AppModel(
    context: Context
) {
    init {
        FirebaseApp.initializeApp(context)
    }

    val applicationContext = context
    val auth = FirebaseAuth.getInstance()

    private val currentDateTime =
        "1970-01-01T00:00:00+00:00" //LocalDateTime.parse(LocalDateTime.now().toString(), DateTimeFormatter.ISO_DATE_TIME).toString()

    val db = Firebase.firestore
    val TAG = "TTDB"
    //val userId by mutableStateOf("GQQwO62zw1M2TGBvoojK")

    // --- Functions ---
    // Logged-in user
    fun getPersonal(): Flow<Pair<String, Person>> = callbackFlow {
        val listener = auth.currentUser?.uid?.let {
            db.collection("people")
                .document(it)
                .addSnapshotListener { r, e ->
                    if (r != null) {
                        val person = r.toObject(Person::class.java)
                        val id = r.id
                        if (person != null) trySend(Pair(id, person))
                        else trySend(Pair("-1", Person())) // TODO: Check correctness
                    } else {
                        Log.e("ERROR", e.toString())
                        trySend(Pair("-1", Person())) // TODO: Check correctness
                    }
                }
        }
        awaitClose {
            if (listener != null) {
                listener.remove()
            }
        }
    }

    // People
    fun getPeople(): Flow<List<Pair<String, Person>>> = callbackFlow {
        val listener = db.collection("people")
            .addSnapshotListener { r, e ->
                if (r != null) {
                    val l = mutableListOf<Pair<String, Person>>()

                    for (obj in r) {
                        val id = obj.id
                        val name = obj.getString("name") ?: ""
                        val surname = obj.getString("surname") ?: ""
                        val email = obj.getString("email") ?: ""
                        val username = obj.getString("username") ?: ""
                        val loginMethod = obj.getString("loginMethod") ?: ""
                        val image = obj.getString("image") ?: ""
                        val emailVerified = obj.getBoolean("emailVerified") ?: false
                        val location = obj.getString("location") ?: ""
                        val bio = obj.getString("bio") ?: ""
                        val tasks = obj.get("tasks") as List<String>
                        val teams = obj.get("teams") as List<String>

                        val person = Person(
                            name, surname, email, username,
                            loginMethod, image, emailVerified,
                            location, bio, teams, tasks
                        )

                        l.add(Pair(id, person))
                    }

                    trySend(l)
                } else {
                    Log.e("ERROR", e.toString())
                    trySend(emptyList())
                }
            }
        awaitClose { listener.remove() }
    }

    // Tasks
    fun getTasks(): Flow<List<Pair<String, Task>>> = callbackFlow {
        val listener = auth.currentUser?.uid?.let {
            db.collection("tasks")
                .whereArrayContains("people", it)
                .addSnapshotListener { r, e ->
                    if (r != null) {
                        val l = mutableListOf<Pair<String, Task>>()

                        for (obj in r) {
                            val id = obj.id
                            val teamId = obj.getString("teamId") ?: "Toy team 1"
                            val title = obj.getString("title") ?: ""
                            val description = obj.getString("description") ?: ""
                            val creatorId = obj.getString("creatorId") ?: ""
                            val creationDate = obj.getString("creationDate") ?: currentDateTime
                            val deadline = obj.getString("deadline") ?: currentDateTime
                            val prioritized = obj.getBoolean("prioritized") ?: false
                            val status = obj.getString("status") ?: ""
                            val tags = obj.get("tags") as List<String>
                            val recurrence = obj.getString("recurrence") ?: ""
                            val people = obj.get("people") as List<String>

                            val task = Task(
                                teamId as String, title, description, creatorId,
                                creationDate, deadline, prioritized, status,
                                tags, recurrence, people
                            )

                            l.add(Pair(id, task))
                        }

                        trySend(l)
                    } else {
                        Log.e("ERROR", e.toString())
                        trySend(emptyList())
                    }
                }
        }
        awaitClose {
            if (listener != null) {
                listener.remove()
            }
        }
    }


    // Comments
    fun getComments(): Flow<List<Pair<String, Comment>>> = callbackFlow {
        // TODO: Fetch tasks for logged-in user
        // TODO: Filter comments according to taskId
        val listener = db.collection("comments")
            .addSnapshotListener { r, e ->
                if (r != null) {
                    val l = mutableListOf<Pair<String, Comment>>()

                    for (obj in r) {
                        val id = obj.id
                        val taskId = obj.getString("taskId") ?: "Toy task 1"
                        val senderId = obj.getString("senderId") ?: "Toy person 1"
                        val timestamp = obj.getString("timestamp") ?: currentDateTime
                        val body = obj.getString("body") ?: ""
                        val media = obj.getString("media") ?: ""
                        val repliesAllowed = obj.getBoolean("repliesAllowed") ?: false
                        val replies = obj.get("replies") as List<String>

                        val comment = Comment(
                            taskId, senderId, timestamp, body,
                            media, repliesAllowed, replies
                        )

                        l.add(Pair(id, comment))
                    }

                    trySend(l)
                } else {
                    Log.e("ERROR", e.toString())
                    trySend(emptyList())
                }
            }
        awaitClose { listener.remove() }
    }

    // Private messages
    fun getPrivateMessages(): Flow<List<Pair<String, PrivateMessage>>> = callbackFlow {
        val listener = db.collection("private_messages")
            .where(
                Filter.or(
                    Filter.equalTo("senderId", auth.currentUser?.uid),
                    Filter.equalTo("receiverId", auth.currentUser?.uid)
                )
            )
            .addSnapshotListener { r, e ->
                if (r != null) {
                    val l = mutableListOf<Pair<String, PrivateMessage>>()

                    for (obj in r) {
                        val id = obj.id
                        val senderId = obj.getString("senderId") ?: "Toy person 1"
                        val receiverId = obj.getString("receiverId") ?: "Toy person 2"
                        val timestamp = obj.getString("timestamp") ?: currentDateTime
                        val body = obj.getString("body") ?: ""
                        val media = obj.getString("media") ?: ""
                        val read = obj.getBoolean("read") ?: false

                        val pm = PrivateMessage(
                            senderId, receiverId, timestamp,
                            body, media, read
                        )

                        l.add(Pair(id, pm))
                    }

                    trySend(l)
                } else {
                    Log.e("ERROR", e.toString())
                    trySend(emptyList())
                }
            }
        awaitClose { listener.remove() }
    }

    // Task replies
    fun getTaskReplies(): Flow<List<Pair<String, TaskReply>>> = callbackFlow {
        val listener = db.collection("task_replies")
            // TODO: Same filters of Comment
            .addSnapshotListener { r, e ->
                if (r != null) {
                    val l = mutableListOf<Pair<String, TaskReply>>()

                    for (obj in r) {
                        val id = obj.id
                        val commentId = obj.getString("commentId") ?: "Toy comment 1"
                        val senderId = obj.getString("senderId") ?: "Toy person 1"
                        val timestamp = obj.getString("timestamp") ?: currentDateTime
                        val body = obj.getString("body") ?: ""
                        val media = obj.getString("media") ?: ""

                        val tr = TaskReply(commentId, senderId, timestamp, body, media)

                        l.add(Pair(id, tr))
                    }

                    trySend(l)
                } else {
                    Log.e("ERROR", e.toString())
                    trySend(emptyList())
                }
            }
        awaitClose { listener.remove() }
    }

    // Teams
    fun getTeams(): Flow<List<Pair<String, Team>>> = callbackFlow {
        val listener = auth.currentUser?.uid?.let {
            db.collection("teams")
                .whereArrayContains("members", it)
                .addSnapshotListener { r, e ->
                    if (r != null) {
                        val l = mutableListOf<Pair<String, Team>>()

                        for (obj in r) {
                            val id = obj.id
                            val name = obj.getString("name") ?: ""
                            val image = obj.getString("image") ?: ""
                            val ownerId = obj.getString("ownerId") ?: "Toy person 1"
                            val admins = obj.get("admins") as List<String>
                            val inviteLink = obj.getString("inviteLink") ?: ""
                            val creationDate = obj.getString("creationDate") ?: ""
                            val category = obj.getString("category") ?: ""
                            val members = obj.get("members") as List<String>
                            val tasks = obj.get("tasks") as List<String>

                            val team = Team(
                                name, image, ownerId, admins, inviteLink,
                                creationDate, category, members, tasks
                            )

                            l.add(Pair(id, team))
                        }

                        trySend(l)
                    } else {
                        Log.e("ERROR", e.toString())
                        trySend(emptyList())
                    }
                }
        }
        awaitClose {
            if (listener != null) {
                listener.remove()
            }
        }
    }

    // Notifications
    fun getNotifications(): Flow<List<Pair<String, Notification>>> = callbackFlow {
        val listener = auth.currentUser?.uid?.let {
            db.collection("notifications")
                .whereArrayContains("receivers", it)
                .addSnapshotListener { r, e ->
                    if (r != null) {
                        val l = mutableListOf<Pair<String, Notification>>()

                        for (obj in r) {
                            val id = obj.id
                            val senderId = obj.getString("senderId") ?: "Toy person 1"
                            val taskId = obj.getString("taskId") ?: "Toy task 1"
                            val body = obj.getString("body") ?: ""
                            val timestamp = obj.getString("timestamp") ?: currentDateTime
                            val typology = obj.getLong("typology") ?: 0L
                            val teamId = obj.getString("teamId") ?: ""
                            val fromGroup = obj.getBoolean("fromGroup") ?: false
                            val receivers = obj.get("receivers") as List<String>

                            val n = Notification(
                                senderId, taskId, body, timestamp,
                                typology, teamId, fromGroup, receivers
                            )

                            l.add(Pair(id, n))
                        }

                        trySend(l)
                    } else {
                        Log.e("ERROR", e.toString())
                        trySend(emptyList())
                    }
                }
        }
        awaitClose {
            if (listener != null) {
                listener.remove()
            }
        }
    }

    // Team messages
    fun getTeamMessages(): Flow<List<Pair<String, TeamMessage>>> = callbackFlow {
        val listener = db.collection("team_messages")
            // TODO: teamId in people.teams
            .addSnapshotListener { r, e ->
                if (r != null) {
                    val l = mutableListOf<Pair<String, TeamMessage>>()

                    for (obj in r) {
                        val id = obj.id
                        val teamId = obj.getString("teamId") ?: "Toy team 1"
                        val senderId = obj.getString("senderId") ?: "Toy person 1"
                        val timestamp = obj.getString("timestamp") ?: currentDateTime
                        val body = obj.getString("body") ?: ""
                        val media = obj.getString("media") ?: ""

                        val tm = TeamMessage(teamId, senderId, timestamp, body, media)

                        l.add(Pair(id, tm))
                    }

                    trySend(l)
                } else {
                    Log.e("ERROR", e.toString())
                    trySend(emptyList())
                }
            }
        awaitClose { listener.remove() }
    }

    // Team participants
    fun getTeamParticipants(): Flow<List<TeamParticipant>> = callbackFlow {
        val listener = db.collection("team_participants")
            .where(Filter.equalTo("personId", auth.currentUser?.uid))
            .addSnapshotListener { r, e ->
                if (r != null) {
                    val tps = r.toObjects(TeamParticipant::class.java)
                    trySend(tps)
                } else {
                    Log.e("ERROR", e.toString())
                    trySend(emptyList())
                }
            }
        awaitClose { listener.remove() }
    }

    // Team participants
    fun getRealTeamParticipants(): Flow<List<TeamParticipant>> = callbackFlow {
        val listener = db.collection("team_participants")
            .addSnapshotListener { r, e ->
                if (r != null) {
                    val tps = r.toObjects(TeamParticipant::class.java)
                    trySend(tps)
                } else {
                    Log.e("ERROR", e.toString())
                    trySend(emptyList())
                }
            }
        awaitClose { listener.remove() }
    }

    // User notifications
    fun getUserNotifications(): Flow<List<UserNotification>> = callbackFlow {
        val listener = db.collection("user_notifications")
            .where(Filter.equalTo("userId", auth.currentUser?.uid))
            .addSnapshotListener { r, e ->
                if (r != null) {
                    val uns = r.toObjects(UserNotification::class.java)
                    trySend(uns)
                } else {
                    Log.e("ERROR", e.toString())
                    trySend(emptyList())
                }
            }
        awaitClose { listener.remove() }
    }

    // --- Tasks per team ---
    fun getTeamTasks(teamId: String): Flow<List<Pair<String, Task>>> = callbackFlow {
        val listener = db.collection("teams").document(teamId)
            .collection("tasks")
            .addSnapshotListener { r, e ->
                if (r != null) {
                    val tasks = mutableListOf<Pair<String, Task>>()
                    val taskIds = r.toObjects(String::class.java)
                    taskIds.forEach { id ->
                        val task = db.collection("tasks").document(id)
                            .get().result.toObject(Task::class.java)
                        if (task != null) tasks.add(Pair(id, task))
                    }
                    trySend(tasks)
                } else {
                    Log.e("ERROR", e.toString())
                    trySend(emptyList())
                }
            }
        awaitClose { listener.remove() }
    }

    // People by teamId
    fun getPeopleByTeamId(teamId: String): Flow<List<Pair<String, TeamMemberInfo>>> = callbackFlow {
        val listener = db.collection("people")
            .where(Filter.arrayContains("teams", teamId))
            .addSnapshotListener { r, e ->
                if (r != null) {
                    val l = mutableListOf<Pair<String, TeamMemberInfo>>()

                    for (obj in r) {
                        val id = obj.id
                        val name = obj.getString("name") ?: ""
                        val surname = obj.getString("surname") ?: ""
                        val username = obj.getString("username") ?: ""
                        val image = obj.getString("image") ?: ""

                        val person = TeamMemberInfo(
                            id, username, name, surname, image
                        )

                        l.add(Pair(id, person))
                    }

                    trySend(l)
                } else {
                    Log.e("ERROR_MEMBER_TAG", e.toString())
                    trySend(emptyList())
                }
            }
        awaitClose { listener.remove() }
    }

    // Private messages given a personId
    fun getPrivateMessagesById(personId: String): Flow<List<Pair<String, PrivateMessage>>> =
        callbackFlow {
            val listener = db.collection("private_messages")
                .where(
                    Filter.or(
                        Filter.or(
                            Filter.equalTo("senderId", personId),
                            Filter.equalTo("receiverId", auth.currentUser?.uid)
                        ),
                        Filter.or(
                            Filter.equalTo("senderId", auth.currentUser?.uid),
                            Filter.equalTo("receiverId", personId)
                        ),
                    )
                )
                .addSnapshotListener { r, e ->
                    if (r != null) {
                        val l = mutableListOf<Pair<String, PrivateMessage>>()

                        for (obj in r) {
                            val id = obj.id
                            val senderId = obj.getString("senderId") ?: "Toy person 1"
                            val receiverId = obj.getString("receiverId") ?: "Toy person 2"
                            val timestamp = obj.getString("timestamp") ?: currentDateTime
                            val body = obj.getString("body") ?: ""
                            val media = obj.getString("media") ?: ""
                            val read = obj.getBoolean("read") ?: false

                            val pm = PrivateMessage(
                                senderId, receiverId, timestamp,
                                body, media, read
                            )

                            l.add(Pair(id, pm))
                        }

                        trySend(l)
                    } else {
                        Log.e("ERROR", e.toString())
                        trySend(emptyList())
                    }
                }
            awaitClose { listener.remove() }
        }

    // Team messages given a teamId
    fun getTeamMessagesByTeamId(chatId: String): Flow<List<Pair<String, TeamMessage>>> =
        callbackFlow {
            val listener = db.collection("team_messages")
                .where(Filter.equalTo("teamId", chatId))
                // TODO: teamId in people.teams
                .addSnapshotListener { r, e ->
                    if (r != null) {
                        val l = mutableListOf<Pair<String, TeamMessage>>()

                        for (obj in r) {
                            val id = obj.id
                            val teamId = obj.getString("teamId") ?: "Toy team 1"
                            val senderId = obj.getString("senderId") ?: "Toy person 1"
                            val timestamp = obj.getString("timestamp") ?: currentDateTime
                            val body = obj.getString("body") ?: ""
                            val media = obj.getString("media") ?: ""

                            val tm = TeamMessage(teamId, senderId, timestamp, body, media)

                            l.add(Pair(id, tm))
                        }

                        trySend(l)
                    } else {
                        Log.e("ERROR", e.toString())
                        trySend(emptyList())
                    }
                }
            awaitClose { listener.remove() }
        }

    // --- Generators ---
    // People
    private fun generatePeople() {
        val people = listOf(
            Person(
                "Mario",
                "Rossi",
                "mario.rossi@email.com",
                "m_red",
                "email",
                "",
                true,
                "Turin, IT",
                "Hey, there! I am using TeamTask!",
                listOf("0L", "1L", "2L"),
                listOf("0L", "1L", "2L", "3L", "4L", "5L")
            ),
            Person(
                "Luca",
                "Bianchi",
                "luca.bianchi@email.com",
                "l_whites",
                "email",
                "",
                true,
                "Milan, IT",
                "Kotlin developer",
                listOf("0L", "1L", "3L"),
                listOf("0L", "1L", "2L", "3L", "4L", "6L")
            ),
            Person(
                "Veronica",
                "Verdi",
                "veronica.verdi@email.com",
                "ver_ver",
                "email",
                "",
                true,
                "Turin, IT",
                "Ver",
                listOf("1L", "2L", "3L"),
                listOf("1L", "4L", "5L", "6L")
            ),
            Person(
                "Lucia",
                "Neri",
                "lucia.neri@email.com",
                "lucia_n",
                "google",
                "",
                false,
                "Venice, IT",
                "Hey, there! I am proudly using TeamTask!",
                listOf("0L", "2L"),
                listOf("0L", "2L", "3L", "5L")
            ),
            Person(
                "Marco",
                "Masini",
                "marco_masini@email.com",
                "mmasini",
                "google",
                "",
                false,
                "Florence, IT",
                "Hey, there! I am proudly using TeamTask!",
                emptyList(),
                emptyList()
            ),
            Person(
                "Valerio",
                "Sartori",
                "v.sartori@email.com",
                "sartov",
                "email",
                "",
                true,
                "Paris, FR",
                "Hey, there! I am proudly using TeamTask!",
                listOf("3L", "4L"),
                listOf("6L", "7L", "8L")
            ),
            Person(
                "Pamela",
                "Smith",
                "pamelas@email.com",
                "pamsmith",
                "email",
                "",
                true,
                "London, GB",
                "Hey, there! I am proudly using TeamTask!",
                listOf("4L", "5L"),
                listOf("7L", "8L", "9L")
            ),
            Person(
                "Silvia",
                "Giacomazzi",
                "silgia@email.com",
                "silgia",
                "email",
                "",
                false,
                "Turin, IT",
                "Hey, there! I am proudly using TeamTask!",
                listOf("4L", "5L"),
                listOf("7L", "8L", "9L")
            ),
            Person(
                "Angela",
                "Filippelli",
                "anfilip@email.com",
                "anfilip",
                "google",
                "",
                false,
                "Milan, IT",
                "Hey, there! I am proudly using TeamTask!",
                listOf("4L", "5L"),
                listOf("7L", "8L", "9L")
            ),
            Person(
                "Oscar",
                "Montinaro",
                "oscar.montinaro@email.com",
                "montino",
                "google",
                "",
                true,
                "Rome, IT",
                "Hey, there! I am proudly using TeamTask!",
                listOf("5L"),
                listOf("9L")
            )
        )
        people.forEach { p -> db.collection("people").add(p) }
    }


    //ADD FIRST USER WITH ID USERID
    fun generatePersonWithUserId() {
        val person = Person(
            "Valerio",
            "Sartori",
            "v.sartori@email.com",
            "sartov",
            "email",
            "boy_1.jpg",
            true,
            "Paris, FR",
            "Hey, there! I am proudly using TeamTask!",
            listOf("ALx2Fsv1mFmK9idVgh8s"),
            listOf("9dA7vp3d8M0lajgLjL2q")
        )
        try {
            auth.currentUser?.uid?.let { db.collection("people").document(it).set(person) }
        } catch (e: Exception) {

        }
    }


    // Tasks
    private fun generateTasks() {
        val tasks = listOf(
            Task(
                "0L",
                "Task name 1",
                "Task description",
                "0L",
                "2024-04-30T19:41:12+00:00",
                "2024-05-30T19:41:12+00:00",
                true,
                "Completed",
                listOf("tag1", "tag2"),
                "None",
                listOf("0L", "1L", "3L")
            ),
            Task(
                "1L",
                "Task name 1",
                "Task description",
                "0L",
                "2024-04-27T19:40:12+00:00",
                "2024-04-30T19:41:12+00:00",
                false,
                "Expired",
                listOf("tag3", "tag4"),
                "Weekly",
                listOf("0L", "1L", "2L")
            ),
            Task(
                "0L",
                "Task name 3",
                "Task description",
                "0L",
                "2024-04-30T19:40:12+00:00",
                "2024-05-02T13:27:08+00:00",
                false,
                "Completed",
                listOf("tag1", "tag3"),
                "None",
                listOf("0L", "1L", "3L")
            ),
            Task(
                "0L",
                "Task name 4",
                "Task description",
                "0L",
                "2024-05-01T19:41:12+00:00",
                "2024-05-12T14:11:10+00:00",
                false,
                "Completed",
                listOf("tag4", "tag2"),
                "None",
                listOf("0L", "1L", "3L")
            ),
            Task(
                "1L",
                "Task name 5",
                "Task description",
                "1L",
                "2024-05-01T19:42:00+00:00",
                "2024-05-12T11:15:33+00:00",
                true,
                "Expired",
                listOf("tag3"),
                "Monthly",
                listOf("0L", "1L", "2L")
            ),
            Task(
                "2L",
                "Task name 6",
                "Task description",
                "2L",
                "2024-05-02T20:11:33+00:00",
                "2024-05-13T12:40:44+00:00",
                true,
                "Expired",
                listOf("tag1", "tag2"),
                "None",
                listOf("0L", "2L", "3L")
            ),
            Task(
                "3L",
                "Task name 7",
                "Task description",
                "2L",
                "2024-05-03T19:41:12+00:00",
                "2024-05-20T15:33:12+00:00",
                false,
                "Scheduled",
                listOf("tag1", "tag2"),
                "None",
                listOf("1L", "2L", "5L")
            ),
            Task(
                "4L",
                "Task name 8",
                "Task description",
                "7L",
                "2024-05-04T13:21:00+00:00",
                "2024-05-22T12:27:12+00:00",
                false,
                "Scheduled",
                listOf("tag1", "tag2"),
                "None",
                listOf("5L", "6L", "7L", "8L")
            ),
            Task(
                "4L",
                "Task name 9",
                "Task description",
                "7L",
                "2024-05-05T14:41:22+00:00",
                "2024-05-30T15:41:12+00:00",
                true,
                "Scheduled",
                listOf("tag1", "tag3"),
                "None",
                listOf("5L", "6L", "7L", "8L")
            ),
            Task(
                "5L",
                "Task name 10",
                "Task description",
                "9L",
                "2024-05-06T14:31:12+00:00",
                "2024-05-30T18:41:12+00:00",
                false,
                "Scheduled",
                listOf("tag1", "tag4"),
                "None",
                listOf("6L", "7L", "8L", "9L")
            )
        )
        tasks.forEach { t -> db.collection("tasks").add(t) }
    }

    // Comments
    private fun generateComments() {
        val comments = listOf(
            Comment(
                "1L",
                "2L",
                "2024-04-30T19:41:12+00:00",
                "This is a comment",
                null,
                true,
                emptyList()
            ),
            Comment(
                "1L",
                "1L",
                "2024-04-30T19:41:15+00:00",
                "This is another comment",
                null,
                true,
                emptyList()
            ),
            Comment(
                "2L",
                "3L",
                "2024-04-28T19:41:00+00:00",
                "This is a comment",
                null,
                false,
                emptyList()
            ),
            Comment(
                "2L",
                "1L",
                "2024-04-29T13:18:12+00:00",
                "This is another comment",
                null,
                true,
                emptyList()
            ),
            Comment(
                "0L",
                "1L",
                "2024-04-30T19:41:12+00:00",
                "This is a comment",
                null,
                true,
                emptyList()
            ),
            Comment(
                "0L",
                "1L",
                "2024-04-30T20:00:12+00:00",
                "This is another comment",
                null,
                true,
                emptyList()
            ),
            Comment(
                "7L",
                "5L",
                "2024-04-30T18:17:54+00:00",
                "This is a comment",
                null,
                true,
                emptyList()
            )
        )
        comments.forEach { c -> db.collection("comments").add(c) }
    }

    // Private messages
    private fun generatePrivateMessages() {
        val messages = listOf(
            PrivateMessage(
                "0L",
                "2L",
                "2024-04-30T19:41:12+00:00",
                "This is a private message",
                null,
                true
            ),
            PrivateMessage(
                "2L",
                "0L",
                "2024-05-02T19:43:23+00:00",
                "This is a reply to a private message",
                null,
                true
            ),
            PrivateMessage(
                "1L",
                "2L",
                "2024-05-02T19:28:32+00:00",
                "This is a private message",
                null,
                false
            ),
            PrivateMessage(
                "0L",
                "1L",
                "2024-05-03T19:45:45+00:00",
                "This is another private message",
                null,
                true
            ),
            PrivateMessage(
                "2L",
                "1L",
                "2024-05-03T19:41:27+00:00",
                "This is a private message",
                null,
                true
            ),
            PrivateMessage(
                "1L",
                "0L",
                "2024-05-04T16:33:28+00:00",
                "This is a private message",
                null,
                true
            ),
            PrivateMessage(
                "0L",
                "2L",
                "2024-05-04T18:41:55+00:00",
                "This is another private message",
                null,
                true
            ),
            PrivateMessage(
                "2L",
                "0L",
                "2024-05-05T19:45:33+00:00",
                "This is another private message",
                null,
                true
            ),
            PrivateMessage(
                "1L",
                "0L",
                "2024-05-05T19:45:35+00:00",
                "This is a private message",
                null,
                true
            ),
            PrivateMessage(
                "0L",
                "1L",
                "2024-05-05T19:51:12+00:00",
                "This is another private message",
                null,
                true
            )
        )
        messages.forEach { m -> db.collection("private_messages").add(m) }
    }

    // Task replies
    private fun generateTaskReplies() {
        val trs = listOf(
            TaskReply("0L", "1L", "2024-05-12T19:26:00+00:00", "This is a reply", null),
            TaskReply("1L", "2L", "2024-04-30T19:41:12+00:00", "This is a reply", null),
            TaskReply("1L", "2L", "2024-05-12T19:43:00+00:00", "This is another reply", null),
            TaskReply("3L", "3L", "2024-05-14T19:50:37+00:00", "This is another reply", null)
        )
        trs.forEach { tr -> db.collection("task_replies").add(tr) }
    }

    // Teams
    private fun generateTeams() {
        val teams = listOf(
            Team(
                "Team name 1",
                "",
                "0L",
                listOf("1L"),
                "https://teamtask.com/invite/0",
                "2024-04-30T19:41:12+00:00",
                "Work",
                listOf("0L", "1L", "3L"),
                listOf("0L", "2L", "3L")
            ),
            Team(
                "Team name 2",
                "",
                "0L",
                listOf("2L"),
                "https://teamtask.com/invite/1",
                "2024-04-30T19:41:12+00:00",
                "Study",
                listOf("0L", "1L", "2L"),
                listOf("1L", "4L")
            ),
            Team(
                "Team name 3",
                "",
                "0L",
                listOf(),
                "https://teamtask.com/invite/2",
                "2024-04-30T19:41:12+00:00",
                "Sport",
                listOf("0L", "2L", "3L"),
                listOf("5L")
            ),
            Team(
                "Team name 4",
                "",
                "1L",
                listOf("5L"),
                "https://teamtask.com/invite/3",
                "2024-04-30T19:41:12+00:00",
                "Hobby",
                listOf("1L", "2L", "5L"),
                listOf("6L")
            ),
            Team(
                "Team name 5",
                "",
                "5L",
                listOf("6L"),
                "https://teamtask.com/invite/4",
                "2024-04-30T19:41:12+00:00",
                "Work",
                listOf("5L", "6L", "7L", "8L"),
                listOf("7L", "8L")
            ),
            Team(
                "Team name 6",
                "",
                "6L",
                listOf("5L", "6L"),
                "https://teamtask.com/invite/5",
                "2024-04-30T19:41:12+00:00",
                "Study",
                listOf("6L", "7L", "8L", "9L"),
                listOf("9L")
            ),
            Team(
                "Team name 7",
                "",
                "1L",
                emptyList(),
                "https://teamtask.com/invite/6",
                "2024-04-30T19:41:12+00:00",
                "Sport",
                listOf("1L"),
                emptyList()
            ),
            Team(
                "Team name 8",
                "",
                "1L",
                emptyList(),
                "https://teamtask.com/invite/7",
                "2024-04-30T19:41:12+00:00",
                "Hobby",
                listOf("1L"),
                emptyList()
            )
        )
        teams.forEach { t -> db.collection("teams").add(t) }
    }

    // Notifications
    private fun generateNotifications() {
        val notifications = listOf(
            Notification(
                "1L",
                "1L",
                "You have a new task",
                "2024-04-30T19:41:12+00:00",
                1L,
                "",
                false,
                listOf("0L", "2L")
            ),
            Notification(
                "2L",
                "1L",
                "*Marco* posted in *Task name*",
                "2024-04-30T18:27:12+00:00",
                2L,
                "",
                false,
                listOf("0L", "1L")
            ),
            Notification(
                "0L",
                "2L",
                "*Marco* replied to your comment in *Task name*",
                "2024-05-01T19:16:33+00:00",
                3L,
                "",
                true,
                listOf("3L")
            ),
            Notification(
                "0L",
                "4L",
                "*Task name* has been completed successfully",
                "2024-05-05T12:17:00+00:00",
                4L,
                "",
                true,
                listOf("1L", "2L")
            ),
            Notification(
                "1L",
                "6L",
                "Oops! *Task name* expired",
                "2024-05-05T19:22:12+00:00",
                5L,
                "",
                true,
                listOf("3L", "5L")
            ),
            Notification(
                "2L",
                "5L",
                "*Task name* has been completed successfully",
                "2024-05-11T16:23:00+00:00",
                4L,
                "",
                false,
                listOf("0L", "3L")
            ),
            Notification(
                "0L",
                "3L",
                "*Task name* has been completed successfully",
                "2024-05-11T19:41:12+00:00",
                4L,
                "",
                true,
                listOf("1L", "3L")
            ),
            Notification(
                "1L",
                "3L",
                "You have a new task",
                "2024-04-13T19:00:12+00:00",
                1L,
                "",
                false,
                listOf("0L", "3L")
            ),
            Notification(
                "2L",
                "4L",
                "Oops! *Task name* expired",
                "2024-05-12T12:41:12+00:00",
                5L,
                "",
                false,
                listOf("0L", "1L")
            ),
            Notification(
                "0L",
                "2L",
                "*Marco* replied to your comment in *Task name*",
                "2024-05-14T14:33:00+00:00",
                3L,
                "",
                true,
                listOf("3L")
            )
        )
        notifications.forEach { n -> db.collection("notifications").add(n) }
    }

    // Team messages
    private fun generateTeamMessages() {
        val messages = listOf(
            TeamMessage("0L", "1L", "2024-05-02T12:18:12+00:00", "This is a team message", null),
            TeamMessage("1L", "2L", "2024-05-03T12:30:00+00:00", "This is a team message", null),
            TeamMessage(
                "0L",
                "2L",
                "2024-05-02T12:25:07+00:00",
                "This is another team message",
                null
            ),
            TeamMessage("2L", "0L", "2024-05-04T14:18:00+00:00", "This is a team message", null),
            TeamMessage(
                "1L",
                "0L",
                "2024-05-05T11:14:00+00:00",
                "This is another team message",
                null
            ),
            TeamMessage(
                "2L",
                "1L",
                "2024-05-06T12:18:12+00:00",
                "This is another team message",
                null
            ),
            TeamMessage("0L", "1L", "2024-05-06T13:18:12+00:00", "This is a team message", null),
            TeamMessage(
                "1L",
                "2L",
                "2024-05-06T13:22:99+00:00",
                "This is another team message",
                null
            ),
            TeamMessage(
                "0L",
                "2L",
                "2024-05-09T12:18:12+00:00",
                "This is another team message",
                null
            ),
            TeamMessage("2L", "0L", "2024-05-09T12:10:00+00:00", "This is a team message", null)
        )
        messages.forEach { m -> db.collection("team_messages").add(m) }
    }

    // Team participants
    private fun generateTeamParticipants() {
        val tps = listOf(
            TeamParticipant("0L", "0L", true, "Admin", 2L, 3L),
            TeamParticipant("0L", "1L", false, "Member", 1L, 3L),
            TeamParticipant("1L", "2L", true, "Admin", 3L, 3L),
            TeamParticipant("1L", "3L", false, "Member", 0L, 3L),
            TeamParticipant("2L", "0L", true, "Admin", 1L, 3L),
            TeamParticipant("2L", "2L", false, "Member", 2L, 3L),
            TeamParticipant("3L", "3L", true, "Admin", 0L, 3L),
            TeamParticipant("3L", "0L", false, "Member", 3L, 3L),
            TeamParticipant("4L", "4L", true, "Member", 2L, 3L),
            TeamParticipant("4L", "5L", false, "Member", 1L, 3L)
        )
        tps.forEach { tp -> db.collection("team_participants").add(tp) }
    }

    // User notifications
    fun generateUserNotifications() {
        val uns = listOf(
            UserNotification("1L", "3L", false),
            UserNotification("2L", "3L", false),
            UserNotification("0L", "3L", true),
            UserNotification("1L", "1L", true),
            UserNotification("0L", "2L", true),
            UserNotification("2L", "0L", false),
            UserNotification("0L", "1L", true),
            UserNotification("1L", "0L", true),
            UserNotification("0L", "4L", false),
            UserNotification("2L", "5L", false)
        )
        uns.forEach { un -> db.collection("user_notifications").add(un) }
    }

    fun generateData() {
        generatePeople()
        generateTasks()
        generateComments()
        generatePrivateMessages()
        generateTaskReplies()
        generateTeams()
        generateNotifications()
        generateTeamMessages()
        generateTeamParticipants()
        generateUserNotifications()
    }

    //OTHER POST REQUESTS:
    //Send message
    fun addChatMessage(message: ClientMessage, isGroupChat: Boolean, chatId: String) {
        var newDocument: DocumentReference? = null
        var filesRef: List<String>? = null

        try {
            CoroutineScope(Dispatchers.IO).launch {
                if (isGroupChat) {
                    val messageToSend = auth.currentUser?.uid?.let {
                        TeamMessage(
                            chatId,
                            it,
                            message.date.format(DateTimeFormatter.ISO_DATE_TIME),
                            message.body.text,
                            null,
                        )
                    }
                    newDocument = messageToSend?.let { db.collection("team_messages").add(it).await() }
                } else {
                    val messageToSend = auth.currentUser?.uid?.let {
                        PrivateMessage(
                            it,
                            chatId,
                            message.date.format(DateTimeFormatter.ISO_DATE_TIME),
                            message.body.text,
                            null,
                            false
                        )
                    }
                    newDocument = messageToSend?.let { db.collection("private_messages").add(it).await() }
                }

                //upload files to firebase storage and eventually update the message with the media if they correctly been uploaded
                if (message.files != null) {
                    try {
                        filesRef =
                            auth.currentUser?.uid?.let {
                                uploadFilesToFirebaseStorage(
                                    message.files.map { it.firebaseUri },
                                    it,
                                    chatId,
                                    applicationContext
                                )
                            }

                        if (isGroupChat)
                            db.collection("team_messages").document(newDocument?.id ?: "")
                                .update("media", Gson().toJson(filesRef))
                        else
                            db.collection("private_messages").document(newDocument?.id ?: "")
                                .update("media", Gson().toJson(filesRef))
                    } catch (e: Exception) {
                        Log.e("FILE UPLOADING ERROR: ", e.toString())
                    }
                }
            }
        } catch (e: Exception) {
            //toast showed into uploadFilesToFirebaseStorage
            Log.e("FILE UPLOADING ERROR: ", e.toString())
        }
    }

    fun deleteMessageById(message: ClientMessage, isGroupChat: Boolean): Boolean {
        if (isGroupChat) {
            db.collection("team_messages").document(message.id).get().addOnSuccessListener {
                val messageToDelete = it.toObject(TeamMessage::class.java)
                if (messageToDelete != null) {
                    if (messageToDelete.senderId == auth.currentUser?.uid) {
                        db.collection("team_messages").document(message.id).delete()
                    }
                }
            }
        } else {
            db.collection("private_messages").document(message.id).get().addOnSuccessListener {
                val messageToDelete = it.toObject(PrivateMessage::class.java)
                if (messageToDelete != null) {
                    if (messageToDelete.senderId == auth.currentUser?.uid) {
                        db.collection("private_messages").document(message.id).delete()
                    }
                }
            }
        }
        return true
    }

    fun createTeam(teamName: String, teamCategory: String, imageUri: Uri?): Boolean {
        val newTeam = auth.currentUser?.let {
            Team(
                name = teamName,
                "",
                ownerId = it.uid,
                admins = emptyList(),
                "https://teamtask.com/invite/0",
                creationDate = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
                category = teamCategory,
                members = listOf(auth.currentUser!!.uid),
                tasks = emptyList()
            )
        }
        try {
            CoroutineScope(Dispatchers.IO).launch {
                //create team
                val team = newTeam?.let { db.collection("teams").add(it).await() }
                //add team partecipant logged user id as owner
                if (team != null) {
                    auth.currentUser?.uid?.let {
                        TeamParticipant(
                            team.id,
                            it,
                            false,
                            "Owner",
                            0L,
                            0L
                        )
                    }?.let {
                        db.collection("team_participants").add(
                            it
                        )
                    }
                }

                //add team to userId
                if (team != null) {
                    auth.currentUser?.uid?.let { db.collection("people").document(it).update("teams", FieldValue.arrayUnion(team.id)) }
                }

                //upload image if present
                if (imageUri != null) {
                    val imageRef = team?.let { uploadTeamImage(imageUri, it.id, applicationContext) }
                    if (team != null) {
                        db.collection("teams").document(team.id).update("image", imageRef)
                    }
                }
            }
            return true
        } catch (e: Exception) {
            Toast.makeText(applicationContext, e.toString(), Toast.LENGTH_SHORT).show()
            return false
        }
    }
}


class AppFactory(
    context: Context
) : ViewModelProvider.Factory {
    val model = (context.applicationContext as? TeamTask)?.model
        ?: throw IllegalArgumentException("Wrong application class")

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            // model.generateData()
            @Suppress("UNCHECKED_CAST")
            return AppViewModel(model) as T
        } else if (modelClass.isAssignableFrom(SingleChatViewModel::class.java)) {
            // model.generateData()
            @Suppress("UNCHECKED_CAST")
            return SingleChatViewModel(model) as T
        } else if (modelClass.isAssignableFrom(TeamsViewModel::class.java)) {
            // model.generateData()
            @Suppress("UNCHECKED_CAST")
            return TeamsViewModel(model) as T
        } else throw IllegalArgumentException("Unexpected ViewModel class")
    }
}


class AppViewModel(
    val appModel: AppModel
) : ViewModel() {
    // Login status
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> get() = _isLoggedIn

    private val _isSignUpFlow = MutableStateFlow(false)
    val isSignUpFlow: StateFlow<Boolean> get() = _isSignUpFlow

    private val _isAccountBeenDeleted = MutableStateFlow(false)
    val isAccountBeenDeleted: StateFlow<Boolean> get() = _isAccountBeenDeleted


    // Function to update login status
    fun updateLoginStatus(isLoggedIn: Boolean) {
        _isLoggedIn.value = isLoggedIn
    }
    // Function to update login status
    fun updateIsSignUpFlow(isSignUpFlow: Boolean) {
        _isSignUpFlow.value = isSignUpFlow
    }
    fun updateAccountBeenDeletedStatus(isAccountBeenDeleted: Boolean) {
        _isAccountBeenDeleted.value = isAccountBeenDeleted
    }


    fun getPersonal() = appModel.getPersonal()

    // People
    fun getPeople() = appModel.getPeople()

    // Tasks
    fun getTasks() = appModel.getTasks()

    // Comments
    fun getComments() = appModel.getComments()

    // Private messages
    fun getPrivateMessages() = appModel.getPrivateMessages()

    // Task replies
    fun getTaskReplies() = appModel.getTaskReplies()

    // Teams
    fun getTeams() = appModel.getTeams()

    // Notifications
    fun getNotifications() = appModel.getNotifications()

    // Team messages
    fun getTeamMessages() = appModel.getTeamMessages()

    // Team participants
    fun getTeamParticipants() = appModel.getTeamParticipants()
    fun getRealTeamParticipants() = appModel.getRealTeamParticipants()

    // User notifications
    fun getUserNotifications() = appModel.getUserNotifications()

    // Tasks per team
    fun getTeamTasks(teamId: String) = appModel.getTeamTasks(teamId.toString())
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AppMainScreen(
    email: String,
    signInWithGoogle: () -> Unit,
    signOut: () -> Unit,
    signUpWithGoogle: () -> Unit,
    saveLoginStatus: (Boolean) -> Unit,
    performPendingGoogleSignIn: (String) -> Unit,
    resetPendingGoogleSignInAccount: () -> Unit,
    signUpWithEmail: (String, String, String, String, String) -> Unit,
    signInWithEmail: (String, String) -> Unit,
    appVM: AppViewModel = viewModel(factory = AppFactory(LocalContext.current)),
    profileVM: ProfileFormViewModel = viewModel(),
    teamVM: SpecificTeamViewModel = viewModel()
) {
    val navController = rememberNavController()
    Actions.initialize(navController) // Initialize Actions here
    val typography = TeamTaskTypography

    val isLoggedIn by appVM.isLoggedIn.collectAsState()
    val isSignUpFlow by appVM.isSignUpFlow.collectAsState()
    val isAccountBeenDeleted by appVM.isAccountBeenDeleted.collectAsState()

    val auth = FirebaseAuth.getInstance()

    val personal by appVM.getPersonal().collectAsState(initial = Pair("", Person()))
    val people by appVM.getPeople().collectAsState(initial = listOf())
    val tasks by appVM.getTasks().collectAsState(initial = listOf())
    val comments by appVM.getComments().collectAsState(initial = listOf())
    val privateMessages by appVM.getPrivateMessages().collectAsState(initial = listOf())
    val taskReplies by appVM.getTaskReplies().collectAsState(initial = listOf())
    val teams by appVM.getTeams().collectAsState(initial = listOf())
    val notifications by appVM.getNotifications().collectAsState(initial = listOf())
    val teamMessages by appVM.getTeamMessages().collectAsState(initial = listOf())
    val teamParticipants by appVM.getTeamParticipants().collectAsState(initial = listOf())
    val realTeamParticipants by appVM.getRealTeamParticipants()
        .collectAsState(initial = listOf())
    val userNotifications by appVM.getUserNotifications().collectAsState(initial = listOf())

    // Get the list of team IDs the user is part of
    val teamIds = teams.map { it.first }

    Log.e("ppp", isLoggedIn.toString())

    Scaffold(
        topBar = {
            if(isLoggedIn) {
                auth.currentUser?.let {
                    TopBar(
                        navController,
                        it.uid,
                        profileVM::setShwDeleteAccountModal,
                        {profileVM.deleteAccount(signOut)},
                        profileVM::setShwLogoutModal,
                        profileVM.showMenu,
                        profileVM::setShowMen,
                        profileVM.showBackButtonModal,
                        profileVM::setBackButtModal,
                        profileVM::goBackToPresentation,
                        profileVM::editProfile,
                        profileVM::validate,
                        profileVM::cancelEditProfile,
                        people,
                        teams,
                        tasks
                    )
                }
            }
        },
        bottomBar = {
            BottomBar(
                navController,
                teamVM::goToPreviousStep,
                teamVM::onSearchQueryChanged, teamVM::validateCreateTask
            )
        },
        floatingActionButton = { FloatingButton(navController) }
    ) { it ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            val teamsVM: TeamsViewModel = viewModel(factory = AppFactory(LocalContext.current))

            NavHost(
                navController = navController,
                startDestination = if (!isLoggedIn) { "firstScreen" } else {"home"}
            ) {
                if (!isLoggedIn ) {
                    composable("firstScreen") {
                        if(isAccountBeenDeleted){
                            AlertDialog(
                                onDismissRequest = {
                                    appVM.updateAccountBeenDeletedStatus(false)
                                },
                                title = { Text(text = "Account Deleted") },
                                text = { Text(text = "Your account has been deleted successfully!") },
                                confirmButton = {
                                    Button(onClick = {
                                        appVM.updateAccountBeenDeletedStatus(false)
                                    }) {
                                        Text(
                                            text = "Yes",
                                            style = typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = CaribbeanCurrent
                                        )
                                    }
                                }
                            )
                        }
                        FirstScreen(
                            email = email,
                            signInWithGoogle = signInWithGoogle,
                            signUpWithGoogle = signUpWithGoogle,
                            isSignUpFlow = isSignUpFlow,
                            updateIsSignUpFlow = appVM::updateIsSignUpFlow,
                            saveLoginStatus = saveLoginStatus,
                            performPendingGoogleSignIn = performPendingGoogleSignIn,
                            resetPendingGoogleSignInAccount = resetPendingGoogleSignInAccount,
                            signUpWithEmail = signUpWithEmail,
                            signInWithEmail = signInWithEmail,
                            appViewModel = appVM
                        )
                    }
                } else {
                    composable("home") {
                        // Frequently accessed teams
                        val filteredTeams = teamParticipants
                            .filter { tp -> tp.frequentlyAccessed }
                            .flatMap { tp -> teams.filter { it.first == tp.teamId } }

                        // Tasks
                        val filteredTasks = personal.second.tasks
                            .flatMap { taskId -> tasks.filter { it.first == taskId } }


                        //Log.d("FilteredTasks", filteredTasks.toString())

                        HomeScreen(
                            filteredTeams, teams,
                            filteredTasks, tasks, Actions.getInstance().goToTaskComments
                        )
                    }

                    composable("homeCalendar") {
                        // Tasks
                        val filteredTasks = personal.second.tasks
                            .flatMap { taskId -> tasks.filter { it.first == taskId } }

                        val filteredTeams = teamParticipants
                            .filter { tp -> tp.frequentlyAccessed }
                            .flatMap { tp -> teams.filter { it.first == tp.teamId } }

                        CalendarWithEvents(filteredTasks, filteredTeams, teams)
                    }

//                composable("teams") {
//                    // Filtered teams
//                    val filteredTeams = personal.second.teams
//                        .mapNotNull { teamId -> teams.find { it.first == teamId } }
//
//                    TeamsScreen(
//                        teams,
//                        Actions.getInstance().goToTeamTasks
//                    )
//                }
//
//                composable("teams/filter") {
//                    CategoryFilterScreen()
//                }

                    composable("teams") {
                        TeamsScreen(
                            teams,
                            Actions.getInstance().goToTeamTasks, teamsVM
                        )
                    }

                    composable("teams/filter") {
                        CategoryFilterScreen(teamsVM)
                    }

                    composable("teams/{teamId}/tasksCalendar") {
                        // Tasks
                        val filteredTasks = personal.second.tasks
                            .flatMap { taskId -> tasks.filter { it.first == taskId } }

                        val filteredTeams = teamParticipants
                            .filter { tp -> tp.frequentlyAccessed }
                            .flatMap { tp -> teams.filter { it.first == tp.teamId } }

                        CalendarWithEvents(filteredTasks, filteredTeams, teams)
                    }

                    composable("teams/{teamId}/statistics") { TeamPerformances() }

                    composable("teams/newTeam/info") {
                        NewTeam(
                            isInCreation = true,
                            "0123"
                        )
                    } //TODO: HARDCODED TEAMID
                    composable("teams/newTeam/share") {
                        AddMemberToTeamScreen(
                            showSnackbar = true,
                            teamId = "0123",
                            teamName = "Team Prova"
                        )
                    } //TODO: HARDCODED TEAMID and teamName

                    composable("teams/{teamId}/tasks") { backStackEntry ->
                        val teamId = backStackEntry.arguments?.getString("teamId")

                        // Filter tasks that belong to the specified team
                        val filteredTasks = tasks.filter { it.second.teamId == teamId }

                        // Filter participants that belong to the specified team
                        val filteredTeamParticipant =
                            realTeamParticipants.filter { it.teamId == teamId }

                        // Get the team document from the teams collection
                        val teamDocument = teams.find { it.first == teamId }

                        // Determine the ownerId for the specified team
                        val ownerId = teamDocument?.second?.ownerId

                        // Retrieve the list of admins for the specified team
                        val admins = teamDocument?.second?.admins ?: emptyList()

                        // Filter team members and set the permission field accordingly
                        val filteredTeamMembers =
                            filteredTeamParticipant.mapNotNull { participant ->
                                people.find { person -> person.first == participant.personId }
                                    ?.let { person ->
                                        val permission = when {
                                            person.first == ownerId -> "Owner"
                                            admins.contains(person.first) -> "Admin"
                                            else -> ""
                                        }
                                        Pair(
                                            person.first,
                                            PersonData(
                                                personId = person.first,
                                                name = person.second.name,
                                                surname = person.second.surname,
                                                username = person.second.username,
                                                role = participant.role,  // Extract role from participant
                                                permission = permission  // Set permission based on ownerId and admins list
                                            )
                                        )
                                    }
                            }
                        if (teamId != null) {
                            SpecificTeamScreen(
                                teamId = teamId,
                                rawToDoTasks = filteredTasks,
                                rawTeamParticipants = filteredTeamMembers,
                                rawPeople = people,
                                vm = teamVM
                            )
                        }
                    }

                    composable("teams/{teamId}/description") {
                        NotImplementedScreen()
                    } // TODO: Implement
                    composable("teams/{teamId}/people") {
                        NotImplementedScreen()
                    } // TODO: Implement

                    composable("teams/{teamId}/filterTasks") { FilterTasksScreen() }
                    composable("teams/{teamId}/edit/status") { backStackEntry ->
                        val teamId = backStackEntry.arguments?.getString("teamId")
                        teamId?.let {
                            NewTeam(isInCreation = false, teamId = teamId)
                        }
                    }
                    composable("teams/{teamId}/edit/description") { NotImplementedScreen() } // TODO: Implement
                    composable("teams/{teamId}/edit/people") { backStackEntry ->
                        val teamId = backStackEntry.arguments?.getString("teamId")
                        val teamName = teams.find { it.first == teamId }?.second?.name
                        teamId?.let {
                            if (teamName != null) {
                                AddMemberToTeamScreen(
                                    showSnackbar = false,
                                    teamId = teamId,
                                    teamName = teamName
                                )
                            }
                        }
                    }

                    composable("teams/{teamId}/newTask/status") { backStackEntry ->
                        val teamId = backStackEntry.arguments?.getString("teamId")
                        teamId?.let {
                            NewTask(teamId)
                        }
                    }
                    composable("teams/{teamId}/newTask/description") { NotImplementedScreen() } // TODO: Implement
                    composable("teams/{teamId}/newTask/people") { NotImplementedScreen() } // TODO: Implement

                    composable("teams/{teamId}/tasks/{taskId}/comments") { backStackEntry ->
                        val teamId = backStackEntry.arguments?.getString("teamId")
                        val taskId = backStackEntry.arguments?.getString("taskId")

                        if (teamId != null) {
                            if (taskId != null) {
                                ShowTaskDetails(teamId, taskId)
                            }
                        }

                        //NotImplementedScreen()

                    } // TODO: Implement
                    composable("teams/{teamId}/tasks/{taskId}/comments/{commentId}") { NotImplementedScreen() } // TODO: Implement
                    composable("teams/{teamId}/tasks/{taskId}/info") { NotImplementedScreen() } // TODO: Implement
                    composable("teams/{teamId}/tasks/{taskId}/description") { NotImplementedScreen() } // TODO: Implement
                    composable("teams/{teamId}/tasks/{taskId}/people") { NotImplementedScreen() } // TODO: Implement

                    composable("teams/{teamId}/tasks/{taskId}/edit/info") { NotImplementedScreen() } // TODO: Implement
                    composable("teams/{teamId}/tasks/{taskId}/edit/description") { NotImplementedScreen() } // TODO: Implement
                    composable("teams/{teamId}/tasks/{taskId}/edit/people") { NotImplementedScreen() } // TODO: Implement

                    composable("chats") {
                        // Get the list of team IDs the user is part of
                        val teamIds = teams.map { it.first }

                        // Filter team messages based on team IDs
                        val filteredTeamMessages = teamMessages.filter { teamMessage ->
                            teamIds.contains(teamMessage.second.teamId)
                        }

                        // Create a list of chat messages
                        var combinedMessages = filteredTeamMessages.map {
                            ChatMessage.TeamChatMessage(
                                it.first,
                                it.second
                            )
                        } +
                                privateMessages.map {
                                    ChatMessage.PrivateChatMessage(
                                        it.first,
                                        it.second
                                    )
                                }

                        // Sort messages by timestamp in descending order
                        combinedMessages = combinedMessages.sortedByDescending {
                            when (it) {
                                is ChatMessage.TeamChatMessage -> it.message.timestamp
                                is ChatMessage.PrivateChatMessage -> it.message.timestamp
                            }
                        }

                        // Render the chat screen
                        auth.currentUser?.let { it1 -> ChatScreen(combinedMessages, people, teams, it1.uid) }
                    }

                    composable("chats/{isGroupChat}/{chatId}") { navBackStackEntry ->
                        val chatId = navBackStackEntry.arguments?.getString("chatId")
                        val isGroupChat =
                            navBackStackEntry.arguments?.getString("isGroupChat").toBoolean()
                        SingleChatScreen(chatId!!, isGroupChat)
                    }

                    composable("notifications") {
                        // User notifications
                        val filteredNotifications = userNotifications
                            .map { un -> un.notificationId }


                        NotificationsScreen(
                            filteredNotifications,
                            notifications, userNotifications,
                            teams, people,
                            Actions.getInstance().goToTaskComments
                        )
                    }

                    composable("accounts/{accountId}") { backStackEntry ->
                        val accountId = backStackEntry.arguments?.getString("accountId")

                        val filteredTeamParticipant = people.first { it.first == accountId }
                        ShowProfile(filteredTeamParticipant)
                    } // TODO: Implement

                    composable("profile") {
                        auth.currentUser?.let { it1 ->
                            ProfileScreen(
                                personal, it1.uid, teams, teamParticipants,
                                profileVM, onLogout = signOut, updateAccountBeenDeletedStatus = appVM::updateAccountBeenDeletedStatus
                            )
                        }
                    }
                    composable("profile/edit") {
                        auth.currentUser?.let { it1 -> EditProfilePane(personal, it1.uid, profileVM) }
                    }

                    composable(
                        "invite/{hash}",
                        deepLinks = listOf(navDeepLink {
                            uriPattern = "https://teamtask.com/invite/{hash}"
                        })
                    ) { backStackEntry ->
                        val hash = backStackEntry.arguments?.getString("hash")
                        InviteConfirmationScreen()
                    }

                    composable("notImplemented") { NotImplementedScreen() }
                }
            }
        }
    }
}



@Composable
fun NotImplementedScreen(
) {
    val typography = TeamTaskTypography

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.teamtasklogo),
                contentDescription = "App logo"
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                "This section of the app is still under development!",
                style = typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

class Actions(
    val navCont: NavHostController
) {
    // Home
    val goToHome: () -> Unit = { navCont.navigate("home") }

    val goToHomeCalendar: () -> Unit = { navCont.navigate("homeCalendar") }

    // List of teams
    val goToTeams: () -> Unit = { navCont.navigate("teams") }

    // Filter teams
    val goToFilterTeams: () -> Unit = { navCont.navigate("teams/filter") }

    // Creation of a new team
    val goToCreateTeamInfo: () -> Unit = { navCont.navigate("teams/newTeam/info") }
    val goToCreateTeamPeople: () -> Unit = { navCont.navigate("teams/newTeam/share") }

    // Watch team elements
    val goToTeamTasks: (String) -> Unit = { teamId -> navCont.navigate("teams/$teamId/tasks") }
    val goToTeamDescription: (String) -> Unit =
        { teamId -> navCont.navigate("teams/$teamId/description") }
    val goToTeamPeople: (String) -> Unit = { teamId -> navCont.navigate("teams/$teamId/people") }

    val goToTeamTasksCalendar: (String) -> Unit =
        { teamId -> navCont.navigate("teams/$teamId/tasksCalendar") }
    val goToTeamStatistics: (String) -> Unit =
        { teamId -> navCont.navigate("teams/$teamId/statistics") }

    // Edit team elements
    val goToFilterTeamTasks: (String) -> Unit =
        { teamId -> navCont.navigate("teams/$teamId/filterTasks") }
    val goToEditTeamStatus: (String) -> Unit =
        { teamId -> navCont.navigate("teams/$teamId/edit/status") }
    val goToEditTeamDescription: (String) -> Unit =
        { teamId -> navCont.navigate("teams/$teamId/edit/description") }
    val goToEditTeamPeople: (String) -> Unit =
        { teamId -> navCont.navigate("teams/$teamId/edit/people") }

    // Add a new task to a team
    val goToCreateTaskStatus: (String) -> Unit =
        { teamId -> navCont.navigate("teams/$teamId/newTask/status") }
    val goToCreateTaskDescription: (String) -> Unit =
        { teamId -> navCont.navigate("teams/$teamId/newTask/description") }
    val goToCreateTaskPeople: (String) -> Unit =
        { teamId -> navCont.navigate("teams/$teamId/newTask/people") }

    // Watch task elements
    val goToTaskComments: (String, String) -> Unit =
        { teamId, taskId -> navCont.navigate("teams/$teamId/tasks/$taskId/comments") }
    val goToTaskComment: (String, String, String) -> Unit =
        { teamId, taskId, commentId -> navCont.navigate("teams/$teamId/tasks/$taskId/comments/$commentId") }
    val goToTaskInfo: (String, String) -> Unit =
        { teamId, taskId -> navCont.navigate("teams/$teamId/tasks/$taskId/info") }
    val goToTaskDescription: (String, String) -> Unit =
        { teamId, taskId -> navCont.navigate("teams/$teamId/tasks/$taskId/description") }
    val goToTaskPeople: (String, String) -> Unit =
        { teamId, taskId -> navCont.navigate("teams/$teamId/tasks/$taskId/people") }

    // Edit task elements
    val goToEditTaskInfo: (String, String) -> Unit =
        { teamId, taskId -> navCont.navigate("teams/$teamId/tasks/$taskId/edit/info") }
    val goToEditTaskDescription: (String, String) -> Unit =
        { teamId, taskId -> navCont.navigate("teams/$teamId/tasks/$taskId/edit/description") }
    val goToEditTaskPeople: (String, String) -> Unit =
        { teamId, taskId -> navCont.navigate("teams/$teamId/tasks/$taskId/edit/people") }

    // List of chats
    val goToChats: () -> Unit = { navCont.navigate("chats") }
    val goToChat: (String, Boolean) -> Unit =
        { chatId, isGroupChat -> navCont.navigate("chats/$isGroupChat/$chatId") }

    // Notifications
    val goToNotifications: () -> Unit = { navCont.navigate("notifications") }

    // Account
    val goToAccount: (String) -> Unit = { accountId -> navCont.navigate("accounts/$accountId") }

    // Profile
    val goToProfile: () -> Unit = { navCont.navigate("profile") }
    val goToEditProfile: () -> Unit = { navCont.navigate("profile/edit") }

    // Unimplemented screen
    val goToUnimplemented: (String) -> Unit = { _ -> navCont.navigate("notImplemented") }

    val navigateBack: () -> Unit = {
        navCont.popBackStack()
    }

    // Get current route
    @Composable
    fun getCurrentRoute(): String? {
        val navBackStackEntry by navCont.currentBackStackEntryAsState()
        return navBackStackEntry?.destination?.route
    }

    @Composable
    fun getStringParameter(parameter: String): String? {
        val navBackStackEntry by navCont.currentBackStackEntryAsState()
        return navBackStackEntry?.arguments?.getString(parameter)
    }

    @Composable
    fun getBooleanParameter(parameter: String): Boolean {
        val navBackStackEntry by navCont.currentBackStackEntryAsState()
        return navBackStackEntry?.arguments?.getString(parameter).toBoolean()
    }

    companion object { //IN THIS WAY WE CAN CALL THE ACTIONS FROM anywhere in THE project
        lateinit var navController: NavHostController

        fun initialize(navController: NavHostController) {
            this.navController = navController
        }

        fun getInstance(): Actions {
            return Actions(navController)
        }
    }
}
