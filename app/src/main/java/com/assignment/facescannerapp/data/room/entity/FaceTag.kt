package com.assignment.facescannerapp.data.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "FaceTag")
data class FaceTag(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val imageUri: String,
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
    val name: String
)
