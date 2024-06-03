package com.polito.mad.teamtask.chat.visualization

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.polito.mad.teamtask.R
import com.polito.mad.teamtask.components.FilesBar
import com.polito.mad.teamtask.components.getFileNameWithExtension
import com.polito.mad.teamtask.ui.theme.TeamTaskTypography
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDateTime
import kotlin.random.Random

data class MemberTag(
    val memberId: String,
    val username: String,
    val profilePic: Uri
)

class MyOffsetMapping : OffsetMapping {
    override fun originalToTransformed(offset: Int): Int {
        return offset
    }

    override fun transformedToOriginal(offset: Int): Int {
        return offset
    }
}

class WriteMessageViewModel : ViewModel() {
    var myText = mutableStateOf(TextFieldValue(""))
        private set

    fun setText(text: TextFieldValue) {
        myText.value = text
    }

    var showSuggestions = mutableStateOf(false)
        private set

    fun setShowSuggestions(value: Boolean) {
        showSuggestions.value = value
    }

    var suggestions = mutableStateOf(listOf<MemberTag>())
        private set

    fun setSuggestions(value: List<MemberTag>) {
        suggestions.value = value
    }

    var attachments: MutableStateFlow<Set<Uri>> = MutableStateFlow(emptySet())
        private set

    fun addAttachment(uri: Uri) {
        attachments.value += uri
    }

    fun removeAllAttachment() {
        attachments.value = emptySet()
    }

    fun removeAttachment(uri: Uri) {
        attachments.value -= uri
    }

    var setStoragePermission by mutableStateOf(false)
        private set

    fun setStoragePermission(isGranted: Boolean) {
        setStoragePermission = isGranted
    }
}

