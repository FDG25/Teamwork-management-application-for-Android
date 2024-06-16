package com.polito.mad.teamtask.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.compose.rememberImagePainter
import coil.request.ImageRequest
import com.google.firebase.storage.FirebaseStorage
import com.polito.mad.teamtask.Actions
import com.polito.mad.teamtask.ChatMessage
import com.polito.mad.teamtask.Person
import com.polito.mad.teamtask.R
import com.polito.mad.teamtask.Team
import com.polito.mad.teamtask.components.CustomSearchBar
import com.polito.mad.teamtask.ui.theme.TeamTaskTypography
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


data class Message(
    val username: String,               // User or Team name
    val lastMessage: String?,
    val profilePic: String?,            // Path to the profile picture
    val timeStamp: String,
    val unreadMessages: Int?,
    val isATeam: Boolean,               // Team or User
    val senderName: String?,            // Name of the last person who sent a message on the team
    val chatId: String,
)


class ChatViewModel : ViewModel() {
    var searchQuery = mutableStateOf("")

    // Mutable list of messages
    private val _messages = mutableStateOf<List<Message>>(listOf())

    // Provide an immutable view of the messages to the UI
    val messages: List<Message> get() = _messages.value
    val filteredMessages: List<Message>
        get() = if (searchQuery.value.isEmpty()) {
            messages
        } else {
            messages.filter {
                it.username.contains(searchQuery.value, ignoreCase = true)
            }
        }.sortedByDescending { it.timeStamp }

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }

    // Update messages based on chat data
    fun updateMessages(
        chat: List<ChatMessage>,
        people: List<Pair<String, Person>>,
        teams: List<Pair<String, Team>>,
        userId: String
    ) {
        _messages.value = chat.map { chatMessage ->
            when (chatMessage) {
                is ChatMessage.TeamChatMessage -> {
                    val message = chatMessage.message
                    val team = teams.find { it.first == message.teamId }?.second
                    val sender = people.find { it.first == message.senderId }?.second


                    // Fetch team image URL from Firebase Storage
                    if (team != null && team.image.isNotEmpty()) {
                        val teamPicRef =
                            FirebaseStorage.getInstance().reference.child("teamImages/${team.image}")
                        teamPicRef.downloadUrl.addOnSuccessListener { uri ->
                            _messages.value = _messages.value.map {
                                if (it.username == team.name) {
                                    it.copy(profilePic = uri.toString())
                                } else {
                                    it
                                }
                            }
                        }
                    }

                    Message(
                        username = team?.name ?: "Unknown Team",
                        lastMessage = message.body,
                        profilePic = team?.image?.toString() ?: "",
                        timeStamp = message.timestamp,
                        unreadMessages = null, // Add logic to determine unread messages
                        isATeam = true,
                        senderName = if (message.senderId == userId) {
                            "You: "
                        } else {
                            (sender?.name + ": ") ?: "Unknown Sender"
                        },
                        chatId = message.teamId
                    )
                }

                is ChatMessage.PrivateChatMessage -> {
                    val message = chatMessage.message
                    val sender = people.find { it.first == message.receiverId }?.second

                    if (sender != null && sender.image.isNotEmpty()) {
                        // Fetch user profile picture URL from Firebase Storage
                        val userProfilePicRef =
                            FirebaseStorage.getInstance().reference.child("profileImages/${sender.image}")
                        userProfilePicRef.downloadUrl.addOnSuccessListener { uri ->
                            _messages.value = _messages.value.map {
                                if (it.username == (sender.name + " " + sender.surname)) {
                                    it.copy(profilePic = uri.toString())
                                } else {
                                    it
                                }
                            }
                        }
                    }

                    Message(
                        username = (sender?.name + " " + sender?.surname),
                        lastMessage = message.body,
                        profilePic = sender?.image?.toString() ?: "",
                        timeStamp = message.timestamp,
                        unreadMessages = null, // Add logic to determine unread messages
                        isATeam = false,
                        senderName = if (message.senderId == userId) {
                            "You: "
                        } else {
                            ""
                        },
                        chatId = if (message.senderId == userId) message.receiverId else message.senderId
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageEntry(
    message: Message
) {
    val typography = TeamTaskTypography
    val palette = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .background(palette.surfaceVariant, RoundedCornerShape(5.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        //TEAM IMAGE
        if (message.isATeam) {
            if (message.profilePic != "") { // image SET
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(message.profilePic ?: R.drawable.baseline_groups_24)
                        .crossfade(true)
                        .error(R.drawable.baseline_groups_24)
                        .build(),
                    contentDescription = "Team Image",
                    modifier = Modifier
                        .size(48.dp)
                        .border(1.dp, palette.secondary),
                    //.padding(4.dp)
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.baseline_groups_24), // TODO: Replace with placeholder for teams
                    contentDescription = "Default Team image",
                    modifier = Modifier
                        .size(48.dp)
                        .border(1.dp, palette.secondary)
                        .padding(4.dp)
                )
            }
        } else {
            //USER IMAGE
            if (message.profilePic != "") { // image SET
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(message.profilePic ?: R.drawable.baseline_person_24)
                        .crossfade(true)
                        .error(R.drawable.baseline_person_24)
                        .build(),
                    contentDescription = "Profile Image",
                    contentScale = ContentScale.Crop,
                    modifier =
                    Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .border(1.dp, palette.secondary, CircleShape)
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.baseline_person_24), // TODO: Replace with placeholder for teams
                    contentDescription = "Default Team image",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .border(1.dp, palette.secondary, CircleShape)
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
                // User or team name
                Text(
                    modifier = Modifier.weight(1f),
                    text = if (message.username != "null null") message.username else {
                        if (message.isATeam) "Deleted Team" else "Deleted User"
                    },
                    style = typography.bodyMedium,
                    color = palette.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Timestamp
                val messageTimestamp = message.timeStamp
                val formatter = DateTimeFormatter.ISO_DATE_TIME
                val messageDateTime = LocalDateTime.parse(messageTimestamp, formatter)
                val messageDate = messageDateTime.toLocalDate()
                val today = LocalDate.now()
                val yesterday = today.minusDays(1)

                val displayText = when {
                    messageDate.isEqual(today) -> messageTimestamp.split('T')[1].split('+')[0].slice(
                        IntRange(0, 4)
                    )

                    messageDate.isEqual(yesterday) -> "Yesterday"
                    messageDate.isBefore(yesterday) -> messageTimestamp.split("T")[0]
                    else -> ""
                }

                Text(
                    text = displayText,
                    color = palette.onSurface,
                    style = typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Message preview
                Text(
                    text = (if (message.senderName != null) {
                        message.senderName + message.lastMessage
                    } else {
                        message.lastMessage
                    }).toString(),
                    color = palette.onSurface,
                    style = typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Unread messages badge
                message.unreadMessages?.let {
                    Badge(
                        containerColor = palette.secondary,
                        contentColor = palette.background
                    ) {
                        Text(
                            text = (message.unreadMessages).toString(),
                            style = typography.bodySmall,
                            color = palette.background,
                            modifier = Modifier
                                .padding(4.dp)
                                .background(color = palette.secondary),
                        )
                    }
                }
            }
        }
    }
}


fun getSortedPair(id1: String, id2: String): Pair<String, String> {
    return if (id1 < id2) {
        id1 to id2
    } else {
        id2 to id1
    }
}

@Composable
fun ChatScreen(
    chats: List<ChatMessage>,
    people: List<Pair<String, Person>>,
    teams: List<Pair<String, Team>>,
    userId: String,
    vm: ChatViewModel = viewModel()
) {
    LaunchedEffect(chats, people, teams, userId) {
        //Log.e("ChatScreen", chats.toString())

        // Remove duplicates for the purpose of correct visualization of the different chat entries
        val uniqueChats = chats.distinctBy {
            when (it) { // It keeps track of the timestamp of the last message
                is ChatMessage.TeamChatMessage -> it.message.teamId
                is ChatMessage.PrivateChatMessage -> {
                    val sortedPair = getSortedPair(it.message.receiverId, it.message.senderId)
                    sortedPair
                }
            }
        }.map { chatMessage ->
            if (chatMessage is ChatMessage.PrivateChatMessage) {
                val lastMessage = chatMessage.message
                if (lastMessage.senderId == userId) {
                    chatMessage.copy(
                        message = lastMessage.copy(receiverId = lastMessage.receiverId)
                    )
                } else {
                    chatMessage.copy(
                        message = lastMessage.copy(receiverId = lastMessage.senderId)
                    )
                }
            } else {
                chatMessage
            }
        }

        vm.updateMessages(uniqueChats, people, teams, userId)
    }

    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography

    Column {
        // Search bar
        if (chats.isNotEmpty()) {
            CustomSearchBar(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .fillMaxWidth(),
                placeholderText = "Search teams or users",
                vm.searchQuery.value, vm::onSearchQueryChanged
            )
        }
        if (chats.isEmpty()) {
            Spacer(modifier = Modifier.height(26.dp))
            Column(verticalArrangement = Arrangement.Center) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        "You have no chats right now \uD83D\uDC4B",
                        style = typography.labelMedium,
                        color = palette.onSurfaceVariant
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        "Press on the button below to start one!",
                        style = typography.labelMedium,
                        color = palette.onSurfaceVariant
                    )
                }
            }
        }

        // List of chats
        LazyColumn {
            // Messages
            items(vm.filteredMessages) { message ->
                Box(modifier = Modifier.clickable {
                    Actions.getInstance().goToChat(message.chatId, message.isATeam)
                }) {
                    MessageEntry(message = message)
                }
                Spacer(modifier = Modifier.height(5.dp))
            }

            // No messages found
            item {
                if (chats.isNotEmpty() && vm.filteredMessages.isEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "No results found",
                            style = typography.labelMedium,
                            color = palette.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.padding(bottom = 10.dp))
            }
        }
    }
}

