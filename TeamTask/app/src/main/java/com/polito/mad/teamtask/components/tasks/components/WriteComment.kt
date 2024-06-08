@file:Suppress("UNCHECKED_CAST")

package com.polito.mad.teamtask.components.tasks.components

import android.app.Activity
import android.content.ContentResolver
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.polito.mad.teamtask.R
import com.polito.mad.teamtask.ui.theme.TeamTaskTypography
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.core.app.ActivityCompat
import com.polito.mad.teamtask.chat.visualization.UriCouple
import com.polito.mad.teamtask.chat.visualization.multipleFilesContract
import com.polito.mad.teamtask.components.FilesBar
import java.time.LocalDateTime


data class EditableObject(
    val id: String,    //id of comment or reply
    val commentId: String?,
    val profilePic: Uri,
    val username: String,
    val role: String,
    var text: String,
    val date: LocalDateTime,
    val attachments: Set<UriCouple>,
    val repliesNumber: Int?,
)

data class SendObject(
    val id: String?,
    val taskId: String,
    val senderId: String,
    val timestamp: LocalDateTime,
    val body: String?,
    val media: Set<UriCouple>?,
    val repliesAllowed: Boolean?,
    val replies: List<String>?,
    val commentId: String?
)


class WTViewModel : ViewModel() {
    var isEditing by mutableStateOf(false)
        private set

    fun setIsEditing(value: Boolean) {
        isEditing = value
    }

    var text by mutableStateOf(TextFieldValue(""))
        private set

    fun setMyText(text: TextFieldValue) {
        this.text = text
    }

    var editingObject: EditableObject =
        EditableObject(
            "",
            null,
            Uri.EMPTY,
            "",
            "",
            "",
            LocalDateTime.now(),
            emptySet(),
            null
        )

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

    fun goEditing(editingObject: EditableObject) {
        attachments.value = editingObject.attachments.mapNotNull { it.firebaseUri }.toSet()
        text = TextFieldValue(editingObject.text)
        this.editingObject = editingObject
        isEditing = true
    }
}


