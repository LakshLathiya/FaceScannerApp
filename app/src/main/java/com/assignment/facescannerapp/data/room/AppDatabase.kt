package com.assignment.facescannerapp.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.assignment.facescannerapp.data.room.dao.FaceTagDao
import com.assignment.facescannerapp.data.room.entity.FaceTag

@Database(entities = [FaceTag::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun faceTagDao(): FaceTagDao
}
