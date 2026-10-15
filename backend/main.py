import os
from datetime import datetime, date
from pathlib import Path
from typing import List, Optional, Dict
from contextlib import asynccontextmanager

from fastapi import FastAPI, HTTPException, status, Query
from fastapi.middleware.cors import CORSMiddleware

from backend.database import get_db_connection, init_db
from backend.seed_data import seed_events
from backend.models import (
    EventOut,
    EventDetail,
    EventCreate,
    RegistrationIn,
    RegistrationOut,
    RegistrationDetailOut,
    StatsOut,
)

BASE_DIR = Path(__file__).resolve().parent.parent

@asynccontextmanager
async def lifespan(app: FastAPI):
    # Initialize DB and seed events on startup
    init_db()
    try:
        seed_events()
    except Exception as e:
        print(f"Notice on seed check: {e}")
    yield

app = FastAPI(
    title="Zenith '26 College Fest Management System API",
    version="1.0.0",
    description="REST API for Zenith '26 College Fest Android and Web portals.",
    lifespan=lifespan,
)

# Enable CORS for local development
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# ==========================================
# API Endpoints
# ==========================================

@app.get("/", summary="Root health and index")
def root():
    return {
        "status": "online",
        "service": "Zenith '26 College Fest Management System API",
        "docs": "/docs",
        "endpoints": {
            "events": "/api/events",
            "stats": "/api/stats",
            "registrations": "/api/registrations"
        }
    }


@app.get("/api/events", response_model=List[EventOut], summary="List all events with computed available seats")
def get_events(
    category: Optional[str] = Query(None, description="Filter by event category"),
    search: Optional[str] = Query(None, description="Search in event title (case-insensitive)")
):
    conn = get_db_connection()
    cursor = conn.cursor()

    query = """
        SELECT e.id, e.title, e.category, e.description, e.event_date, e.event_time,
               e.venue, e.capacity, e.fee, e.icon,
               (e.capacity - COUNT(r.id)) AS available_seats
        FROM events e
        LEFT JOIN registrations r ON e.id = r.event_id
        WHERE 1=1
    """
    params = []

    if category and category.strip() and category.strip().lower() != "all":
        query += " AND LOWER(e.category) = LOWER(?)"
        params.append(category.strip())

    if search and search.strip():
        query += " AND LOWER(e.title) LIKE ?"
        params.append(f"%{search.strip().lower()}%")

    query += " GROUP BY e.id ORDER BY e.event_date, e.event_time"

    rows = cursor.execute(query, params).fetchall()
    conn.close()

    events = []
    for r in rows:
        events.append(EventOut(
            id=r["id"],
            title=r["title"],
            category=r["category"],
            description=r["description"],
            event_date=r["event_date"],
            event_time=r["event_time"],
            venue=r["venue"],
            capacity=r["capacity"],
            fee=r["fee"],
            icon=r["icon"],
            available_seats=max(0, r["available_seats"]),
        ))
    return events


@app.get("/api/events/{event_id}", response_model=EventDetail, summary="Get one event by ID")
def get_event(event_id: int):
    conn = get_db_connection()
    cursor = conn.cursor()

    row = cursor.execute("""
        SELECT e.id, e.title, e.category, e.description, e.event_date, e.event_time,
               e.venue, e.capacity, e.fee, e.icon,
               (e.capacity - COUNT(r.id)) AS available_seats
        FROM events e
        LEFT JOIN registrations r ON e.id = r.event_id
        WHERE e.id = ?
        GROUP BY e.id
    """, (event_id,)).fetchone()
    conn.close()

    if not row:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Event with ID {event_id} not found."
        )

    return EventDetail(
        id=row["id"],
        title=row["title"],
        category=row["category"],
        description=row["description"],
        event_date=row["event_date"],
        event_time=row["event_time"],
        venue=row["venue"],
        capacity=row["capacity"],
        fee=row["fee"],
        icon=row["icon"],
        available_seats=max(0, row["available_seats"]),
    )


@app.post("/api/events/{event_id}/register", response_model=RegistrationOut, status_code=status.HTTP_201_CREATED, summary="Register a student for an event")
def register_for_event(event_id: int, reg: RegistrationIn):
    # Server-side validation 1: required fields
    if not reg.student_name or not reg.student_name.strip():
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Student name is required.")
    if not reg.roll_number or not reg.roll_number.strip():
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Roll number is required.")
    if not reg.email or not reg.email.strip():
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Email is required.")
    
    # Server-side validation 2: valid email format
    cleaned_email = reg.email.strip().lower()
    if "@" not in cleaned_email or len(cleaned_email) < 3:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Invalid email format: must contain '@'.")

    conn = get_db_connection()
    cursor = conn.cursor()

    # Server-side validation 3: event must exist
    event = cursor.execute("SELECT id, title, capacity FROM events WHERE id = ?", (event_id,)).fetchone()
    if not event:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Event with ID {event_id} not found."
        )

    # Server-side validation 4: event must not be at capacity
    reg_count = cursor.execute("SELECT COUNT(*) AS cnt FROM registrations WHERE event_id = ?", (event_id,)).fetchone()["cnt"]
    if reg_count >= event["capacity"]:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="Event is full."
        )

    # Server-side validation 5: student must not already be registered for this event
    existing = cursor.execute(
        "SELECT id FROM registrations WHERE event_id = ? AND LOWER(email) = ?",
        (event_id, cleaned_email)
    ).fetchone()
    if existing:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="Already registered for this event."
        )

    # Insert registration
    now_iso = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    cursor.execute("""
        INSERT INTO registrations (event_id, student_name, roll_number, email, phone, registered_at)
        VALUES (?, ?, ?, ?, ?, ?)
    """, (
        event_id,
        reg.student_name.strip(),
        reg.roll_number.strip().upper(),
        cleaned_email,
        reg.phone.strip() if reg.phone else None,
        now_iso,
    ))
    new_id = cursor.lastrowid
    conn.commit()
    conn.close()

    return RegistrationOut(
        id=new_id,
        event_id=event_id,
        student_name=reg.student_name.strip(),
        registered_at=now_iso,
    )


