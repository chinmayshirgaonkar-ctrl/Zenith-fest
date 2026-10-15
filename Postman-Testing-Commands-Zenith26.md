# Postman Testing Guide: Zenith '26 College Fest Management System

This guide outlines all **18 API testing requests** to verify the server-side validation, HTTP status codes, data integrity, and API contract conformity for Zenith '26.

---

## 🛠️ Postman Desktop Setup

1. **Launch Postman** (free; no account sign-in required).
2. Click **Import** in the top-left corner.
3. Select `zenith26-api.postman_collection.json` located at the root of `zenith26-fest-system/`.
4. Ensure the backend server is running:
   ```bash
   .\venv\Scripts\uvicorn backend.main:app --reload --port 5000
   ```
5. Confirm base URL is set to `http://localhost:5000`.

---

## 📋 Comprehensive 18-Test Checklist

### 1. `GET /api/events` (All Events)
- **Method:** `GET`
- **URL:** `http://localhost:5000/api/events`
- **Expected Status:** `200 OK`
- **Verification:** Returns array of 14 seed events. Each event contains `available_seats` computed live.

---

### 2. `GET /api/events?category=Technical`
- **Method:** `GET`
- **URL:** `http://localhost:5000/api/events?category=Technical`
- **Expected Status:** `200 OK`
- **Verification:** Returns only the 4 technical events (Code Sprint 2.0, Robo Race, AI Pitch Fest, Web Design Rumble).

---

### 3. `GET /api/events?category=Cultural`
- **Method:** `GET`
- **URL:** `http://localhost:5000/api/events?category=Cultural`
- **Expected Status:** `200 OK`
- **Verification:** Returns only the 5 cultural events (Battle of Bands, Dance Fiesta, Nukkad Natak, Fashion Fusion, Open Mic Night).

---

### 4. `GET /api/events?search=code`
- **Method:** `GET`
- **URL:** `http://localhost:5000/api/events?search=code`
- **Expected Status:** `200 OK`
- **Verification:** Case-insensitive match returns Code Sprint 2.0 and Intro to Vibe Coding.

---

### 5. `GET /api/events?search=nonexistent`
- **Method:** `GET`
- **URL:** `http://localhost:5000/api/events?search=nonexistent`
- **Expected Status:** `200 OK`
- **Verification:** Returns empty array `[]`.

---

### 6. `GET /api/events/1` (Valid Event Detail)
- **Method:** `GET`
- **URL:** `http://localhost:5000/api/events/1`
- **Expected Status:** `200 OK`
- **Verification:** Returns single event JSON object with `id: 1`, `title: "Code Sprint 2.0"`, `capacity: 100`.

---

### 7. `GET /api/events/999` (Non-existent Event Detail)
- **Method:** `GET`
- **URL:** `http://localhost:5000/api/events/999`
- **Expected Status:** `404 Not Found`
- **Body Response:**
  ```json
  {
    "detail": "Event with id 999 not found"
  }
  ```

---

### 8. `POST /api/events/1/register` (Valid Registration)
- **Method:** `POST`
- **URL:** `http://localhost:5000/api/events/1/register`
- **Headers:** `Content-Type: application/json`
- **Body (raw JSON):**
  ```json
  {
    "student_name": "Rohan Verma",
    "roll_number": "23CS101",
    "email": "rohan.verma@example.edu",
    "phone": "+91 9876543210"
  }
  ```
- **Expected Status:** `201 Created`
- **Verification:** Returns `RegistrationOut` with `id`, `event_id: 1`, `student_name`, and ISO timestamp `registered_at`.

---

### 9. `POST /api/events/1/register` (Duplicate Registration)
- **Method:** `POST`
- **URL:** `http://localhost:5000/api/events/1/register`
- **Headers:** `Content-Type: application/json`
- **Body (Same email as test 8):**
  ```json
  {
    "student_name": "Rohan Verma",
    "roll_number": "23CS101",
    "email": "rohan.verma@example.edu",
    "phone": "+91 9876543210"
  }
  ```
- **Expected Status:** `409 Conflict`
- **Body Response:**
  ```json
  {
    "detail": "Already registered"
  }
  ```

