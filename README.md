# Face Scanner App

An Android app that detects faces in gallery images, allows tagging of individual faces, and displays them in a grid with pagination support.

## Features

### ✅ Pagination with Android Paging 3
- Efficient loading of images using `PagingData`.
- Loads images page-by-page to reduce memory usage and improve performance.
- Handles empty states and loading states gracefully.

### 🖼️ Grid View of Face-Detected Images
- Images are displayed in a `LazyVerticalGrid`.
- Each image shows detected faces as circles (red for unnamed, green for tagged).
- Users can tap on a face to assign or edit a name.

### ⚡ Optimized Face Tagging and State Management
- Avoids redundant face detection DB calls by caching face results in memory using `StateFlow`.
- Updates only the changed face box when a tag is saved.
- Maintains tagged faces across paging loads.

### 🔒 Permission-Driven Access
- Face scanning starts **only after permission** is granted.
- Handles cases for permanently denied permissions by guiding the user to app settings.

## Performance Improvements
- Face detection results are stored in a local Room database and reused when tagging.
- Caching (`latestFacesMap`) prevents re-running MLKit detection for already scanned images.
- Reduces unnecessary recomposition by using `StateFlow` and clean Compose patterns.

## Architecture

- MVVM + Clean Architecture
- Kotlin
- Jetpack Compose (Declarative way of defining Ui)
- Pagination using Paging 3
- Coil for image loading
- State management : `StateFlow`
- Dependency injection : Hilt
- Persistence : Room (Wrapper on Sqlite)
- Face detection : Google ML Kit Vision (FaceDetector)
---

## 🔍 Assumptions

- Face position is matched on restart by comparing bounding box coordinates
- Gallery scan accesses only local images using `MediaStore`
- Each image might contain multiple faces, each tagged individually
- Tags are stored per image and bounding box, no face recognition used

---

## 🛠️ How to Run

1. Clone the repo

```bash
git https://github.com/LakshLathiya/FaceScannerApp.git
cd FaceScannerApp
```

2. Build App on Actual device

3. Give Permission to access gallery

4. you will screen app is fetching images from your device gallery

5. you can tag faces on any image by giving them name

***************************  Thank you ******************************
