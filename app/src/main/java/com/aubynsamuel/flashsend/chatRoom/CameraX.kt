package com.aubynsamuel.flashsend.chatRoom

import android.content.Context
import android.net.Uri
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.aubynsamuel.flashsend.Screen
import com.aubynsamuel.flashsend.functions.createFile
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraXScreen(
    navController: NavController,
    roomId: String,
    profileUrl: String?,
    deviceToken: String,
    onError: (Throwable) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    // Camera UI state
    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    var flashMode by remember { mutableIntStateOf(ImageCapture.FLASH_MODE_OFF) }
    var isCapturing by remember { mutableStateOf(false) }

    // Camera controls
    val previewView = remember { PreviewView(context) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var camera: Camera? by remember { mutableStateOf(null) }
    var zoomRatio by remember { mutableFloatStateOf(1f) }

    var expanded by remember { mutableStateOf(false) }
    var selectedAspect by remember { mutableStateOf("4:3") }
    val aspectOptions = listOf("16:9", "4:3", "1:1")
    var rotateValue by remember { mutableFloatStateOf(0f) }


    // Camera configuration
    LaunchedEffect(lensFacing, selectedAspect, flashMode) {
        val cameraProvider = ProcessCameraProvider.getInstance(context).get()

        // Prepare builder instances for preview and image capture
        val previewBuilder = Preview.Builder()
        val captureBuilder = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setFlashMode(flashMode)

        // Apply different configurations based on the selected aspect ratio
        if (selectedAspect == "1:1") {
            // For a square aspect ratio, set a target resolution (adjust as needed)
            val squareSize = android.util.Size(1080, 1080)
            previewBuilder.setTargetResolution(squareSize)
            captureBuilder.setTargetResolution(squareSize)
        } else {
            val targetAspectRatio = if (selectedAspect == "16:9")
                AspectRatio.RATIO_16_9
            else
                AspectRatio.RATIO_4_3
            previewBuilder.setTargetAspectRatio(targetAspectRatio)
            captureBuilder.setTargetAspectRatio(targetAspectRatio)
        }

        // Build the preview and assign its surface provider
        val preview = previewBuilder.build().also {
            it.surfaceProvider = previewView.surfaceProvider
        }
        imageCapture = captureBuilder.build()

        // Create the camera selector
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        try {
            cameraProvider.unbindAll()
            camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )
        } catch (exc: Exception) {
            onError(exc)
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        // ─── CAMERA PREVIEW AREA WITH PINCH-ZOOM AND TAP-TO-FOCUS ─────────────────
        val aspectRatioValue = when (selectedAspect) {
            "16:9" -> 16f / 9f
            "4:3" -> 4f / 3f
            "1:1" -> 1f
            else -> 16f / 9f
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, zoomChange, _ ->
                        val maxZoom = camera?.cameraInfo?.zoomState?.value?.maxZoomRatio ?: 4f
                        zoomRatio = (zoomRatio * zoomChange).coerceIn(1f, maxZoom)
                        camera?.cameraControl?.setZoomRatio(zoomRatio)
                    }
                }
        ) {
            AndroidView(
                factory = { previewView },
                modifier = Modifier
                    .fillMaxSize()
                    .aspectRatio(aspectRatioValue)
            )
            // Tap-to-focus overlay
            Box(modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { tapOffset ->
                        camera?.cameraControl?.startFocusAndMetering(
                            FocusMeteringAction.Builder(
                                previewView.meteringPointFactory.createPoint(
                                    tapOffset.x,
                                    tapOffset.y
                                ),
                                FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE
                            ).build()
                        )
                    }
                }
            )
        }

        // ─── TOP APP BAR WITH ASPECT RATIO SELECTION ──────────────────────────────
        TopAppBar(
            title = { Text("") },
            actions = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .animateContentSize()
                ) {
                    if (!expanded) {
                        TextButton(
                            onClick = { expanded = true },
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .alpha(if (expanded) 0f else 1f)
                        ) {
                            Text(
                                text = "Aspect: $selectedAspect",
                                textAlign = TextAlign.End,
                                color = Color.White
                            )
                        }
                    } else {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = expanded,
                            enter = fadeIn(),
                            exit = fadeOut(),
                            modifier = Modifier
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                aspectOptions.forEach { option ->
                                    Text(
                                        text = option,
                                        modifier = Modifier
                                            .clickable {
                                                selectedAspect = option
                                                expanded = false
                                            },
                                        color = Color.White,
                                    )
                                }
                            }
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth(),
            colors = TopAppBarColors(
                containerColor = Color.Transparent,
                navigationIconContentColor = Color.White,
                actionIconContentColor = Color.White,
                titleContentColor = Color.White,
                scrolledContainerColor = Color.Transparent
            )
        )

        // Camera controls overlay
        // ─── BOTTOM BAR WITH ZOOM SLIDER AND OTHER CONTROLS ─────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
