package com.polito.mad.teamtask

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.polito.mad.teamtask.screens.LoadingScreen
import com.polito.mad.teamtask.ui.theme.CaribbeanCurrent
import com.polito.mad.teamtask.ui.theme.TeamTaskTheme
import com.polito.mad.teamtask.ui.theme.TeamTaskTypography
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val viewModel: AppViewModel by viewModels { AppFactory(applicationContext) }
    private val db = Firebase.firestore

    // State for showing alert dialog
    private val isEmailAlreadyRegistered = mutableStateOf(false)
    private val emailState = mutableStateOf("")
    private val isLoading = mutableStateOf(false)

    // SIGN UP WITH EMAIL AND PASSWORD
    private fun signUpWithEmail(
        name: String, surname: String, username: String,
        email: String, password: String
    ) {
        lifecycleScope.launch {
            try {
                auth.createUserWithEmailAndPassword(email, password).await()
                val user = auth.currentUser
                if (user != null) {
                    val uid = user.uid
                    val salt = generateSalt()
                    val hashedPassword = hashPassword(password, salt)
                    val newUser = hashMapOf(
                        "email" to email.lowercase(),
                        "name" to name,
                        "surname" to surname,
                        "username" to username.lowercase(),
                        "password" to hashedPassword,
                        "salt" to salt,
                        "bio" to "Hey, there! I am proudly using TeamTask!",
                        "image" to "",
                        "location" to "Unknown",
                        "loginMethod" to "email",
                        "emailVerified" to user.isEmailVerified,
                        "tasks" to emptyList<Long>(),
                        "teams" to emptyList<Long>()
                    )

                    db.collection("people").document(uid).set(newUser).await()
                    Log.d("MainActivity", "User document added with UID: $uid")
                } else {
                    Log.w("MainActivity", "User is null")
                }
            } catch (e: Exception) {
                Log.w("MainActivity", "Error during sign-up: ${e.message}")
            }
        }
    }

    private fun generateSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return Base64.getEncoder().encodeToString(salt)
    }

    private fun hashPassword(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val saltBytes = Base64.getDecoder().decode(salt)
        digest.update(saltBytes)
        val hashedBytes = digest.digest(password.toByteArray())
        return Base64.getEncoder().encodeToString(hashedBytes)
    }

    private fun signInWithEmail(email: String, password: String) {
        lifecycleScope.launch {
            isLoading.value = true // Show loading dialog
            try {
                val documents = db.collection("people").whereEqualTo("email", email.lowercase()).get().await()
                if (!documents.isEmpty) {
                    auth.signInWithEmailAndPassword(email, password).await()
                    viewModel.updateLoginStatus(true)
                    saveLoginStatus(true)
                    Log.d("MainActivity", "signInWithEmail:success")
                } else {
                    Log.w("MainActivity", "No user found with email: $email")
                }
            } catch (e: Exception) {
                viewModel.updateLoginStatus(false)
                saveLoginStatus(false)
                Log.w("MainActivity", "signInWithEmail:failure", e)
            } finally {
                isLoading.value = false // Hide loading dialog
            }
        }
    }

    //SIGN IN WITH GOOGLE
    private var pendingGoogleSignInAccount: GoogleSignInAccount? = null

    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(Exception::class.java)
            checkIfEmailExists(account)
        } catch (e: Exception) {
            Log.w("MainActivity", "Google sign in failed", e)
        }
    }

    private fun checkIfEmailExists(account: GoogleSignInAccount?) {
        lifecycleScope.launch {
            try {
                val email = account?.email
                if (email != null) {
                    emailState.value = email // Update email state
                    val documents = db.collection("people").whereEqualTo("email", email).get().await()
                    if (documents.isEmpty) {
                        pendingGoogleSignInAccount = account
                        viewModel.updateLoginStatus(false)
                        saveLoginStatus(false)
                        viewModel.updateIsSignUpFlow(true)
                        Log.d("MainActivity", "signInWithCredential:success")
                    } else {
                        val userDocument = documents.documents[0]
                        val loginMethod = userDocument.getString("loginMethod")
                        if (loginMethod == "email") {
                            isEmailAlreadyRegistered.value = true
                            resetPendingGoogleSignInAccount()
                        } else {
                            performFirebaseSignInWithGoogle(account)
                            Log.d("MainActivity", "Email already exists, signing in")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("MainActivity", "Error checking email existence", e)
            }
        }
    }

    private fun performFirebaseSignInWithGoogle(account: GoogleSignInAccount?) {
        lifecycleScope.launch {
            isLoading.value = true // Show loading dialog
            try {
                val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
                auth.signInWithCredential(credential).await()
                if(auth.currentUser?.uid != null) {
                    viewModel.updateLoginStatus(true)
                    saveLoginStatus(true)
                }
                Log.d("MainActivity", "signInWithCredential:success")
            } catch (e: Exception) {
                viewModel.updateLoginStatus(false)
                saveLoginStatus(false)
                Log.w("MainActivity", "signInWithCredential:failure", e)
            } finally {
                isLoading.value = false // Hide loading dialog
            }
        }
    }

    //SIGN UP WITH GOOGLE
    private val googleSignUpLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        lifecycleScope.launch {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                pendingGoogleSignInAccount = task.getResult(Exception::class.java)
                val email = pendingGoogleSignInAccount?.email
                if (email != null) {
                    emailState.value = email
                    val documents = db.collection("people").whereEqualTo("email", email).get().await()
                    if (documents.isEmpty) {
                        Log.d("MainActivity", "entered email")
                        // Email does not exist
                        viewModel.updateLoginStatus(false)
                        saveLoginStatus(false)
                        viewModel.updateIsSignUpFlow(true)
                        //Log.d("MainActivity", "signInWithCredential:success")
                    } else {
                        val userDocument = documents.documents[0]
                        val loginMethod = userDocument.getString("loginMethod")
                        if (loginMethod == "email") {
                            isEmailAlreadyRegistered.value = true
                            resetPendingGoogleSignInAccount()
                        } else {
                            performFirebaseSignInWithGoogle(pendingGoogleSignInAccount)
                            Log.d("MainActivity", "Email already exists, signing in")
                        }
                    }
                }
            } catch (e: Exception) {
                viewModel.updateLoginStatus(false)
                saveLoginStatus(false)
                Log.w("MainActivity", "Google sign up failed", e)
            }
        }
    }

    private fun performFirebaseSignUpWithGoogle(account: GoogleSignInAccount?, username: String) {
        lifecycleScope.launch {
            isLoading.value = true // Show loading dialog
            try {
                val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
                auth.signInWithCredential(credential).await()

                val email = account?.email
                if (email != null) {
                    val documents = db.collection("people").whereEqualTo("email", email).get().await()
                    if (documents.isEmpty) {
                        val user = auth.currentUser
                        val uid = user?.uid
                        val name = account.givenName ?: ""
                        val surname = account.familyName ?: ""
                        val bio = "Hey, there! I am proudly using TeamTask!"
                        val image = ""
                        val location = "Unknown"
                        val loginMethod = "google"
                        val emailVerified = user?.isEmailVerified ?: false
                        val newUser = hashMapOf(
                            "email" to email.lowercase(),
                            "name" to name,
                            "surname" to surname,
                            "username" to username.lowercase(),
                            "bio" to bio,
                            "image" to image,
                            "location" to location,
                            "loginMethod" to loginMethod,
                            "emailVerified" to emailVerified,
                            "tasks" to emptyList<Long>(),
                            "teams" to emptyList<Long>()
                        )

                        if (uid != null) {
                            db.collection("people").document(uid).set(newUser).await()
                            Log.d("MainActivity", "User document added with UID: $uid")
                        }
                        //I MOVED HERE THESE 2 LINES FROM validateCompleteSignupWithGoogle OF FIRSTSCREEN.KT:
                        viewModel.updateLoginStatus(true)
                        saveLoginStatus(true)

                    } else {
                        performFirebaseSignInWithGoogle(account)
                        Log.d("MainActivity", "Email already exists, signing in")
                    }
                }
            } catch (e: Exception) {
                Log.w("MainActivity", "Error during Google sign-up: ${e.message}")
            } finally {
                isLoading.value = false // Hide loading dialog
            }
        }
    }

    private fun performPendingGoogleSignIn(username: String) {
        pendingGoogleSignInAccount?.let {
            performFirebaseSignUpWithGoogle(it, username)
            pendingGoogleSignInAccount = null
        }
    }

    private fun resetPendingGoogleSignInAccount() {
        pendingGoogleSignInAccount?.let {
            pendingGoogleSignInAccount = null
        }
        googleSignInClient.signOut().addOnCompleteListener(this) {
            Log.d("MainActivity", "GoogleSignInClient reset")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val typography = TeamTaskTypography
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        // Google Sign-In CONFIGURATION
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // default_web_client_id DEFINED IN strings.xml
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Load login status and sign-up flow from SharedPreferences
        loadLoginStatus()

        //NOW WE HAVE LOADED THE STATE FOR isLoggedIn STATE VARIABLE!
        //IN THIS WAY ANY PRECEDENTLY SELECTED EMAIL WILL BE FORGOTTEN.
        if(!viewModel.isLoggedIn.value){  //without this if WHAT HAPPENS IS THAT EVERYTIME I OPEN AGAIN THE APP I WILL BE LOGGED OUT!
            googleSignInClient.signOut().addOnCompleteListener(this) {
                viewModel.updateLoginStatus(false)
                saveLoginStatus(false)
                viewModel.updateIsSignUpFlow(false)
            }
        }


        setContent {
            TeamTaskTheme {
                if (isEmailAlreadyRegistered.value) {
                    AlertDialog(
                        onDismissRequest = {
                            isEmailAlreadyRegistered.value = false
                        },
                        title = { Text(text = "Email Already Registered") },
                        text = { Text(text = "The email address is already registered with a different login method. Please use that method to log in.") },
                        confirmButton = {
                            Button(onClick = {
                                isEmailAlreadyRegistered.value = false
                            }) {
                                Text(
                                    text = "Ok",
                                    style = typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CaribbeanCurrent
                                )
                            }
                        }
                    )
                }
                // Show loading screen if isLoading is true
                if (isLoading.value) {
                    LoadingScreen()
                } else {
                    AppMainScreen(
                        email = emailState.value,
                        signInWithGoogle = this::signInWithGoogle,
                        signOut = this::signOut,
                        signUpWithGoogle = this::signUpWithGoogle,
                        saveLoginStatus = this::saveLoginStatus,
                        performPendingGoogleSignIn = this::performPendingGoogleSignIn,
                        resetPendingGoogleSignInAccount = this::resetPendingGoogleSignInAccount,
                        signUpWithEmail = this::signUpWithEmail,
                        signInWithEmail = this::signInWithEmail,
                        appVM = viewModel
                    )
                }

                AppMainScreen(
                    email = emailState.value,
                    signInWithGoogle = this::signInWithGoogle,
                    signOut = this::signOut,
                    signUpWithGoogle = this::signUpWithGoogle,
                    saveLoginStatus = this::saveLoginStatus,
                    performPendingGoogleSignIn = this::performPendingGoogleSignIn,
                    resetPendingGoogleSignInAccount = this::resetPendingGoogleSignInAccount,
                    signUpWithEmail = this::signUpWithEmail,
                    signInWithEmail = this::signInWithEmail,
                    appVM = viewModel
                )
            }
        }
    }

    private fun signInWithGoogle() {
        isEmailAlreadyRegistered.value = false // Reset state
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun signUpWithGoogle() {
        isEmailAlreadyRegistered.value = false // Reset state
        val signUpIntent = googleSignInClient.signInIntent
        googleSignUpLauncher.launch(signUpIntent)
    }

    private fun signOut() {
        auth.currentUser?.uid?.let { Log.e("prova", it) }
        auth.signOut()
        googleSignInClient.signOut().addOnCompleteListener(this) {
            viewModel.updateLoginStatus(false)
            saveLoginStatus(false)
            viewModel.updateIsSignUpFlow(false)
        }
    }

    private fun saveLoginStatus(isLoggedIn: Boolean) {
        val sharedPreferences = getSharedPreferences("com.polito.mad.teamtask", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("isLoggedIn", isLoggedIn)
        editor.apply()
    }

    private fun loadLoginStatus() {
        val sharedPreferences = getSharedPreferences("com.polito.mad.teamtask", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        viewModel.updateLoginStatus(isLoggedIn)
    }
}
