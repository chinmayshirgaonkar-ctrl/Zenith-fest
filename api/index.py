import sys
from pathlib import Path

# Ensure the root directory is on the python search path
ROOT_DIR = Path(__file__).resolve().parent.parent
if str(ROOT_DIR) not in sys.path:
    sys.path.insert(0, str(ROOT_DIR))

from backend.main import app

# Expose app for Vercel
__all__ = ["app"]
