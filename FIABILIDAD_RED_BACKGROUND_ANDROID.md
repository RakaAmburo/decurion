# Ajuste del aguila del icono

## Que se hizo bien

- se dejo de tocar solo `ic_launcher_round.xml` y se ajusto el `foreground` real en `mipmap-*/ic_launcher_foreground.webp`;
- primero se centro por contenido oscuro (aguila negra), no por lienzo;
- despues se aumento tamano de forma uniforme manteniendo centro;
- se valido compilacion despues del cambio.

## Scripts usados (Python)

- `tools/center_eagle_icon.py`: centra el aguila usando mascara de pixeles oscuros (fallback por alpha).
- `tools/scale_eagle_icon.py`: escala el `foreground` (actualmente `1.10x`) manteniendo centro.

## Como repetirlo

```powershell
Set-Location "C:\repos\decurion"
python -m pip install --user Pillow
python -u "C:\repos\decurion\tools\center_eagle_icon.py"
python -u "C:\repos\decurion\tools\scale_eagle_icon.py"
.
\gradlew.bat :app:compileDebugJavaWithJavac --no-daemon
```

## Como ver el resultado en movil

```powershell
Set-Location "C:\repos\decurion"
.
\gradlew.bat :app:installDebug
```

Checklist rapido:

1. quitar y volver a agregar el icono en la pantalla de inicio;
2. si no cambia, desinstalar e instalar la app;
3. revisar icono normal y redondo;
4. si el launcher cachea fuerte, limpiar cache/datos del launcher o reiniciar el telefono.

---

## Incidente 2026-06-12 (timeout/background)

- Se intentaron cambios agresivos en `WorkManager` para depurar (`REPLACE`, encolados extra, logs de prueba).
- Eso afecto el comportamiento en segundo plano del telefono y genero regresion percibida en otras apps.
- Se hizo rollback y se volvio a una base estable; el telefono quedo normal otra vez.

## Estado actual confirmado

- `CheckWorker` en flujo simple original (`/status`).
- `startPeriodicWork()` sin encolados de prueba agresivos.
- Ajuste del aguila conservado.
- Fix aplicado en `SharedPreferencesHelper` para evitar `LinkedList.addFirst(...) on null`.

## Regla para retomar manana (importante)

1. un solo cambio por vez;
2. no usar `REPLACE` en periodicos para probar;
3. primero validar manual (`status`), despues worker;
4. si hay impacto lateral en el telefono, rollback inmediato.

