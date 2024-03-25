package com.dugue.canipark.ui.camera

import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.dugue.canipark.R
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView

@Composable
@Preview
fun CameraUIPreview() {
    CameraUI()
}

@Composable
fun CameraScreen(
    cameraState: CameraState,
    onCameraReady: (view: PreviewView) -> Unit = {},
    onPictureTaken: () -> Unit = {},
    onDismiss: () -> Unit = {},
    onAdViewReady: (adView: AdView) -> Unit = {}
) {
    when (cameraState) {
        is CameraState.Error -> MessageDialog(
            message = cameraState.message,
            isPositive = false,
            onDismiss = onDismiss
        )
        CameraState.Idle -> CameraUI(
            onCameraReady = onCameraReady,
            onPictureTaken = onPictureTaken,
            onAdViewReady = onAdViewReady
        )
        CameraState.Loading -> LoadingDialog()
        is CameraState.ParkingNotAllowed -> MessageDialog(
            message = cameraState.message,
            isPositive = false,
            onDismiss = onDismiss
        )
        is CameraState.ParkingAllowed -> MessageDialog(
            message = cameraState.message,
            isPositive = true,
            onDismiss = onDismiss
        )
    }
}

@Composable
private fun CameraUI(
    onCameraReady: (view: PreviewView) -> Unit = {},
    onPictureTaken: () -> Unit = {},
    onAdViewReady: (adView: AdView) -> Unit = {}
) {
    Box {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                PreviewView(context)
            },
            update = { view ->
                onCameraReady(view)
            }
        )
        AndroidView(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            factory = { context ->
                AdView(context).apply {
                    adUnitId = "ca-app-pub-2138105660848240/1475290586"
                    setAdSize(com.google.android.gms.ads.AdSize.BANNER)
                    loadAd(AdRequest.Builder().build())
                }
            },
            update = { view ->
                onAdViewReady(view)
            }
        )
        BottomAppBar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .alpha(0.4f),
            backgroundColor = MaterialTheme.colors.surface
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) { // Column to center the button
                IconButton(
                    onClick = onPictureTaken,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .background(
                            color = MaterialTheme.colors.primary,
                            shape = RoundedCornerShape(25)
                        )
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.camera),
                        contentDescription = "Take picture",
                        tint = MaterialTheme.colors.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageDialog(
    message: String,
    isPositive: Boolean,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(8.dp),
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    imageVector = if (isPositive) ImageVector.vectorResource(id = R.drawable.ic_check) else ImageVector.vectorResource(id = R.drawable.ic_error),
                    contentDescription = "Result",
                    modifier = Modifier.size(96.dp),
                    colorFilter = ColorFilter.tint(
                        if (isPositive) MaterialTheme.colors.primary else MaterialTheme.colors.error
                    )
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.body1
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onDismiss
                ) {
                    Text("OK")
                }
            }
        }
    }
}

@Composable
private fun LoadingDialog() {
    Dialog(onDismissRequest = {}) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter).padding(8.dp),
                factory = { context ->
                    AdView(context).apply {
                        adUnitId = "ca-app-pub-2138105660848240/7041466126"
                        setAdSize(com.google.android.gms.ads.AdSize.BANNER)
                        loadAd(AdRequest.Builder().build())
                    }
                },
                update = { view ->
                    // onAdViewReady(view)
                }
            )
            Card(
                modifier = Modifier.padding(16.dp).align(Alignment.Center),
                shape = RoundedCornerShape(8.dp),
                backgroundColor = Color.White
            ) { // Card to give a background color
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Analyzing...", style = MaterialTheme.typography.h6)
                    Spacer(modifier = Modifier.height(8.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.height(150.dp).width(150.dp),
                        color = MaterialTheme.colors.primary,
                        strokeWidth = 15.dp
                    )
                }
            }
            AndroidView(
                modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(8.dp),
                factory = { context ->
                    AdView(context).apply {
                        adUnitId = "ca-app-pub-2138105660848240/1597567752"
                        setAdSize(com.google.android.gms.ads.AdSize.BANNER)
                        loadAd(AdRequest.Builder().build())
                    }
                },
                update = { view ->
                    // onAdViewReady(view)
                }
            )
        }
    }
}