@Composable
fun WriteMessage(
    sendMessage: (ClientMessage, Boolean, String) -> Unit,
    groupUsers: List<MemberTag>? = null,
    chatId: String,
    clientId: String,
    vm: WriteMessageViewModel = viewModel(),
    isThereTaskTag: Boolean = false
) {
    val files by vm.attachments.collectAsState()

    val context = LocalContext.current
    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    val groupUsersFiltered = groupUsers?.filter { it.memberId != clientId }

    //Class that permits to transform the text while writing (using directly the AnnotationString in TextFiledValue is not possible due to a bug reported recently)
    class MyVisualTransformation :
        VisualTransformation {
        override fun filter(text: AnnotatedString): TransformedText {

            val transformedText = buildAnnotatedString {
                if (text.isNotEmpty()) {
                    val chunks = text.split(Regex("(?=(@\\w+))"))
                    chunks.forEach { word ->
                        if (word.length > 1
                            && word.startsWith("@")
                            && groupUsersFiltered != null
                            && groupUsersFiltered.map { e -> e.username }.contains(
                                word.substringBefore(" ").substring(1)
                            )
                        ) {
                            val tag = word.substringBefore(" ").substring(1)
                            pushStringAnnotation(tag = "USER_TAG", annotation = tag)
                            withStyle(style = SpanStyle(color = palette.secondary)) {
                                append("@$tag")
                            }
                            pop()
                            append(word.substring(tag.length + 1))
                        } else {
                            // Questo è un testo normale
                            append(word)
                        }
                    }
                }
            }

            return TransformedText(
                text = transformedText,
                offsetMapping = MyOffsetMapping()
            )
        }
    }

    //get multiple files
    val getContent = rememberLauncherForActivityResult(multipleFilesContract) { uris ->
        // Handle the returned URIs here
        uris.forEach { uri ->
            val fileSizeInMB =
                context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length.div(1024.0).div(1024.0) }
            if (fileSizeInMB != null && fileSizeInMB < 5.0)
            vm.addAttachment(uri)
            else {
                val fileName = getFileNameWithExtension(uri, context.contentResolver)
                Toast.makeText(
                    context,
                    "File $fileName too big: you can upload a file of max 5MB",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    //storage permission
    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            if (isGranted) {
                if (!vm.setStoragePermission) {
                    vm.setStoragePermission(true)
                    Toast.makeText(context, "Storage permission granted", Toast.LENGTH_SHORT).show()
                }
                // If permission is granted, launch the gallery picker
                getContent.launch("*/*")
            } else { // Permission is denied
                vm.setStoragePermission(false)
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

    //onValueChange that check if there are @tag
    val onValueChange: (TextFieldValue) -> Unit = {
        vm.setText(it)

        // Check if the text has '@' symbol
        if (it.text.contains("@") && !it.text.substringAfterLast("@").contains(" ")) {
            // Show suggestions
            vm.setShowSuggestions(true)

            // Filter the users list based on the input after '@'
            val input = it.text.substringAfterLast("@")
            if (groupUsersFiltered != null) {
                vm.setSuggestions(groupUsersFiltered.filter { user ->
                    user.username.contains(
                        input,
                        ignoreCase = true
                    )
                })
            }
        } else {
            // Hide suggestions
            vm.setShowSuggestions(false)
        }
    }

    //function when you click the send button
    val onClickFunction = {
        if (files.size > 5) {
            Toast.makeText(
                context,
                "You can upload a maximum of 5 files per message (5MB max for each file)",
                Toast.LENGTH_SHORT
            ).show()
        }

        if (vm.myText.value.text.isNotEmpty() || (files.isNotEmpty() && files.size <= 5)) {
            //reset suggestions visualization
            vm.setShowSuggestions(false)

            val finalText = createAnnotatedStringFromString(vm.myText.value.text, groupUsers)

            //send message
            sendMessage(
                ClientMessage(
                    id = Random.nextInt(100000).toString(),
                    body = finalText,
                    date = LocalDateTime.now(),
                    files = if (files.isNotEmpty()) files.map {
                        UriCouple(
                            it,
                            ""
                        )
                    }.toSet() else null
                ),
                groupUsers != null,
                chatId
            )

            focusManager.clearFocus()
            vm.setText(TextFieldValue(""))
            vm.removeAllAttachment()
        }
    }

    //View
    Column {
        //Suggestions
        if (vm.showSuggestions.value) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(
                        max = (if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE)
                            100.dp else 200.dp)
                    )
                    .clip(RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp))
                    .background(palette.primaryContainer)
            ) {
                LazyColumn {
                    items(vm.suggestions.value) { suggestion ->
                        Row(
                            modifier = Modifier
                                .padding(10.dp)
                                .clickable {
                                    val endIndex = vm.myText.value.text.lastIndexOf("@")
                                    // Replace the text after the last '@' with the selected username
                                    val newText =
                                        vm.myText.value.text.substring(
                                            0,
                                            endIndex
                                        ) + "@${suggestion.username} "
                                    vm.setText(
                                        TextFieldValue(
                                            text = newText,
                                            selection = TextRange(newText.length)
                                        )
                                    )
                                    vm.setShowSuggestions(false)
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            //Profile Pic
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(suggestion.profilePic)
                                    .crossfade(true)
                                    .build(),
                                placeholder = painterResource(R.drawable.avatar),
                                contentDescription = "Team Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth(
                                        (if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE)
                                            0.05f else 0.1f)
                                    )
                                    .aspectRatio(1f)
                                    .clip(CircleShape)
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            //Username
                            Text(
                                text = suggestion.username,
                                modifier = Modifier
                                    .fillMaxWidth(),
                                style = typography.labelSmall.copy(
                                    color = palette.onSurface,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            )
                        }
                    }
                }
            }
        }

        //Files attachments
        if (files.isNotEmpty()) {
            FilesBar(files = files, removeAttachment = vm::removeAttachment)
        }

        // TexField
        BasicTextField(
            modifier = Modifier
                .fillMaxWidth()
                .background(palette.surfaceVariant)
                .padding(4.dp)
                .focusRequester(focusRequester),
            value = vm.myText.value,
            onValueChange = onValueChange,
            cursorBrush = SolidColor(palette.onSurface),
            textStyle = typography.bodyMedium.copy(
                color = palette.onSurface,
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal
            ),
            visualTransformation = MyVisualTransformation(),
            keyboardOptions = KeyboardOptions.Default,
            maxLines = 5,
            decorationBox = { innerTextField ->
                // Text Field
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Add attachment
                    IconButton(onClick = {
                        // Select from from the file manager
                        storagePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }) {
                        Icon(
                            tint = palette.onSurface,
                            painter = painterResource(id = R.drawable.outline_add_24),
                            contentDescription = "options",
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Text field
                    Box(
                        modifier = Modifier.weight(1f),
                        //verticalAlignment = Alignment.CenterVertically
                    ) {
//                        //TaskTag icon
//                        if(isThereTaskTag) {
//                            Icon(
//                                tint = palette.onSurface,
//                                painter = painterResource(id = R.drawable.outline_work_outline_24),
//                                contentDescription = "taskTag",
//                                modifier = Modifier.size(30.dp)
//                            )
//                        }
                        //replace empty text
                        if (vm.myText.value.text.isEmpty()) {
                            Text(
                                text = "Write a message",
                                style = typography.bodyMedium.copy(
                                    color = palette.onSurfaceVariant,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            )
                        }
                        //input text
                        innerTextField()
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Send button
                    IconButton(onClick = onClickFunction) {
                        Icon(
                            tint = palette.onSurface,
                            painter = painterResource(
                                id = R.drawable.baseline_send_24
                            ),
                            contentDescription = "send message",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        )
    }
}

val multipleFilesContract = object : ActivityResultContract<String, List<Uri>>() {
    override fun createIntent(context: Context, input: String): Intent {
        return Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = input
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): List<Uri> {
        val clipData = intent?.clipData
        val contentUriList = mutableListOf<Uri>()
        if (clipData != null) {
            for (i in 0 until clipData.itemCount) {
                contentUriList.add(clipData.getItemAt(i).uri)
            }
        } else {
            intent?.data?.let { contentUriList.add(it) }
        }
        return contentUriList
    }
}

fun createAnnotatedStringFromString(text: String, groupUsers: List<MemberTag>?): AnnotatedString {
    return buildAnnotatedString {
        val chunks = text.split(Regex("(?=(@\\w+))"))
        chunks.forEach { word ->
            if (word.length > 1
                && word.startsWith("@")
                && groupUsers != null
                && groupUsers.map { e -> e.username }.contains(
                    word.substringBefore(" ").substring(1)
                )
            ) {
                val tag = word.substringBefore(" ").substring(1)
                pushStringAnnotation(tag = "USER_TAG", annotation = tag)
                withStyle(style = SpanStyle(color = Color(0xFF006D77))) {
                    append("@$tag")
                }
                pop()
                append(word.substring(tag.length + 1))
            } else {
                // Questo è un testo normale
                append(word)
            }
        }
    }
}