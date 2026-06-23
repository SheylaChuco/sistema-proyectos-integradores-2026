"""
TT-02: Carga de data historica limpia a la BD.
Idempotente: verificar por ciclo+codigo_seccion antes de insertar.
"""
import csv
from pathlib import Path

from django.core.management.base import BaseCommand

from apps.proyectos.models import Proyecto

CSV_PATH = Path(__file__).resolve().parents[6] / 'docs' / 'data' / 'proyectos_historicos_limpio.csv'


class Command(BaseCommand):
    help = 'Importa proyectos historicos desde el CSV limpio generado en TT-01'

    def handle(self, *args, **options):
        if not CSV_PATH.exists():
            self.stderr.write(f'Archivo no encontrado: {CSV_PATH}')
            return

        importados = 0
        omitidos = 0

        with open(CSV_PATH, encoding='utf-8-sig', newline='') as f:
            reader = csv.DictReader(f)
            for row in reader:
                ciclo = row['ciclo'].strip()
                codigo_seccion = row['codigo_seccion'].strip()
                nombre = row['nombre'].strip()
                descripcion = row['descripcion'].strip()

                if not nombre or not descripcion:
                    omitidos += 1
                    continue

                existe = Proyecto.objects.filter(
                    ciclo=ciclo,
                    codigo_seccion=codigo_seccion,
                    origen=Proyecto.Origen.HISTORICO,
                ).exists()

                if existe:
                    omitidos += 1
                    continue

                Proyecto.objects.create(
                    origen=Proyecto.Origen.HISTORICO,
                    nombre=nombre,
                    descripcion=descripcion,
                    ciclo=ciclo,
                    codigo_seccion=codigo_seccion,
                    grupo=None,
                    estado=None,
                    comentario_evaluacion=None,
                    url=None,
                )
                importados += 1

        self.stdout.write(self.style.SUCCESS(
            f'Importacion completada: {importados} importados, {omitidos} omitidos.'
        ))
