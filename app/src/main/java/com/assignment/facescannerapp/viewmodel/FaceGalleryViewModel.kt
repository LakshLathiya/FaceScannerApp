package com.assignment.facescannerapp.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.assignment.facescannerapp.domain.model.FaceBox
import com.assignment.facescannerapp.domain.model.FaceImage
import com.assignment.facescannerapp.domain.usecase.GetPagedFaceImagesUseCase
import com.assignment.facescannerapp.domain.usecase.SaveFaceTagUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.absoluteValue

@HiltViewModel
class FaceGalleryViewModel @Inject constructor(
    getPagedFaceImagesUseCase: GetPagedFaceImagesUseCase,
    private val saveFaceTagUseCase: SaveFaceTagUseCase
) : ViewModel() {

    val pagedImages: Flow<PagingData<FaceImage>> =
        getPagedFaceImagesUseCase().flowOn(Dispatchers.IO).cachedIn(viewModelScope)

    private val _selectedBox = MutableStateFlow<FaceBox?>(null)
    val selectedBox = _selectedBox.asStateFlow()

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)

    private val _showDialog = MutableStateFlow(false)
    val showDialog = _showDialog.asStateFlow()

    private val _latestFacesMap = MutableStateFlow<Map<Uri, List<FaceBox>>>(emptyMap())
    val latestFacesMap = _latestFacesMap.asStateFlow()


    fun onFaceClick(uri: Uri, box: FaceBox, allFaces: List<FaceBox>) {
        _selectedBox.value = box
        _selectedImageUri.value = uri
        if (!_latestFacesMap.value.containsKey(uri)) {
            _latestFacesMap.value = _latestFacesMap.value.toMutableMap().apply {
                this[uri] = allFaces
            }
        }
        _showDialog.value = true
    }


    fun dismissDialog() {
        _showDialog.value = false
    }

    fun saveTag(name: String) {
        val uri = _selectedImageUri.value ?: return
        val selectedBox = _selectedBox.value ?: return
        val allFaces = latestFacesMap.value[uri] ?: return

        viewModelScope.launch(Dispatchers.IO) {
            saveFaceTagUseCase.saveTag(uri.toString(), selectedBox, name)

            val updatedFaces = allFaces.map { face ->
                if (areFacesSame(face, selectedBox)) {
                    face.copy(name = name)
                } else {
                    face
                }
            }

            _latestFacesMap.value = _latestFacesMap.value.toMutableMap().apply {
                this[uri] = updatedFaces
            }

            _showDialog.value = false
        }
    }


    private fun areFacesSame(f1: FaceBox, f2: FaceBox): Boolean {
        val epsilon = 10f
        return (f1.left - f2.left).absoluteValue < epsilon &&
                (f1.top - f2.top).absoluteValue < epsilon &&
                (f1.right - f2.right).absoluteValue < epsilon &&
                (f1.bottom - f2.bottom).absoluteValue < epsilon
    }
}