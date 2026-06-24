from django.db import migrations


TABLAS = [
    'usuario', 'estudiante', 'administrador',
    'grupo', 'grupo_integrante',
    'propuesta', 'propuesta_version', 'proyecto',
]


def build_sql(reverse=False):
    lines = []
    for tabla in TABLAS:
        if not reverse:
            lines.append(f"ALTER TABLE {tabla} ALTER COLUMN created_at SET DEFAULT now();")
            lines.append(f"ALTER TABLE {tabla} ALTER COLUMN updated_at SET DEFAULT now();")
        else:
            lines.append(f"ALTER TABLE {tabla} ALTER COLUMN created_at DROP DEFAULT;")
            lines.append(f"ALTER TABLE {tabla} ALTER COLUMN updated_at DROP DEFAULT;")
    return "\n".join(lines)


class Migration(migrations.Migration):

    dependencies = [
        ('proyectos', '0001_initial'),
    ]

    operations = [
        migrations.RunSQL(
            sql=build_sql(reverse=False),
            reverse_sql=build_sql(reverse=True),
        ),
    ]
