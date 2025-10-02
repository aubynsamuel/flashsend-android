package com.aubynsamuel.flashsend.chat.presentation.screens

import android.content.Context
import android.net.Uri
import android.util.Size
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.aubynsamuel.flashsend.chat.presentation.utils.createFile
import com.aubynsamuel.flashsend.core.presentation.navigation.ImagePreviewScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CameraXScreen(
    navController: NavController,
    deviceToken: String,
    onError: (Throwable) -> Unit = {},
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


    LaunchedEffect(lensFacing, selectedAspect, flashMode) {
        val cameraProvider = ProcessCameraProvider.getInstance(context).get()

        val resolutionSelectorBuilder = ResolutionSelector.Builder()
        if (selectedAspect == "1:1") {
            resolutionSelectorBuilder
                .setResolutionStrategy(
                    ResolutionStrategy(
                        Size(1080, 1080),
                        ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
                    )
                )
                .setAspectRatioStrategy(AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY)
        } else {
            val aspectRatio = if (selectedAspect == "16:9")
                AspectRatio.RATIO_16_9
            else
                AspectRatio.RATIO_4_3

            resolutionSelectorBuilder.setAspectRatioStrategy(
                when (aspectRatio) {
                    AspectRatio.RATIO_16_9 -> AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY
                    else -> AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY
                }
            )
        }
        val resolutionSelector = resolutionSelectorBuilder.build()

        val previewBuilder = Preview.Builder()
            .setResolutionSelector(resolutionSelector)

        val captureBuilder = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setFlashMode(flashMode)
            .setResolutionSelector(resolutionSelector)


        val preview = previewBuilder.build().also {
            it.surfaceProvider = previewView.surfaceProvider
        }
        imageCapture = captureBuilder.build()

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
            .background(MaterialTheme.colorScheme.surface)
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
            Box(
                modifier = Modifier
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
                                color = MaterialTheme.colorScheme.onSurface
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
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                            }
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth(),
            colors = TopAppBarDefaults.topAppBarColors().copy(
                containerColor = Color.Transparent,
                navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                scrolledContainerColor = Color.Transparent
            )
        )

        // ─── BOTTOM BAR WITH ZOOM SLIDER AND OTHER CONTROLS ─────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(10.dp)
        ) {

            Slider(
                value = zoomRatio,
                onValueChange = { newVal ->
                    zoomRatio = newVal
                    camera?.cameraControl?.setZoomRatio(newVal)
                },
                valueRange = 1f..(camera?.cameraInfo?.zoomState?.value?.maxZoomRatio ?: 4f),
                modifier = Modifier.padding(horizontal = 16.dp),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
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
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    IconButton(onClick = {
                        flashMode = if (flashMode == ImageCapture.FLASH_MODE_OFF)
                            ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
                        imageCapture?.flashMode = flashMode
                    }) {
                        Icon(
                            imageVector = if (flashMode == ImageCapture.FLASH_MODE_OFF)
                                Icons.Default.FlashOff else Icons.Default.FlashOn,
                            contentDescription = "Toggle Flash",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }


                // Capture button
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clickable(onClick = {
                            if (!isCapturing) {
                                isCapturing = true
                                scope.launch {
                                    captureImage(
                                        context = context,
                                        imageCapture = imageCapture,
                                        onImageCaptured = { savedUri ->
                                            val route = ImagePreviewScreen(
                                                imageUri = savedUri.toString(),
                                                takenFromCamera = true,
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
                        .border(
                            width = 4.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = MaterialShapes.Sunny.toShape()
                        )
                        .clip(MaterialShapes.Sunny.toShape()),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .animateContentSize()
                            .size(if (isCapturing) 65.dp else 70.dp)
                            .background(
                                MaterialTheme.colorScheme.primary,
                                MaterialShapes.Sunny.toShape()
                            )
                            .border(
                                width = 2.dp,
                                shape = MaterialShapes.Sunny.toShape(),
                                color = if (isCapturing) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                            )
                    )
                }

                // Camera flip button
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary
                ) {
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
                            animationSpec = tween(durationMillis = 300), label = ""
                        )
                        Icon(
                            imageVector = Icons.Default.Cameraswitch,
                            contentDescription = "Switch Camera",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.graphicsLayer {
                                rotationY = rotateX.value
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun captureImage(
    context: Context,
    imageCapture: ImageCapture?,
    onImageCaptured: (Uri) -> Unit,
    onError: (ImageCaptureException) -> Unit,
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
