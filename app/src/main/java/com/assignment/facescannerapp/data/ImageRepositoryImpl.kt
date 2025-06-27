package com.assignment.facescannerapp.data

import android.content.Context
import android.net.Uri
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.assignment.facescannerapp.data.room.dao.FaceTagDao
import com.assignment.facescannerapp.data.room.entity.FaceTag
import com.assignment.facescannerapp.domain.model.FaceBox
import com.assignment.facescannerapp.domain.model.FaceImage
import com.assignment.facescannerapp.domain.repo_interface.ImageRepository
import com.assignment.facescannerapp.helper.loadBitmapFromUri
import com.assignment.facescannerapp.paging.FaceImagePagingSource
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.math.abs

class ImageRepositoryImpl @Inject constructor(
    private val context: Context,
    private val dao: FaceTagDao
) : ImageRepository {
    private val faceDetector: FaceDetector by lazy {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .enableTracking()
            .build()

        FaceDetection.getClient(options)
    }

    override fun getPagedFaceImages(): Flow<PagingData<FaceImage>> {
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false,
                initialLoadSize = 5
            ),
            pagingSourceFactory = {
                FaceImagePagingSource(context, dao, faceDetector)
            }
        ).flow
    }

    override suspend fun saveFaceTag(uri: String, box: FaceBox, name: String) {
        dao.insert(
            FaceTag(
                imageUri = uri,
                left = box.left,
                top = box.top,
                right = box.right,
                bottom = box.bottom,
                name = name
            )
        )
    }
}
