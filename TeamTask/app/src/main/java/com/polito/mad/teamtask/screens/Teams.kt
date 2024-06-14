package com.polito.mad.teamtask.screens

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ListResult
import com.polito.mad.teamtask.Actions
import com.polito.mad.teamtask.AppModel
import com.polito.mad.teamtask.R
import com.polito.mad.teamtask.Team
import com.polito.mad.teamtask.components.CustomSearchBar
import com.polito.mad.teamtask.components.TeamEntry
import com.polito.mad.teamtask.components.categories
import com.polito.mad.teamtask.ui.theme.TeamTaskTypography
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

data class TeamAndNotification(
    val name: String,
    val notificationNumber: Int,
)

class TeamsViewModel(private val myModel: AppModel) : ViewModel() {
    var searchQuery by mutableStateOf("")
    private val storage = FirebaseStorage.getInstance()
    private val _teamImages = MutableStateFlow<Map<String, Uri?>>(emptyMap())
    val teamImages: StateFlow<Map<String, Uri?>> = _teamImages

    fun onSearchQueryChanged(query: String) {
        searchQuery = query
    }

    //filter by category
    var isFilterActive by mutableStateOf(false)
        private set

    fun setIsFilterActive(value: Boolean) {
        isFilterActive = value
    }

    private val _selectedCategories = mutableStateListOf<String>()
    val selectedCategories: List<String> = _selectedCategories

    fun addCategory(category: String) {
        if (!_selectedCategories.contains(category)) {
            _selectedCategories.add(category)
        }
    }

    fun removeCategory(category: String) {
        _selectedCategories.remove(category)
    }

    fun clearSelectedCategories() {
        _selectedCategories.clear()
    }


    fun fetchTeamImage(imageName: String, teamId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val imageRef = storage.reference.child("teamImages/$imageName").downloadUrl.await()

                if (imageRef != null) {
                    _teamImages.value =
                        _teamImages.value.toMutableMap().apply { put(teamId, imageRef) }
                } else {
                    _teamImages.value = _teamImages.value.toMutableMap().apply { put(teamId, null) }
                }
            } catch (e: Exception) {
                _teamImages.value = _teamImages.value.toMutableMap().apply { put(teamId, null) }
            }
        }
    }

    var teamNameValue by mutableStateOf("")
        private set
    var teamNameError by mutableStateOf("")
        private set

    fun setTeamName(n: String) {
        teamNameValue = n
    }

    var teamCategory by mutableStateOf("")
        private set

    var teamCategoryError by mutableStateOf("")
        private set

    fun setMyTeamCategory(c: String) {
        teamCategory = c
    }

    var isLoading by mutableStateOf(false)
        private set

    private fun checkTeamName() {
        // Remove leading and trailing spaces
        val trimmedTeamName = teamNameValue.trim()

        teamNameError = if (trimmedTeamName.isBlank()) {
            "Team name cannot be blank!"
        } else if (!trimmedTeamName.matches(Regex("^(?=.*[a-zA-Z0-9])[a-zA-Z0-9 ]{1,50}\$"))) {
            "Max 50 characters. Only letters, numbers and spaces are allowed!"
        }
        //SHOULD WE ALLOW TEAMS TO HAVE THE SAME NAME?
        else {
            ""
        }

        // Update the taskNameValue with the trimmed version if there are no errors
        if (teamNameError.isBlank()) {
            teamNameValue = trimmedTeamName
        }
    }

    private fun checkTeamCategory() {
        return if (teamCategory.isBlank()) {
            teamCategoryError = "Please select a category!"
        } else {
            teamCategoryError = ""
        }
    }

    fun validate(isInCreation: Boolean, teamId: String) {
        checkTeamName()
        checkTeamCategory()
        if (teamNameError.isBlank() && teamCategoryError.isBlank()) {
            setIsShowingCreateTeam(false)
            if (isInCreation) {
                //create new Team on Firebase
                viewModelScope.launch {
                    isLoading = true
                    val result = myModel.createTeam(teamNameValue, teamCategory, imageUri)
                    isLoading = false
                    //if success go to create team people
                    if (result.isNotEmpty()) Actions.getInstance().goToCreateTeamPeople(result, teamNameValue)
                    else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                myModel.applicationContext,
                                "Error during the creation of the team",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } else {
                Actions.getInstance().goToTeamTasks(teamId)
            }
        }
    }

    fun saveTeamChangesToDB(teamId: String) {
        var myImageUri: Uri? = if (imageUri?.scheme=="content" || imageUri?.scheme=="file") imageUri else Uri.EMPTY
        //no image for the team
        if (imageUri==null) myImageUri = null

        viewModelScope.launch(Dispatchers.IO) {
            isLoading = true
            myModel.updateTeamStatus(teamId, teamNameValue, teamCategory, myImageUri)
            isLoading = false
            withContext(Dispatchers.Main) {
                Actions.getInstance().navigateBack()
            }
        }
    }

    var imageUri by mutableStateOf<Uri?>(null)
    fun setUri(uri: Uri? = null) {
        imageUri = uri
    }

    private var hasStoragePermission by mutableStateOf(false)
    fun setStoragePermission(sp: Boolean) {
        hasStoragePermission = sp
    }

    var showBottomSheet by mutableStateOf(false)
    fun setShowBottomMenu(bm: Boolean) {
        showBottomSheet = bm
    }

    var isShowingCreateTeam by mutableStateOf(false)
    fun setIsShowingCreateTeam(value: Boolean) {
        isShowingCreateTeam = value
    }

    //hardcoded list of messages
    private val _teamandnotifications = mutableStateOf(
        listOf(
            TeamAndNotification("Team1", 1),
            TeamAndNotification("Team2", 2),
            TeamAndNotification("Team3", 3),
            TeamAndNotification("Team1", 1),
            TeamAndNotification("Team2", 2),
            TeamAndNotification("Team1", 1),
            TeamAndNotification("Team2", 2),
            TeamAndNotification("Team1", 1),
            TeamAndNotification("Team2", 2),
            TeamAndNotification("Team1", 1),
            TeamAndNotification("Team2", 2),
        )
    )


    // Provide an immutable view of the messages to the UI
    private val teamandnotifications: List<TeamAndNotification> get() = _teamandnotifications.value

    // Computed list that filters people based on search query
    val filteredteamandnotifications: List<TeamAndNotification>
        get() = if (searchQuery.isEmpty()) {
            teamandnotifications
        } else {
            teamandnotifications.filter {
                it.name.contains(searchQuery, ignoreCase = true)
            }
        }
}


