package com.polito.mad.teamtask.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.polito.mad.teamtask.Actions
import com.polito.mad.teamtask.screens.TeamsViewModel
import com.polito.mad.teamtask.ui.theme.TeamTaskTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryFilterScreen(
    teamsVM: TeamsViewModel
) {
    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography

    val categories = categories

    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            //label
            Text(
                text = "Category",
                style = typography.labelMedium,
                color = palette.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            //Dropbox
            ExposedDropdownMenuBox(
                expanded = isExpanded,
                onExpandedChange = { isExpanded = it }
            ) {
                TextField(
                    value = teamsVM.selectedCategories.joinToString(", "),
                    onValueChange = {},
                    label = {
                        Text(
                            "Category",
                            color = if (isExpanded) palette.secondary else palette.onSurface
                        )
                    },
                    placeholder = {
                        Text(text = "Select some categories")
                    },
                    readOnly = true, // Makes the TextField clickable
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
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
                    modifier = Modifier
                        .background(palette.background)
                        .heightIn(max = 235.dp)
                        .exposedDropdownSize()
                ) {
                    categories.forEach { category ->
                        AnimatedContent(
                            targetState = teamsVM.selectedCategories.contains(category),
                            label = "Animate the selected item"
                        ) { isSelected ->
                            if (isSelected) {
                                DropdownMenuItem(
                                    text = {
                                        Text(text = category)
                                    },
                                    onClick = {
                                        teamsVM.removeCategory(category)
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
                                        Text(text = category)
                                    },
                                    onClick = {
                                        teamsVM.addCategory(category)
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
        //Bottom bar
        BottomAppBar(
            containerColor = palette.background,
            contentColor = palette.secondary,
            modifier = Modifier.height(65.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Clear button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Button(
                        onClick = {
                            teamsVM.clearSelectedCategories()
                            teamsVM.setIsFilterActive(false)
                            Actions.getInstance().goToTeams()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.primary,
                            contentColor = palette.secondary
                        )
                    ) {
                        Text(
                            "Clear",
                            style = typography.bodyMedium,
                            color = palette.secondary
                        )
                    }
                }

                // Apply button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Button(
                        onClick = {
                            teamsVM.setIsFilterActive(true)
                            Actions.getInstance().goToTeams()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.secondary,
                            contentColor = palette.background
                        )
                    ) {
                        Text(
                            "Apply",
                            style = typography.bodyMedium,
                            color = palette.background
                        )
                    }
                }
            }
        }
    }

}