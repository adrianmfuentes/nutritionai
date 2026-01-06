# Guía de Conectividad Frontend-Backend (Contexto para LLM)

Este documento define la arquitectura de conexión, contratos de API, y puntos de configuración críticos entre el Frontend (Android/Kotlin) y Backend (Node.js/TypeScript). Úsalo como contexto principal para tareas de integración, depuración de red o refactorización de API.

## 1. Arquitectura de Conexión

El sistema sigue una arquitectura cliente-servidor RESTful clásica.

- **Backend**: Node.js v20+ con Express. Se ejecuta en el puerto `3000` (por defecto).
- **Frontend**: Aplicación nativa Android (Kotlin) usando Retrofit 2 para networking.
- **Protocolo**: HTTP/1.1 (JSON).
- **Seguridad**: JWT (JSON Web Tokens) en headers `Authorization`.

## 2. Contrato de API (Backend Interface)

### Información Base

- **Puerto Default**: `3000`
- **Prefijo de V1**: `/v1`
- **URL Base Local**: `http://localhost:3000/v1`
- **Endpoint de Salud**: `GET /health` (Sin prefijo /v1, retorna 200 OK)

### Estándar de Peticiones

- **Content-Type**: `application/json` (excepto subida de imágenes `multipart/form-data`)
- **Autenticación**: Header `Authorization: Bearer <TOKEN>`

### Estándar de Respuestas

Todas las respuestas siguen un formato JSON predecible (aunque no estricto envelope).

- **Éxito (2xx)**: Retorna el objeto solicitado o mensaje de éxito.
- **Error (4xx, 5xx)**:
  ```json
  {
    "error": "Descripción del error legible"
  }
  ```

### Mapa de Rutas Principales (`backend/src/routes/`)

| Dominio       | Ruta Base       | Archivo de Ruta       | Controlador               |
| :------------ | :-------------- | :-------------------- | :------------------------ |
| **Auth**      | `/v1/auth`      | `auth.routes.ts`      | `auth.controller.ts`      |
| **Comidas**   | `/v1/meals`     | `meals.routes.ts`     | `meals.controller.ts`     |
| **Nutrición** | `/v1/nutrition` | `nutrition.routes.ts` | `nutrition.controller.ts` |
| **Perfil**    | `/v1/profile`   | `profile.routes.ts`   | (Gestión de usuario)      |

## 3. Implementación Cliente (Frontend Android)

### Configuración de Red Crítica

La configuración de la URL base **NO** es dinámica en runtime actualmente. Se define como constante en compilación.

- **Archivo Objetivo**: [`frontend/app/src/main/java/com/health/nutritionai/data/remote/ApiClient.kt`](frontend/app/src/main/java/com/health/nutritionai/data/remote/ApiClient.kt)
- **Variable**: `private const val BASE_URL`

#### Direcciones IP Comunes para Desarrollo

1.  **Emulador Android Oficial**: `http://10.0.2.2:3000/v1/` (Apusnta al localhost del host).
2.  **Dispositivo Físico (LAN)**: `http://<TU_IP_LOCAL>:3000/v1/` (Ej: `192.168.1.50`).
3.  **Genymotion**: `http://10.0.3.2:3000/v1/`

### Seguridad de Red (Manifest)

- **Archivo**: [`frontend/app/src/main/AndroidManifest.xml`](frontend/app/src/main/AndroidManifest.xml)
- **Configuración**: `android:usesCleartextTraffic="true"`
  - _Nota_: Necesario porque en desarrollo usamos HTTP, no HTTPS. Para producción, esto debe cambiar.

### Capa de Red (Retrofit & OkHttp)

1.  **Interceptor de Auth**:
    - Archivo: `AuthInterceptor.kt`
    - Función: Inyecta automáticamente el header `Authorization` si existe un token guardado en `TokenManager`.
2.  **Definición de Servicios**:
    - Archivo: `NutritionApiService.kt`
    - Descripción: Interfaz Kotlin que mapea los endpoints REST a funciones suspendidas.

## 4. Guía de Depuración para Agentes AI

Si el usuario reporta "Error de conexión" o "Network Error", sigue estos pasos lógicos:

1.  **Verificar el Servidor**:

    - ¿Está corriendo el backend? (Revisar terminal).
    - ¿Responde a `curl http://localhost:3000/health`?

2.  **Verificar la IP en Frontend**:

    - Leer `frontend/.../ApiClient.kt`.
    - ¿Está usando `localhost` (incorrecto para Android)? -> Debe ser `10.0.2.2` o IP LAN.

3.  **Verificar Visibilidad**:

    - Si es dispositivo físico, ¿están en la misma WiFi?
    - ¿El firewall de Windows/Mac está bloqueando el puerto 3000 de Node?

4.  **Verificar Contrato**:
    - ¿El endpoint invocado en `NutritionApiService.kt` coincide _exactamente_ con `backend/src/routes/...`?
    - Prestar atención a barras finales `/` o prefijos `/v1`.

## 5. Snippets Útiles

### Obtener IP Local (Terminal)

Utiliza el script incluido en el root para ver la IP rápidamente:

```bash
./get-local-ip.sh  # Unix/Mac
./get-local-ip.bat # Windows
```

### Configuración Rápida de `ApiClient.kt`

```kotlin
// Para Emulador
private const val BASE_URL = "http://10.0.2.2:3000/v1/"

// Para Dispositivo Físico (Reemplazar X)
// private const val BASE_URL = "http://192.168.1.X:3000/v1/"
```
