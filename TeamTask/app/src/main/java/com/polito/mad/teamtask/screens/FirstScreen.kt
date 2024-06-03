package com.polito.mad.teamtask

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.HorizontalAlignmentLine
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.firestore.FirebaseFirestore
import com.polito.mad.teamtask.ui.theme.TeamTaskTypography
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.Base64
import java.util.Locale


class LoginSignupViewModel: ViewModel() {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    var showBottomSheet by mutableStateOf(false)
    var isShowingLogin by mutableStateOf(false) //true = login, false = signup
    var isContinuingWithGoogle by mutableStateOf(false) //true = google, false = email
    var isShowingForm by mutableStateOf(false) //true = form, false = firstscreen

    fun setShowBottomMenu(bm: Boolean) {
        showBottomSheet = bm
    }

    fun setShowLogin(value: Boolean) {
        isShowingLogin = value
    }

    fun setContinueWithGoogle(value: Boolean) {
        isContinuingWithGoogle = value
    }

    fun setShowForm(value: Boolean) {
        isShowingForm = value
    }


    //SIGNUP HANDLING
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
        if (emailAddressValue.isBlank()) {
            emailAddressError = "Email cannot be blank!"
        } else if (!emailAddressValue.matches(Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"))) {
            emailAddressError = "Invalid email address!"
        } else if (emailAddressValue.length > 120) {
            emailAddressError = "Email must be at most 120 characters!"
        } else {
            emailAddressError = ""
        }
    }
    private suspend fun verifyEmailAddress(): Boolean {
        return try {
            val documents = db.collection("people").whereEqualTo("email", emailAddressValue.lowercase()).get().await()
            documents.isEmpty
        } catch (e: Exception) {
            emailAddressError = "Error checking email: ${e.message}"
            false
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
    private fun checkUsername() {
        if (usernameValue.isBlank()) {
            usernameError = "Username cannot be blank!"
        } else if (!usernameValue.matches(Regex("^[a-zA-Z0-9._]{1,20}$"))) {
            usernameError = "Max 20 characters. Only letters, numbers, periods, and underscores are allowed!"
        } else if (usernameValue.length <= 2) {
            usernameError = "Username must have at least 3 characters!"
        } else {
            usernameError = ""
        }
    }

    private suspend fun verifyUsername(): Boolean {
        return try {
            val documents = db.collection("people").whereEqualTo("username", usernameValue.lowercase()).get().await()
            documents.isEmpty
        } catch (e: Exception) {
            usernameError = "Error checking username: ${e.message}"
            false
        }
    }


    fun clearValuesAndErrors() {
        setName("")
        setSurname("")
        setEmailAddress("")
        setUsername("")
        setPassword("")
        setConfirmPassword("")
        setPasswordToBeVisible(false)
        setConfirmPasswordToBeVisible(false)
        setCheckState(false)
        nameError = ""
        surnameError = ""
        emailAddressError = ""
        usernameError = ""
        passwordError = ""
        checkedStateError = ""
        loginError = ""
    }
    fun validateSignup(signUpWithEmail: (String, String, String, String, String) -> Unit ) {
        viewModelScope.launch {
            checkName()
            checkSurname()
            checkEmailAddress()
            checkUsername()
            checkPassword()
            checkCheckState()
            if (nameError.isBlank() && surnameError.isBlank() &&
                emailAddressError.isBlank() && usernameError.isBlank() &&
                passwordError.isBlank() && confirmPasswordError.isBlank() && checkedStateError.isBlank()
            ) {
                val isEmailValid = verifyEmailAddress()
                val isUsernameValid = verifyUsername()

                if (isEmailValid && isUsernameValid) {
                    signUpWithEmail(
                        nameValue,
                        surnameValue,
                        usernameValue.lowercase(),
                        emailAddressValue.lowercase(),
                        passwordValue
                    )
                    setName("")
                    setSurname("")
                    setEmailAddress("")
                    setUsername("")
                    setPassword("")
                    setConfirmPassword("")
                    setCheckState(false)
                    // Show the login form after successful signup
                    setShowLogin(true)
                    setShowForm(true)
                } else {
                    if(!isEmailValid) {
                        emailAddressError = "Email already exists!"
                    }
                    if(!isUsernameValid) {
                        usernameError = "Username already exists!"
                    }
                }
            }
        }
    }

    var loginError by mutableStateOf("")

    private suspend fun verifyLoginCredentials(): Boolean {
        return try {
            val documents = db.collection("people")
                .whereEqualTo("email", emailAddressValue)
                .get().await()

            if (documents.isEmpty) {
                loginError = "Invalid username or password!"
                false
            } else {
                val document = documents.documents[0]
                val storedSalt = document.getString("salt") ?: return false
                val storedHashedPassword = document.getString("password") ?: return false
                val inputHashedPassword = hashPassword(passwordValue, storedSalt)

                if (storedHashedPassword == inputHashedPassword) {
                    true
                } else {
                    loginError = "Invalid username or password!"
                    false
                }
            }
        } catch (e: Exception) {
            loginError = "Error checking credentials: ${e.message}"
            false
        }
    }

    private fun hashPassword(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val saltBytes = Base64.getDecoder().decode(salt)
        digest.update(saltBytes)
        val hashedBytes = digest.digest(password.toByteArray())
        return Base64.getEncoder().encodeToString(hashedBytes)
    }

    fun validateLogin(signInWithEmail: (String, String) -> Unit) {
        viewModelScope.launch {
            loginError = ""
            var flag = 0

            if(emailAddressValue.isBlank() || passwordValue.isBlank()) {
                loginError = "Email and password cannot be blank!"
                flag = 1
            }

            if(flag == 0) {
                val areCredentialsValid = verifyLoginCredentials()
                if (areCredentialsValid) {
                    signInWithEmail(emailAddressValue.lowercase(), passwordValue)
                    setUsername("")
                    setPassword("")
                    setShowForm(false)
                } else {
                    loginError = "Invalid email or password!"
                }
            }
        }
    }


    fun validateCompleteSignupWithGoogle(
        saveLoginStatus: (Boolean) -> Unit,
        performPendingGoogleSignIn: (String) -> Unit,
        appViewModel: AppViewModel
    ) {
        viewModelScope.launch {
            checkUsername()
            checkCheckState()

            if (usernameError.isBlank() && checkedStateError.isBlank()) {
                val isUsernameValid = verifyUsername()
                if (isUsernameValid) {
                    performPendingGoogleSignIn(usernameValue)
                    appViewModel.updateLoginStatus(true)
                    saveLoginStatus(true)
                    setUsername("")
                    setCheckState(false)
                }
                else {
                   usernameError = "Username already exists!"
                }
            }
        }
    }

    // ----- Password -----
    var passwordValue by mutableStateOf("")
        private set
    var passwordVisible by mutableStateOf(false)
        private set
    fun setPasswordToBeVisible(value: Boolean) {
        passwordVisible = value
    }

    var passwordError by mutableStateOf("")
        private set
    fun setPassword(t: String) {
        passwordValue = t.trim()
    }
    private fun checkPassword() {
        passwordError = when {
            passwordValue.isBlank() -> "Password cannot be blank!"
            passwordValue.length < 8 -> "Password must have at least 8 characters!"
            !passwordValue.matches(Regex(".*[A-Z].*")) -> "Password must contain at least one uppercase letter!"
            !passwordValue.matches(Regex(".*[a-z].*")) -> "Password must contain at least one lowercase letter!"
            !passwordValue.matches(Regex(".*[0-9].*")) -> "Password must contain at least one digit!"
            !passwordValue.matches(Regex(".*[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) ->
                "Password must contain at least one special character (e.g. !@#$%^&*)!"
            passwordValue != confirmPasswordValue -> "Passwords do not match!"
            else -> ""
        }
    }

    // ----- Confirm Password -----
    var confirmPasswordValue by mutableStateOf("")
        private set
    var confirmPasswordVisible by mutableStateOf(false)
        private set
    fun setConfirmPasswordToBeVisible(value: Boolean) {
        confirmPasswordVisible = value
    }


    var confirmPasswordError by mutableStateOf("")
        private set
    fun setConfirmPassword(t: String) {
        confirmPasswordValue = t.trim()
    }

    var checkedState by mutableStateOf(false) //checkbox for conditions
        private set
    var checkedStateError by mutableStateOf("")
        private set
    fun setCheckState(value: Boolean) {
        checkedState = value
    }
    private fun checkCheckState() {
        if (!checkedState) {
            checkedStateError = "You need to confirm before you can continue."
        } else {
            checkedStateError = ""
        }
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirstScreenComponent (
    showBottomSheet: Boolean, setShowBottomMenu: (Boolean) -> Unit,
    isShowingLogin: Boolean, setShowLogin: (Boolean) -> Unit,
    isContinuingWithGoogle: Boolean, setContinueWithGoogle: (Boolean) -> Unit,
    isShowingForm: Boolean, setShowForm: (Boolean) -> Unit,
    nameValue: String, nameError: String, setName: (String) -> Unit,
    surnameValue: String, surnameError: String, setSurname: (String) -> Unit,
    emailAddressValue: String, emailAddressError: String, setEmailAddress: (String) -> Unit,
    usernameValue: String, usernameError: String, setUsername: (String) -> Unit,
    passwordValue: String, passwordVisible: Boolean, setPasswordToBeVisible: (Boolean) -> Unit, passwordError: String, setPassword: (String) -> Unit,
    confirmPasswordValue: String, confirmPasswordVisible: Boolean, setConfirmPasswordToBeVisible: (Boolean) -> Unit, confirmPasswordError: String, setConfirmPassword: (String) -> Unit,
    checkedState: Boolean, setCheckState: (Boolean) -> Unit, checkedStateError: String,
    signInWithGoogle: () -> Unit,
    signUpWithGoogle: () -> Unit,
    validateLogin: () -> Unit,
    validateSignup: () -> Unit,
    loginError: String,
    clearValuesAndErrors: () -> Unit
) {
    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography

    val sheetState = rememberModalBottomSheetState()

    if(LocalConfiguration.current.orientation != Configuration.ORIENTATION_LANDSCAPE) {
        if (!isShowingForm) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                item { Spacer(modifier = Modifier.height(100.dp)) }

                item {
                    Column {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.teamtasklogo),
                                contentDescription = "Team Image",
                                modifier = Modifier.size(200.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "TeamTask",
                            style = typography.bodyLarge,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Working together has never been so good before!",
                            textAlign = TextAlign.Center,
                            style = typography.titleLarge,
                            color = palette.onSurfaceVariant,
                            modifier = Modifier
                                .padding(horizontal = 30.dp)
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(100.dp)) }

                item {
                    Button(
                        onClick = {
                            setShowLogin(false)
                            setShowBottomMenu(true)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 5.dp)
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.primary,
                            contentColor = palette.secondary
                        )
                    ) {
                        Text(
                            "Create an account",
                            style = typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = palette.secondary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            setShowLogin(true)
                            setShowBottomMenu(true)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 5.dp)
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.secondary,
                            contentColor = palette.background
                        )
                    ) {
                        Text(
                            "I already have an account",
                            style = typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = palette.background
                        )
                    }
                }
            }
        }
    } else {
        Row (
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.teamtasklogo),
                        contentDescription = "Team Image",
                        modifier = Modifier.size(150.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "TeamTask",
                    style = typography.bodyLarge,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Working together has never been so good before!",
                    textAlign = TextAlign.Center,
                    style = typography.titleLarge,
                    color = palette.onSurfaceVariant,
                    modifier = Modifier
                        .padding(horizontal = 30.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box {
                    Column {
                        Button(
                            onClick = {
                                setShowLogin(false)
                                setShowBottomMenu(true)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 30.dp, vertical = 5.dp)
                                .height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = palette.primary,
                                contentColor = palette.secondary
                            )
                        ) {
                            Text(
                                "Create an account",
                                style = typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = palette.secondary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                setShowLogin(true)
                                setShowBottomMenu(true)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 30.dp, vertical = 5.dp)
                                .height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = palette.secondary,
                                contentColor = palette.background
                            )
                        ) {
                            Text(
                                "I already have an account",
                                style = typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = palette.background
                            )
                        }
                    }
                }
            }
        }
    }

