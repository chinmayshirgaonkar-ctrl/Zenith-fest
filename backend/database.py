import os
import shutil
import sqlite3
from pathlib import Path

# Path to fest.db at project root for SQLite fallback
ROOT_DIR = Path(__file__).resolve().parent.parent
ORIGINAL_DB_PATH = ROOT_DIR / "fest.db"

# Check for PostgreSQL connection string in environment variables
POSTGRES_URL = (
    os.environ.get("POSTGRES_URL")
    or os.environ.get("DATABASE_URL")
    or os.environ.get("POSTGRES_PRISMA_URL")
    or os.environ.get("POSTGRES_URL_NON_POOLING")
)

def is_postgres() -> bool:
    """Returns True if a PostgreSQL connection string is configured in environment variables."""
    val = (
        os.environ.get("POSTGRES_URL")
        or os.environ.get("DATABASE_URL")
        or os.environ.get("POSTGRES_PRISMA_URL")
        or os.environ.get("POSTGRES_URL_NON_POOLING")
    )
    return bool(val and val.strip())

def get_postgres_url() -> str:
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

# If using SQLite on Vercel serverless without PostgreSQL configured, use /tmp
if os.environ.get("VERCEL") == "1" and not is_postgres():
    DB_PATH = Path("/tmp/fest.db")
else:
    DB_PATH = ORIGINAL_DB_PATH

def ensure_sqlite_db():
    """Ensures SQLite database exists in /tmp when running on Vercel without PostgreSQL."""
    if os.environ.get("VERCEL") == "1" and not is_postgres():
        if not DB_PATH.exists() and ORIGINAL_DB_PATH.exists():
            try:
                shutil.copyfile(str(ORIGINAL_DB_PATH), str(DB_PATH))
            except Exception as e:
                print(f"Notice: failed to copy SQLite DB to /tmp: {e}")


class PostgresCursorWrapper:
    """Wraps psycopg2 cursor to provide a unified API matching sqlite3.Row and lastrowid."""
    def __init__(self, raw_cursor):
        self.cursor = raw_cursor
        self._lastrowid = None

    def execute(self, query: str, params=None):
        # Convert SQLite style ? to PostgreSQL %s
        clean_query = query.replace("?", "%s")
        is_insert = "INSERT INTO" in clean_query.upper()
        if is_insert and "RETURNING" not in clean_query.upper():
            clean_query = clean_query.strip().rstrip(";") + " RETURNING id"

        if params is not None:
            if isinstance(params, tuple):
                p = params
            elif isinstance(params, list):
                p = tuple(params)
            else:
                p = (params,)
            self.cursor.execute(clean_query, p)
        else:
            self.cursor.execute(clean_query)

        if is_insert:
            try:
                row = self.cursor.fetchone()
                if row:
                    if isinstance(row, dict) and "id" in row:
                        self._lastrowid = row["id"]
                    elif hasattr(row, "__getitem__"):
                        self._lastrowid = row[0]
            except Exception:
                pass
        return self

    @property
    def lastrowid(self):
        return self._lastrowid

    @property
    def rowcount(self):
        return self.cursor.rowcount

    def __iter__(self):
        return iter(self.cursor)

    def fetchone(self):
        return self.cursor.fetchone()

    def fetchall(self):
        return self.cursor.fetchall()

    def close(self):
        self.cursor.close()

    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        self.close()


class PostgresConnectionWrapper:
    """Wraps psycopg2 connection with auto-cursor and transaction helpers."""
    def __init__(self, raw_conn):
        self.conn = raw_conn

    def cursor(self):
        return PostgresCursorWrapper(self.conn.cursor())

    def execute(self, query: str, params=None):
        cur = self.cursor()
        cur.execute(query, params)
        return cur

    def commit(self):
        self.conn.commit()

    def rollback(self):
        self.conn.rollback()

    def close(self):
        self.conn.close()

    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        if exc_type:
            self.rollback()
        else:
            self.commit()


def get_db_connection():
    """Returns a unified database connection for either PostgreSQL or SQLite."""
    if is_postgres():
        import psycopg2
        import psycopg2.extras
        url = get_postgres_url()
        raw_conn = psycopg2.connect(url, cursor_factory=psycopg2.extras.RealDictCursor)
        return PostgresConnectionWrapper(raw_conn)
    else:
        ensure_sqlite_db()
        conn = sqlite3.connect(str(DB_PATH))
        conn.row_factory = sqlite3.Row
        conn.execute("PRAGMA foreign_keys = ON")
        return conn


def init_db():
    """Initializes the database schema if tables do not exist."""
    conn = get_db_connection()
    cursor = conn.cursor()

    if is_postgres():
        cursor.execute("""
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
        cursor.execute("""
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
    else:
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
        );
        """)
        cursor.execute("""
        CREATE TABLE IF NOT EXISTS registrations (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            event_id INTEGER NOT NULL REFERENCES events(id) ON DELETE CASCADE,
            student_name TEXT NOT NULL,
            roll_number TEXT NOT NULL,
            email TEXT NOT NULL,
            phone TEXT,
            registered_at TEXT NOT NULL,
            UNIQUE(event_id, email)
        );
        """)

    conn.commit()
    conn.close()


if __name__ == "__main__":
    init_db()
    db_type = "PostgreSQL" if is_postgres() else f"SQLite ({DB_PATH})"
    print(f"Database initialized successfully using {db_type}.")
