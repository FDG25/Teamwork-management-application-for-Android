package com.polito.mad.teamtask.components.tasks

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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.polito.mad.teamtask.AppFactory
import com.polito.mad.teamtask.components.tasks.components.WTViewModel
import com.polito.mad.teamtask.screens.PeopleSection
import com.polito.mad.teamtask.screens.SpecificTeamViewModel
import com.polito.mad.teamtask.ui.theme.TeamTaskTypography
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Tab4Screen (
    tabs: List<String>,
    teamId: String,
    taskId: String,
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

    var isInAddMode by remember { mutableStateOf(false) }
    val setAddMode = fun (value: Boolean) {
        isInAddMode = value
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
                1 -> Info()
                2 -> Description(vm.taskDescriptionValue, vm::setTaskDescription)
                3 -> PeopleSection(
                    teamId,
                    vm.taskpeople,  vm.teampeople,  vm.selectedPeople, vm::clearSelectedPeople,
                    vm::addPerson,  vm::removePerson,
                    vm::addSelectedTeamPeopleToTask, vm::removePersonFromTask,
                    vm.filteredPeople,
                    vm.searchQuery.value, vm::onSearchQueryChanged,
                    {},
                    isInTeamPeople = false
                )
            }
        }
    }
}