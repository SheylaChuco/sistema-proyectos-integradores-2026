"""
TT-03: Crea el usuario administrador inicial desde variables de entorno.
Idempotente: si el correo ya existe, no hace nada.
"""
import os

from django.contrib.auth.hashers import make_password
from django.core.management.base import BaseCommand

from apps.proyectos.models import Administrador, Usuario


class Command(BaseCommand):
    help = 'Crea el usuario administrador inicial desde variables de entorno'

    def handle(self, *args, **options):
        email = os.getenv('ADMIN_EMAIL', 'encargado@tecsup.edu.pe')
        password = os.getenv('ADMIN_PASSWORD', '')

        if not password:
            self.stderr.write('ADMIN_PASSWORD no esta configurado en .env')
            return

        if Usuario.objects.filter(correo=email).exists():
            self.stdout.write(f'Admin {email} ya existe, omitiendo.')
            return

        usuario = Usuario.objects.create(
            nombre='Administrador TECSUP',
            correo=email,
            password=make_password(password),
            rol=Usuario.Rol.ADMIN,
            activo=True,
        )
        Administrador.objects.create(usuario=usuario)
        self.stdout.write(self.style.SUCCESS(f'Admin creado: {email}'))
