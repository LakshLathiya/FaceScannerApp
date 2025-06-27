package com.assignment.facescannerapp.domain.usecase

import androidx.paging.PagingData
import com.assignment.facescannerapp.domain.model.FaceImage
import com.assignment.facescannerapp.domain.repo_interface.ImageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPagedFaceImagesUseCase @Inject constructor(
    private val repository: ImageRepository
) {
    operator fun invoke(): Flow<PagingData<FaceImage>> = repository.getPagedFaceImages()
}