@app.get("/api/registrations", response_model=List[RegistrationDetailOut], summary="List registrations (optionally filtered by event_id)")
def get_registrations(event_id: Optional[int] = Query(None, description="Filter registrations by event_id")):
    conn = get_db_connection()
    cursor = conn.cursor()

    query = """
        SELECT r.id, r.event_id, r.student_name, r.roll_number, r.email, r.phone, r.registered_at,
               e.title AS event_title
        FROM registrations r
        JOIN events e ON r.event_id = e.id
        WHERE 1=1
    """
    params = []

    if event_id is not None:
        query += " AND r.event_id = ?"
        params.append(event_id)

    query += " ORDER BY r.registered_at DESC, r.id DESC"

    rows = cursor.execute(query, params).fetchall()
    conn.close()

    return [
        RegistrationDetailOut(
            id=r["id"],
            event_id=r["event_id"],
            student_name=r["student_name"],
            roll_number=r["roll_number"],
            email=r["email"],
            phone=r["phone"],
            registered_at=r["registered_at"],
            event_title=r["event_title"],
        )
        for r in rows
    ]


@app.get("/api/stats", response_model=StatsOut, summary="Live computed dashboard statistics")
def get_stats():
    conn = get_db_connection()
    cursor = conn.cursor()

    # Total events
    total_events = cursor.execute("SELECT COUNT(*) AS cnt FROM events").fetchone()["cnt"]

    # Total registrations
    total_registrations = cursor.execute("SELECT COUNT(*) AS cnt FROM registrations").fetchone()["cnt"]

    # Total capacity
    cap_row = cursor.execute("SELECT COALESCE(SUM(capacity), 0) AS total_cap FROM events").fetchone()
    total_capacity = cap_row["total_cap"]

    # Overall utilization percentage
    if total_capacity > 0:
        overall_utilization_pct = round((total_registrations / total_capacity) * 100.0, 1)
    else:
        overall_utilization_pct = 0.0

    # Upcoming events count (event_date today or later)
    today_str = date.today().isoformat()
    upcoming_events_count = cursor.execute(
        "SELECT COUNT(*) AS cnt FROM events WHERE event_date >= ?", (today_str,)
    ).fetchone()["cnt"]

    # Events by category
    cat_rows = cursor.execute("""
        SELECT category, COUNT(*) AS cnt
        FROM events
        GROUP BY category
        ORDER BY category
    """).fetchall()
    events_by_category = {r["category"]: r["cnt"] for r in cat_rows}

    conn.close()

    return StatsOut(
        total_events=total_events,
        total_registrations=total_registrations,
        total_capacity=total_capacity,
        overall_utilization_pct=overall_utilization_pct,
        upcoming_events_count=upcoming_events_count,
        events_by_category=events_by_category,
    )


# ==========================================
# Stretch Goal Endpoints
# ==========================================

@app.post("/api/events", response_model=EventOut, status_code=status.HTTP_201_CREATED, summary="[Admin] Add a new event")
def create_event(event_in: EventCreate):
    if not event_in.title or not event_in.title.strip():
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Title is required.")
    if not event_in.category or not event_in.category.strip():
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Category is required.")
    if event_in.capacity <= 0:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Capacity must be greater than zero.")
    if event_in.fee < 0:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Fee cannot be negative.")

    conn = get_db_connection()
    cursor = conn.cursor()

    cursor.execute("""
        INSERT INTO events (title, category, description, event_date, event_time, venue, capacity, fee, icon)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
    """, (
        event_in.title.strip(),
        event_in.category.strip(),
        event_in.description.strip() if event_in.description else "",
        event_in.event_date.strip(),
        event_in.event_time.strip(),
        event_in.venue.strip(),
        event_in.capacity,
        event_in.fee,
        event_in.icon or "📅",
    ))
    new_id = cursor.lastrowid
    conn.commit()
    conn.close()

    return EventOut(
        id=new_id,
        title=event_in.title.strip(),
        category=event_in.category.strip(),
        description=event_in.description.strip() if event_in.description else "",
        event_date=event_in.event_date.strip(),
        event_time=event_in.event_time.strip(),
        venue=event_in.venue.strip(),
        capacity=event_in.capacity,
        fee=event_in.fee,
        icon=event_in.icon or "📅",
        available_seats=event_in.capacity,
    )


@app.delete("/api/registrations/{registration_id}", status_code=status.HTTP_200_OK, summary="[Admin] Cancel a registration")
def delete_registration(registration_id: int):
    conn = get_db_connection()
    cursor = conn.cursor()

    reg = cursor.execute("SELECT id, event_id, student_name FROM registrations WHERE id = ?", (registration_id,)).fetchone()
    if not reg:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Registration with ID {registration_id} not found."
        )

    cursor.execute("DELETE FROM registrations WHERE id = ?", (registration_id,))
    conn.commit()
    conn.close()

    return {
        "message": f"Registration #{registration_id} for {reg['student_name']} cancelled successfully.",
        "registration_id": registration_id,
        "event_id": reg["event_id"]
    }
