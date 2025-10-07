# 🏃‍♂️ Health Mantra - Exercise Management App

An Android application for scheduling and managing exercise routines with intelligent conflict detection.

## ✨ Features

- 📝 **Manual Exercise Logging** - Schedule exercises with name, duration, calories, and specific date/time
- 🔄 **Google Health Sync** - Import exercises from Google Health (simulated for demo)
- ⚠️ **Intelligent Conflict Detection** - Automatically detects when exercises have overlapping time ranges
- ✅ **One-Tap Conflict Resolution** - Resolve scheduling conflicts by selecting which exercise to keep
- 🎨 **Modern Material 3 UI** - Clean, intuitive interface built with Jetpack Compose
- 📅 **Future-Focused View** - Shows only current and upcoming exercises, past ones auto-removed
- 🎨 **Source Tracking** - Visual badges show exercise origin (Manual, Google Health, etc.)

## 🛠️ Tech Stack

### Architecture & Design Patterns
- **MVVM (Model-View-ViewModel)** - Clean separation of concerns
- **Repository Pattern** - Single source of truth for data
- **Dependency Injection** - Hilt for managing dependencies

### Android Jetpack Components
- **Jetpack Compose** - Modern declarative UI toolkit
- **Material 3** - Latest Material Design system
- **Room Database** - Local data persistence with SQLite
- **Navigation Component** - Type-safe screen navigation
- **ViewModel** - Lifecycle-aware state management
- **Flow** - Reactive data streams

### Language & Async
- **Kotlin** - 100% Kotlin codebase
- **Coroutines** - Asynchronous programming
- **Flow** - Reactive data observation

## 📱 App Preview

### Main Features Demonstrated

**Manual Exercise Logging:**
- Add exercises with custom name, duration, and calorie information
- Schedule for any future date and time using native pickers
- Exercises stored locally in Room database

**Google Health Sync:**
- Simulates importing exercises from external health platforms
- Demonstrates multi-source data integration
- Prevents duplicate syncs

**Conflict Detection:**
- Automatically detects when two or more exercises have overlapping time ranges
- Visual indicators: red backgrounds and warning icons on conflicted exercises
- Conflict count badge in app bar
- Example: Swimming (10:00-10:30) conflicts with Jogging (10:15-11:00)

**Conflict Resolution:**
- Dedicated screen showing all conflict groups
- Select which exercise to keep with one tap
- Other conflicting exercises automatically removed
- UI updates reactively
