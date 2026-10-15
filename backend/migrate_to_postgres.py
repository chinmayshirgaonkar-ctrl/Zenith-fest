"""
Zenith '26 College Fest - SQLite to PostgreSQL Data Migration Tool

Usage:
    python -m backend.migrate_to_postgres [POSTGRES_URL]
    
If POSTGRES_URL is omitted as an argument, it reads from the POSTGRES_URL or
DATABASE_URL environment variables.
"""

import os
import sys
import sqlite3
from pathlib import Path
import psycopg2
import psycopg2.extras

ROOT_DIR = Path(__file__).resolve().parent.parent
SQLITE_DB_PATH = ROOT_DIR / "fest.db"


def get_target_postgres_url() -> str:
    # 1. From CLI argument
    if len(sys.argv) > 1 and sys.argv[1].strip():
        url = sys.argv[1].strip()
    else:
        # 2. From environment variable
        url = (
            os.environ.get("POSTGRES_URL")
            or os.environ.get("DATABASE_URL")
            or os.environ.get("POSTGRES_PRISMA_URL")
            or os.environ.get("POSTGRES_URL_NON_POOLING")
            or ""
        ).strip()

    if url.startswith("postgres://"):
        url = "postgresql://" + url[len("postgres://"):]
    return url


def migrate():
    postgres_url = get_target_postgres_url()

    if not postgres_url:
        print("ERROR: No PostgreSQL connection URL provided.")
        print("Please provide it as an argument or set POSTGRES_URL environment variable:")
        print("    python -m backend.migrate_to_postgres \"postgresql://user:password@host/db\"")
        sys.exit(1)

    if not SQLITE_DB_PATH.exists():
        print(f"ERROR: SQLite database file not found at {SQLITE_DB_PATH}")
        sys.exit(1)

    print(f"Connecting to SQLite: {SQLITE_DB_PATH}")
    sqlite_conn = sqlite3.connect(str(SQLITE_DB_PATH))
    sqlite_conn.row_factory = sqlite3.Row
    sqlite_cursor = sqlite_conn.cursor()

    # Mask credentials for display
    display_url = postgres_url.split("@")[-1] if "@" in postgres_url else postgres_url
    print(f"Connecting to PostgreSQL: ...@{display_url}")
    try:
        pg_conn = psycopg2.connect(postgres_url, cursor_factory=psycopg2.extras.RealDictCursor)
        pg_cursor = pg_conn.cursor()
    except Exception as e:
        print(f"ERROR: Failed to connect to PostgreSQL: {e}")
        sqlite_conn.close()
        sys.exit(1)

    print("Creating PostgreSQL tables if they don't already exist...")
    pg_cursor.execute("""
    CREATE TABLE IF NOT EXISTS events (
        id SERIAL PRIMARY KEY,
        title TEXT NOT NULL,
        category TEXT NOT NULL,
        description TEXT,
        event_date TEXT NOT NULL,
        event_time TEXT NOT NULL,
        venue TEXT NOT NULL,
        capacity INTEGER NOT NULL,
        fee INTEGER NOT NULL DEFAULT 0,
        icon TEXT
    );
    """)

    pg_cursor.execute("""
    CREATE TABLE IF NOT EXISTS registrations (
        id SERIAL PRIMARY KEY,
        event_id INTEGER NOT NULL REFERENCES events(id) ON DELETE CASCADE,
        student_name TEXT NOT NULL,
        roll_number TEXT NOT NULL,
        email TEXT NOT NULL,
        phone TEXT,
        registered_at TEXT NOT NULL,
        UNIQUE(event_id, email)
    );
    """)
    pg_conn.commit()

    # 1. Migrate Events
    print("\n[1/2] Migrating Events...")
    sqlite_cursor.execute("SELECT id, title, category, description, event_date, event_time, venue, capacity, fee, icon FROM events ORDER BY id")
    events = sqlite_cursor.fetchall()
    print(f"Found {len(events)} events in SQLite.")

    events_migrated = 0
    for ev in events:
        pg_cursor.execute("""
            INSERT INTO events (id, title, category, description, event_date, event_time, venue, capacity, fee, icon)
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
            ON CONFLICT (id) DO UPDATE SET
                title = EXCLUDED.title,
                category = EXCLUDED.category,
                description = EXCLUDED.description,
                event_date = EXCLUDED.event_date,
                event_time = EXCLUDED.event_time,
                venue = EXCLUDED.venue,
                capacity = EXCLUDED.capacity,
                fee = EXCLUDED.fee,
                icon = EXCLUDED.icon;
        """, (
            ev["id"], ev["title"], ev["category"], ev["description"],
            ev["event_date"], ev["event_time"], ev["venue"],
            ev["capacity"], ev["fee"], ev["icon"]
        ))
        events_migrated += 1

    # Sync events sequence
    pg_cursor.execute("""
        SELECT setval(
            pg_get_serial_sequence('events', 'id'),
            COALESCE((SELECT MAX(id) FROM events), 1)
        );
    """)
    pg_conn.commit()
    print(f"Migrated/synced {events_migrated} events into PostgreSQL.")

    # 2. Migrate Registrations
    print("\n[2/2] Migrating Registrations...")
    sqlite_cursor.execute("SELECT id, event_id, student_name, roll_number, email, phone, registered_at FROM registrations ORDER BY id")
    registrations = sqlite_cursor.fetchall()
    print(f"Found {len(registrations)} registrations in SQLite.")

    regs_migrated = 0
    for reg in registrations:
        pg_cursor.execute("""
            INSERT INTO registrations (id, event_id, student_name, roll_number, email, phone, registered_at)
            VALUES (%s, %s, %s, %s, %s, %s, %s)
            ON CONFLICT (id) DO UPDATE SET
                student_name = EXCLUDED.student_name,
                roll_number = EXCLUDED.roll_number,
                phone = EXCLUDED.phone,
                registered_at = EXCLUDED.registered_at;
        """, (
            reg["id"], reg["event_id"], reg["student_name"],
            reg["roll_number"], reg["email"], reg["phone"], reg["registered_at"]
        ))
        regs_migrated += 1

    # Sync registrations sequence
    pg_cursor.execute("""
        SELECT setval(
            pg_get_serial_sequence('registrations', 'id'),
            COALESCE((SELECT MAX(id) FROM registrations), 1)
        );
    """)
    pg_conn.commit()
    print(f"Migrated/synced {regs_migrated} registrations into PostgreSQL.")

    sqlite_conn.close()
    pg_conn.close()

    print("\n==========================================")
    print(" MIGRATION COMPLETED SUCCESSFULLY! ")
    print(f" Events transferred: {events_migrated}")
    print(f" Registrations transferred: {regs_migrated}")
    print(" Sequences synchronized for auto-increment.")
    print("==========================================\n")


if __name__ == "__main__":
    migrate()
