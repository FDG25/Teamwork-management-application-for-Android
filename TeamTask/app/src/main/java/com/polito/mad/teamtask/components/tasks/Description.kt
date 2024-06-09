package com.polito.mad.teamtask.components.tasks

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.polito.mad.teamtask.Actions
import com.polito.mad.teamtask.AppFactory
import com.polito.mad.teamtask.AppModel
import com.polito.mad.teamtask.R
import com.polito.mad.teamtask.ui.theme.TeamTaskTypography


class DescriptionViewModel(private val model: AppModel) : ViewModel() {
    //    var finalDescription =
//        "In any industry where the people behind a company are as important as the company itself, you’re likely to find a kind of expanded “about” page that includes information on individual employees. “Meet the Team” pages are popular among web design and other creative firms, but are also found on sites within various other industries. These pages are a valuable addition to any site where human contact is an important part of the industry. It adds a personal touch to the company and can lend trust to visitors."
//
//    fun setMyFinalDescription(value: String) {
//        finalDescription = value
//    }
    var myTeamDescription = mutableStateOf(TextFieldValue(""))
        private set

    fun setMyTeamDescription(value: TextFieldValue) {
        myTeamDescription.value = value
    }

    var myTaskDescription = mutableStateOf(TextFieldValue(""))
        private set

    fun setMyTaskDescription(value: TextFieldValue) {
        myTaskDescription.value = value
    }

    var isDescriptionEditing by mutableStateOf(false)
        private set

    fun setIsDescriptionEditing(value: Boolean) {
        isDescriptionEditing = value
    }

    fun updateTeamDescriptionDB(teamId: String) {
        model.updateTeamDescription(teamId, myTeamDescription.value.text)
        Actions.getInstance().navigateBack()
    }

    fun updateTaskDescriptionDB(taskId: String) {
        model.updateTaskDescription(taskId, myTaskDescription.value.text)
    }
}

