package com.assignment.facescannerapp.paging

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.assignment.facescannerapp.data.room.dao.FaceTagDao
import com.assignment.facescannerapp.domain.model.FaceBox
import com.assignment.facescannerapp.domain.model.FaceImage
import com.assignment.facescannerapp.helper.loadBitmapFromUri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetector
import kotlinx.coroutines.tasks.await

class FaceImagePagingSource(
    private val context: Context,
    private val dao: FaceTagDao,
    private val faceDetector: FaceDetector
) : PagingSource<Int, FaceImage>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, FaceImage> {
        val page = params.key ?: 0
        val pageSize = params.loadSize

        return try {
            val allUris = fetchImageUris()
            val pageUris = allUris.drop(page * pageSize).take(pageSize)

            val images = pageUris.mapNotNull { uri ->
                val bitmap = loadBitmapFromUri(context, uri)
                val inputImage = InputImage.fromBitmap(bitmap ?: return@mapNotNull null, 0)

                val faces = faceDetector.process(inputImage).await()
                if (faces.isEmpty()) return@mapNotNull null

                val savedTags = dao.getTagsForImage(uri.toString())
                val boxes = faces.map { face ->
                    val bounds = face.boundingBox
                    val matchedTag = savedTags.find {
                        it.left == bounds.left.toFloat() &&
                                it.top == bounds.top.toFloat() &&
                                it.right == bounds.right.toFloat() &&
                                it.bottom == bounds.bottom.toFloat()
                    }

                    FaceBox(
                        left = bounds.left.toFloat(),
                        top = bounds.top.toFloat(),
                        right = bounds.right.toFloat(),
                        bottom = bounds.bottom.toFloat(),
                        name = matchedTag?.name
                    )
                }

                FaceImage(uri = uri, faces = boxes)
            }

            LoadResult.Page(
                data = images,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (images.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, FaceImage>): Int? = null

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
}