@Composable
fun WriteComment(
    vm: WTViewModel = viewModel(),
    onSend: (SendObject) -> Unit = {},
    onEdit: (SendObject) -> Unit = {},
    isComment: Boolean = true,
    commentId: String?,
    senderId: String,
    taskId: String
) {

    val palette = MaterialTheme.colorScheme
    val typography = TeamTaskTypography

    //var textState by remember { mutableStateOf(TextFieldValue(text = vm.text)) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val files by vm.attachments.collectAsState()

    val context = LocalContext.current

    LaunchedEffect(vm.isEditing) {
        if (vm.isEditing) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    //reset all when the view change from Comments to Replies and vice versa
    LaunchedEffect(isComment) {
        vm.setIsEditing(false)
        vm.setMyText(TextFieldValue(""))
        vm.removeAllAttachment()
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
                val fileName = com.polito.mad.teamtask.components.getFileNameWithExtension(
                    uri,
                    context.contentResolver
                )
                Toast.makeText(
                    context,
                    "File $fileName too big: you can upload a file of max 5MB",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

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
                        android.Manifest.permission.READ_EXTERNAL_STORAGE
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
    //onClick Function
    val onClickFunction = {
        if (files.size > 5) {
            Toast.makeText(
                context,
                "You can upload a maximum of 5 files per message (5MB max for each file)",
                Toast.LENGTH_SHORT
            ).show()
        }

        if ((vm.isEditing && isComment && vm.editingObject.id.isBlank()) ||
            (vm.isEditing && !isComment && vm.editingObject.id.isBlank())
        ) {
            throw RuntimeException("Comment/Reply ID cannot be null or empty during editing")
        }

        if (vm.isEditing) {
            onEdit(
                if (isComment) {
                    SendObject(
                        id = vm.editingObject.id,
                        taskId = taskId,
                        senderId = senderId,
                        timestamp = vm.editingObject.date,
                        body = vm.text.text,
                        media = if (files.isNotEmpty()) files.map { UriCouple(
                            firebaseUri = it,
                            firebaseRelativePath = ""
                        ) }.toSet() else null,
                        repliesAllowed = true,
                        replies = emptyList(),
                        null
                    )
                } else {
                    SendObject(
                        id = vm.editingObject.id,
                        taskId = taskId,
                        senderId = senderId,
                        timestamp = vm.editingObject.date,
                        body = vm.text.text,
                        media = if (files.isNotEmpty()) files.map { UriCouple(
                            firebaseUri = it,
                            firebaseRelativePath = ""
                        ) }.toSet() else null,
                        repliesAllowed = null,
                        replies = null,
                        commentId
                    )
                }
            )
            //reset editing state
            vm.setIsEditing(false)
        } else if(vm.text.text.isNotBlank() || files.isNotEmpty()) {
            onSend(
                if (isComment) {
                    SendObject(
                        id = null,
                        taskId = taskId,
                        senderId = senderId,
                        timestamp = LocalDateTime.now(),
                        body = vm.text.text,
                        media = if (files.isNotEmpty()) files.map { UriCouple(
                            firebaseUri = it,
                            firebaseRelativePath = ""
                        ) }.toSet() else null,
                        repliesAllowed = true,
                        replies = emptyList(),
                        null
                    )
                } else {
                    SendObject(
                        id = null,
                        taskId = taskId,
                        senderId = senderId,
                        timestamp = LocalDateTime.now(),
                        body = vm.text.text,
                        media = if (files.isNotEmpty()) files.map { UriCouple(
                            firebaseUri = it,
                            firebaseRelativePath = ""
                        ) }.toSet() else null,
                        repliesAllowed = null,
                        replies = null,
                        commentId
                    )
                }
            )
        }
        vm.setMyText(TextFieldValue(""))
        vm.removeAllAttachment()
    }


    // View
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(palette.surfaceVariant)
    ) {
        //Dismiss changes bar in case of editing
        if (vm.isEditing) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(palette.primaryContainer)
                    .padding(6.dp)
            ) {
                Box(modifier = Modifier
                    .clickable {
                        //reset editing state
                        vm.setIsEditing(false)
                        vm.setMyText(TextFieldValue(""))
                        vm.removeAllAttachment()
                    }
                    .padding(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        //Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            tint = palette.error,
                            painter = painterResource(id = R.drawable.outline_close_24),
                            contentDescription = "close",
                            modifier = Modifier.size(30.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Discard changes",
                            style = typography.bodyMedium.copy(
                                color = palette.error,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal
                            )
                        )
                    }
                }
            }
        }

        // File attachments
        if(files.isNotEmpty())
            FilesBar(files = files, vm::removeAttachment)

        // TexField
        BasicTextField(
            modifier = Modifier
                .fillMaxWidth()
                .background(palette.surfaceVariant)
                .padding(4.dp)
                .focusRequester(focusRequester)
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        vm.setMyText(
                            vm.text.copy(
                                text = vm.text.text,
                                selection = TextRange(vm.text.text.length)
                            )
                        )
                    }
                },
            value = vm.text,
            onValueChange = {
                vm.setMyText(it)
            },
            cursorBrush = SolidColor(palette.onSurface),
            textStyle = typography.bodyMedium.copy(
                color = palette.onSurface,
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal
            ),
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
                        storagePermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
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
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (vm.text.text.isEmpty()) {
                            Text(
                                text = "Write a message",
                                style = typography.bodyMedium.copy(
                                    color = palette.onSurfaceVariant,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            )
                        }
                        innerTextField()
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Send button
                    IconButton(onClick = onClickFunction) {
                        Icon(
                            tint = palette.onSurface,
                            painter = painterResource(
                                id = if(!vm.isEditing) R.drawable.baseline_send_24 else R.drawable.outline_done_24
                            ),
                            contentDescription = "options",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        )
    }
}


fun getFileNameWithExtension(
    uri: Uri,
    contentResolver: ContentResolver
): String? {
    var name: String? = null

    if (uri.scheme.equals("content")) {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor.use {
            if (it != null && it.moveToFirst()) {
                name =
                    it.getString(it.getColumnIndexOrThrow(android.provider.OpenableColumns.DISPLAY_NAME))
            }
        }
    }

    if (name == null) {
        name = uri.path
        val cut = name?.lastIndexOf('/')
        if (cut != -1) {
            if (cut != null) {
                name = name?.substring(cut + 1)
            }
        }
    }

    return name
}
