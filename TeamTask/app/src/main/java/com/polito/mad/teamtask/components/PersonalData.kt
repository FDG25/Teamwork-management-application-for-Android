package com.polito.mad.teamtask.components

import android.net.Uri
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.polito.mad.teamtask.R
import com.polito.mad.teamtask.ui.theme.TeamTaskTypography


// --- Profile picture ---
@Composable
fun ProfilePictureSection (
    name: String,
    surname: String,
    username: String,
    isEditingProfile: Boolean,
    imageUri: Uri? = null,
    setShowBottomMenu: (Boolean) -> Unit = {}
) {
    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography

    Box {
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .border(1.dp, palette.secondary, CircleShape)
                .background(color = palette.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null && imageUri != Uri.EMPTY) { // User set an image
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUri)
                        .crossfade(true)
                        //.error()
                        .build(),
                    contentDescription = "Profile",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .border(1.dp, palette.secondary, CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else { // No image set
                Text(
                    text = "${name.firstOrNull()?.uppercaseChar() ?: ""}${
                        surname.firstOrNull()?.uppercaseChar() ?: ""
                    }",
                    modifier = Modifier
                        .padding(top = 6.dp),
                    style = typography.headlineLarge,
                    color = palette.onSurface
                )
            }
        }

        if(isEditingProfile) { // Edit profile image
            IconButton(
                onClick = {
                    setShowBottomMenu(true)
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(palette.primary)
                    .border(1.dp, palette.secondary, shape = CircleShape)
                    .align(Alignment.BottomEnd)
                    .size(35.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.outline_camera_alt_24),
                    contentDescription = "Profile",
                    modifier = Modifier.fillMaxSize(0.7f)
                )
            }
        }
    }

    if (!isEditingProfile) {
        Text(
            text = username,
            modifier = Modifier
                .padding(top = 6.dp),
            style = typography.bodyLarge,
            color = palette.onSurface
        )
    }
}


