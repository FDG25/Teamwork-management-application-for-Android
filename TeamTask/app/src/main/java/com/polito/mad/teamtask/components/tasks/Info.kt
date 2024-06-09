package com.polito.mad.teamtask.components.tasks

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.polito.mad.teamtask.R
import com.polito.mad.teamtask.Task
import com.polito.mad.teamtask.screens.CustomToggle
import com.polito.mad.teamtask.screens.DateTimePicker
import com.polito.mad.teamtask.screens.PersonData
import com.polito.mad.teamtask.screens.RecurrencyDropdownMenu
import com.polito.mad.teamtask.screens.ToDoTask
import com.polito.mad.teamtask.ui.theme.TeamTaskTypography
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class InfoViewModel: ViewModel() {
    val priorityOptions: List<String> = listOf("Priority", "Non priority")

    //---Created By---
    var createdBy by mutableStateOf("Luca Bianchi")
        private set

    /*fun setCreator(creator: String) {
        createdBy = creator
    } */

    //---Creation Date---
    var selectedCreationDateTime by mutableStateOf("2024-05-10, 23:59")
    /*fun setCreationDateDateTime(value: String) {
        selectedCreationDateTime = value
    }*/

    //---Due Date---
    var selectedDueDateTime by mutableStateOf("2024-07-30T23:59:09+02:00")
    fun setDueDateDateTime(value: String) {
        selectedDueDateTime = value
    }
    var selectedDueDateTimeError by mutableStateOf("")
        private set
    private fun checkSelectedDueDateTimeError() {
        val iso8601Format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
        selectedDueDateTimeError = if (selectedDueDateTime.isBlank()) {
            "Please, select a due date!"
        } else {
            val selectedDate = iso8601Format.parse(selectedDueDateTime) //NOT NEEDED

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
    var showDueDatePicker by mutableStateOf(false)
    fun setShowingDueDatePicker(value: Boolean) {
        showDueDatePicker = value
    }

    var showDueTimePicker by mutableStateOf(false)
    fun setShowingDueTimePicker(value: Boolean) {
        showDueTimePicker = value
    }

    // Hardcoded list of scheduled tasks
    private val _toDoTasks = mutableStateOf(listOf(
        ToDoTask("0", "Task 1", "Completed", 1, "Weekly", "2024-04-30T12:53:00+02:00", "2024-04-01T09:00:00+02:00",
            listOf(
                PersonData("0", "Luca", "Bianchi", "luca_bianchi", "CEO", "Owner", ""),
                PersonData("1", "Name1ejwnewjneees", "Surname1fskfsmkfnsk", "username1", "CTO", "Admin", ""),
                PersonData("2", "Sofia", "Esposito", "sofia_esposito", "Marketing Director", "", ""),
                PersonData("3", "Giulia", "Ricci", "giulia_ricci", "HR Manager", "", ""),
            ).sortedBy { it.name },
            listOf("#test1", "#test2")),
        ToDoTask("1", "Task 2", "Expired", 0, "Never", "2024-04-20T16:42:00+02:00", "2024-04-01T10:00:00+02:00",
            listOf(
                PersonData("0", "Luca", "Bianchi", "luca_bianchi", "CEO", "Owner", ""),
                PersonData("1", "Giulia", "Ricci", "giulia_ricci", "HR Manager", "", ""),
            ).sortedBy { it.name },
            listOf("#test1", "#test2")),
        ToDoTask("2", "Task 2.5", "Completed", 0, "Never", "2024-04-20T16:42:00+02:00", "2024-04-01T10:00:00+02:00",
            listOf(
                PersonData("0", "Luca", "Bianchi", "luca_bianchi", "CEO", "Owner", ""),
                PersonData("1", "Sofia", "Esposito", "sofia_esposito", "Marketing Director", "", ""),
            ).sortedBy { it.name },
            listOf("#test1", "#test2")),
        ToDoTask("3", "Task 2.6", "Completed", 0, "Never", "2024-04-20T16:42:00+02:00", "2024-04-01T10:00:00+02:00",
            listOf(
                PersonData("0", "Luca", "Bianchi", "luca_bianchi", "CEO", "Owner", ""),
                PersonData("1", "Sofia", "Esposito", "sofia_esposito", "Marketing Director", "", ""),
            ).sortedBy { it.name },
            listOf("#test1", "#test2")),
        ToDoTask("4", "Task 2.7", "Completed", 0, "Never", "2024-04-20T16:42:00+02:00", "2024-04-01T10:00:00+02:00",
            listOf(
                PersonData("0", "Luca", "Bianchi", "luca_bianchi", "CEO", "Owner", ""),
                PersonData("1", "Sofia", "Esposito", "sofia_esposito", "Marketing Director", "", ""),
            ).sortedBy { it.name },
            listOf("#test1", "#test2")),
        ToDoTask("5", "Task 2.8", "Completed", 0, "Never", "2024-04-20T16:42:00+02:00", "2024-04-01T10:00:00+02:00",
            listOf(
                PersonData("0", "Luca", "Bianchi", "luca_bianchi", "CEO", "Owner", ""),
                PersonData("1", "Sofia", "Esposito", "sofia_esposito", "Marketing Director", "", ""),
            ).sortedBy { it.name },
            listOf("#test1", "#test2")),
        ToDoTask("6", "Task 3", "Scheduled", 1, "Never", "2024-05-07T13:36:00+02:00", "2024-04-02T11:00:00+02:00",
            listOf(
                PersonData("0", "Name1ejwnewjneees", "Surname1fskfsmkfnsk", "username1", "CTO", "Admin", ""),
                PersonData("1", "Giulia", "Ricci", "giulia_ricci", "HR Manager", "", ""),
            ).sortedBy { it.name },
            listOf("#test4", "#test5")),
        ToDoTask("7", "Task 4", "Scheduled", 0, "Monthly", "2024-05-30T12:12:00+02:00", "2024-04-02T12:00:00+02:00",
            listOf(
                PersonData("0", "Sofia", "Esposito", "sofia_esposito", "Marketing Director", "", ""),
                PersonData("1", "Giulia", "Ricci", "giulia_ricci", "HR Manager", "", ""),
            ).sortedBy { it.name },
            listOf("#test1", "#test2")),
        ToDoTask("8", "Task 5", "Scheduled", 1, "Yearly", "2024-05-07T22:21:00+02:00", "2024-04-03T08:00:00+02:00",
            listOf(
                PersonData("0", "Sofia", "Esposito", "sofia_esposito", "Marketing Director", "", ""),
                PersonData("1", "Giulia", "Ricci", "giulia_ricci", "HR Manager", "", ""),
            ).sortedBy { it.name },
            listOf("#test4", "#test5"))
    ))

    // Provide an immutable view of the messages to the UI
    //val toDoTasks: List<toDoTask> get() = _toDoTasks.value


    //---Task Name---
    var taskNameValue by mutableStateOf("Task name")
        private set
    var taskNameError by mutableStateOf("")
        private set
    fun setTaskName(n: String) {
        taskNameValue = n
    }
    private fun checkTaskName() {
        // Remove leading and trailing spaces
        val trimmedTaskName = taskNameValue.trim()

        taskNameError = if (trimmedTaskName.isBlank()) {
            "Task name cannot be blank!"
        } else if (!trimmedTaskName.matches(Regex("^(?=.*[a-zA-Z0-9])[a-zA-Z0-9 ]{1,50}\$"))) {
            "Max 50 characters. Only letters, numbers and spaces are allowed!"
        } else if (_toDoTasks.value.any { it.taskName.equals(trimmedTaskName, ignoreCase = true) }) {
            "A task with this name already exists!"
        } else {
            ""
        }

        // Update the taskNameValue with the trimmed version if there are no errors
        if (taskNameError.isBlank()) {
            taskNameValue = trimmedTaskName
        }
    }


    var taskTagsList by mutableStateOf(listOf(
        "#test1", "#test2", "#test3", "#test4", "#test5", "#test6",
        "#test7", "#test8", "#test9"
    ).sorted())
        private set

    private val _selectedTags = mutableStateListOf<String>()
    val selectedTags: List<String> = _selectedTags

    fun addTag(tag: String) {
        if (!_selectedTags.contains(tag)) {
            _selectedTags.add(tag)
        }
    }
    fun removeTag(tag: String) {
        _selectedTags.remove(tag)
    }

    //---Priority---
    var taskPriority by mutableIntStateOf(0)
        private set

    fun setPriority(priority: Int) {
        taskPriority = priority
    }

    //---Repeats---
    var recurrencyOptions by mutableStateOf(listOf("Never", "Weekly", "Monthly", "Yearly"))
    var expandedRecurrenceDropdown by mutableStateOf(false)
    fun setExpandedRecurrencDropdown(value: Boolean) {
        expandedRecurrenceDropdown = value
    }

    var selectedTextForRecurrence by mutableStateOf(recurrencyOptions[0])

    fun setTaskRecurrency(n: String) {
        selectedTextForRecurrence = n
    }

    //---Status---
    var taskStatus by mutableStateOf("Scheduled")
        private set

    /*fun setStatus(status: String) {
        taskStatus = status
    }*/

    var isInfoEditing by mutableStateOf(false)
        private set

    fun setIsInfoEditing(value: Boolean) {
        isInfoEditing = value
    }

    fun validateCreateTask() {
        checkTaskName()
        checkSelectedDueDateTimeError()
        if(taskNameError.isBlank() && selectedDueDateTimeError.isBlank()) {
            setIsInfoEditing(false)
        }
    }
}

/*
data class Tag(
    val id: Int,
    val name: String,
)


@Composable
fun Tag(
    tag: Tag,
    onTagRemoved: (Tag) -> Unit,
    //modifier: Modifier
) {
    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography

    Row(
        modifier = Modifier
            .padding(8.dp)
            .background(palette.primaryContainer)
            .clickable { onTagRemoved(tag) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = tag.name,
            style = typography.labelSmall
        )

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(onClick = { onTagRemoved(tag) }) {
            Image(
                painter = painterResource(id = R.drawable.outline_close_24),
                contentDescription = "Delete tag",
                colorFilter = ColorFilter.tint(palette.onSurface)
            )
        }
    }
}
*/

@Composable
fun ScrollingGrid(
    tags: List<String>,
    modifier: Modifier,
    //onTagRemoved: (String) -> Unit
) {
    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography

    if(tags.isNotEmpty()){
        LazyVerticalGrid(
            columns = GridCells.FixedSize(120.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = modifier
                .heightIn(max = 140.dp)
                .border(1.dp, palette.onSurfaceVariant, RoundedCornerShape(5))
                .padding(10.dp)
        ) {
            items(tags.size) { index ->
                val tag = tags[index]
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(
                            palette.background,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    /* TO HAVE TAGS REMOVABLE DIRECTLY FROM THE BADGES:
                    Image(
                        painter = painterResource(id = R.drawable.outline_close_24),
                        contentDescription = "Remove",
                        modifier = Modifier
                            .size(25.dp)
                            .clickable(onClick = { onTagRemoved(tag) })
                            .padding(4.dp),
                        colorFilter = ColorFilter.tint(palette.onSurface)
                    ) */
                    Text(
                        text = tag,
                        style = typography.labelSmall,
                        modifier = Modifier.padding(4.dp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    } else {
        Text("No tags selected for this task", style = typography.bodySmall)
    }
}


@Composable
fun InfoRow(
    labelValue: String,
    value: String,
) {
    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography

    Row {
        Text(
            labelValue,
            style = typography.bodySmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(end = 5.dp),
            color = palette.onSurfaceVariant,
            maxLines = 1
        )
    }
    Row {
        Text(
            value,
            style = typography.bodySmall
        )
    }
}

@Composable
fun InfoSection(
    createdBy: String,
    creationDate: String,
    taskName: String,
    dueDate: String,
    selectedTags: List<String>,
    //addTag: (String) -> Unit,
    //removeTag: (String) -> Unit,
    taskPriority: Int,
    priorityOpt: List<String>,
    taskRecurrence: String,
    taskStatus: String,
    setIsInfoEditing: (Boolean) -> Unit
){
    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography

    Scaffold(
        bottomBar = {}
    ) {
        Box(
            modifier = Modifier
                .padding(it)
                .padding(16.dp)
                .fillMaxSize()
                .shadow(elevation = 10.dp, shape = RoundedCornerShape(10.dp))
                .background(palette.surfaceVariant)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                item {
                    InfoRow(labelValue = "Created By: ", value = createdBy)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    InfoRow(labelValue = "Creation Date: ", value =  if (creationDate == "") {""} else {"${creationDate.split('T')[0]}, ${creationDate.split('T')[1].split('+')[0].split(':')[0]}:${creationDate.split('T')[1].split('+')[0].split(':')[1]}"})
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    InfoRow(labelValue = "Task Name: ", value = taskName)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    InfoRow(labelValue = "Due Date: ", value =  if (dueDate == "") {""} else {"${dueDate.split('T')[0]}, ${dueDate.split('T')[1].split('+')[0].split(':')[0]}:${dueDate.split('T')[1].split('+')[0].split(':')[1]}"})
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    InfoRow(labelValue = "Recurrence: ", value = taskRecurrence)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    InfoRow(labelValue = "Priority: ", value = priorityOpt[taskPriority] + " task")
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    InfoRow(labelValue = "Status: ", value = taskStatus)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    Row {
                        Text(
                            text = "Tags: ",
                            style = typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 5.dp, end = 5.dp),
                            color = palette.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                    Row {
                        ScrollingGrid(selectedTags, Modifier)
                    }
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }
            }
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomEnd
        ) {
            FloatingActionButton(
                onClick = { setIsInfoEditing(true) },
                containerColor = palette.secondary,
                modifier = Modifier.padding(25.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.outline_edit_24),
                    contentDescription = "Add",
                    modifier = Modifier.size(30.dp),
                    colorFilter = ColorFilter.tint(palette.background)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagsDropdownMenu(tags: List<String>, selectedTags: List<String>,
                     addTag: (String) -> Unit,
                     removeTag: (String) -> Unit
) {
    var isExpanded by remember {
        mutableStateOf(false)
    }
    val palette = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 0.dp, end = 0.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        ExposedDropdownMenuBox(
            expanded = isExpanded,
            onExpandedChange = { isExpanded = it }
        ) {
            TextField(
                value = selectedTags.joinToString(", "),
                onValueChange = {},
                label = { Text("Tags", color = if(isExpanded) palette.secondary else palette.onSurface) },
                placeholder = {
                    Text(text = "Select some tags")
                },
                readOnly = true, // Makes the TextField clickable
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
                },
                modifier = Modifier
                    .menuAnchor()
                    .background(palette.background),
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
                expanded = isExpanded,
                onDismissRequest = { isExpanded = false },
                modifier = Modifier.background(palette.background).heightIn(max = 235.dp).exposedDropdownSize()
            ) {
                tags.forEach { tag ->
                    AnimatedContent(
                        targetState = selectedTags.contains(tag),
                        label = "Animate the selected item"
                    ) { isSelected ->
                        if (isSelected) {
                            DropdownMenuItem(
                                text = {
                                    Text(text = tag)
                                },
                                onClick = {
                                    removeTag(tag)
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null
                                    )
                                }
                            )
                        } else {
                            DropdownMenuItem(
                                text = {
                                    Text(text = tag)
                                },
                                onClick = {
                                    addTag(tag)
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun EditInfoSection(
    taskName: String, setTaskTitle: (String) -> Unit, taskNameError: String,
    selectedDueDateTime: String, selectedDueDateTimeError: String, setDueDateDateTime: (String) -> Unit,
    showDueDatePicker: Boolean, setShowingDueDatePicker: (Boolean) -> Unit,
    showDueTimePicker: Boolean, setShowingDueTimePicker: (Boolean) -> Unit,
    taskPriority: Int,
    setPriority: (Int) -> Unit,
    recurrencyOptions: List<String>, expandedRecurrenceDropdown: Boolean, setExpandedRecurrencDropdown: (Boolean) -> Unit,
    selectedTextForRecurrence: String, setTaskRecurrency: (String) -> Unit,
    //setIsInfoEditing: (Boolean) -> Unit,
    taskTagsList: List<String>, selectedTags: List<String>,
    addTag: (String) -> Unit,
    removeTag: (String) -> Unit,
    validateCreateTask: () -> Unit
){
    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography

    Scaffold(
        bottomBar = {}
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = taskName,
                    onValueChange = setTaskTitle,
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
            }

            item {
                DateTimePicker(
                    selectedDueDateTime, selectedDueDateTimeError, setDueDateDateTime,
                    showDueDatePicker, setShowingDueDatePicker,
                    showDueTimePicker, setShowingDueTimePicker
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                RecurrencyDropdownMenu(
                    recurrencyOptions, expandedRecurrenceDropdown, setExpandedRecurrencDropdown,
                    selectedTextForRecurrence, setTaskRecurrency
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                TagsDropdownMenu(taskTagsList, selectedTags,
                    addTag, removeTag
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                CustomToggle(
                    label = "Priority",
                    opt = listOf("Priority", "Non priority"),
                    customTempState = taskPriority,
                    customSetTempState = setPriority,
                    isInCreateTaskOrTaskInfoEdit = true
                )
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomEnd      // Align the content to the bottom end of the Box
        ) {
            // Floating Action Button at the bottom end
            FloatingActionButton(
                onClick = {
                    validateCreateTask()
                },
                containerColor = palette.secondary,
                modifier = Modifier.padding(25.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.outline_done_24),
                    contentDescription = "Add",
                    modifier = Modifier.size(30.dp),
                    colorFilter = ColorFilter.tint(palette.background)
                )
            }
        }
    }
}

@Composable
fun Info(
    task: Task,
    creator: String,
    vm: InfoViewModel = viewModel(),
) {
    if(!vm.isInfoEditing){
        InfoSection(
            createdBy = creator,
            creationDate = task.creationDate,
            taskName = task.title,
            dueDate = task.deadline,
            selectedTags = task.tags,
            //addTag = vm::addTag,
            //removeTag = vm::removeTag,
            taskPriority = if (task.prioritized) 0 else 1,
            priorityOpt = vm.priorityOptions,
            taskRecurrence = task.recurrence,
            taskStatus = task.status,
            setIsInfoEditing = vm::setIsInfoEditing
        )
    } else {
        EditInfoSection(
            taskName = vm.taskNameValue,
            setTaskTitle = vm::setTaskName,
            taskNameError = vm.taskNameError,
            vm.selectedDueDateTime, vm.selectedDueDateTimeError, vm::setDueDateDateTime,
            vm.showDueDatePicker, vm::setShowingDueDatePicker,
            vm.showDueTimePicker, vm::setShowingDueTimePicker,
            taskPriority = vm.taskPriority,
            setPriority = vm::setPriority,
            recurrencyOptions = vm.recurrencyOptions, expandedRecurrenceDropdown = vm.expandedRecurrenceDropdown,
            setExpandedRecurrencDropdown = vm::setExpandedRecurrencDropdown,
            selectedTextForRecurrence = vm.selectedTextForRecurrence, setTaskRecurrency = vm::setTaskRecurrency,
            //setIsInfoEditing = vm::setIsInfoEditing,
            taskTagsList = vm.taskTagsList, selectedTags = vm.selectedTags,
            addTag = vm::addTag,
            removeTag = vm::removeTag,
            validateCreateTask = vm::validateCreateTask
        )
    }
}