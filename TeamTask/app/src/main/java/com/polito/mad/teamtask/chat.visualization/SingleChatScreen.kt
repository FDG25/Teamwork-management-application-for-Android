package com.polito.mad.teamtask.chat.visualization

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.polito.mad.teamtask.AppFactory
import com.polito.mad.teamtask.AppModel
import com.polito.mad.teamtask.R
import com.polito.mad.teamtask.ui.theme.TeamTaskTypography
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SingleChatViewModel(private val myModel: AppModel) : ViewModel() {
    var iHaveToRecompose by mutableStateOf(false)
        private set

    fun recomposeParent() {
        iHaveToRecompose = !iHaveToRecompose
    }

    val clientId = myModel.auth.currentUser?.uid



    var messagesFlow: (chatId: String, isGroup: Boolean) -> Flow<List<Message>> =
        { chatId, isGroup ->
            if (!isGroup) {
                myModel.getPrivateMessagesById(chatId).map { messageList ->
                    messageList.map {
                        val messageDateTime = LocalDateTime.parse(
                            it.second.timestamp,
                            DateTimeFormatter.ISO_DATE_TIME
                        )

                        if (it.second.senderId == myModel.auth.currentUser?.uid) {
                            ClientMessage(
                                it.first,
                                //Gson().fromJson(it.second.body?:"", AnnotatedString::class.java),
                                buildAnnotatedString { append(it.second.body) },
                                messageDateTime,
                                files = if (!it.second.media.isNullOrBlank()) {
                                    Gson().fromJson(it.second.media, Array<String>::class.java)
                                        .map { uriString ->
                                            UriCouple(
                                                try {
                                                    FirebaseStorage.getInstance().reference.child(
                                                        uriString
                                                    ).downloadUrl.await()
                                                } catch (e: Exception) {
                                                    null
                                                },
                                                uriString
                                            )
                                        }.toSet()
                                } else null
                            )
                        } else {
                            InterlocutorMessage(
                                it.first,
                                buildAnnotatedString { append(it.second.body) },
                                //Gson().fromJson(it.second.body?:"", AnnotatedString::class.java),
                                messageDateTime,
                                files = if (!it.second.media.isNullOrBlank()) {
                                    Gson().fromJson(it.second.media, Array<String>::class.java)
                                        .map { uriString ->
                                            UriCouple(
                                                try {
                                                    FirebaseStorage.getInstance().reference.child(
                                                        uriString
                                                    ).downloadUrl.await()
                                                } catch (e: Exception) {
                                                    null
                                                },
                                                uriString
                                            )
                                        }.toSet()
                                } else null
                            )
                        }
                    }
                }.transform { myList -> emit(myList.sortedBy { x -> x.date }) }
            } else {
                myModel.getTeamMessagesByTeamId(chatId)
                    //"combine" over "zip" in order to obtain an update whenever one of the two flows emits a new value (zip require that both have a new value)
                    .combine(teamMembersFlow(chatId, true)) { groupMessageList, teamMembers ->
                        groupMessageList.map {
                            val messageDateTime = LocalDateTime.parse(
                                it.second.timestamp,
                                DateTimeFormatter.ISO_DATE_TIME
                            )

                            if (it.second.senderId == myModel.auth.currentUser?.uid) {
                                ClientMessage(
                                    it.first,
                                    createAnnotatedStringFromString(it.second.body, teamMembers),
                                    //Gson().fromJson(it.second.body, AnnotatedString::class.java),
                                    messageDateTime,
                                    files = if (!it.second.media.isNullOrBlank()) {
                                        Gson().fromJson(it.second.media, Array<String>::class.java)
                                            .map { uriString ->
                                                UriCouple(
                                                    try {
                                                        FirebaseStorage.getInstance().reference.child(
                                                            uriString
                                                        ).downloadUrl.await()
                                                    } catch (e: Exception) {
                                                        null
                                                    },
                                                    uriString
                                                )
                                            }.toSet()
                                    } else null
                                )
                            } else {
                                GroupMessage(
                                    it.first,
                                    createAnnotatedStringFromString(it.second.body, teamMembers),
                                    //Gson().fromJson(it.second.body, AnnotatedString::class.java),
                                    messageDateTime,
                                    //Timestamp.valueOf(it.second.timestamp),
                                    teamMembers.find { member -> member.memberId == it.second.senderId }?.profilePic
                                        ?: Uri.parse(""),
                                    teamMembers.find { member -> member.memberId == it.second.senderId }?.username
                                        ?: "",
                                    Color.Magenta, //todo I don't have color
                                    files = if (!it.second.media.isNullOrBlank()) {
                                        Gson().fromJson(it.second.media, Array<String>::class.java)
                                            .map { uriString ->
                                                UriCouple(
                                                    try {
                                                        FirebaseStorage.getInstance().reference.child(
                                                            uriString
                                                        ).downloadUrl.await()
                                                    } catch (e: Exception) {
                                                        null
                                                    },
                                                    uriString
                                                )
                                            }.toSet()
                                    } else null
                                )
                            }
                        }
                    }.transform { myList -> emit(myList.sortedBy { x -> x.date }) }
            }
        }

    //team members informations with the profile image/username
    var teamMembersFlow: (teamId: String, isGroup: Boolean) -> Flow<List<MemberTag>> =
        { teamId, isGroup ->
            if (isGroup) {
                myModel.getPeopleByTeamId(teamId)
                    .map { peopleList ->
                        peopleList
                            .map {
                            val imageUri: Uri? =
                                FirebaseStorage.getInstance().reference.child("profileImages/${it.second.image}").downloadUrl.await()

                            MemberTag(
                                it.first,
                                it.second.username,
                                imageUri ?: Uri.parse(""),
                            )
                        }
                    }
            } else MutableStateFlow(emptyList())
        }

    //var members: StateFlow<List<MemberTag>> = MutableStateFlow(emptyList())

//    var messages = MutableStateFlow<List<Message>>(
//        listOf(
//            GroupMessage(
//                Random.nextInt(10000).toString(),
//                AnnotatedString("Hi, I'm Luca. What are you doing?, please let me know it is important for me. :)))))"),
//                Timestamp(System.currentTimeMillis() - 63800000),
//                Uri.parse("android.resource://com.polito.mad.teamtask/drawable/person_4"),
//                "luca_bianchi",
//                Color.Blue
//            ),
//            GroupMessage(
//                Random.nextInt(10000).toString(),
//                AnnotatedString("Hi, I'm Marcel. I'm fine, you?!"),
//                Timestamp(System.currentTimeMillis() - 61800000),
//                Uri.parse("android.resource://com.polito.mad.teamtask/drawable/person_3"),
//                "marcel_radavan",
//                Color.Magenta
//            ),
//            ClientMessage(
//                Random.nextInt(10000).toString(),
//                AnnotatedString("I'm fine too, thanks for asking"),
//                Timestamp(System.currentTimeMillis() - 59800000)
//            )
//        )
//    )
//        private set

    fun sendMessage(message: ClientMessage, isGroup: Boolean, chatId: String) {
        myModel.addChatMessage(message, isGroup, chatId)
    }

    fun deleteMessage(message: ClientMessage, isGroup: Boolean): Boolean {
        return myModel.deleteMessageById(message, isGroup)
    }
}