//                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                .padding(8.dp)
        ) {
            // Zoom slider control
            Slider(
                value = zoomRatio,
                onValueChange = { newVal ->
                    zoomRatio = newVal
                    camera?.cameraControl?.setZoomRatio(newVal)
                },
                valueRange = 1f..(camera?.cameraInfo?.zoomState?.value?.maxZoomRatio ?: 4f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.padding(vertical = 10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Flash toggle button
                IconButton(onClick = {
                    flashMode = if (flashMode == ImageCapture.FLASH_MODE_OFF)
                        ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
                    imageCapture?.flashMode = flashMode
                }) {
                    Icon(
                        imageVector = if (flashMode == ImageCapture.FLASH_MODE_OFF)
                            Icons.Default.FlashOff else Icons.Default.FlashOn,
                        contentDescription = "Toggle Flash",
                        tint = Color.White
                    )
                }

                // Capture button
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(Color.White, CircleShape)
                        .clickable(onClick = {
                            if (!isCapturing) {
                                isCapturing = true
                                scope.launch {
                                    captureImage(
                                        context = context,
                                        imageCapture = imageCapture,
                                        onImageCaptured = { savedUri ->
                                            val route = Screen.ImagePreview.createRoute(
                                                imageUri = savedUri.toString(),
                                                roomId = roomId,
                                                takenFromCamera = true,
                                                profileUrl = profileUrl.orEmpty(),
                                                recipientsToken = deviceToken
                                            )
                                            navController.navigate(route)
                                        },
                                        onError = {
                                            isCapturing = false
                                            onError(it)
                                        }
                                    )
                                }
                            }

                        })
                ) {
                    Box(
                        modifier = Modifier
                            .animateContentSize()
                            .size(if (isCapturing) 55.dp else 60.dp)
                            .align(Alignment.Center)
                            .background(Color.White, CircleShape)
                            .border(
                                width = 4.dp,
                                shape = CircleShape,
                                color = if (isCapturing) Color.Black else Color.White
                            )
                    ) {}
                }

                // Camera flip button
                IconButton(onClick = {
                    if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                        lensFacing = CameraSelector.LENS_FACING_FRONT
                        rotateValue = 180f
                    } else {
                        lensFacing = CameraSelector.LENS_FACING_BACK
                        rotateValue = 0f
                    }
                }) {
                    val rotateX = animateFloatAsState(
                        rotateValue,
                        animationSpec = tween(durationMillis = 300)
                    )
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = "Switch Camera",
                        tint = Color.White,
                        modifier = Modifier.graphicsLayer {
                            rotationY = rotateX.value
                        }
                    )
                }
            }
        }
    }
}

private fun captureImage(
    context: Context,
    imageCapture: ImageCapture?,
    onImageCaptured: (Uri) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {
    val photoFile = createFile(context)
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture?.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val savedUri = outputFileResults.savedUri ?: Uri.fromFile(photoFile)
                onImageCaptured(savedUri)
            }

            override fun onError(exception: ImageCaptureException) {
                onError(exception)
            }
        }
    )
}

@Composable
@androidx.compose.ui.tooling.preview.Preview
fun Prev() {
    Box(
        modifier = Modifier
            .size(60.dp)
            .background(Color.White, CircleShape)
            .clickable(onClick = {})
    ) {
        Box(
            modifier = Modifier
                .animateContentSize()
                .size(if (true) 55.dp else 60.dp)
                .align(Alignment.Center)
                .background(Color.White, CircleShape)
                .border(width = 4.dp, shape = CircleShape, color = Color.Black)
        ) {}
    }

}