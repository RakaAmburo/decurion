# Mejoras futuras — Decurion

> Documento complementario de foco técnico: `FIABILIDAD_RED_BACKGROUND_ANDROID.md`

> Actualizacion 2026-06-12: hubo regresion por cambios agresivos de scheduler en pruebas de timeout/background; se hizo rollback. Retomar con cambios minimos y uno por vez (detalle en `FIABILIDAD_RED_BACKGROUND_ANDROID.md`).

## 🔴 Bugs / errores

### 1. `RequestQueue` se instancia en cada request — memory leak
**Archivos:** `RestClient.java`, `RestPoster.java`  
Cada llamada crea un nuevo `Volley.newRequestQueue()` con sus thread pools internos que nunca se cierran.  
→ Convertir a singleton estático o inyectado.

### 2. `RestPoster` — SSL roto + sin token
**Archivo:** `RestPoster.java`  
- `trusted.load()` comentado → keystore nunca inicializado → crash en runtime si se usa.  
- `getHeaders()` tiene `authorization: ""` → sin token.  
- RetryPolicy sigue en `1f` (sin backoff exponencial).  
→ Limpiar o eliminar si ya no se usa activamente.

### 3. `EvaluateExecResponse.processError` no registra errores
**Archivo:** `EvaluateExecResponse.java`  
`EvaluateStatusResponse` guarda errores en `SharedPreferencesHelper`, pero `EvaluateExecResponse` no.  
→ Agregar `SharedPreferencesHelper.addErrorToFront(context, message)`.

### 4. NPE potencial en `onResults` — sin null check en `matches`
**Archivo:** `MainActivity.java`  
```java
if (matches.size() == 0)  // NullPointerException si matches == null
```
→ Agregar `if (matches == null || matches.isEmpty())`.

---

## 🟡 Deuda técnica / mejoras

### 5. `Thread.sleep(100)` en UI thread
**Archivos:** `MainActivity.java`, `BaseActivityAndRecognitionListener.java`  
`onEndOfSpeech()` hace `Thread.sleep(100)` en el main thread.  
→ Reemplazar por `Handler.postDelayed()`.

### 6. `CheckWorker.notify()` — método muerto con dependencia Jsoup
**Archivo:** `CheckWorker.java`  
El método `notify()` con lógica de fetch de IP dinámica vía Jsoup está comentado y sin usar.  
→ Evaluar si recuperar el fetch dinámico de IP (útil si la IP del servidor cambia) o eliminar el método y la dependencia.

### 7. Código duplicado entre `BaseActivity` y `MainActivity`
`createSpeech()`, `onError()`, `onResume()`, `onPause()` están definidos en ambos sin llamar a `super`.  
→ Limpiar la clase base o eliminarla y consolidar en `MainActivity`.

### 8. `askForPermission()` — ambas ramas del if/else son idénticas
**Archivo:** `MainActivity.java`  
→ Eliminar el if/else, dejar solo `requestPermissions(...)`.

### 9. `startPeriodicWork()` — `REPLACE` reinicia el worker en cada apertura
**Archivo:** `MainActivity.java`  
`getLiveData().getValue()` siempre devuelve `null` al arrancar → siempre encola.  
`ExistingPeriodicWorkPolicy.REPLACE` cancela y recrea el worker cada vez que se abre la app.  
→ Cambiar a `KEEP` para que solo arranque si no existe ya.

### 10. IP pública hardcodeada en `SecuredProperties`
IP `217.71.203.118` fija → si cambia, hay que recompilar.  
→ Considerar config dinámica (SharedPreferences + UI para cambiarla, o recuperar lógica Jsoup del `CheckWorker`).

### 11. Notification channel ID placeholder
**Archivo:** `MainActivity.java`, `EvaluateStatusResponse.java`, `CheckWorker.java`  
El channel ID es literalmente `"Your_channel_id"`.  
→ Renombrar a algo descriptivo como `"decurion_alerts"`.