@Preview
@Composable
fun SingleChatScreen(
    chatId: String = "",
    isGroupChat: Boolean = true,
    vm: SingleChatViewModel = viewModel(factory = AppFactory(LocalContext.current))
) {
    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography



    val messages by vm.messagesFlow(chatId, isGroupChat).collectAsState(initial = listOf())
    val teamMembers by vm.teamMembersFlow(chatId, isGroupChat).collectAsState(initial = listOf())

    var dropdownMenuExpanded by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(key1 = messages, vm.iHaveToRecompose) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(index = messages.size - 1)
        }
    }

    Log.d("SingleChatScreen", "Members: $teamMembers, chatId: $chatId, isGroupChat: $isGroupChat")

    Scaffold(
        bottomBar = {
            vm.clientId?.let {
                WriteMessage(
                    sendMessage = vm::sendMessage,
                    groupUsers = teamMembers.ifEmpty { null },
                    chatId = chatId,
                    clientId = it
                )
            }
        },
        //modifier = Modifier.imePadding()
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.padding(padding)
        ) {
            item {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                )
            }

            messages.forEach {
                item(key = it.id) {
                    var visible by remember { mutableStateOf(true) }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp)
                    ) {

                        Box(modifier = Modifier
                            .clickable { dropdownMenuExpanded = it.id }
                        ) {
                            Message(it, vm::recomposeParent)

                            //DropDownMenu
                            if (it is ClientMessage && dropdownMenuExpanded == it.id)
                                DropdownMenu(
                                    expanded = dropdownMenuExpanded.isNotEmpty(),
                                    onDismissRequest = { dropdownMenuExpanded = "" },
                                    modifier = Modifier.background(palette.background)
                                ) {
                                    DropdownMenuItem(onClick = {
                                        visible = false
                                        vm.deleteMessage(it, isGroupChat)
                                        dropdownMenuExpanded = ""
                                    },
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    tint = palette.error,
                                                    painter = painterResource(R.drawable.outline_delete_outline_24),
                                                    contentDescription = "delete Message",
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Text(
                                                    text = "Delete Message",
                                                    style = typography.labelSmall.copy(
                                                        color = palette.error,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Normal
                                                    )
                                                )
                                            }
                                        }
                                    )
                                }
                        }
                    }

                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                    )
                }
            }
        }
    }
}