@Composable
fun TeamsScreen(
    teams: List<Pair<String, Team>>,
    goToTeamTasks: (String) -> Unit,
    teamsVM: TeamsViewModel
) {
    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography

    // Filter teams based on the search query
    val searchedTeams = remember(teamsVM.searchQuery, teams, teamsVM.isFilterActive) {
        teams.filter {
            it.second.name.contains(teamsVM.searchQuery, ignoreCase = true)
                    && (!teamsVM.isFilterActive || (teamsVM.isFilterActive && teamsVM.selectedCategories.contains(
                it.second.category
            )))
        }
    }

    LazyColumn {
        item {
            if (teams.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    CustomSearchBar(
                        modifier = Modifier.weight(1f),
                        placeholderText = "Search for a team",
                        searchQuery = teamsVM.searchQuery,
                        onSearchQueryChanged = { teamsVM.onSearchQueryChanged(it) } // Fixed line
                    )
                    // Filtering option
                    IconButton(
                        onClick = {
                            //clearSelectedDateRangeError()
                            Actions.getInstance().goToFilterTeams()
                        }
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.outline_tune_24),
                            contentDescription = "Filter Category",
                            modifier = Modifier.size(30.dp),
                            colorFilter = ColorFilter.tint(palette.secondary)
                        )
                    }
                    // Filtering option
                    if (teamsVM.isFilterActive)
                        IconButton(
                            onClick = {
                                teamsVM.clearSelectedCategories()
                                teamsVM.setIsFilterActive(false)
                            }
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.outline_close_24),
                                contentDescription = "Filter Category",
                                modifier = Modifier.size(30.dp),
                                colorFilter = ColorFilter.tint(palette.secondary)
                            )
                        }
                }
            }
        }

        if (teamsVM.isFilterActive)
            if (teamsVM.selectedCategories.isNotEmpty()) {
                item {
                    LazyRow(
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    ) {
                        teamsVM.selectedCategories.forEach {
                            item {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(end = 16.dp)
                                ) {
                                    Text(
                                        it,
                                        style = typography.labelMedium,
                                        color = palette.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                item {
                    Text(
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                        text = "❌ No categories selected",
                        style = typography.labelMedium,
                        color = palette.onSurfaceVariant
                    )
                }
            }

        if (teams.isEmpty()) {
            // Show this message when there are no teams at all
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
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
                            "You have no teams right now \uD83E\uDE90",
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
                            "Press on the + button to create one!",
                            style = typography.labelMedium,
                            color = palette.onSurfaceVariant
                        )
                    }
                }
            }
        } else if (searchedTeams.isEmpty()) {
            // Show this message when there are teams but none match the search query
            item {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        "No teams match your search query",
                        style = typography.labelMedium,
                        color = palette.onSurfaceVariant
                    )
                }
            }
        } else {
            item {
                Column {
                    searchedTeams.forEach { (id, team) ->
                        val unreadNotifications = 5
                        val imageUri = teamsVM.teamImages.collectAsState().value[id]

                        LaunchedEffect(id) {
                            teamsVM.fetchTeamImage(team.image, id)
                        }

                        Button(
                            onClick = { goToTeamTasks(id) },
                            shape = RoundedCornerShape(5.dp),
                            colors = ButtonDefaults.buttonColors(
                                contentColor = Color.Transparent,
                                containerColor = Color.Transparent
                            ),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.padding(horizontal = 15.dp)
                        ) {
                            TeamEntry(team, unreadNotifications, imageUri)
                        }

                        Spacer(Modifier.height(5.dp))
                    }
                }
            }
        }
    }
}

