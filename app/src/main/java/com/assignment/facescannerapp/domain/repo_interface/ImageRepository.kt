package com.assignment.facescannerapp.domain.repo_interface

import android.net.Uri
import com.assignment.facescannerapp.domain.model.FaceBox
import com.assignment.facescannerapp.domain.model.FaceImage
import kotlinx.coroutines.flow.Flow

interface ImageRepository {
    fun loadImagesWithFacesIncrementally(): Flow<FaceImage>
    suspend fun saveFaceTag(uri: String, box: FaceBox, name: String)
    suspend fun getTagsForImage(uri: String): List<FaceBox>
    suspend fun detectFacesForImage(uri: Uri): List<FaceBox>
}