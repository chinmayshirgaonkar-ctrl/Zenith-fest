import os
import shutil
import sqlite3
from pathlib import Path

# Path to fest.db at project root
ROOT_DIR = Path(__file__).resolve().parent.parent
ORIGINAL_DB_PATH = ROOT_DIR / "fest.db"

# On Vercel serverless functions, the root is read-only; use /tmp for writeable SQLite
if os.environ.get("VERCEL") == "1":
    DB_PATH = Path("/tmp/fest.db")
else:
    DB_PATH = ORIGINAL_DB_PATH

def ensure_db():
    """Ensures that the SQLite database exists in /tmp when running on Vercel."""
    if os.environ.get("VERCEL") == "1":
        if not DB_PATH.exists() and ORIGINAL_DB_PATH.exists():
            try:
                shutil.copyfile(str(ORIGINAL_DB_PATH), str(DB_PATH))
            except Exception as e:
                print(f"Notice: failed to copy DB to /tmp: {e}")

def get_db_connection() -> sqlite3.Connection:
    """Returns a SQLite connection with Row factory and foreign keys enabled."""
    ensure_db()
    conn = sqlite3.connect(str(DB_PATH))
    conn.row_factory = sqlite3.Row
    conn.execute("PRAGMA foreign_keys = ON")
    return conn

def init_db():
    """Initializes the database schema if tables do not exist."""
    conn = get_db_connection()
    cursor = conn.cursor()
    
    # 1. events table
    cursor.execute("""
    CREATE TABLE IF NOT EXISTS events (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        title TEXT NOT NULL,
        category TEXT NOT NULL,
        description TEXT,
        event_date TEXT NOT NULL,
        event_time TEXT NOT NULL,
        venue TEXT NOT NULL,
        capacity INTEGER NOT NULL,
        fee INTEGER NOT NULL DEFAULT 0,
        icon TEXT
    )
    """)
    
    # 2. registrations table
    cursor.execute("""
    CREATE TABLE IF NOT EXISTS registrations (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        event_id INTEGER NOT NULL REFERENCES events(id),
        student_name TEXT NOT NULL,
        roll_number TEXT NOT NULL,
        email TEXT NOT NULL,
        phone TEXT,
        registered_at TEXT NOT NULL,
        UNIQUE(event_id, email)
    )
    """)
    
    conn.commit()
    conn.close()

if __name__ == "__main__":
    init_db()
    print("Database initialized successfully at:", DB_PATH)
