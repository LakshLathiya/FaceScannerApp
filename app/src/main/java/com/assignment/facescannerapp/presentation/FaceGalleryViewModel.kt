package com.assignment.facescannerapp.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.assignment.facescannerapp.domain.model.FaceBox
import com.assignment.facescannerapp.domain.usecase.DetectFacesForImageUseCase
import com.assignment.facescannerapp.domain.usecase.FaceDetectorUseCase
import com.assignment.facescannerapp.domain.usecase.GetFaceTagForImageUseCase
import com.assignment.facescannerapp.domain.usecase.SaveFaceTagUseCase
import com.assignment.facescannerapp.presentation.state.FaceGalleryUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FaceGalleryViewModel @Inject constructor(
    private val faceDetectorUseCase: FaceDetectorUseCase,
    private val saveFaceTagUseCase: SaveFaceTagUseCase,
    private val getFaceTagForImageUseCase: GetFaceTagForImageUseCase,
    private val detectFacesForImageUseCase: DetectFacesForImageUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FaceGalleryUiState())
    val uiState: StateFlow<FaceGalleryUiState> = _uiState

    init {
        scanGallery()
    }

    private fun scanGallery() {
        viewModelScope.launch {
            faceDetectorUseCase.scanImagesIncrementally().onStart {
                _uiState.value = uiState.value.copy(isLoading = true)
            }.collect { faceImage ->
                val currentList = _uiState.value.faceImages.toMutableList()
                currentList.add(faceImage)
                _uiState.value = uiState.value.copy(
                    isLoading = false, faceImages = currentList
                )
            }
        }
    }


    fun onFaceClick(uri: Uri, box: FaceBox) {
        _uiState.value = _uiState.value.copy(
            selectedBox = box, selectedImageUri = uri, showTagDialog = true
        )
    }

    fun saveTag(name: String) {
        val uri = _uiState.value.selectedImageUri ?: return
        val box = _uiState.value.selectedBox ?: return

        viewModelScope.launch {
            saveFaceTagUseCase.saveTag(uri.toString(), box, name)
            val detectedFaces = detectFacesForImageUseCase.detectFacesForImage(uri)
            val savedTags = getFaceTagForImageUseCase.loadTagsForImage(uri.toString())
            val mergedFaces = detectedFaces.map { detectedBox ->
                val match = savedTags.find {
                    it.left == detectedBox.left &&
                            it.top == detectedBox.top &&
                            it.right == detectedBox.right &&
                            it.bottom == detectedBox.bottom
                }
                detectedBox.copy(name = match?.name)
            }
            val updatedList = _uiState.value.faceImages.map {
                if (it.uri == uri) it.copy(faces = mergedFaces) else it
            }
            _uiState.value = _uiState.value.copy(
                faceImages = updatedList,
                selectedImageUri = null,
                selectedBox = null,
                showTagDialog = false
            )
        }
    }


    fun dismissDialog() {
        _uiState.value = _uiState.value.copy(showTagDialog = false)
    }

}