---

### 10. `POST /api/events/1/register` (Missing Student Name)
- **Method:** `POST`
- **URL:** `http://localhost:5000/api/events/1/register`
- **Headers:** `Content-Type: application/json`
- **Body:**
  ```json
  {
    "student_name": "",
    "roll_number": "23CS102",
    "email": "student2@example.edu"
  }
  ```
- **Expected Status:** `400 Bad Request`
- **Verification:** Server refuses empty name.

---

### 11. `POST /api/events/1/register` (Missing Roll Number)
- **Method:** `POST`
- **URL:** `http://localhost:5000/api/events/1/register`
- **Headers:** `Content-Type: application/json`
- **Body:**
  ```json
  {
    "student_name": "Aarav Gupta",
    "roll_number": "",
    "email": "aarav@example.edu"
  }
  ```
- **Expected Status:** `400 Bad Request`

---

### 12. `POST /api/events/1/register` (Missing Email)
- **Method:** `POST`
- **URL:** `http://localhost:5000/api/events/1/register`
- **Headers:** `Content-Type: application/json`
- **Body:**
  ```json
  {
    "student_name": "Aarav Gupta",
    "roll_number": "23CS103",
    "email": ""
  }
  ```
- **Expected Status:** `400 Bad Request`

---

### 13. `POST /api/events/1/register` (Invalid Email Format)
- **Method:** `POST`
- **URL:** `http://localhost:5000/api/events/1/register`
- **Headers:** `Content-Type: application/json`
- **Body:**
  ```json
  {
    "student_name": "Aarav Gupta",
    "roll_number": "23CS103",
    "email": "invalidemailaddress.com"
  }
  ```
- **Expected Status:** `400 Bad Request`
- **Body Response:**
  ```json
  {
    "detail": "Invalid email: Email must contain '@'"
  }
  ```

---

### 14. `POST /api/events/999/register` (Register for Non-existent Event)
- **Method:** `POST`
- **URL:** `http://localhost:5000/api/events/999/register`
- **Headers:** `Content-Type: application/json`
- **Body:**
  ```json
  {
    "student_name": "Aarav Gupta",
    "roll_number": "23CS103",
    "email": "aarav@example.edu"
  }
  ```
- **Expected Status:** `404 Not Found`

---

### 15. `GET /api/registrations` (All Registrations)
- **Method:** `GET`
- **URL:** `http://localhost:5000/api/registrations`
- **Expected Status:** `200 OK`
- **Verification:** Returns all registration rows, each including `event_title` joined directly from the events table.

---

### 16. `GET /api/registrations?event_id=1` (Filter by Event)
- **Method:** `GET`
- **URL:** `http://localhost:5000/api/registrations?event_id=1`
- **Expected Status:** `200 OK`
- **Verification:** Returns only registrations where `event_id == 1`.

---

### 17. `GET /api/stats` (Live Operations Stats)
- **Method:** `GET`
- **URL:** `http://localhost:5000/api/stats`
- **Expected Status:** `200 OK`
- **Verification:**
  ```json
  {
    "total_events": 14,
    "total_registrations": 1,
    "total_capacity": 1427,
    "overall_utilization_pct": 0.1,
    "upcoming_events_count": 14,
    "events_by_category": { ... }
  }
  ```

---

### 18. Stretch Goals: Admin Event Creation & Cancellation
#### 18a. `POST /api/events` (Add Event)
- **Method:** `POST`
- **URL:** `http://localhost:5000/api/events`
- **Body:**
  ```json
  {
    "title": "Quantum AI Hackathon",
    "category": "Technical",
    "description": "24-hour hackathon building quantum algorithms.",
    "event_date": "2026-11-15",
    "event_time": "02:00 PM",
    "venue": "Lab 5",
    "capacity": 30,
    "fee": 0,
    "icon": "⚛️"
  }
  ```
- **Expected Status:** `201 Created`

#### 18b. `DELETE /api/registrations/1` (Cancel Registration)
- **Method:** `DELETE`
- **URL:** `http://localhost:5000/api/registrations/1`
- **Expected Status:** `200 OK`
- **Verification:** Frees up the seat immediately.
