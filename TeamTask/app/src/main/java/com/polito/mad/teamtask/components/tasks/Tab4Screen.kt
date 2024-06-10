package com.polito.mad.teamtask.components.tasks

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.storage.FirebaseStorage
import com.polito.mad.teamtask.AppFactory
import com.polito.mad.teamtask.AppModel
import com.polito.mad.teamtask.Task
import com.polito.mad.teamtask.chat.visualization.MemberTag
import com.polito.mad.teamtask.components.tasks.components.WTViewModel
import com.polito.mad.teamtask.screens.PeopleSection
import com.polito.mad.teamtask.screens.PersonData
import com.polito.mad.teamtask.screens.SpecificTeamViewModel
import com.polito.mad.teamtask.ui.theme.TeamTaskTypography
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class TaskViewModel(val model: AppModel, val taskId: String, val teamId: String): ViewModel() {
    val infoSection = model.getTaskById(taskId)
        .stateIn(
            scope = viewModelScope,
            initialValue = null,
            started = SharingStarted.WhileSubscribed(5000L)
        )

//    private var peopleRoles = model.getRealTeamParticipantsByTeamId(teamId)
//        .stateIn(
//            scope = viewModelScope,
//            initialValue = emptyList(),
//            started = SharingStarted.WhileSubscribed(5000L)
//        )
//
    private var taskMembersStateFlow = model.getPeopleByTeamId(teamId)
//        .map { list -> list.filter { infoSection.value?.people?.contains(it.first) ?: false } }
//        .combine(peopleRoles) { peopleList, peopleRules ->
//            peopleList
//                .map {
//                    val role = peopleRules.find { teamPart ->
//                        teamPart.personId == it.first
//                    }
//
//                    var imageUri: Uri? = null
//
//                    if (it.second.image.isNotEmpty() && isNetworkAvailable(model.applicationContext))
//                        imageUri =
//                            FirebaseStorage.getInstance().reference.child("profileImages/${it.second.image}").downloadUrl.await()
//
//                   PersonData(
//                        it.first,
//                        it.second.name,
//                        it.second.surname,
//                        it.second.username,
//                       role?.role ?: "",
//                        permission = "",
//                        imageUri.toString(),
//                    )
//                }
//        }
        .stateIn(
            scope = viewModelScope,
            initialValue = emptyList(),
            started = SharingStarted.WhileSubscribed(5000L)
        )



}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Tab4Screen (
    tabs: List<String>,
    teamId: String,
    taskId: String,
    task: Task,
    creator: String,
    vm: SpecificTeamViewModel = viewModel(),
    descriptionVm: DescriptionViewModel = viewModel(factory = AppFactory(LocalContext.current))
) {
    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography

    val pagerState = rememberPagerState {tabs.size}
    val animationScope = rememberCoroutineScope()

    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(pagerState.currentPage) {
        descriptionVm.setIsDescriptionEditing(false)
        keyboardController?.hide()
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
                    text = { Text(
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
                1 -> Info(task, creator)
                2 -> DescriptionVariant(task.description, vm::setTaskDescription, taskId, false)
                3 -> PeopleSection(
                    teamId,
                    vm.taskpeople,  vm.teampeople,  vm.selectedPeople, vm::clearSelectedPeople,
                    vm::addPerson,  vm::removePerson,
                    vm::addSelectedTeamPeopleToTask, vm::removePersonFromTask,
                    vm.filteredPeople,
                    vm.searchQuery.value, vm::onSearchQueryChanged,
                    {},
                    isInTeamPeople = false,
                    peopleError = ""
                )
            }
        }
    }
}