// ----- Profile info -----
@Composable
fun ProfileInfoSection (
    nameValue: String, nameError: String, setName: (String) -> Unit,
    surnameValue: String, surnameError: String, setSurname: (String) -> Unit,
    emailAddressValue: String, emailAddressError: String, setEmailAddress: (String) -> Unit,
    usernameValue: String, usernameError: String, setUsername: (String) -> Unit,
    locationValue: String, locationError: String, setLocation: (String) -> Unit,
    descriptionValue: String, descriptionError: String, setDescription: (String) -> Unit,
    isEditingProfile: Boolean
) {
    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography

    if (!isEditingProfile) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            // Name
            Row {
                Column (
                    modifier = Modifier.fillMaxWidth(0.3f)
                ) {
                    Text("Name", style = typography.bodyMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 5.dp), color = palette.onSurfaceVariant, maxLines = 1)
                }
                Column (
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(nameValue, style = typography.bodyMedium, color = palette.onSurface)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Surname
            Row {
                Column (
                    modifier = Modifier.fillMaxWidth(0.3f)
                ) {
                    Text("Surname", style = typography.bodyMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 5.dp), color = palette.onSurfaceVariant, maxLines = 1)
                }
                Column (
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(surnameValue, style = typography.bodyMedium, color = palette.onSurface)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Email
            Row {
                Column (
                    modifier = Modifier.fillMaxWidth(0.3f)
                ) {
                    Text("Email", style = typography.bodyMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 5.dp), color = palette.onSurfaceVariant)
                }
                Column (
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(emailAddressValue, style = typography.bodyMedium, color = palette.onSurface)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Location
            Row {
                Column (
                    modifier = Modifier.fillMaxWidth(0.3f)
                ) {
                    Text("Location", style = typography.bodyMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 5.dp), color = palette.onSurfaceVariant, maxLines = 1)
                }
                Column (
                    modifier = Modifier.fillMaxWidth(0.65f)
                ) {
                    Text(locationValue, style = typography.bodyMedium, color = palette.onSurface)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bio
            Row {
                Column (
                    modifier = Modifier.fillMaxWidth(0.3f)
                ) {
                    Text("Bio", style = typography.bodyMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 5.dp), color = palette.onSurfaceVariant, maxLines = 1)
                }
                Column (
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (descriptionValue == "") {
                        Text("Hey, there! I am using TeamTask", style = typography.bodyMedium, fontStyle = FontStyle.Italic)
                    } else {
                        Text(descriptionValue, style = typography.bodyMedium, color = palette.onSurface)
                    }
                }
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Username
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = usernameValue,
                onValueChange = setUsername,
                label = { Text("Username") },
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
                isError = usernameError.isNotBlank()
            )
            if (usernameError.isNotBlank()) {
                Text(
                    text = usernameError,
                    color = palette.error,
                    style = typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    maxLines = 3
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Name
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = nameValue,
                onValueChange = setName,
                label = { Text("Name") },
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
                isError = nameError.isNotBlank()
            )
            if (nameError.isNotBlank()) {
                Text(
                    text = nameError,
                    color = palette.error,
                    style = typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    maxLines = 3
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Surname
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = surnameValue,
                onValueChange = setSurname,
                label = { Text("Surname") },
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
                isError = surnameError.isNotBlank()
            )
            if (surnameError.isNotBlank()) {
                Text(
                    text = surnameError,
                    color = palette.error,
                    style = typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    maxLines = 3
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Email
            /*TextField(
                modifier = Modifier.fillMaxWidth(),
                value = emailAddressValue,
                onValueChange = setEmailAddress,
                label = { Text("Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
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
                isError = emailAddressError.isNotBlank()
            )
            if (emailAddressError.isNotBlank()) {
                Text(
                    text = emailAddressError,
                    color = palette.error,
                    style = typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    maxLines = 3
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            */
            // Location
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = locationValue,
                onValueChange = setLocation,
                label = { Text("Location") },
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
                isError = locationError.isNotBlank()
            )
            if (locationError.isNotBlank()) {
                Text(
                    text = locationError,
                    color = palette.error,
                    style = typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    maxLines = 3
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bio
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = descriptionValue,
                onValueChange = { newValue ->
                    if (newValue.length <= 70) {
                        setDescription(newValue)
                    }
                },
                label = { Text("Bio (${70 - descriptionValue.length} characters left)") },
                placeholder = {
                    Text(
                        "Insert at most ${70 - descriptionValue.length} characters",
                        style = typography.bodySmall,
                        color = palette.onSurfaceVariant
                    )
                },
                singleLine = false,
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
                )
            )
            if (descriptionError.isNotBlank()) {
                Text(
                    text = descriptionError,
                    color = palette.error,
                    style = typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    maxLines = 3
                )
            }
        }
    }
}

@Composable
fun ProfileVerification (
    emailVerified: Boolean,
    loginMethod: String
) {
    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography

    Column (
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        /*
        if (emailVerified && loginMethod=="email") {
            Row (
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Image (
                    painter = painterResource(R.drawable.outline_done_24),
                    contentDescription = "Email verified",
                    colorFilter = ColorFilter.tint(palette.inversePrimary)
                )

                Spacer(Modifier.width(12.dp))

                Text(
                    text = "Email verified",
                    style = typography.bodySmall,
                    color = palette.inversePrimary,
                    modifier = Modifier.padding(top = 1.dp)
                )
            }
        } else if (!emailVerified &&
            loginMethod=="email") {
            Row (
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Image (
                    painter = painterResource(R.drawable.outline_close_24),
                    contentDescription = "Email not verified",
                    colorFilter = ColorFilter.tint(palette.error)
                )

                Spacer(Modifier.width(12.dp))

                Text(
                    text = "Email not verified",
                    style = typography.bodySmall,
                    color = palette.error,
                    modifier = Modifier.padding(top = 1.dp)
                )
            }
        }
         */

        if (loginMethod=="email") {
            Row (
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Image (
                    painter = painterResource(R.drawable.email_logo),
                    contentDescription = "Email Logo",
                )

                Spacer(Modifier.width(12.dp))

                Text (
                    text = "Account created with Email",
                    style = typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    color = palette.onSurfaceVariant
                )
            }
        }
        if (loginMethod=="google") {
            Row (
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Image (
                    painter = painterResource(R.drawable.google_logo),
                    contentDescription = "Google Logo",
                )

                Spacer(Modifier.width(12.dp))

                Text (
                    text = "Account created with Google",
                    style = typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    color = palette.onSurfaceVariant
                )
            }
        }
    }
}
