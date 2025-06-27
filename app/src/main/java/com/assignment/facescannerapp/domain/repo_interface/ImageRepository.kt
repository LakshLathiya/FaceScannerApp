package com.assignment.facescannerapp.domain.repo_interface

import android.net.Uri
import androidx.paging.PagingData
import com.assignment.facescannerapp.domain.model.FaceBox
import com.assignment.facescannerapp.domain.model.FaceImage
import kotlinx.coroutines.flow.Flow

interface ImageRepository {
    fun getPagedFaceImages(): Flow<PagingData<FaceImage>>
    suspend fun saveFaceTag(uri: String, box: FaceBox, name: String)
}