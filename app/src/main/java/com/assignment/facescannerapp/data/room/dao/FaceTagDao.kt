package com.assignment.facescannerapp.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.assignment.facescannerapp.data.room.entity.FaceTag

@Dao
interface FaceTagDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tag: FaceTag)

    @Query("SELECT * FROM FaceTag WHERE imageUri = :uri")
    suspend fun getTagsForImage(uri: String): List<FaceTag>
}
