package com.assignment.facescannerapp.data

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.assignment.facescannerapp.data.room.dao.FaceTagDao
import com.assignment.facescannerapp.data.room.entity.FaceTag
import com.assignment.facescannerapp.domain.model.FaceBox
import com.assignment.facescannerapp.domain.model.FaceImage
import com.assignment.facescannerapp.domain.repo_interface.ImageRepository
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

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

    override fun loadImagesWithFacesIncrementally(): Flow<FaceImage> = flow {
        val imageUris = fetchImageUris()
        for (uri in imageUris) {
            val bitmap = loadBitmapFromUri(uri) ?: continue
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            try {
                val faces = faceDetector.process(inputImage).await()
                if (faces.isNotEmpty()) {
                    val savedTags = dao.getTagsForImage(uri.toString())
                    val boxes = faces.map { face ->
                        val bounds = face.boundingBox
                        val name = savedTags.find {
                            it.left == bounds.left.toFloat() && it.top == bounds.top.toFloat() && it.right == bounds.right.toFloat() && it.bottom == bounds.bottom.toFloat()
                        }?.name

                        FaceBox(
                            left = bounds.left.toFloat(),
                            top = bounds.top.toFloat(),
                            right = bounds.right.toFloat(),
                            bottom = bounds.bottom.toFloat(),
                            name = name
                        )
                    }
                    emit(FaceImage(uri, boxes))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }.flowOn(Dispatchers.IO)

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

    override suspend fun getTagsForImage(uri: String): List<FaceBox> {
        return dao.getTagsForImage(uri).map {
            FaceBox(it.left, it.top, it.right, it.bottom, it.name)
        }
    }

    private fun fetchImageUris(): List<Uri> {
        val uris = mutableListOf<Uri>()
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        val query = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )

        query?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val uri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                uris.add(uri)
            }
        }
        return uris
    }

    private fun loadBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