    if(!isShowingForm) {
        if (showBottomSheet) {
            BackHandler {
                setShowBottomMenu(false)
            }
            ModalBottomSheet(
                onDismissRequest = { setShowBottomMenu(false) },
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
                        text = if (isShowingLogin) {"Log In"} else {"Sign up"},
                        style = typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 10.dp)
                            .background(palette.background)
                    )

                    Divider(color = palette.onSurfaceVariant)

                    Row (modifier = Modifier.navigationBarsPadding()) {
                        Column {
                            Button(
                                onClick = {
                                    if (isShowingLogin) { //LOGIN WITH GOOGLE
                                        signInWithGoogle()
                                        setShowBottomMenu(false)
                                    }
                                    if (!isShowingLogin) { //SIGNUP WITH GOOGLE
                                        signUpWithGoogle()
                                        setShowBottomMenu(false)
                                    }
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
                                            painter = painterResource(id = R.drawable.google_logo),
                                            contentDescription = "Continue with Google",
                                            modifier = Modifier.size(30.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            "Continue with Google", color = palette.onSurface,
                                            style = typography.labelMedium
                                        )
                                    }
                                }
                            }


                            Button(
                                onClick = {
                                    setContinueWithGoogle(false)
                                    setShowForm(true)
                                    setShowBottomMenu(false)
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
                                            painter = painterResource(id = R.drawable.email_logo),
                                            contentDescription = "Continue with Email",
                                            modifier = Modifier.size(30.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            "Continue with Email", color = palette.onSurface,
                                            style = typography.labelMedium
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
    if(isShowingForm) {

        /*
        if (!isShowingLogin && isContinuingWithGoogle) {

        }
        if (isShowingLogin && isContinuingWithGoogle) {

        }
        */

        if (isShowingLogin && !isContinuingWithGoogle) {
            BackHandler {
                setShowLogin(false)
                setShowForm(false)
                clearValuesAndErrors()
            }
            LoginWithEmail(
                emailAddressValue, emailAddressError, setEmailAddress,
                passwordValue, passwordVisible, setPasswordToBeVisible, setPassword,
                validateLogin, loginError
            )
        }
        if (!isShowingLogin && !isContinuingWithGoogle) {
            BackHandler {
                setShowForm(false)
                clearValuesAndErrors()
            }
            SignupWithEmail(
                nameValue, nameError, setName,
                surnameValue, surnameError, setSurname,
                emailAddressValue, emailAddressError, setEmailAddress,
                usernameValue, usernameError, setUsername,
                passwordValue, passwordVisible, setPasswordToBeVisible, passwordError, setPassword,
                confirmPasswordValue, confirmPasswordVisible, setConfirmPasswordToBeVisible, confirmPasswordError, setConfirmPassword,
                checkedState, setCheckState, checkedStateError,
                validateSignup
            )
        }
    }
}

@Composable
fun LoginWithGoogle (

) {
    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography

}

@Composable
fun LoginWithEmail(
    emailAddressValue: String, emailAddressError: String, setEmailAddress: (String) -> Unit,
    passwordValue: String, passwordVisible: Boolean, setPasswordToBeVisible: (Boolean) -> Unit, setPassword: (String) -> Unit,
    validateLogin: () -> Unit,
    loginError: String
) {
    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography

    Spacer(Modifier.height(100.dp))

    Column (
        modifier = Modifier.padding(16.dp)
    ) {
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = emailAddressValue.lowercase(),
            onValueChange = setEmailAddress,
            label = { Text("Enter your email address") },
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

        TextField(
            value = passwordValue,
            onValueChange = { setPassword(it) },
            label = { Text("Enter your password") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                Image(
                    painter = painterResource(R.drawable.outline_remove_red_eye_24),
                    contentDescription = "Show or Hide password",
                    colorFilter = ColorFilter.tint(if (passwordVisible) palette.secondary else palette.onSurfaceVariant),
                    modifier = Modifier.clickable(onClick = { setPasswordToBeVisible(!passwordVisible) })
                )
            },
            modifier = Modifier
                .height(56.dp)
                .fillMaxWidth(),
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
        if (loginError.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = loginError,
                color = palette.error,
                style = typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                maxLines = 3
            )
        }

        Spacer(modifier = Modifier.height(100.dp))

        Button(
            onClick = {
                validateLogin()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = palette.primary, contentColor = palette.secondary)
        ) {
            Text(
                "Log in",
                style = typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = palette.secondary
            )
        }
    }
}

@Composable
fun SignupWithEmail (
    nameValue: String, nameError: String, setName: (String) -> Unit,
    surnameValue: String, surnameError: String, setSurname: (String) -> Unit,
    emailAddressValue: String, emailAddressError: String, setEmailAddress: (String) -> Unit,
    usernameValue: String, usernameError: String, setUsername: (String) -> Unit,
    passwordValue: String, passwordVisible: Boolean, setPasswordToBeVisible: (Boolean) -> Unit, passwordError: String, setPassword: (String) -> Unit,
    confirmPasswordValue: String, confirmPasswordVisible: Boolean, setConfirmPasswordToBeVisible: (Boolean) -> Unit, confirmPasswordError: String, setConfirmPassword: (String) -> Unit,
    checkedState: Boolean, setCheckState: (Boolean) -> Unit, checkedStateError: String,
    validateSignup: () -> Unit
) {
    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography

    LazyColumn {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Name
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = nameValue,
                    onValueChange = setName,
                    label = { Text("Enter your name") },
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
                    label = { Text("Enter your surname") },
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

                // Username
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = usernameValue,
                    onValueChange = setUsername,
                    label = { Text("Enter your username") },
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

                // Email
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = emailAddressValue.lowercase(),
                    onValueChange = setEmailAddress,
                    label = { Text("Enter your email") },
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

                // PASSWORD
                TextField(
                    value = passwordValue,
                    onValueChange = { setPassword(it) },
                    label = { Text("Enter your password") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        Image(
                            painter = painterResource(R.drawable.outline_remove_red_eye_24),
                            contentDescription = "Show or Hide password",
                            colorFilter = ColorFilter.tint(if (passwordVisible) palette.secondary else palette.onSurfaceVariant),
                            modifier = Modifier.clickable(onClick = { setPasswordToBeVisible(!passwordVisible) })
                        )
                    },
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
                    modifier = Modifier
                        .height(56.dp)
                        .fillMaxWidth()
                )
                if (passwordError.isNotBlank()) {
                    Text(
                        text = passwordError,
                        color = palette.error,
                        style = typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        maxLines = 3
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Confirm Password Field
                TextField(
                    value = confirmPasswordValue,
                    onValueChange = { setConfirmPassword(it) },
                    label = { Text("Re-enter your password") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        Image(
                            painter = painterResource(R.drawable.outline_remove_red_eye_24),
                            contentDescription = "Show or Hide confirm password",
                            colorFilter = ColorFilter.tint(if (confirmPasswordVisible) palette.secondary else palette.onSurfaceVariant),
                            modifier = Modifier.clickable(onClick = { setConfirmPasswordToBeVisible(!confirmPasswordVisible) })
                        )
                    },
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
                    modifier = Modifier
                        .height(56.dp)
                        .fillMaxWidth()
                )
                if (confirmPasswordError.isNotBlank()) {
                    Text(
                        text = confirmPasswordError,
                        color = palette.error,
                        style = typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        maxLines = 3
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                // CHECKBOX
                Row(modifier = Modifier.padding(8.dp)) {
                    Checkbox(
                        checked = checkedState,
                        onCheckedChange = { setCheckState(it) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = palette.secondary
                        )
                    )
                    Text(
                        text = "By signing up, I confirm that I accept TeamTask's Terms and Condition and have read the Privacy Policy",
                        modifier = Modifier.padding(start = 8.dp),
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if(checkedStateError.isNotBlank()){
                    Text(
                        text = checkedStateError,
                        color = palette.error,
                        style = typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        maxLines = 3
                    )
                }
            }
            Button(
                onClick = {
                    validateSignup()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = palette.primary, contentColor = palette.secondary)
            ) {
                Text(
                    "Sign up",
                    style = typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = palette.secondary
                )
            }
        }
    }
}


@Composable
fun CompleteSignupWithGoogle (
    usernameValue: String, usernameError: String, setUsername: (String) -> Unit,
    checkedState: Boolean, setCheckState: (Boolean) -> Unit, checkedStateError: String,
    validateCompleteSignupWithGoogle: () -> Unit
) {
    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography

    LazyColumn {
        item {
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
                    label = { Text("Choose your username") },
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

                Spacer(modifier = Modifier.height(40.dp))

                // CHECKBOX
                Row(modifier = Modifier.padding(8.dp)) {
                    Checkbox(
                        checked = checkedState,
                        onCheckedChange = { setCheckState(it) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = palette.secondary
                        )
                    )
                    Text(
                        text = "By signing up, I confirm that I accept TeamTask's Terms and Condition and have read the Privacy Policy",
                        modifier = Modifier.padding(start = 8.dp),
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if(checkedStateError.isNotBlank()){
                    Text(
                        text = checkedStateError,
                        color = palette.error,
                        style = typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        maxLines = 3
                    )
                }
            }
            Button(
                onClick = {
                    validateCompleteSignupWithGoogle()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = palette.primary, contentColor = palette.secondary)
            ) {
                Text(
                    "Sign up",
                    style = typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = palette.secondary
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirstScreen(
    email: String,
    signInWithGoogle: () -> Unit,
    signUpWithGoogle: () -> Unit,
    isSignUpFlow: Boolean,
    updateIsSignUpFlow: (Boolean) -> Unit,
    saveLoginStatus: (Boolean) -> Unit,
    onLogout: () -> Unit,
    performPendingGoogleSignIn: (String) -> Unit,
    resetPendingGoogleSignInAccount: () -> Unit,
    signUpWithEmail: (String, String, String, String, String) -> Unit,
    signInWithEmail: (String, String) -> Unit,
    appViewModel: AppViewModel,
    vm: LoginSignupViewModel = viewModel(),
) {
    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography

    if(!isSignUpFlow){
        FirstScreenComponent(
            vm.showBottomSheet, vm::setShowBottomMenu,
            vm.isShowingLogin, vm::setShowLogin,
            vm.isContinuingWithGoogle, vm::setContinueWithGoogle,
            vm.isShowingForm, vm::setShowForm,
            vm.nameValue, vm.nameError, vm::setName,
            vm.surnameValue, vm.surnameError, vm::setSurname,
            vm.emailAddressValue, vm.emailAddressError, vm::setEmailAddress,
            vm.usernameValue, vm.usernameError, vm::setUsername,
            vm.passwordValue, vm.passwordVisible, vm::setPasswordToBeVisible, vm.passwordError, vm::setPassword,
            vm.confirmPasswordValue, vm.confirmPasswordVisible, vm::setConfirmPasswordToBeVisible, vm.confirmPasswordError, vm::setConfirmPassword,
            vm.checkedState, vm::setCheckState, vm.checkedStateError,
            signInWithGoogle, signUpWithGoogle,
            { vm.validateLogin(signInWithEmail) },
            { vm.validateSignup(signUpWithEmail) },
            vm.loginError, vm::clearValuesAndErrors
        )
    } else {
        var isShowingConsentForm by remember { mutableStateOf(true) }

        BackHandler {
            resetPendingGoogleSignInAccount()
            updateIsSignUpFlow(false)
            appViewModel.updateLoginStatus(false)
            saveLoginStatus(false)
            vm.clearValuesAndErrors()
        }
        if(isShowingConsentForm){
            AlertDialog(
                onDismissRequest = {
                    resetPendingGoogleSignInAccount()
                    updateIsSignUpFlow(false)
                    appViewModel.updateLoginStatus(false)
                    saveLoginStatus(false)
                    vm.clearValuesAndErrors()
                    isShowingConsentForm = false
                },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Sign in to TeamTask", modifier = Modifier.padding(bottom = 4.dp))
                        IconButton(onClick = {
                            resetPendingGoogleSignInAccount()
                            updateIsSignUpFlow(false)
                            appViewModel.updateLoginStatus(false)
                            saveLoginStatus(false)
                            vm.clearValuesAndErrors()
                            isShowingConsentForm = false
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                },
                text = {
                    Column {
                        Text("By continuing, Google will share your name, surname and email address with TeamTask.")
                        Spacer(modifier = Modifier.height(10.dp))
                        //Text("You can manage sign in with Google in your Google Account.")
                        //Spacer(modifier = Modifier.height(55.dp))
                        Text("Email: $email")
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { isShowingConsentForm = false},
                        colors = ButtonDefaults.buttonColors(containerColor = palette.primary, contentColor = palette.secondary)
                    ) {
                        Text(
                            text = "Continue",
                            style = typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = palette.secondary
                        )
                    }
                }
            )
        }
        CompleteSignupWithGoogle (
            vm.usernameValue, vm.usernameError, vm::setUsername,
            vm.checkedState, vm::setCheckState, vm.checkedStateError,
        ) { vm.validateCompleteSignupWithGoogle(saveLoginStatus, performPendingGoogleSignIn, appViewModel) }
    }
}

