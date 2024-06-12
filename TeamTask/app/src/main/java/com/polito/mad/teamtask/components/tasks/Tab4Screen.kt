package com.polito.mad.teamtask.components.tasks

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.polito.mad.teamtask.AppFactory
import com.polito.mad.teamtask.AppModel
import com.polito.mad.teamtask.ParametricFactory
import com.polito.mad.teamtask.Person
import com.polito.mad.teamtask.Task
import com.polito.mad.teamtask.screens.AddPeopleInTaskSection
import com.polito.mad.teamtask.screens.PeopleSection
import com.polito.mad.teamtask.screens.PersonData
import com.polito.mad.teamtask.screens.SpecificTeamViewModel
import com.polito.mad.teamtask.screens.ToDoTask
import com.polito.mad.teamtask.ui.theme.CaribbeanCurrent
import com.polito.mad.teamtask.ui.theme.TeamTaskTypography
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class TaskViewModel(val model: AppModel, val taskId: String, val teamId: String) : ViewModel() {
    val team = model.getTeamById(teamId)
        .stateIn(
            scope = viewModelScope,
            initialValue = null,
            started = SharingStarted.WhileSubscribed(5000L)
        )

//    val infoSection = model.getTaskById(taskId)
//        .stateIn(
//            scope = viewModelScope,
//            initialValue = null,
//            started = SharingStarted.WhileSubscribed(5000L)
//        )

    private var peopleRoles = model.getRealTeamParticipantsByTeamId(teamId)
        .stateIn(
            scope = viewModelScope,
            initialValue = emptyList(),
            started = SharingStarted.WhileSubscribed(5000L)
        )

    var taskMembersStateFlow = model.getPeopleByTeamId(teamId)
        //.map { list -> list.filter { infoSection.value?.people?.contains(it.first) ?: false } }
        .combine(peopleRoles) { peopleList, peopleRules ->
            peopleList
                .map {
                    val role = peopleRules.find { teamPart ->
                        teamPart.personId == it.first
                    }

                    val permission = if (team.value?.ownerId == it.first) "Owner"
                    else if (team.value?.admins?.contains(it.first) == true) "Admin"
                    else ""


                    var imageUri: Uri? = null

                    if (it.second.image.isNotEmpty() && isNetworkAvailable(model.applicationContext))
                        imageUri =
                            FirebaseStorage.getInstance().reference.child("profileImages/${it.second.image}").downloadUrl.await()

                    PersonData(
                        it.first,
                        it.second.name,
                        it.second.surname,
                        it.second.username,
                        role?.role ?: "",
                        permission = permission,
                        imageUri.toString(),
                    )
                }
        }
        .stateIn(
            scope = viewModelScope,
            initialValue = emptyList(),
            started = SharingStarted.WhileSubscribed(5000L)
        )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Tab4Screen(
    tabs: List<String>,
    teamId: String,
    taskId: String,
    task: Task,
    creator: String,
    rawToDoTasks: List<Pair<String, Task>>,
    rawPeople: List<Pair<String, Person>>,
    vm: SpecificTeamViewModel = viewModel(),
    descriptionVm: DescriptionViewModel = viewModel(factory = AppFactory(LocalContext.current)),
    taskVm: TaskViewModel = viewModel(
        factory = ParametricFactory(
            LocalContext.current,
            taskId = taskId,
            teamId = teamId,
            commentId = ""
        )
    )
) {
    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography
    val auth = FirebaseAuth.getInstance()

    val pagerState = rememberPagerState { tabs.size }
    val animationScope = rememberCoroutineScope()

    val keyboardController = LocalSoftwareKeyboardController.current

    val peopleOfTeam = taskVm.taskMembersStateFlow.collectAsState()

    val team = taskVm.team.collectAsState()

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
                    role = "",
                    permission = "",
                    image = ""
                )
            },
            tags = task.tags
        )
    }

    LaunchedEffect(pagerState.currentPage) {
        descriptionVm.setIsDescriptionEditing(false)
        keyboardController?.hide()
    }

//    LaunchedEffect(Unit) {
//        vm.init(toDoTasks)
//    }
//
//

    if (vm.showExitFromTaskModal) {
        AlertDialog(
            onDismissRequest = {
                vm.setShwExitFromTeamModal(false)
            },
            title = { Text(text = "Exit From Task") },
            text = { Text(text = "Are you sure that you want to exit from this task?") },
            confirmButton = {
                Button(onClick = {
                    vm.exitFromTask(teamId, taskId)
                    vm.setShwExitFromTaskModal(false)
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
                    vm.setShwExitFromTaskModal(false)
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
    if (vm.showDeleteTaskModal) {
        AlertDialog(
            onDismissRequest = {
                vm.setStrinValueForDelete("")
                vm.setShwDeleteTeamModal(false)
            },
            title = { Text(text = "Delete Task", color = palette.error) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Write \"" + (auth.currentUser?.email
                            ?: "your email") + "\" and press \"Delete\" to permanently delete this task:"
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
                    vm.validateStringForDeleteTask(teamId, taskId)
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
                    vm.setShwDeleteTaskModal(false)
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

    Column {
        // Tab row
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
                    text = {
                        Text(
                            currentTab,
                            style = typography.labelMedium.copy(
                                fontSize = 11.2.sp
                            ),
                            color = palette.secondary
                        )
                    },
                    selected = pagerState.currentPage == index,
                    onClick = {
                        animationScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    }
                )
            }
        }

        // Swipeable main content
        HorizontalPager(
            state = pagerState,
        ) { page ->
            when (page) {
                0 -> Comments(teamId, taskId)
                1 -> Info(task, creator, taskId)
                2 -> DescriptionVariant(task.description, vm::setTaskDescription, taskId, false)
                3 -> PeopleSection(
                    teamId,
                    taskId,
                    peopleOfTeam.value.filter { task.people.contains(it.personId) },
                    peopleOfTeam.value, vm.selectedPeople, vm::clearSelectedPeople,
                    vm::addPerson, vm::removePerson,
                    vm::addSelectedTeamPeopleToTask, vm::removePersonFromTask,
                    vm.filteredPeople,
                    vm.searchQuery.value, vm::onSearchQueryChanged,
                    {},
                    isInTeamPeople = false,
                    peopleOrTaskNameError = "",
                    vm::init
                )
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AddMembersInTask(
    teamId: String,
    taskId: String,
    vm: SpecificTeamViewModel = viewModel(),
) {
    AddPeopleInTaskSection(
        teamId,
        taskId,
        vm.taskpeople, vm.teampeople, vm.selectedPeople, vm::clearSelectedPeople,
        vm::addPerson, vm::removePerson,
        vm::addSelectedTeamPeopleToTask, vm::removePersonFromTask,
        vm.filteredPeople,
        vm.searchQuery.value, vm::onSearchQueryChanged,
        {},
        isInTeamPeople = false,
        peopleOrTaskNameError = "",
        vm::addPersonToTask,
        vm.isLoadingTaskAddMembers.value
    )
}