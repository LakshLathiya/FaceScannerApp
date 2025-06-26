package com.assignment.facescannerapp.domain.usecase

import android.net.Uri
import com.assignment.facescannerapp.domain.model.FaceBox
import com.assignment.facescannerapp.domain.repo_interface.ImageRepository
import javax.inject.Inject

class DetectFacesForImageUseCase @Inject constructor(
    private val imageRepository: ImageRepository
) {
    suspend fun detectFacesForImage(uri: Uri): List<FaceBox> {
        return imageRepository.detectFacesForImage(uri)
    }
}