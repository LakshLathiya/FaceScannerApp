package com.assignment.facescannerapp.domain.usecase

import com.assignment.facescannerapp.domain.model.FaceBox
import com.assignment.facescannerapp.domain.repo_interface.ImageRepository
import javax.inject.Inject

class SaveFaceTagUseCase @Inject constructor(
    private val imageRepository: ImageRepository
) {
    suspend fun saveTag(uri: String, box: FaceBox, name: String) =
        imageRepository.saveFaceTag(uri, box, name)
}