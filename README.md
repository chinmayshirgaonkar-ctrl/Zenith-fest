# Zenith '26 College Fest Management System (Android + FastAPI)

A full-stack, native Android college fest management system built with **Kotlin (Native Android)**, **FastAPI (Python backend)**, **SQLite (relational database)**, and **Postman (API contract testing)**.

---

## 🏗️ Architecture & Technology Stack

The project follows a clean **client-server 3-layer architecture**:

- **Mobile Client Layer (`android/`):**
  - **Platform:** Native Android with Kotlin
  - **UI Toolkit:** Jetpack Compose & Material 3 (100% native declarative Kotlin UI)
  - **Networking:** Retrofit 2 + OkHttp + Coroutines
  - **Theme:** Zenith dark theme (`#0F172A` Slate-900 background, `#7C3AED` Violet accents, responsive cards, category badges)
- **Application Server Layer (`backend/`):**
  - **Framework:** FastAPI + Uvicorn (ASGI)
  - **Validation & Models:** Pydantic v2 (server-side data validation & type enforcement)
  - **Lifecycle:** Auto-initializes schema and seeds events on startup
- **Data Layer (`backend/database.py`, `fest.db`):**
  - **Engine:** SQLite with Foreign Keys enabled
  - **Tables:** `events` (capacity, fee, venue, schedule), `registrations` (unique constraint on `event_id, email`)
  - **Seed File:** `data/events_seed.xlsx` (14 initial fest events loaded via `openpyxl`)
- **API Testing Layer:**
  - `zenith26-api.postman_collection.json` (18 API tests covering success and failure contracts)

---

## 📁 Project Directory Structure

```text
zenith26-fest-system/
├── backend/
│   ├── main.py                     # FastAPI REST API endpoints & business validation
│   ├── database.py                 # SQLite connection and schema setup
│   ├── models.py                   # Pydantic request/response schemas
│   └── seed_data.py                # Excel seed reader (data/events_seed.xlsx)
├── android/
│   ├── settings.gradle.kts         # Gradle project settings
│   ├── build.gradle.kts            # Root build script
│   ├── gradle/wrapper/             # Gradle wrapper properties
│   └── app/
│       ├── build.gradle.kts        # App module dependencies (Compose, Retrofit, Coroutines)
│       └── src/main/
│           ├── AndroidManifest.xml # Permissions (INTERNET) & cleartext config
│           ├── java/com/zenith26/fest/
│           │   ├── MainActivity.kt # Root Activity & screen navigation
│           │   ├── network/        # ApiConfig, ApiClient, ZenithApiService, NetworkModels
│           │   ├── ui/
│           │   │   ├── theme/      # Dark Color palette, Typography, ZenithTheme
│           │   │   ├── components/ # TopNavbar, HeroSection, EventCard, CategoryFilterBar, EventDetailModal, StatCard
│           │   │   └── screens/    # StudentPortalScreen, AdminDashboardScreen, ServerSettingsDialog
│           └── res/                # XML colors, strings, themes, network_security_config.xml
├── data/
│   └── events_seed.xlsx            # Companion 14-event seed spreadsheet
├── requirements.txt                # Python dependencies
├── zenith26-api.postman_collection.json # Ready-to-import Postman collection
├── Postman-Testing-Commands-Zenith26.md # Testing guide with all 18 test cases
└── README.md                       # Project documentation & run guide
```

---

## 🚀 How to Run

### 1. Start the FastAPI Backend

Open a PowerShell or Terminal window at the `zenith26-fest-system/` directory:

```powershell
# 1. Activate the virtual environment
.\venv\Scripts\Activate.ps1

# 2. Run the FastAPI development server with auto-reload
python -m uvicorn backend.main:app --reload --host 0.0.0.0 --port 5000
```

- **Interactive API Documentation (Swagger UI):** Open [http://localhost:5000/docs](http://localhost:5000/docs) in your browser.
- **Alternative Docs (ReDoc):** Open [http://localhost:5000/redoc](http://localhost:5000/redoc).

---

### 2. Run the Android App

1. Launch **Android Studio**.
2. Select **Open** and choose the `zenith26-fest-system/android/` folder.
3. Allow Gradle to sync the dependencies.
4. Launch an Android Virtual Device (AVD Emulator) or connect a physical device via USB debugging.
5. Click **Run 'app'** (`Shift + F10`).

> **Connecting from Android to Backend:**
> - **In the Android Emulator:** The app connects to `http://10.0.2.2:5000/` by default (the standard Android Emulator alias for your host machine's localhost).
> - **On a Physical Phone:** Tap the **⚙️ Settings** icon in the app's top bar and set the Base URL to your computer's local Wi-Fi IP address (e.g., `http://192.168.1.15:5000/`).

---

## 📡 REST API Endpoints Reference

| Method | Endpoint | Description | Status Codes |
|---|---|---|---|
| `GET` | `/api/events` | List all events (supports `?category=` and `?search=`). Computes `available_seats` live. | `200 OK` |
| `GET` | `/api/events/{event_id}` | Retrieve details for a single event. | `200 OK`, `404 Not Found` |
| `POST` | `/api/events/{event_id}/register` | Register a student for an event. Performs server-side validations: required fields, email format, duplicate email check, capacity limit check. | `201 Created`, `400 Bad Request`, `404 Not Found`, `409 Conflict` |
| `GET` | `/api/registrations` | List student registrations (supports `?event_id=` filter). Includes `event_title`. | `200 OK` |
| `GET` | `/api/stats` | Live computed admin statistics: total events, total registrations, overall capacity utilization %, upcoming events count, category breakdown. | `200 OK` |
| `POST` | `/api/events` *(Stretch Goal)* | Admin endpoint to create a new event with validation. | `201 Created`, `400 Bad Request` |
| `DELETE` | `/api/registrations/{id}` *(Stretch Goal)* | Admin endpoint to cancel a registration and restore seat count. | `200 OK`, `404 Not Found` |

---

## 🧪 Testing with Postman

1. Open the Postman Desktop App.
2. Click **Import** in the upper left.
3. Select `zenith26-api.postman_collection.json`.
4. Ensure the backend server is running on `http://localhost:5000`.
5. Execute the test collection according to `Postman-Testing-Commands-Zenith26.md`.
