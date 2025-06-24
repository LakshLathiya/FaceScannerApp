package com.assignment.facescannerapp.domain.usecase

import com.assignment.facescannerapp.domain.model.FaceBox
import com.assignment.facescannerapp.domain.repo_interface.ImageRepository
import javax.inject.Inject

class GetFaceTagForImageUseCase @Inject constructor(
    private val imageRepository: ImageRepository
) {
    suspend fun loadTagsForImage(uri: String): List<FaceBox> {
        return imageRepository.getTagsForImage(uri)
    }
}