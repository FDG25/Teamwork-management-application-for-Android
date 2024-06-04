package com.polito.mad.teamtask.screens

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalConfiguration
import com.polito.mad.teamtask.Person
import com.polito.mad.teamtask.Team
import com.polito.mad.teamtask.TeamParticipant
import com.polito.mad.teamtask.components.ProfileInfoSection
import com.polito.mad.teamtask.components.ProfileVerification
import com.polito.mad.teamtask.components.Statistics
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.polito.mad.teamtask.CameraActivity
import com.polito.mad.teamtask.R
import com.polito.mad.teamtask.components.ProfilePictureSection
import com.polito.mad.teamtask.components.StatisticsData
import com.polito.mad.teamtask.components.TeamTaskData
import com.polito.mad.teamtask.ui.theme.TeamTaskTypography
import java.util.Locale


import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.polito.mad.teamtask.Actions
import com.polito.mad.teamtask.ui.theme.CaribbeanCurrent
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class ProfileFormViewModel : ViewModel() {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    private var originalName: String = ""
    private var originalSurname: String = ""
    private var originalEmailAddress: String = ""
    private var originalUsername: String = ""
    private var originalLocation: String = ""
    private var originalDescription: String = ""
    private var originalImageUri: Uri? = null

    var showMenu by mutableStateOf(false)
    var showBackButtonModal by mutableStateOf(false)
    var isLoading by mutableStateOf(false) // Add a state for loading

    fun setShowMen(bool: Boolean) {
        showMenu = bool
    }

    fun setBackButtModal(bool: Boolean) {
        showBackButtonModal = bool
    }

    var showLogoutModal by mutableStateOf(false)
    fun setShwLogoutModal(bool: Boolean) {
        showLogoutModal = bool
    }

    var showDeleteAccountModal by mutableStateOf(false)
    fun setShwDeleteAccountModal(bool: Boolean) {
        showDeleteAccountModal = bool
    }

    var emailAddressValueForDelete by mutableStateOf("")
        private set
    var emailAddressErrorForDelete by mutableStateOf("")
        private set
    fun setEmailAddresValueForDelete(a: String) {
        emailAddressValueForDelete = a.trim()
        emailAddressErrorForDelete = ""
    }


    fun validateEmailForDeleteAccount(onLogout: () -> Unit, updateAccountBeenDeletedStatus: (Boolean) -> Unit) {
        viewModelScope.launch {
                if (emailAddressValueForDelete == auth.currentUser?.email) {
                    deleteAccount(onLogout)
                    setShwDeleteAccountModal(false)
                    setEmailAddresValueForDelete("")
                    emailAddressErrorForDelete = ""
                    updateAccountBeenDeletedStatus(true)
                    //onLogout() //i moved it to deleteaccount, which is an asynchronous operation (viewModelScope.launch), otherwise onLogout is done before the deleteaccount and the account is not deleted because no auth.currentuser.uid exists
                } else {
                    emailAddressErrorForDelete = "Inserted email is not correct!"
                }
        }
    }

    fun goBackToPresentation() {
        if (nameValue != originalName ||
            surnameValue != originalSurname ||
            //emailAddressValue != originalEmailAddress ||
            usernameValue != originalUsername ||
            locationValue != originalLocation ||
            descriptionValue != originalDescription ||
            imageUri != originalImageUri
        ) {
            setBackButtModal(true)
        } else {
            cancelEditProfile()
        }
    }

    var isEditingProfile by mutableStateOf(false)
        private set

    fun editProfile() {
        originalName = nameValue
        originalSurname = surnameValue
        //originalEmailAddress = emailAddressValue
        originalUsername = usernameValue
        originalLocation = locationValue
        originalDescription = descriptionValue
        originalImageUri = imageUri

        isEditingProfile = true
        Actions.getInstance().goToEditProfile()
    }

    private fun clearErrors() {
        nameError = ""
        surnameError = ""
        //emailAddressError = ""
        usernameError = ""
        locationError = ""
        descriptionError = ""
    }

    fun cancelEditProfile() {
        setName(originalName)
        setSurname(originalSurname)
        //setEmailAddress(originalEmailAddress)
        setUsername(originalUsername)
        setLocation(originalLocation)
        setDescription(originalDescription)
        setUri(originalImageUri)

        clearErrors()

        isEditingProfile = false
        Actions.getInstance().goToProfile()
    }

    private suspend fun uploadImageToFirebaseStorage(userId: String): String? {
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference.child("profileImages/${userId}")
        return try {
            val uploadTask = storageRef.putFile(imageUri!!).await()
            storageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            // Handle error
            null
        }
    }

    // Update Firestore database using coroutines
    suspend fun updateProfileInFirestore(userId: String) {
        val userDocument = db.collection("people").document(userId)

        val imageFileName = if (imageUri != null) userId else ""

        val updatedData = mapOf<String, Any>(
            "name" to nameValue,
            "surname" to surnameValue,
            "username" to usernameValue,
            "location" to locationValue,
            "bio" to descriptionValue,
            "image" to imageFileName
        )

        try {
            userDocument.update(updatedData).await()
            // Successfully updated
            isLoading = false // Hide loading screen
            Actions.getInstance().goToProfile()
            isEditingProfile = false
        } catch (e: Exception) {
            isLoading = false // Hide loading screen
            // Handle the error
            // You can set an error message or log the error
        }
    }

    fun validate(userId: String) {
        viewModelScope.launch {
            checkName()
            checkSurname()
            //checkEmailAddress()
            val isUsernameValid = checkUsername(userId)
            checkLocation()
            checkDescription()

            Log.d("Validation", "Name Error: $nameError, Surname Error: $surnameError, Username Error: $usernameError, Location Error: $locationError, Description Error: $descriptionError")

            if (nameError.isBlank() && surnameError.isBlank() &&
                //emailAddressError.isBlank() &&
                isUsernameValid && locationError.isBlank() && descriptionError.isBlank()
            ) {
                isLoading = true // Show loading screen

                val previousImageUri = imageUri // Store the original URI
                if (imageUri != null) {
                    uploadImageToFirebaseStorage(userId)
                }
                imageUri = previousImageUri // Reset to original URI
                updateProfileInFirestore(userId)
            }
        }
    }


    suspend fun deleteDocumentByUid() {
        val userDocument = auth.currentUser?.let { db.collection("people").document(it.uid) }
        Log.e("oddo", "oddo2")
        try {
            val document = userDocument?.get()?.await()
            if (document != null) {
                if (document.exists()) {
                    document.reference.delete().await()
                } else {
                    // Handle case where no document found with the uid
                }
            }
        } catch (e: Exception) {
            // Handle potential errors during deletion
        }
    }

    fun deleteAccount(onLogout: () -> Unit) {
        viewModelScope.launch {
            try {
                deleteDocumentByUid()
                // Handle successful deletion (e.g., show a success message)
                onLogout()
            } catch (e: Exception) {
                // Handle potential errors during deletion (e.g., show an error message)
            }
        }
    }


    // ----- Profile Picture -----
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

    // ----- Name -----
    var nameValue by mutableStateOf("")
        private set
    var nameError by mutableStateOf("")
        private set
    fun setName(n: String) {
        nameValue = n
            .split(' ')
            .joinToString(" ") { part ->
                part.replaceFirstChar { char ->
                    if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString()
                }
            }
    }

    private fun checkName() {
        nameError = if (nameValue.isBlank()) {
            "Name cannot be blank!"
        } else if (!nameValue.matches(Regex("^[a-zA-Z]+(?:\\s+[a-zA-Z]+)*\\s*$"))) {
            "Name must contain only letters!"
        } else if (nameValue.length > 120) {
            "Name must be at most 120 characters!"
        } else {
            ""
        }
    }

    // ----- Surname -----
    var surnameValue by mutableStateOf("")
        private set
    var surnameError by mutableStateOf("")
        private set
    fun setSurname(n: String) {
        surnameValue = n
            .split(' ')
            .joinToString(" ") { part ->
                part.replaceFirstChar { char ->
                    if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString()
                }
            }
    }

    private fun checkSurname() {
        surnameError = if (surnameValue.isBlank()) {
            "Surname cannot be blank!"
        } else if (!surnameValue.matches(Regex("^[a-zA-Z]+(?:\\s+[a-zA-Z]+)*\\s*$"))) {
            "Surname must contain only letters!"
        } else if (surnameValue.length > 120) {
            "Surname must be at most 120 characters!"
        } else {
            ""
        }
    }

    // ----- Email Address -----
    var emailAddressValue by mutableStateOf("")
        private set
    var emailAddressError by mutableStateOf("")
        private set
    fun setEmailAddress(a: String) {
        emailAddressValue = a.trim()
    }

    private fun checkEmailAddress() {
        emailAddressError = if (emailAddressValue.isBlank()) {
            "Email cannot be blank!"
        } else if (!emailAddressValue.matches(Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"))) {
            "Invalid email address!"
        } else if (emailAddressValue.length > 120) {
            "Email must be at most 120 characters!"
        } else {
            ""
        }
    }

    // ----- Username -----
    var usernameValue by mutableStateOf("")
        private set
    var usernameError by mutableStateOf("")
        private set
    fun setUsername(t: String) {
        usernameValue = t.trim()
    }

    private suspend fun checkUsernameExists(username: String, userId: String): Boolean {
        return try {
            val querySnapshot = db.collection("people")
                .whereEqualTo("username", username)
                .get()
                .await()

            // Check if there are any documents with the same username but different userId
            querySnapshot.documents.any { document -> document.id != userId }
        } catch (e: Exception) {
            // Handle the error
            false
        }
    }

    private suspend fun checkUsername(userId: String): Boolean {
        return if (usernameValue.isBlank()) {
            usernameError = "Username cannot be blank!"
            false
        } else if (!usernameValue.matches(Regex("^[a-zA-Z0-9._]{1,20}$"))) {
            usernameError = "Max 20 characters. Only letters, numbers, periods, and underscores are allowed!"
            false
        } else if (usernameValue.length <= 2) {
            usernameError = "Username must have at least 3 characters!"
            false
        } else if (checkUsernameExists(usernameValue, userId)) {
            usernameError = "Username already exists!"
            false
        } else {
            usernameError = ""
            true
        }
    }

    // ----- Location -----
    var locationValue by mutableStateOf("")
        private set
    var locationError by mutableStateOf("")
        private set
    fun setLocation(loc: String) {
        locationValue = loc
    }

    private fun checkLocation() {
        locationError = when {
            !locationValue.matches(Regex("^[A-Za-z0-9\\s,-]*\\s*$")) -> {
                "Only letters, numbers, spaces, commas, or hyphens are allowed!"
            }
            locationValue.length > 120 -> {
                "Location must be at most 120 characters!"
            }
            else -> ""
        }
    }

    // ----- Description -----
    var descriptionValue by mutableStateOf("")
        private set
    var descriptionError by mutableStateOf("")
        private set
    fun setDescription(desc: String) {
        descriptionValue = desc
    }

    private fun checkDescription() {
        descriptionError = if (descriptionValue.length > 70) {
            "Description must be at most 70 characters!"
        } else {
            ""
        }
    }

    // ----- Personal stats -----
    var statistics by mutableStateOf(StatisticsData(3, 18, 30, listOf(
        TeamTaskData("Team Rocket", R.drawable.image1, "Admin", 5, 6),
        TeamTaskData("Team2", R.drawable.image1, "Owner", 3, 30),
        TeamTaskData("Super team with a long name", R.drawable.image1, "", 18, 60),
        TeamTaskData("Team four!", R.drawable.image1, "Admin", 120, 500),
    )))

    fun fetchProfileImage(userId: String) {
        viewModelScope.launch {
            val storage = FirebaseStorage.getInstance()

            //isLoading = true

            try {
                val document = db.collection("people").document(userId).get().await()
                val imageName = document.getString("image")
                if (imageName != null) {
                    val imageRef = storage.reference.child("profileImages/$imageName")
                    val uri = imageRef.downloadUrl.await()
                    setUri(uri)
                } else {
                    setUri(null)
                }
            } catch (e: Exception) {
                // Handle the error, e.g., set a default image URI
                setUri(null)
            } finally {
                //isLoading = false
            }
        }
    }

    // Initialize with user data
    fun initialize(user: Pair<String, Person>) {
        val person = user.second
        setName(person.name)
        setSurname(person.surname)
        setEmailAddress(person.email)
        setUsername(person.username)
        setLocation(person.location)
        setDescription(person.bio)
        imageUri = person.image?.let { Uri.parse(it) }
        // Set original values for comparison during cancel
        originalName = person.name
        originalSurname = person.surname
        //originalEmailAddress = person.email
        originalUsername = person.username
        originalLocation = person.location
        originalDescription = person.bio
        originalImageUri = person.image?.let { Uri.parse(it) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen (
    user: Pair<String, Person>,
    userId: String,
    teams: List<Pair<String, Team>>, teamParticipants: List<TeamParticipant>,
    vm: ProfileFormViewModel = viewModel(), onLogout: () -> Unit, updateAccountBeenDeletedStatus: (Boolean) -> Unit
) {
    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography
    val auth = FirebaseAuth.getInstance()


    LaunchedEffect(userId) {
        vm.fetchProfileImage(userId)
        vm.initialize(user)
    }

    if (vm.showLogoutModal) {
        AlertDialog(
            onDismissRequest = {
                vm.setShwLogoutModal(false)
            },
            title = { Text(text = "Log out") },
            text = { Text(text = "Are you sure that you want to log out?") },
            confirmButton = {
                Button(onClick = {
                    onLogout()
                    vm.setShwLogoutModal(false)
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
                    vm.setShwLogoutModal(false)
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
    if (vm.showDeleteAccountModal) {
        AlertDialog(
            onDismissRequest = {
                vm.setShwDeleteAccountModal(false)
            },
            title = { Text(text = "Delete Account", color = palette.error) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Write \"" + (auth.currentUser?.email
                            ?: "your email") + "\" and press \"Delete\" to permanently delete your account:"
                    )
                    Spacer(modifier = Modifier.height(15.dp))
                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = vm.emailAddressValueForDelete,
                        onValueChange = vm::setEmailAddresValueForDelete,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Done
                        ),
                        colors = TextFieldDefaults.colors(
                            // your colors
                        ),
                        isError = vm.emailAddressErrorForDelete.isNotBlank()
                    )
                    if (vm.emailAddressErrorForDelete.isNotBlank()) {
                        Text(
                            text = vm.emailAddressErrorForDelete,
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
                    vm.validateEmailForDeleteAccount(onLogout, updateAccountBeenDeletedStatus)
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
                    vm.setEmailAddresValueForDelete("")
                    vm.setShwDeleteAccountModal(false)
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
    if(LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE){
        Row (
            modifier = Modifier.padding(start = 16.dp, end = 16.dp )
        ){
            Column (
                modifier = Modifier
                    .fillMaxWidth(fraction = 0.33f)
                    .fillMaxHeight()
                    .padding(end = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                ProfilePictureSection(vm.nameValue, vm.surnameValue, vm.usernameValue, false, vm.imageUri)
            }
            LazyColumn (
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item { Spacer(Modifier.height(16.dp)) }

                item {
                    ProfileInfoSection(
                        vm.nameValue, "", {},
                        vm.surnameValue, "", {},
                        vm.emailAddressValue, "", {},
                        vm.usernameValue, "", {},
                        vm.locationValue, "", {},
                        vm.descriptionValue, "", {},
                        false
                    )
                }

                item {
                    ProfileVerification(
                        emailVerified = user.second.emailVerified,
                        loginMethod = user.second.loginMethod
                    )
                }

                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    } else {
        LazyColumn (
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                ProfilePictureSection(vm.nameValue, vm.surnameValue, vm.usernameValue, false, vm.imageUri)
            }

            item { Spacer(Modifier.height(10.dp)) }

            item {
                ProfileInfoSection(
                    vm.nameValue, "", {},
                    vm.surnameValue, "", {},
                    vm.emailAddressValue, "", {},
                    vm.usernameValue, "", {},
                    vm.locationValue, "", {},
                    vm.descriptionValue, "", {},
                    false
                )
            }

            item {
                ProfileVerification(
                    emailVerified = user.second.emailVerified,
                    loginMethod = user.second.loginMethod
                )
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
    if (vm.isLoading) {
        LoadingScreen() // Show loading screen when loading
    }
}



// ----- Profile edit -----
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfilePane(
    user: Pair<String, Person>,
    userId: String,
    vm: ProfileFormViewModel = viewModel()
) {
    val sheetState = rememberModalBottomSheetState()
    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography

    //BackHandler(onBack = vm::goBackToPresentation)

    // Pick from gallery
    val context = LocalContext.current
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            run { vm.setUri(uri) }
        }
    }

    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            run { vm.setStoragePermission(isGranted) }
            if (isGranted) {
                // Launch the gallery picker
                Toast.makeText(context, "Storage permission granted", Toast.LENGTH_SHORT).show()
                galleryLauncher.launch("image/*")
            } else {
                // Permission is denied
                if (ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    // "Deny" was chosen
                    Toast.makeText(context, "Storage permission denied.", Toast.LENGTH_LONG).show()
                } else {
                    // "Deny & Don't ask again" was chosen --> the user needs to manually change the permission in settings
                    Toast.makeText(context, "Permission denied, allow storage permission in the app settings!", Toast.LENGTH_LONG).show()
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
            if (uriResult != null) { vm.setUri(uriResult) }
        }
    }

    Scaffold { contentPadding ->
        // Screen content
        BoxWithConstraints {
            if (this.maxHeight >= this.maxWidth) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(contentPadding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item { Spacer(modifier = Modifier.height(25.dp)) }

                    // Profile picture and username
                    item{
                        ProfilePictureSection(vm.nameValue, vm.surnameValue, vm.usernameValue, true, vm.imageUri, vm::setShowBottomMenu)
                    }

                    item { Spacer(modifier = Modifier.height(25.dp)) }

                    // Profile data
                    item {
                        ProfileInfoSection(
                            vm.nameValue, vm.nameError, vm::setName,
                            vm.surnameValue, vm.surnameError, vm::setSurname,
                            vm.emailAddressValue, vm.emailAddressError, vm::setEmailAddress,
                            vm.usernameValue, vm.usernameError, vm::setUsername,
                            vm.locationValue, vm.locationError, vm::setLocation,
                            vm.descriptionValue, vm.descriptionError, vm::setDescription,
                            true
                        )
                    }
                }
                if (vm.showBottomSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { vm.setShowBottomMenu(false) },
                        sheetState = sheetState,
                        containerColor = palette.background
                    ) {
                        // Sheet content
                        Column (
                            modifier = Modifier
                                .background(palette.background)
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(bottom = 30.dp)
                        ) {
                            // Label
                            Text(
                                text = "Profile Image",
                                style = typography.labelMedium,
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(bottom = 10.dp)
                                    .background(palette.background)
                            )

                            Divider(color = palette.onSurfaceVariant)

                            Row (modifier = Modifier.navigationBarsPadding()) {
                                Column {
                                    // Take photo
                                    Button(
                                        onClick = {
                                            cameraActivityLauncher.launch(intent)
                                            vm.setShowBottomMenu(false)
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = palette.background)
                                    ) {
                                        Box(
                                            modifier = Modifier.background(palette.background)
                                        ) {
                                            Row (
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
                                                android.Manifest.permission.READ_EXTERNAL_STORAGE
                                            )
                                            if (permission != PackageManager.PERMISSION_GRANTED) {
                                                // We don't have permission, so request it
                                                storagePermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                                            } else {
                                                // We have permission, so launch the gallery picker
                                                galleryLauncher.launch("image/*")
                                            }
                                            vm.setShowBottomMenu(false) // Close the bottom sheet in any case
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
                                    if (vm.imageUri != null) {
                                        Button(
                                            onClick = {
                                                vm.setUri(null)
                                                vm.setShowBottomMenu(false) // Close the bottom sheet
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = palette.background)
                                        ) {
                                            Box(
                                                modifier = Modifier.background(palette.background)
                                            ) {
                                                Row (
                                                    verticalAlignment = Alignment.CenterVertically
                                                )  {
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
                Row {
                    // Profile picture
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(fraction = 0.3f)
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        ProfilePictureSection(vm.nameValue, vm.surnameValue, vm.usernameValue, true, vm.imageUri, vm::setShowBottomMenu)
                    }

                    // Profile data
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        item {
                            ProfileInfoSection(
                                vm.nameValue, vm.nameError, vm::setName,
                                vm.surnameValue, vm.surnameError, vm::setSurname,
                                vm.emailAddressValue, vm.emailAddressError, vm::setEmailAddress,
                                vm.usernameValue, vm.usernameError, vm::setUsername,
                                vm.locationValue, vm.locationError, vm::setLocation,
                                vm.descriptionValue, vm.descriptionError, vm::setDescription,
                                true
                            )
                        }
                    }
                }

                if (vm.showBottomSheet) {
                    ModalBottomSheet(
                        onDismissRequest = {
                            vm.setShowBottomMenu(false)
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
                                        vm.setShowBottomMenu(false)
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
                                            android.Manifest.permission.READ_EXTERNAL_STORAGE
                                        )
                                        if (permission != PackageManager.PERMISSION_GRANTED) {
                                            // We don't have permission, so request it
                                            storagePermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                                        } else {
                                            // We have permission, so launch the gallery picker
                                            galleryLauncher.launch("image/*")
                                        }
                                        vm.setShowBottomMenu(false) // Close the bottom sheet in any case
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
                                if (vm.imageUri != null) {
                                    Button(
                                        onClick = {
                                            vm.setUri(null)
                                            vm.setShowBottomMenu(false)
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
    if (vm.isLoading) {
        LoadingScreen() // Show loading screen when loading
    }
}
