package com.polito.mad.teamtask.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.google.firebase.storage.FirebaseStorage
import com.polito.mad.teamtask.components.getFileNameWithExtension
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File

suspend fun uploadTeamImage(uri: Uri, teamId: String, context: Context): String {
    try {
        val storage = FirebaseStorage.getInstance()
        val fileName = getFileNameWithExtension(uri, context.contentResolver)
            ?: throw Exception("File not found")
        val fileRef = storage.reference.child("teamImages/$teamId-$fileName")
        withContext(Dispatchers.IO) {
            fileRef.putFile(uri).await()
        }
        return "$teamId-$fileName"
    } catch (e: Exception) {
        throw e
    }
}

suspend fun uploadFilesToFirebaseStorage(
    uris: List<Uri?>,
    clientId: String,
    chatId: String,
    context: Context
): List<String> {
    val storage = FirebaseStorage.getInstance()
    val fileRefs = mutableListOf<String>()

    if (uris.size <= 5) {
        // Upload each file to Firebase Storage
        for (uri in uris) {
            //it should not happen
            if (uri == null) continue

            val fileSizeInMB =
                context.contentResolver.openAssetFileDescriptor(uri, "r")
                    ?.use { it.length.div(1024.0).div(1024.0) }
            val fileName = getFileNameWithExtension(uri, context.contentResolver)

            Log.d("FILE UPLOAD", "Uploading file $fileName")



            if (fileSizeInMB != null && fileSizeInMB > 5.0) {
                // File too big
                Log.e("FILE TOO BIG", "File $fileName too big: you can upload a file of 5MB Max")
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "File $fileName too big: you can upload a file of 5MB Max",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else if (fileName == null) {
                // File not found
                Log.e("FILE NOT FOUND", "File not found")
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "File not found, can't upload file number ${uris.indexOf(uri) + 1}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                // Upload the file
                val fileRef = storage.reference.child("chats/$clientId/$chatId/${fileName}")
                try {
                    withContext(Dispatchers.IO) {
                        fileRef.putFile(uri).await()
                        fileRefs.add(fileRef.path)
                    }
                } catch (e: Exception) {
                    Log.e(
                        "FILE UPLOAD ERROR",
                        "Error during the uploading of the file: ${e.message}"
                    )
                }
            }
        }
    } else {
        //Too many files
        withContext(Dispatchers.Main) {
            Toast.makeText(
                context,
                "Too many files: you can upload a maximum of 5 files",
                Toast.LENGTH_SHORT
            ).show()
        }
        throw Exception("Too many files: you can upload a maximum of 5 files")
    }

    return fileRefs
}

suspend fun downloadFileFromFirebaseStorage(
    fileRef: String,
    context: Context
) {
    val storage = FirebaseStorage.getInstance()
    // Obtain the reference to the file
    val storageRef = storage.reference.child(fileRef)

    val lastIndex = fileRef.lastIndexOf("/")
    val firstPart = fileRef.substring(0, lastIndex)
    val lastPart = fileRef.substring(lastIndex + 1)

    val directory = File(context.filesDir, firstPart)
    if (!directory.exists()) {
        directory.mkdirs()
    }

    // Create a file in the internal storage
    val destinationFile = File("${context.filesDir}$firstPart", lastPart)
    //val ciao = destinationFile.createNewFile()

    // Waiting the download to complete
    try {
        storageRef.getFile(destinationFile).await()
        //Log.d("DOWNLOADING FILE FROM FIREBASE", "Downloading file ${destinationFile.path}")
    }
    // Error management
    catch (e: Exception) {
        throw Exception("File Download Error: ${e.message}")
    }
    withContext(Dispatchers.Main) {
        Toast.makeText(
            context,
            "File downloaded successfully",
            Toast.LENGTH_SHORT
        ).show()
    }
}

//function that return the Uri of the file if exists in the internal storage or null
fun isFileAlreadyDownloaded(destinationPath: String, context: Context): Uri? {
    val file = File(context.filesDir, destinationPath)
    if (!file.exists()) return null
    val result: Uri
    try {
        //File provider permits to share in a secure way private files
        result = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
        return result
    } catch (e: Exception) {
        Log.e("FILE PROVIDER ERROR", "Error during the creation of the file provider: ${e.message}")
        return null
    }
}

suspend fun deleteFileFromUri(uri: Uri, context: Context) {
    try {
        context.contentResolver.delete(uri, null, null)
    } catch (e: Exception) {
        Log.e("FILE DELETION ERROR", "Error during file deletion: ${e.message}")
        withContext(Dispatchers.Main) {
            Toast.makeText(
                context,
                "Error during file deletion: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}