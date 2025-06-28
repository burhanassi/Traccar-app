# Android Location Tracker with Traccar Integration

This is a Kotlin-based Android application built as part of a take-home challenge. It demonstrates a simple but realistic IoT tracking scenario by integrating with a locally hosted [Traccar](https://www.traccar.org/download/) GPS tracking server.

---

## Features

- Foreground service to fetch high-accuracy location updates.
- Integration with the [Traccar Server](https://www.traccar.org/) using the [OsmAnd protocol](https://www.traccar.org/osmand/).
- Displays current location in the app UI.
- Persistent unique device ID across app restarts.
- Permission handling for location and foreground/background services.
- Supports Android 10 and above (`targetSdk = 34`).
- Error handling for common issues like network failures.

---

## Architecture

- `DriverDashboardActivity`: Main activity UI with a toggle button to start/stop the tracking service.
- `MyLocationService`: Foreground service responsible for requesting and broadcasting location updates.
- `SharedPreferenceWrapper`: Persists last known location and UUID across app sessions.
- `AlarmReceiver`, `LocationListener`: Support utilities for location management and permission checks.

---

## 🛠️ Setup Instructions

### 1. Traccar Server

- Download from [Traccar Download Page](https://www.traccar.org/download/).
- Install and run on your local machine.
- Make sure your Android emulator or device can access the server's IP on port `5055` or whichever is configured.
  - Default OsmAnd port: `5055`
  - Example endpoint: `http://192.168.1.127:8082/?id=UNIQUE_ID&lat=...&lon=...`

### 2. Android App

#### Requirements

- Android Studio Giraffe+ (or compatible)
- Gradle Plugin 8.0+
- A real device or emulator with Google Play Services

#### Build and Run

1. Clone this repository or unzip the project folder.
2. Open in Android Studio.
3. Plug in a device or start an emulator.
4. Run the app (`Shift + F10`) after building.
5. On first launch, grant all permissions requested.
6. Click the **Go** button to start sending location; click again to stop.

---

## Permissions Required

| Permission                        | Purpose                                      |
|----------------------------------|----------------------------------------------|
| `ACCESS_FINE_LOCATION`           | For high-accuracy GPS tracking                |
| `ACCESS_COARSE_LOCATION`         | For fallback location detection               |
| `FOREGROUND_SERVICE_LOCATION`    | Required on Android 14+ for background use    |
| `FOREGROUND_SERVICE`             | To start the persistent location service      |

---

## Configuration Details

- **Device ID**: Generated once using `UUID.randomUUID()` and persisted via `SharedPreferences`.
- **OsmAnd Protocol**: Used to send GPS data to Traccar server in proper query parameter format.
- **Broadcast Receiver**: Listens for location updates in the activity for UI updates.

---

## Assumptions & Limitations

- Traccar Server is expected to run on the same local network as the device/emulator.
- App does not support background start on boot—this is focused on core requirements.
- Battery optimization settings may affect behavior on some OEM devices.

---

## Known Enhancements (Not Implemented Due to Time Constraint)

- Use of ViewModel + LiveData for UI/state separation.
- Offline caching and upload queue.
- User authentication and custom Traccar API integration for device registration.

---

## Screenshots
https://drive.google.com/drive/folders/1UGa6iQwdZlrYnTRAOC26OL7KJ6hAK6tV?usp=sharing 