@Composable
fun Description(
    descriptionValue: String,
    setDescription: (String) -> Unit,
    vm: DescriptionViewModel = viewModel(factory = AppFactory(LocalContext.current))
) {
    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography
    val currentRoute = Actions.getInstance().getCurrentRoute()


    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(vm.isDescriptionEditing) {
        if (vm.isDescriptionEditing) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    Box {
        if (currentRoute != "teams/{teamId}/newTask/status") {
            if (!vm.isDescriptionEditing) {
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize()
                        .shadow(elevation = 10.dp, shape = RoundedCornerShape(10.dp))
                        .background(palette.surfaceVariant)
                ) {
                    Text(
                        text = descriptionValue,
                        modifier = Modifier.padding(16.dp),
                        style = typography.labelSmall.copy(
                            color = palette.onSurface,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            lineHeight = 23.sp
                        )
                    )
                }

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    if (currentRoute != "teams/{teamId}/newTask/status") {
                        FloatingActionButton(
                            onClick = {
                                vm.setIsDescriptionEditing(true)
                                //Actions.getInstance().goToEditTeamDescription(teamId)
                            },
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
            } else {
                BasicTextField(
                    value = descriptionValue,
                    onValueChange = { newValue ->
                        if (newValue.length <= 200) { // Prevent input past max length
                            setDescription(newValue)
                        }
                    },
                    keyboardOptions = KeyboardOptions.Default,
                    textStyle = typography.labelSmall.copy(
                        color = palette.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 23.sp
                    ),
                    cursorBrush = SolidColor(palette.onSurface),
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .onFocusChanged { focusState ->
                            if (!focusState.isFocused) {
                                keyboardController?.show()
                            }
                        },
                    decorationBox = { innerTextField ->
                        LazyColumn(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxSize()
                                .border(1.dp, palette.secondary)
                                .shadow(elevation = 10.dp, shape = RoundedCornerShape(10.dp))
                                .background(palette.surfaceVariant)
                        ) {
                            item {
                                if (descriptionValue.isNotEmpty()) {
                                    Text(
                                        "Description (${200 - descriptionValue.length} characters left)",
                                        style = typography.bodySmall,
                                        color = palette.onSurfaceVariant,
                                        modifier = Modifier.padding(start = 16.dp, top = 10.dp)
                                    )
                                    Box(modifier = Modifier.padding(16.dp)) {
                                        innerTextField()
                                    }
                                } else {
                                    Box(modifier = Modifier.padding(start = 16.dp, top = 10.dp)) {
                                        Text(
                                            "Insert at most ${200 - descriptionValue.length} characters",
                                            style = typography.headlineSmall,
                                            color = palette.onSurfaceVariant
                                        )
                                    }
                                    Box(modifier = Modifier.padding(16.dp)) {
                                        innerTextField()
                                    }
                                }
                            }
                        }
                    }
                )
                //if ((currentRoute != "teams/{teamId}/newTask/people")) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    FloatingActionButton(
                        onClick = {
                            setDescription(descriptionValue)
                            vm.setIsDescriptionEditing(false)
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
                //}
            }
        } else {
            BasicTextField(
                value = descriptionValue,
                onValueChange = { newValue ->
                    if (newValue.length <= 200) { // Prevent input past max length
                        setDescription(newValue)
                    }
                },
                keyboardOptions = KeyboardOptions.Default,
                textStyle = typography.labelSmall.copy(
                    color = palette.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 23.sp
                ),
                cursorBrush = SolidColor(palette.onSurface),
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusState ->
                        if (!focusState.isFocused) {
                            keyboardController?.show()
                        }
                    },
                decorationBox = { innerTextField ->
                    LazyColumn(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxSize()
                            .border(1.dp, palette.secondary)
                            .shadow(elevation = 10.dp, shape = RoundedCornerShape(10.dp))
                            .background(palette.surfaceVariant)
                    ) {
                        item {
                            if (descriptionValue.isNotEmpty()) {
                                Text(
                                    "Description (${200 - descriptionValue.length} characters left)",
                                    style = typography.bodySmall,
                                    color = palette.onSurfaceVariant,
                                    modifier = Modifier.padding(start = 16.dp, top = 10.dp)
                                )
                                Box(modifier = Modifier.padding(16.dp)) {
                                    innerTextField()
                                }
                            } else {
                                Box(modifier = Modifier.padding(start = 16.dp, top = 10.dp)) {
                                    Text(
                                        "Insert at most ${200 - descriptionValue.length} characters",
                                        style = typography.headlineSmall,
                                        color = palette.onSurfaceVariant
                                    )
                                }
                                Box(modifier = Modifier.padding(16.dp)) {
                                    innerTextField()
                                }
                            }
                        }
                    }
                }
            )

        }
    }

}

//Edit of a task description
@Composable
fun DescriptionVariant(
    descriptionValue: String,
    setDescription: (String) -> Unit,
    taskId: String,
    isInCreation: Boolean,
    vm: DescriptionViewModel = viewModel(factory = AppFactory(LocalContext.current))
) {
    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(vm.isDescriptionEditing) {
        if (vm.isDescriptionEditing) {
            vm.setMyTaskDescription(
                TextFieldValue(
                    descriptionValue,
                    selection = TextRange(descriptionValue.length)
                )
            )
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    Box {
        if (!vm.isDescriptionEditing) {
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
                    .shadow(elevation = 10.dp, shape = RoundedCornerShape(10.dp))
                    .background(palette.surfaceVariant)
            ) {
                Text(
                    text = descriptionValue,
                    modifier = Modifier.padding(16.dp),
                    style = typography.labelSmall.copy(
                        color = palette.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 23.sp
                    )
                )
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomEnd
            ) {
                    FloatingActionButton(
                        onClick = {
                            vm.setIsDescriptionEditing(true)
                            //Actions.getInstance().goToEditTeamDescription(teamId)
                        },
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
        } else {
            BasicTextField(
                value = vm.myTaskDescription.value,
                onValueChange = { newValue ->
                    if (newValue.text.length <= 200) { // Prevent input past max length
                        vm.setMyTaskDescription(newValue)
                    }
                },
                keyboardOptions = KeyboardOptions.Default,
                textStyle = typography.labelSmall.copy(
                    color = palette.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 23.sp
                ),
                cursorBrush = SolidColor(palette.onSurface),
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusState ->
                        if (!focusState.isFocused) {
                            keyboardController?.show()
                        }
                    },
                decorationBox = { innerTextField ->
                    LazyColumn(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxSize()
                            .border(1.dp, palette.secondary)
                            .shadow(elevation = 10.dp, shape = RoundedCornerShape(10.dp))
                            .background(palette.surfaceVariant)
                    ) {
                        item {
                            if (descriptionValue.isNotEmpty()) {
                                Text(
                                    "Description (${200 - descriptionValue.length} characters left)",
                                    style = typography.bodySmall,
                                    color = palette.onSurfaceVariant,
                                    modifier = Modifier.padding(start = 16.dp, top = 10.dp)
                                )
                                Box(modifier = Modifier.padding(16.dp)) {
                                    innerTextField()
                                }
                            } else {
                                Box(modifier = Modifier.padding(start = 16.dp, top = 10.dp)) {
                                    Text(
                                        "Insert at most ${200 - descriptionValue.length} characters",
                                        style = typography.headlineSmall,
                                        color = palette.onSurfaceVariant
                                    )
                                }
                                Box(modifier = Modifier.padding(16.dp)) {
                                    innerTextField()
                                }
                            }
                        }
                    }
                }
            )
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomEnd
            ) {
                FloatingActionButton(
                    onClick = {
                        if (!isInCreation)
                            vm.updateTaskDescriptionDB(taskId = taskId)
                        else
                            setDescription(vm.myTaskDescription.value.text)
                        vm.setIsDescriptionEditing(false)
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

}

//Edit only for a Team
@Composable
fun EditTeamDescription(
    descriptionValue: String,
    teamId: String,
    vm: DescriptionViewModel = viewModel(factory = AppFactory(LocalContext.current))
) {
    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val isEffectLaunched = rememberSaveable { mutableStateOf(false) }

    if (!isEffectLaunched.value) {
        LaunchedEffect(Unit) {
            vm.setMyTeamDescription(
                TextFieldValue(
                    descriptionValue,
                    selection = TextRange(descriptionValue.length)
                )
            )
            focusRequester.requestFocus()
            isEffectLaunched.value = true
        }
    }

    BasicTextField(
        value = vm.myTeamDescription.value,
        onValueChange = { newValue ->
            if (newValue.text.length <= 200) { // Prevent input past max length
                vm.setMyTeamDescription(newValue)
            }
        },
        keyboardOptions = KeyboardOptions.Default,
        textStyle = typography.labelSmall.copy(
            color = palette.onSurface,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 23.sp
        ),
        cursorBrush = SolidColor(palette.onSurface),
        modifier = Modifier
            .focusRequester(focusRequester)
            .onFocusChanged { focusState ->
                if (!focusState.isFocused) {
                    keyboardController?.show()
                }
            },
        decorationBox = { innerTextField ->
            LazyColumn(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
                    .border(1.dp, palette.secondary)
                    .shadow(elevation = 10.dp, shape = RoundedCornerShape(10.dp))
                    .background(palette.surfaceVariant)
            ) {
                item {
                    if (descriptionValue.isNotEmpty()) {
                        Text(
                            "Description (${200 - descriptionValue.length} characters left)",
                            style = typography.bodySmall,
                            color = palette.onSurfaceVariant,
                            modifier = Modifier.padding(start = 16.dp, top = 10.dp)
                        )
                        Box(modifier = Modifier.padding(16.dp)) {
                            innerTextField()
                        }
                    } else {
                        Box(modifier = Modifier.padding(start = 16.dp, top = 10.dp)) {
                            Text(
                                "Insert at most ${200 - descriptionValue.length} characters",
                                style = typography.headlineSmall,
                                color = palette.onSurfaceVariant
                            )
                        }
                        Box(modifier = Modifier.padding(16.dp)) {
                            innerTextField()
                        }
                    }
                }
            }
        }
    )
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        FloatingActionButton(
            onClick = {
                vm.updateTeamDescriptionDB(teamId)
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

//View Description only for a team
@Composable
fun DescriptionViewOnly(
    descriptionValue: String,
    teamId: String
) {
    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography

    Box {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
                .shadow(elevation = 10.dp, shape = RoundedCornerShape(10.dp))
                .background(palette.surfaceVariant)
        ) {
            Text(
                text = descriptionValue,
                modifier = Modifier.padding(16.dp),
                style = typography.labelSmall.copy(
                    color = palette.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 23.sp
                )
            )
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomEnd
        ) {
            FloatingActionButton(
                onClick = {
                    Actions.getInstance().goToEditTeamDescription(teamId)
                },
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