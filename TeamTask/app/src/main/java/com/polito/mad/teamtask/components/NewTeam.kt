package com.polito.mad.teamtask.components

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.firebase.storage.FirebaseStorage
import com.polito.mad.teamtask.AppFactory
import com.polito.mad.teamtask.CameraActivity
import com.polito.mad.teamtask.R
import com.polito.mad.teamtask.screens.LoadingScreen
import com.polito.mad.teamtask.screens.TeamsViewModel
import com.polito.mad.teamtask.ui.theme.TeamTaskTypography
import kotlinx.coroutines.tasks.await


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditTeamPictureSection(
    imageUri: Uri? = null, setUri: (Uri?) -> Unit = {},
    setStoragePermission: (Boolean) -> Unit = {},
    showBottomSheet: Boolean, setShowBottomMenu: (Boolean) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography

    // Pick from gallery
    val context = LocalContext.current
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            setUri(uri)
        }
    }

    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            setStoragePermission(isGranted)
            if (isGranted) {
                // Launch the gallery picker
                Toast.makeText(context, "Storage permission granted", Toast.LENGTH_SHORT).show()
                galleryLauncher.launch("image/*")
            } else {
                // Permission is denied
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        context as Activity,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                ) {
                    // "Deny" was chosen
                    Toast.makeText(context, "Storage permission denied.", Toast.LENGTH_LONG).show()
                } else {
                    // "Deny & Don't ask again" was chosen --> the user needs to manually change the permission in settings
                    Toast.makeText(
                        context,
                        "Permission denied, allow storage permission in the app settings!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    )

    // Take photo
    val intent = Intent(context, CameraActivity::class.java)

    val cameraActivityLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uriResult = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                result.data?.getParcelableExtra("photoUri", Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                result.data?.getParcelableExtra("photoUri") as Uri?
            }
            if (uriResult != null) {
                setUri(uriResult)
            }
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .border(1.dp, palette.secondary)
                .background(color = palette.surfaceVariant)
                .align(Alignment.Center)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) { // User set an image
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUri)
                        .crossfade(true)
                        .error(R.drawable.baseline_groups_24)
                        .build(),
                    contentDescription = "Profile",
                    modifier = Modifier
                        .fillMaxSize(),
                    contentScale = ContentScale.Crop//, onError = {}
                )
            }
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

    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth()
    ) {
        if (this.maxHeight >= this.maxWidth) {
            if (showBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = { setShowBottomMenu(false) },
                    sheetState = sheetState,
                    containerColor = palette.background
                ) {
                    // Sheet content
                    Column(
                        modifier = Modifier
                            .background(palette.background)
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(bottom = 30.dp)
                    ) {
                        // Label
                        Text(
                            text = "Team Profile Image",
                            style = typography.labelMedium,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(bottom = 10.dp)
                                .background(palette.background)
                        )

                        Divider(color = palette.onSurfaceVariant)

                        Row(modifier = Modifier.navigationBarsPadding()) {
                            Column {
                                // Take photo
                                Button(
                                    onClick = {
                                        cameraActivityLauncher.launch(intent)
                                        setShowBottomMenu(false)
                                        /*
                                        val file = context.createImageFile()
                                        uri = FileProvider.getUriForFile(
                                            Objects.requireNonNull(context), context.packageName + ".provider", file )

                                        val permissionCheck = ContextCompat.checkSelfPermission(
                                            context, android.Manifest.permission.CAMERA
                                        )

                                        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                            cameraLauncher.launch(uri)
                                        } else {
                                            permissionLauncher.launch(android.Manifest.permission.CAMERA)
                                        }
                                        */
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = palette.background)
                                ) {
                                    Box(
                                        modifier = Modifier.background(palette.background)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Image(
                                                painter = painterResource(id = R.drawable.outline_add_a_photo_24),
                                                contentDescription = "Take photo"
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                "Take photo", color = palette.onSurface,
                                                style = typography.labelMedium
                                            )
                                        }
                                    }
                                }

                                // Pick from gallery
                                Button(
                                    onClick = {
                                        // Check if we have permission to read from the gallery
                                        val permission = ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.READ_EXTERNAL_STORAGE
                                        )
                                        if (permission != PackageManager.PERMISSION_GRANTED) {
                                            // We don't have permission, so request it
                                            storagePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                                        } else {
                                            // We have permission, so launch the gallery picker
                                            galleryLauncher.launch("image/*")
                                        }
                                        setShowBottomMenu(false) // Close the bottom sheet in any case
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = palette.background)
                                ) {
                                    Box(
                                        modifier = Modifier.background(palette.background)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Image(
                                                painter = painterResource(id = R.drawable.outline_insert_photo_24),
                                                contentDescription = "Pick from gallery"
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                "Pick from gallery", color = palette.onSurface,
                                                style = typography.labelMedium
                                            )
                                        }
                                    }
                                }

                                // Remove photo
                                if (imageUri != null) {
                                    Button(
                                        onClick = {
                                            setUri(null)
                                            setShowBottomMenu(false) // Close the bottom sheet
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = palette.background)
                                    ) {
                                        Box(
                                            modifier = Modifier.background(palette.background)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Image(
                                                    painter = painterResource(id = R.drawable.outline_delete_outline_24),
                                                    contentDescription = "Remove photo",
                                                    colorFilter = ColorFilter.tint(palette.error)
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    "Remove photo", color = palette.error,
                                                    style = typography.labelMedium,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            if (showBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = {
                        setShowBottomMenu(false)
                    },
                    sheetState = sheetState,
                    containerColor = palette.background,
                    modifier = Modifier.height(160.dp)
                ) {
                    // Sheet content
                    Column(
                        modifier = Modifier
                            .background(palette.background)
                            .navigationBarsPadding()
                    ) {
                        // Label
                        Text(
                            text = "Profile Image",
                            style = typography.labelMedium,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(bottom = 10.dp)
                        )

                        Divider(color = palette.onSurfaceVariant)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Take photo
                            Button(
                                onClick = {
                                    cameraActivityLauncher.launch(intent)
                                    setShowBottomMenu(false)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = palette.background)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.outline_add_a_photo_24),
                                    contentDescription = "Take photo"
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Take photo", color = palette.onSurface,
                                    style = typography.labelSmall
                                )
                            }

                            // Pick from gallery
                            Button(
                                onClick = {
                                    // Check if we have permission to read from the gallery
                                    val permission = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.READ_EXTERNAL_STORAGE
                                    )
                                    if (permission != PackageManager.PERMISSION_GRANTED) {
                                        // We don't have permission, so request it
                                        storagePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                                    } else {
                                        // We have permission, so launch the gallery picker
                                        galleryLauncher.launch("image/*")
                                    }
                                    setShowBottomMenu(false) // Close the bottom sheet in any case
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = palette.background)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.outline_insert_photo_24),
                                    contentDescription = "Pick from gallery"
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Pick from gallery", color = palette.onSurface,
                                    style = typography.labelSmall
                                )
                            }

                            // Remove photo
                            if (imageUri != null) {
                                Button(
                                    onClick = {
                                        setUri(null)
                                        setShowBottomMenu(false)
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = palette.background)
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.outline_delete_outline_24),
                                        contentDescription = "Remove photo",
                                        colorFilter = ColorFilter.tint(palette.error)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "Remove photo", color = palette.error,
                                        style = typography.labelSmall,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewTeam(
    isInCreation: Boolean,
    teamId: String,
    teamImage: String? = null,
    teamName: String? = null,
    teamCategory: String? = null,
    teamsVM: TeamsViewModel = viewModel(factory = AppFactory(LocalContext.current))
) {
    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography

    var isExpandedCategoryDropdown by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(key1 = Unit) {
        if (!isInCreation) {
            if (!teamImage.isNullOrBlank()) {
                val imageRef =
                    FirebaseStorage.getInstance().reference.child("teamImages/$teamImage").downloadUrl.await()
                teamsVM.setUri(imageRef)
            }
            teamsVM.setTeamName(teamName ?: "")
            teamsVM.setMyTeamCategory(teamCategory ?: "")
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(palette.background)
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            //Spacer(modifier = Modifier.height(16.dp))
            item {
                Column {
                    if (isInCreation) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Your team is the place where you",
                                fontSize = 18.sp,
                                color = palette.onSurface
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "collaborate with others.",
                                fontSize = 18.sp,
                                color = palette.onSurface
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Create one and start!",
                                fontSize = 18.sp,
                                color = palette.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))


                    EditTeamPictureSection(
                        teamsVM.imageUri, teamsVM::setUri,
                        teamsVM::setStoragePermission,
                        teamsVM.showBottomSheet, teamsVM::setShowBottomMenu
                    )


                    Spacer(modifier = Modifier.height(50.dp))

                    //Team Name
                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = teamsVM.teamNameValue,
                        onValueChange = teamsVM::setTeamName,
                        label = { Text("Team Name") },
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
                        isError = teamsVM.teamNameError.isNotBlank()
                    )
                    if (teamsVM.teamNameError.isNotBlank()) {
                        Text(
                            text = teamsVM.teamNameError,
                            color = palette.error,
                            style = typography.bodySmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            maxLines = 3
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    //Team Category
                    ExposedDropdownMenuBox(
                        modifier = Modifier
                            .background(palette.background)
                            .heightIn(max = 235.dp)
                            .fillMaxWidth(),
                        expanded = isExpandedCategoryDropdown,
                        onExpandedChange = { isExpandedCategoryDropdown = it }) {
                        TextField(
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .background(palette.background),
                            value = teamsVM.teamCategory,
                            onValueChange = {},
                            readOnly = true,
                            singleLine = true,
                            label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpandedCategoryDropdown) },
                            colors = ExposedDropdownMenuDefaults.textFieldColors(
                                focusedLabelColor = palette.secondary, // Change this to your desired color when the TextField is focused
                                errorLabelColor = palette.error,
                                focusedIndicatorColor = palette.secondary,
                            ),
                            isError = teamsVM.teamCategoryError.isNotBlank()
                        )
                        ExposedDropdownMenu(
                            expanded = isExpandedCategoryDropdown,
                            onDismissRequest = { isExpandedCategoryDropdown = false },
                            modifier = Modifier.background(palette.background)
                        ) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    modifier = Modifier.background(palette.background),
                                    onClick = {
                                        teamsVM.setMyTeamCategory(category)
                                        isExpandedCategoryDropdown = false
                                    },
                                    text = {
                                        Text(
                                            text = category,
                                            style = typography.bodyMedium,
                                            color = palette.onSurface
                                        )
                                    }
                                )
                            }
                        }
                    }
                    if (teamsVM.teamCategoryError.isNotEmpty()) {
                        Text(
                            text = "Category can't be empty",
                            color = palette.error,
                            style = typography.bodySmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            maxLines = 3
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))


                    Button(
                        onClick = {
                            if (isInCreation)
                                teamsVM.validate(true, teamId)
                            else
                                teamsVM.saveTeamChangesToDB(teamId)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.primary,
                            contentColor = palette.secondary
                        )
                    ) {
                        Text(
                            if (isInCreation) {
                                "Create Team"
                            } else {
                                "Save Changes"
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        //in charging state
        if (teamsVM.isLoading) {
            LoadingScreen()
            /*
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = palette.primary
                )
            }
            */
        }

    }
}

val categories = listOf(
    "🗂️ Administration",
    "🧹 Cleaning",
    "💬 Communication",
    "🍳 Cooking",
    "🎉 Events",
    "👨‍👩‍👧‍👦 Family",
    "💰 Finance",
    "🩺 Health",
    "🎨 Hobbies",
    "🏠 Home",
    "🎡 Leisure",
    "🔧 Maintenance",
    "🗂️ Organization",
    "📁 Projects",
    "🛒 Shopping",
    "🏅 Sports",
    "📚 Study",
    "✈️ Travel",
    "🤝 Volunteering",
    "💼 Work",
)
