# Face Scanner App

An Android app that scans the user's photo gallery, detects faces in images,
draws bounding circles around them, and allows tagging of each face.
Tags are saved locally and persist across app restarts.

---

## Features

- Detects faces in images using Google ML Kit
- Draws bounding circles over each detected face
- Allows users to tap on faces and assign tags (names)
- Tags are persisted locally using Room database
- Runs face detection in parallel, streaming results as they’re ready

---

## Architecture

- MVVM + Clean Architecture
- Kotlin
- Jetpack Compose (Declarative way of defining Ui)
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
