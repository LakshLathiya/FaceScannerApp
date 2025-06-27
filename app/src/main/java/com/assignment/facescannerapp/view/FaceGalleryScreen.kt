package com.assignment.facescannerapp.view

import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.assignment.facescannerapp.domain.model.FaceBox
import com.assignment.facescannerapp.domain.model.FaceImage
import com.assignment.facescannerapp.viewmodel.FaceGalleryViewModel

@Composable
fun FaceGalleryScreen(viewModel: FaceGalleryViewModel = hiltViewModel()) {
    val pagingItems = viewModel.pagedImages.collectAsLazyPagingItems()
    val selectedBox by viewModel.selectedBox.collectAsState()
    val showDialog by viewModel.showDialog.collectAsState()
    val facesMap by viewModel.latestFacesMap.collectAsState()

    when {
        pagingItems.loadState.refresh is LoadState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        pagingItems.itemCount == 0 -> {
            Box(
                modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No faces found in your gallery.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        else -> {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(pagingItems.itemCount) { index ->
                    val item = pagingItems[index] ?: return@items
                    val updatedFaces = facesMap[item.uri] ?: item.faces
                    FaceImageItem(image = item.copy(faces = updatedFaces),
                        onFaceClick = { faceBox ->
                            viewModel.onFaceClick(item.uri, faceBox, updatedFaces)
                        })
                }
            }
        }
    }
    AnimatedVisibility(visible = showDialog && selectedBox != null) {
        TagFaceDialog(box = selectedBox,
            onSave = { name -> viewModel.saveTag(name) },
            onDismiss = { viewModel.dismissDialog() })
    }
}

@Composable
fun FaceImageItem(image: FaceImage, onFaceClick: (FaceBox) -> Unit) {
    val context = LocalContext.current
    val bitmap = remember(image.uri) {
        context.contentResolver.openInputStream(image.uri)?.use {
            BitmapFactory.decodeStream(it)
        }
    }

    bitmap?.let { bmp ->
        val imageWidth = bmp.width
        val imageHeight = bmp.height

        BoxWithConstraints(
            modifier = Modifier
                .size(160.dp)
                .padding(4.dp)
                .border(1.dp, color = Color.Gray)
        ) {
            val containerWidth = constraints.maxWidth.toFloat()
            val containerHeight = constraints.maxHeight.toFloat()
            val scale = minOf(containerWidth / imageWidth, containerHeight / imageHeight)

            val drawnImageWidth = imageWidth * scale
            val drawnImageHeight = imageHeight * scale

            val xOffset = (containerWidth - drawnImageWidth) / 2
            val yOffset = (containerHeight - drawnImageHeight) / 2

            AsyncImage(
                model = image.uri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )

            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val closestBox = image.faces
                                .map { box ->
                                    val cx = ((box.left + box.right) / 2) * scale + xOffset
                                    val cy = ((box.top + box.bottom) / 2) * scale + yOffset
                                    val radius = ((box.right - box.left) / 2) * scale
                                    val distance = (offset - Offset(cx, cy)).getDistance()
                                    Triple(box, distance, radius)
                                }
                                .filter { it.second <= it.third * 1.3f }
                                .minByOrNull { it.second }

                            closestBox?.let { (box, _, _) ->
                                onFaceClick(box)
                            }
                        }
                    }
            ) {
                image.faces.forEach { box ->
                    val cx = ((box.left + box.right) / 2) * scale + xOffset
                    val cy = ((box.top + box.bottom) / 2) * scale + yOffset
                    val radius = ((box.right - box.left) / 2) * scale

                    drawCircle(
                        color = if (box.name.isNullOrEmpty()) Color.Red else Color.Green,
                        radius = radius,
                        center = Offset(cx, cy),
                        style = Stroke(width = 4f)
                    )

                    box.name?.let { name ->
                        drawContext.canvas.nativeCanvas.drawText(
                            name,
                            cx,
                            cy - radius - 10,
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.WHITE
                                textSize = 28f
                                isFakeBoldText = true
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TagFaceDialog(box: FaceBox?, onSave: (String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf(box?.name ?: "") }
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
