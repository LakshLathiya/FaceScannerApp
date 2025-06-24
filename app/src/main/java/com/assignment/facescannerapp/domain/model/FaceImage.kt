package com.assignment.facescannerapp.domain.model

import android.net.Uri

data class FaceImage(
    val uri: Uri,
    val faces: List<FaceBox>
)

data class FaceBox(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
    val name: String? = null
)
