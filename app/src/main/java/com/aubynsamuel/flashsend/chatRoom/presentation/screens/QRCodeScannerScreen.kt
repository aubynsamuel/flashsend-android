package com.aubynsamuel.flashsend.chatRoom.presentation.screens

/**
 * Could be repurpose to start new chats by scanning the QR code of the other user
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

@OptIn(ExperimentalGetImage::class)
private fun processImageProxy(
imageProxy: ImageProxy,
barcodeScanner: BarcodeScanner,
onQrCodeScanned: (String) -> Unit
) {
val mediaImage = imageProxy.image
if (mediaImage == null) {
imageProxy.close()
return
}
val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
barcodeScanner.process(image)
.addOnSuccessListener { barcodes ->
for (barcode in barcodes) {
val qrCode = barcode.rawValue
if (!qrCode.isNullOrEmpty()) {
onQrCodeScanned(qrCode)
}
}
}
.addOnFailureListener { e ->
Log.e("QRScanner", "QR Code scanning failed", e)
}
.addOnCompleteListener {
imageProxy.close()
}
}

@Composable
fun QRCodeScannerScreen(
onQrCodeScanned: (String) -> Unit
) {
val context = LocalContext.current
val lifecycleOwner = LocalLifecycleOwner.current
val previewView = remember { PreviewView(context) }
val executor = ContextCompat.getMainExecutor(context)

LaunchedEffect(Unit) {
val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
cameraProviderFuture.addListener({
val cameraProvider = cameraProviderFuture.get()
val preview = Preview.Builder().build().also {
it.surfaceProvider = previewView.surfaceProvider
}
// Configure the barcode scanner for QR codes.
val barcodeScannerOptions = BarcodeScannerOptions.Builder()
.setBarcodeFormats(Barcode.FORMAT_QR_CODE)
.build()
val barcodeScanner = BarcodeScanning.getClient(barcodeScannerOptions)
val imageAnalyzer = ImageAnalysis.Builder()
.setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
.build().also { analysis ->
analysis.setAnalyzer(executor) { imageProxy ->
processImageProxy(imageProxy, barcodeScanner, onQrCodeScanned)
}
}

val cameraSelector = CameraSelector.Builder()
.requireLensFacing(CameraSelector.LENS_FACING_BACK)
.build()

try {
cameraProvider.unbindAll()
cameraProvider.bindToLifecycle(
lifecycleOwner,
cameraSelector,
preview,
imageAnalyzer
)
} catch (exc: Exception) {
Log.e("QRScanner", "Use case binding failed", exc)
}
}, executor)
}

AndroidView(
factory = { previewView },
modifier = Modifier.fillMaxSize()
)
}

@Composable
fun QRScannerScreen() {
var scannedText by remember { mutableStateOf("") }

Box(modifier = Modifier.fillMaxSize()) {
// The camera preview and QR scanning
QRCodeScannerScreen { qrCode ->
scannedText = qrCode
}
// Overlay UI to display scanned text
Column(
modifier = Modifier
.fillMaxSize()
.padding(16.dp),
verticalArrangement = Arrangement.Bottom,
horizontalAlignment = Alignment.CenterHorizontally
) {
Text(
text = if (scannedText.isEmpty()) "Scan a QR Code" else "Scanned: $scannedText",
fontSize = 18.sp,
color = Color.White
)
}
}
}
 */