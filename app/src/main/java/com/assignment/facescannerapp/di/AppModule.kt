package com.assignment.facescannerapp.di

import android.content.Context
import androidx.room.Room
import com.assignment.facescannerapp.data.ImageRepositoryImpl
import com.assignment.facescannerapp.data.room.AppDatabase
import com.assignment.facescannerapp.data.room.dao.FaceTagDao
import com.assignment.facescannerapp.domain.repo_interface.ImageRepository
import com.assignment.facescannerapp.domain.usecase.GetPagedFaceImagesUseCase
import com.assignment.facescannerapp.domain.usecase.SaveFaceTagUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "face_db").build()

    @Provides
    fun provideDao(db: AppDatabase): FaceTagDao = db.faceTagDao()

    @Provides
    fun provideRepo(@ApplicationContext context: Context, dao: FaceTagDao): ImageRepository =
        ImageRepositoryImpl(context, dao)

    @Provides
    fun provideGetPagedFaceImagesUseCase(repo: ImageRepository): GetPagedFaceImagesUseCase =
        GetPagedFaceImagesUseCase(repo)

    @Provides
    fun provideSaveFaceTagUseCase(repo: ImageRepository): SaveFaceTagUseCase =
        SaveFaceTagUseCase(repo)
}
