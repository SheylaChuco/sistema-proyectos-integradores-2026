import os
from pathlib import Path
from dotenv import load_dotenv

# Cargar .env antes de importar base para que os.getenv() lea los valores reales
load_dotenv(Path(__file__).resolve().parents[4] / '.env')

from .base import *  # noqa: E402, F401, F403

DEBUG = True

DATABASES['default'].update({
    'HOST': os.getenv('DB_HOST', 'localhost'),
    'PORT': os.getenv('DB_PORT', '5432'),
    'NAME': os.getenv('DB_NAME', 'proyectos_integradores'),
    'USER': os.getenv('DB_USER', 'db_admin'),
    'PASSWORD': os.getenv('DB_PASSWORD', ''),
})
