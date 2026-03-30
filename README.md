# 🖼️ ArtFinder

ArtFinder is a modern Android application built with **Jetpack Compose** that allows users to discover, track, and explore artworks geographically. The app integrates with the **Art Institute of Chicago API** and uses **Firebase** for user data and authentication.

---

## 📌 Milestone 1: Authentication, Profile, & Art List

### 🎯 Objective
Build the foundation of the app using authentication, cloud storage, and API-based art discovery.

### 🛠️ Requirements
*   ✅ **Authentication & User Management**
    *   User registration and login (email/password) using **Firebase Auth**.
    *   User profile management (Create, Read, Update) using **Firebase Firestore**.
*   ✅ **Art Discovery (API Integration)**
    *   Retrieve and display a list of artwork using **Retrofit**.
    *   Display artwork **name** and **type** for each list item.
    *   Implement **search functionality** by artwork name.
*   ✅ **User Interface & Experience**
    *   Detailed view for selected artwork.
    *   **Artwork Details Screen** includes: Artist details, image, location & gallery details, and metadata.
*   ✅ **Architecture & Best Practices**
    *   Implement **MVVM + Repository** pattern.
    *   Ensure clear separation between **API**, **Cloud**, and **UI** layers.

---

## 📍 Milestone 2: Location Services & Visited Tracking

### 🎯 Objective
Enable users to explore art geographically using maps and location data while maintaining a persistent list of visited artworks.

### 🛠️ Requirements

*   ✅ **Visited Artwork Management**
    *   **Add to Visited List:** Users can mark any artwork as "Visited".
    *   **Firestore Sync:** Visited data (ID, Title, Image, Timestamp, Lat/Long) is stored in a separate Firestore collection for each specific user.
    *   **History View:** A dedicated screen to view the list of visited artworks.
    *   **Detailed History:** Each visited item shows the artwork details and exactly **when it was visited** (formatted timestamp).
    *   **Deletion:** Users can remove artworks from their visited list, with changes reflected instantly on Firebase.

*   ✅ **Location Services & Maps**
    *   **Integrated Google Maps:** View artwork locations on a map within the Details screen (applies to both the main list and visited list).
    *   **Permissions:** Runtime handling of `ACCESS_FINE_LOCATION` and `ACCESS_COARSE_LOCATION` using Accompanist.
    *   **Device Location:** Users can view their **real current location** on the map (the "blue dot" and "Locate Me" button).
    *   **Dynamic Markers:** 
        *   Markers show the artwork's location based on API coordinates.
        *   **Distinguished Markers:** Marker color changes from **Red** to **Green** automatically if the user has visited that artwork.
    *   **External Navigation:** "Directions" button launches the **inbuilt Google Maps app** to provide driving/walking directions to the artwork.
    *   **Robust Handling:** Added fallback coordinates (Chicago Art Institute) and UI labels for artworks with null location data to prevent app crashes.

---

## 📸 Milestone 3: Photos and Point Rewards (Part 1)

### 🎯 Objective
Allow users to attach photos to their visited artworks, establishing the foundation for the rewards system.

### 🛠️ Requirements (Camera & Storage)

*   ✅ **Camera Integration**
    *   **Instant Photo:** Capture photos of your visit instantly using the system camera.
    *   **Gallery Upload:** Support for uploading existing photos from the device gallery.
    *   **Multiple Photos:** Users can upload one or more photos to a specific artwork.
*   ✅ **Cloud Storage Sync**
    *   **Firebase Storage:** Photos are securely saved to Firebase Storage in user-specific folders (`users/{uid}/artworks/{artworkId}/`).
    *   **Firestore Linking:** Links to the uploaded photos are saved to the corresponding document in the user's visited collection.
*   ✅ **Photo Display**
    *   **Horizontal Gallery:** View all uploaded visit photos in a horizontal scrolling list directly on the artwork details screen.

---

## 🚀 Tech Stack
*   **Language:** Kotlin
*   **UI:** Jetpack Compose (Material 3)
*   **Backend:** Firebase Auth, Cloud Firestore, Firebase Storage
*   **Networking:** Retrofit & OkHttp
*   **Maps:** Google Maps SDK for Android & Maps Compose
*   **Image Loading:** Coil (SVG support)
*   **Navigation:** Jetpack Compose Navigation
