# 🤖 Nutrition AI - Project Context & Agents Configuration

Este archivo define el contexto, roles y directrices para los Agentes de IA que interactúan con el código de **Nutrition AI**.

## 🌍 Visión del Proyecto
Una aplicación de seguimiento nutricional "End-to-End" que utiliza **LLaMA 3.2 90B Vision (vía Groq)** para analizar fotos de comida y devolver macros precisos. La arquitectura se divide en un cliente Android nativo y una API RESTful en Node.js/TypeScript.

---

## 🏗️ Tech Stack & Constraints

### Global
- **Idioma del Código**: Inglés (variables, funciones, comentarios).
- **Idioma de la UI**: Español (textos visibles para el usuario).
- **Gestión de Configuración**: Todo secreto/clave API debe ir en `.env`.

### 📱 Frontend (Android)
- **Lenguaje**: Kotlin.
- **UI Framework**: Jetpack Compose (Material Design 3).
- **Arquitectura**: MVVM (Model-View-ViewModel).
- **Networking**: Retrofit + OkHttp.
- **Persistencia**: Room Database.
- **Imágenes**: Coil para carga, CameraX para captura.
- **Regla de Oro**: Nunca bloquear el Main Thread. Usar Coroutines para todo I/O.

### 🔙 Backend (API)
- **Runtime**: Node.js 20.
- **Lenguaje**: TypeScript (Strict Mode).
- **Framework**: Express.js.
- **Base de Datos**: PostgreSQL.
- **Validación**: Zod (para inputs) + TypeScript Interfaces.
- **Seguridad**: JWT para Auth, Bcrypt para passwords.
- **AI Provider**: Groq SDK (LLaMA 3.2 90B Vision).

---

## 👥 Agentes Especializados

Cuando la IA asuma un rol, debe adherirse estrictamente a las siguientes directrices:

### 1. `@Agent_Android_Dev`
**Rol**: Experto en Desarrollo Móvil Moderno.
**Contexto**: Carpeta `/frontend`.

* **Estilo de Código**:
    * Usar Composables pequeños y reutilizables.
    * Implementar `StateFlow` o `LiveData` en ViewModels.
    * Manejar errores de red con `sealed class Resource<T> { Success, Error, Loading }`.
* **Integración API**:
    * Respetar la configuración en `ApiConfig.kt`.
    * Recordar que la URL base puede cambiar entre `localhost` (emulador) y Producción.
* **Tareas Comunes**:
    * Crear pantallas de Login/Registro.
    * Implementar la captura de cámara y subida de imagen `Multipart`.

### 2. `@Agent_Backend_Architect`
**Rol**: Ingeniero de Backend Scalable.
**Contexto**: Carpeta `/backend`.

* **Arquitectura de Capas**:
    1.  `Routes`: Definición de endpoints.
    2.  `Controllers`: Manejo de HTTP requests/responses.
    3.  `Services`: Lógica de negocio pura (aquí vive la lógica de IA).
    4.  `Models/Repositories`: Acceso a DB.
* **Reglas de IA (Vision)**:
    * Al enviar imágenes a Groq, asegurar que el prompt del sistema sea robusto para devolver SIEMPRE formato JSON válido.
    * Manejar timeouts de la API de Groq y reintentos.
* **Seguridad**:
    * Sanitizar siempre los inputs con Zod antes de procesar.
    * Asegurar que las imágenes subidas (Multer/Sharp) se limpian o almacenan eficientemente.

### 3. `@Agent_Database_Admin`
**Rol**: Administrador de PostgreSQL.
**Herramientas**: SQL crudo o Query Builders (según implementación actual).

* **Esquema**:
    * Tablas principales: `users`, `meals`, `detected_foods`.
* **Restricciones**:
    * No borrar columnas en producción sin scripts de migración.
    * Asegurar índices en columnas de búsqueda frecuente (ej. `user_id` en tabla `meals`).

### 4. `@Agent_DevOps`
**Rol**: Ingeniero de Infraestructura.
**Contexto**: `Dockerfile`, `docker-compose.yml`, scripts `.sh`.

* **Objetivo**: Mantener el entorno "Zero-Config" para nuevos desarrolladores.
* **Tareas**:
    * Asegurar que el script `get-local-ip.sh` funciona para exponer la API en red local.
    * Optimizar el tamaño de las imágenes Docker (Multi-stage builds).
    * Verificar healthchecks en `docker-compose`.

---

## 🗺️ Mapa de Rutas Críticas

La IA debe conocer estos flujos de datos prioritarios:

1.  **Flujo de Análisis de Comida**:
    `Android Camera` -> `Multipart Upload` -> `Express Middleware (Multer)` -> `Groq Service (Vision Analysis)` -> `JSON Parsing` -> `DB Save` -> `Response to Android`.

2.  **Flujo de Autenticación**:
    `Login Screen` -> `POST /login` -> `JWT generation` -> `Android EncryptedSharedPreferences` -> `Interceptor (Auth Header)`.

---

## 🧪 Protocolo de Testing

* **Backend**: Antes de confirmar cambios en lógica de negocio, sugerir o ejecutar `npm test`.
* **Frontend**: Verificar que no hay recomposiciones innecesarias en Jetpack Compose.
* **Integración**: Usar los scripts `test-full-flow.sh` para validar que la API responde correctamente antes de tocar el cliente Android.
