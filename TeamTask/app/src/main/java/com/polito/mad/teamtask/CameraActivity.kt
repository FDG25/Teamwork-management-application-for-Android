package com.polito.mad.teamtask

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.polito.mad.teamtask.components.CameraView
import java.io.File
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import coil.compose.rememberAsyncImagePainter
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class MainViewModel : ViewModel() {
    var shouldShowCamera: MutableState<Boolean> = mutableStateOf(false)
        private set

    fun setShouldShowCamera(value: Boolean) {
        shouldShowCamera.value = value
    }

    var shouldShowPhoto: MutableState<Boolean> = mutableStateOf(false)
        private set

    fun setShouldShowPhoto(value: Boolean) {
        shouldShowPhoto.value = value
    }

    var photoUri: Uri by mutableStateOf(Uri.EMPTY)
        private set

    fun setPhotoUriState(uri: Uri) {
        photoUri = uri
    }

    var permissionGranted: MutableState<Boolean> = mutableStateOf(false)
        private set

    fun setPermissionGranted(value: Boolean) {
        permissionGranted.value = value
    }
}


class CameraActivity : ComponentActivity() {
    private val myViewModel: MainViewModel by viewModels()

    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show()
            myViewModel.setShouldShowCamera(true)
            myViewModel.setPermissionGranted(true)
        } else {
            Log.i("teamtask", "Permission denied")
            Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        setContent {
            if (myViewModel.shouldShowCamera.value) {
                CameraView(
                    outputDirectory = outputDirectory,
                    executor = cameraExecutor,
                    onImageCaptured = ::handleImageCapture,
                    onError = { Log.e("teamtask", "View error:", it) }
                )
            }

            if (myViewModel.shouldShowPhoto.value) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = rememberAsyncImagePainter(myViewModel.photoUri),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 25.dp, start = 60.dp, end = 60.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = {
                                myViewModel.setShouldShowCamera(true)
                                myViewModel.setShouldShowPhoto(false)
                            },
                            modifier = Modifier
                                .size(30.dp)
                                .background(Color.Black, CircleShape)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_clear_24),
                                contentDescription = "Accept Photo",
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        IconButton(
                            onClick = { confirm() },
                            modifier = Modifier
                                .size(30.dp)
                                .background(Color.Black, CircleShape)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.outline_done_24),
                                contentDescription = "Accept Photo",
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                }
            }
        }

        requestCameraPermission()

        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    
    private fun requestCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.i("teamtask", "Permission previously granted")
                if (!myViewModel.permissionGranted.value) {
                    myViewModel.setShouldShowCamera(true)
                    myViewModel.setPermissionGranted(true)
                }
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                android.Manifest.permission.CAMERA
            ) -> {
                Log.i("teamtask", "Show camera permissions dialog")
                Toast.makeText(this, "Permission denied, allow camera permission in the app settings!", Toast.LENGTH_SHORT).show()
                finish()
            }

            else -> requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    
    private fun handleImageCapture(
        uri: Uri
    ) {
        Log.i("teamtask", "Image captured: $uri")
        myViewModel.setShouldShowCamera(false)

        myViewModel.setPhotoUriState(uri)
        myViewModel.setShouldShowPhoto(true)
    }

    
    private fun getOutputDirectory(
    ): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }

        return if (mediaDir != null && mediaDir.exists()) mediaDir else filesDir
    }

    
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    
    private fun confirm() {
        setResult(RESULT_OK, intent.putExtra("photoUri", myViewModel.photoUri))
        finish()
    }
}
