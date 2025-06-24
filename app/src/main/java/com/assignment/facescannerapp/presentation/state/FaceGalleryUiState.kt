package com.assignment.facescannerapp.presentation.state

import android.net.Uri
import com.assignment.facescannerapp.domain.model.FaceBox
import com.assignment.facescannerapp.domain.model.FaceImage

data class FaceGalleryUiState(
    val isLoading: Boolean = false,
    val faceImages: List<FaceImage> = emptyList(),
    val selectedBox: FaceBox? = null,
    val selectedImageUri: Uri? = null,
    val showTagDialog: Boolean = false
)