package com.assignment.facescannerapp

import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import com.assignment.facescannerapp.helper.PermissionHelper
import com.assignment.facescannerapp.view.FaceGalleryScreen
import com.assignment.facescannerapp.view.PermissionRequiredScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val permissionGranted = mutableStateOf(false)
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.all { it.value }) {
            permissionGranted.value = true
        } else {
            handlePermissionDenied(result)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        askGalleryPermission()
        setContent {
            if (permissionGranted.value) {
                FaceGalleryScreen()
            } else {
                PermissionRequiredScreen(onRetry = { askGalleryPermission() })
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkPermissionAfterSettings()
    }

    private fun checkPermissionAfterSettings() {
        val permissions = PermissionHelper.getRequiredPermissions()
        if (permissions.all {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }
        ) {
            permissionGranted.value = true
        }
    }


    private fun askGalleryPermission() {
        val permissions = PermissionHelper.getRequiredPermissions()
        if (permissions.all {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }
        ) {
            permissionGranted.value = true
        } else {
            permissionLauncher.launch(permissions)
        }
    }

    private fun handlePermissionDenied(result: Map<String, Boolean>) {
        val firstDenied = result.entries.firstOrNull { !it.value }?.key ?: return
        if (PermissionHelper.isPermissionPermanentlyDenied(this, firstDenied)) {
            showGoToSettingsDialog()
        } else {
            Toast.makeText(this, "Permission is required to proceed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showGoToSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("You have permanently denied the gallery permission. Please allow it from settings to continue.")
            .setCancelable(false)
            .setPositiveButton("Open Settings") { _, _ ->
                PermissionHelper.openAppSettings(this)
            }
            .setNegativeButton("Exit App") { _, _ ->
                finish()
            }
            .show()
    }
}
