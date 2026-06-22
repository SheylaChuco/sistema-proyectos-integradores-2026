"""
TT-01: Limpieza y normalización del CSV histórico de proyectos integradores.
Genera docs/data/proyectos_historicos_limpio.csv con columnas:
    ciclo, codigo_seccion, nombre, descripcion
"""

import pandas as pd
import re
import sys
from pathlib import Path

INPUT_CSV  = Path(__file__).parent / "proyectos_historicos.csv"
OUTPUT_CSV = Path(__file__).parent / "proyectos_historicos_limpio.csv"

# Filas del CSV original a excluir (1-indexadas como en el expediente, se convierten a 0-indexadas)
# Rango 149-174 son duplicados confirmados del bloque "Final"
EXCLUDED_LINE_RANGE = range(149, 175)   # líneas 149..174 inclusive


def normalizar_ciclo(raw: str) -> str:
    """Convierte '2026-1' → '2026-I', '2025-2' → '2025-II', etc."""
    m = re.match(r"(\d{4})-([12])", raw.strip())
    if m:
        year, sem = m.group(1), int(m.group(2))
        return f"{year}-{'I' if sem == 1 else 'II'}"
    return raw.strip()


def es_codigo_seccion(val: str) -> bool:
    """Devuelve True si el valor parece un código de sección (ej. '1A', '2B', '3C')."""
    return bool(re.match(r"^\d+[A-Za-z]$", val.strip()))


def limpiar(val) -> str:
    s = str(val).strip()
    return "" if s in ("nan", "None", "") else s


def main():
    # ── 1. Leer CSV crudo ──────────────────────────────────────────────
    df_raw = pd.read_csv(INPUT_CSV, header=None, dtype=str,
                         keep_default_na=False, encoding="utf-8-sig")

    total_lineas_originales = len(df_raw)

    # ── 2. Detectar posiciones de ciclo (fila índice 1) ───────────────
    ciclo_row = df_raw.iloc[1]
    ciclo_por_col: dict[int, str] = {}
    for col_idx, val in enumerate(ciclo_row):
        val_clean = limpiar(val)
        if val_clean and col_idx > 0:
            normalizado = normalizar_ciclo(val_clean)
            ciclo_por_col[col_idx] = normalizado

    # Para cada ciclo, las columnas "nombre" y "descripción" son col_inicio y col_inicio+1
    ciclos_ordenados = sorted(ciclo_por_col.items())  # [(col, ciclo), ...]

    # ── 3. Iterar filas de datos (desde índice 3 = línea CSV 4) ───────
    registros: list[dict] = []
    seccion_actual: str = ""
    # Contador de proyectos por (ciclo, seccion) para garantizar unicidad
    conteo: dict[tuple, int] = {}

    excluidas_rango  = 0
    excluidas_vacio  = 0
    excluidas_2020I  = 0
    omitidas_sin_sec = 0

    for idx in range(3, len(df_raw)):
        linea_csv = idx + 1   # línea 1-indexada del archivo

        # — Excluir bloque de duplicados (líneas 149-174) —
        if linea_csv in EXCLUDED_LINE_RANGE:
            excluidas_rango += 1
            continue

        row = df_raw.iloc[idx]
        col0 = limpiar(row.iloc[0])

        # — Actualizar sección actual —
        if col0 and col0 != "nan":
            if es_codigo_seccion(col0):
                seccion_actual = col0.upper()
            else:
                # Podría ser un separador o etiqueta; si la fila anterior era de datos,
                # mantenemos la sección. Si todas las columnas de datos están vacías,
                # la fila es ruido y se ignora.
                seccion_actual = ""

        if not seccion_actual:
            omitidas_sin_sec += 1
            continue

        # — Extraer un proyecto por cada ciclo —
        for col_inicio, ciclo in ciclos_ordenados:
            # Excluir 2020-I (sin nombres según el expediente)
            if ciclo == "2020-I":
                excluidas_2020I += 1
                continue

            nombre_col = col_inicio
            desc_col   = col_inicio + 1

            if nombre_col >= len(row):
                continue

            nombre = limpiar(row.iloc[nombre_col])
            desc   = limpiar(row.iloc[desc_col]) if desc_col < len(row) else ""

            # Omitir si nombre o descripción vacíos
            if not nombre or not desc:
                excluidas_vacio += 1
                continue

            # Construir codigo_seccion único: ciclo-seccion[-N]
            clave = (ciclo, seccion_actual)
            conteo[clave] = conteo.get(clave, 0) + 1
            n = conteo[clave]
            codigo_seccion = f"{ciclo}-{seccion_actual}" if n == 1 else f"{ciclo}-{seccion_actual}-{n}"

            registros.append({
                "ciclo":           ciclo,
                "codigo_seccion":  codigo_seccion,
                "nombre":          nombre,
                "descripcion":     desc,
            })

    # ── 4. Verificaciones de integridad ───────────────────────────────
    df_out = pd.DataFrame(registros)

    # Unicidad de codigo_seccion
    duplicados = df_out[df_out.duplicated(subset=["codigo_seccion"], keep=False)]
    if not duplicados.empty:
        print("⚠  Advertencia: existen codigo_seccion duplicados después del proceso:")
        print(duplicados[["ciclo", "codigo_seccion", "nombre"]].to_string())

    # Sin nombres vacíos (garantía final)
    df_out = df_out[df_out["nombre"].str.len() > 0]
    df_out = df_out[df_out["descripcion"].str.len() > 0]

    # ── 5. Guardar CSV limpio ─────────────────────────────────────────
    df_out.to_csv(OUTPUT_CSV, index=False, encoding="utf-8-sig")

    # ── 6. Reporte ────────────────────────────────────────────────────
    print("\n" + "="*60)
    print("  TT-01 - Resumen de limpieza del CSV historico")
    print("="*60)
    print(f"  Lineas totales en el archivo original    : {total_lineas_originales}")
    print(f"  Lineas excluidas (bloque duplicados)     : {excluidas_rango}")
    print(f"  Registros descartados (ciclo 2020-I)     : {excluidas_2020I}")
    print(f"  Registros descartados (nombre/desc vacio): {excluidas_vacio}")
    print(f"  Registros sin seccion (ignorados)        : {omitidas_sin_sec}")
    print(f"  Registros validos en archivo de salida   : {len(df_out)}")
    print(f"\n  Archivo generado: {OUTPUT_CSV}")
    print("="*60 + "\n")

    # Verificar DoD
    assert df_out["nombre"].notna().all(), "DoD FALLO: hay nombres vacíos"
    assert df_out["codigo_seccion"].nunique() == len(df_out), \
        "DoD FALLO: codigo_seccion no es único"
    assert not (df_out["ciclo"] == "2020-I").any(), \
        "DoD FALLO: hay registros del ciclo 2020-I"

    print("  OK DoD verificado: sin 2020-I, sin nombres vacios, codigo_seccion unico.")

    return 0


if __name__ == "__main__":
    sys.exit(main())
