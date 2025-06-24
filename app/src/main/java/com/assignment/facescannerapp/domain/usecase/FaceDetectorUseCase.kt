package com.assignment.facescannerapp.domain.usecase

import com.assignment.facescannerapp.domain.model.FaceImage
import com.assignment.facescannerapp.domain.repo_interface.ImageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FaceDetectorUseCase @Inject constructor(
    private val imageRepository: ImageRepository
) {
    fun scanImagesIncrementally(): Flow<FaceImage> = imageRepository.loadImagesWithFacesIncrementally()
}