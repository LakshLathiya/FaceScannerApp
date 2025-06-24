package com.assignment.facescannerapp.view

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.assignment.facescannerapp.domain.model.FaceBox
import com.assignment.facescannerapp.domain.model.FaceImage
import com.assignment.facescannerapp.presentation.FaceGalleryViewModel

@Composable
fun FaceGalleryScreen(viewModel: FaceGalleryViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator()
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Top) {
                items(uiState.faceImages) { image ->
                    FaceImageItem(image = image, onFaceClick = { faceBox ->
                        viewModel.onFaceClick(image.uri, box = faceBox)
                    })
                }
            }
        }
    }

    val selectedBox = uiState.selectedBox
    val showDialog = uiState.showTagDialog

    if (showDialog && selectedBox != null) {
        TagFaceDialog(box = selectedBox, onSave = { name ->
            viewModel.saveTag(name)
        }, onDismiss = {
            viewModel.dismissDialog()
        })
    }

}

@Composable
fun FaceImageItem(image: FaceImage, onFaceClick: (FaceBox) -> Unit) {
    val context = LocalContext.current
    val bitmap = remember(image.uri) {
        val stream = context.contentResolver.openInputStream(image.uri)
        BitmapFactory.decodeStream(stream)
    }

    bitmap?.let {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            val imageWidth = it.width
            val imageHeight = it.height

            val containerWidth = constraints.maxWidth.toFloat()
            val scaleFactor = containerWidth / imageWidth
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(imageWidth.toFloat() / imageHeight)
            ) {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )

                Canvas(modifier = Modifier
                    .matchParentSize()
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            image.faces.forEach { box ->
                                val cx = ((box.left + box.right) / 2) * scaleFactor
                                val cy = ((box.top + box.bottom) / 2) * scaleFactor
                                val radius = ((box.right - box.left) / 2) * scaleFactor
                                if ((offset - Offset(cx, cy)).getDistance() <= radius) {
                                    onFaceClick(box)
                                }
                            }
                        }
                    }) {
                    image.faces.forEach { box ->
                        val cx = ((box.left + box.right) / 2) * scaleFactor
                        val cy = ((box.top + box.bottom) / 2) * scaleFactor
                        val radius = ((box.right - box.left) / 2) * scaleFactor

                        drawCircle(
                            color = if (box.name.isNullOrEmpty()) Color.Red else Color.Green,
                            radius = radius,
                            center = Offset(cx, cy),
                            style = Stroke(width = 4f)
                        )

                        box.name?.let { name ->
                            drawContext.canvas.nativeCanvas.drawText(name,
                                cx,
                                cy - radius - 10,
                                android.graphics.Paint().apply {
                                    color = android.graphics.Color.WHITE
                                    textSize = 32f
                                    isFakeBoldText = true
                                })
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun TagFaceDialog(box: FaceBox, onSave: (String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf(box.name ?: "") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Tag Face") }, text = {
        Column {
            Text("Enter name for face")
            TextField(value = name, onValueChange = { name = it })
        }
    }, confirmButton = {
        Button(onClick = {
            onSave(name)
            onDismiss()
        }) {
            Text("Save")
        }
    }, dismissButton = {
        Button(onClick = onDismiss) {
            Text("Cancel")
        }
    })
}


