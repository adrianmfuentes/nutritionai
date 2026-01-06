# ✅ Configuración de Conexión Frontend - Backend

## 📊 Estado Actual

### ✅ Lo que YA está configurado:

1. **AndroidManifest.xml**

   - ✅ Permisos de Internet
   - ✅ `usesCleartextTraffic="true"` (permite HTTP en desarrollo)

2. **NutritionApiService.kt**

   - ✅ Todos los endpoints definidos (auth, meals, nutrition, profile)
   - ✅ Retrofit configurado correctamente

3. **AuthInterceptor.kt**

   - ✅ Interceptor para añadir token JWT automáticamente

4. **ApiClient.kt**

   - ✅ Cliente Retrofit con logging y timeouts

5. **AppModule.kt (Koin DI)**
   - ✅ Inyección de dependencias actualizada con Retrofit

## 🔧 Lo que DEBES Configurar

### Paso 1: Cambiar la URL del Backend

Edita el archivo:

```
frontend/app/src/main/java/com/health/nutritionai/data/remote/ApiClient.kt
```

Busca la línea 16 y cámbiala según tu caso:

#### Opción A - Emulador de Android Studio:

```kotlin
private const val BASE_URL = "http://10.0.2.2:3000/v1/"
```

✅ Usa esto si ejecutas la app en el **emulador**

#### Opción B - Dispositivo Físico (Recomendado para pruebas reales):

```kotlin
private const val BASE_URL = "http://192.168.1.100:3000/v1/"
```

⚠️ **Cambia `192.168.1.100` por la IP real de tu PC**

Para obtener tu IP:

```cmd
# Windows
get-local-ip.bat

# O manualmente
ipconfig
```

#### Opción C - Producción:

```kotlin
private const val BASE_URL = "https://tu-dominio.com/v1/"
```

🌐 Usa esto cuando despliegues en producción

## 🧪 Cómo Probar la Conexión

### 1. Verificar Backend está Corriendo

```cmd
cd backend
docker-compose up -d

# Verificar
curl http://localhost:3000/health
```

Respuesta esperada:

```json
{ "status": "ok", "timestamp": "..." }
```

### 2. Obtener tu IP Local (si usas dispositivo físico)

```cmd
get-local-ip.bat
```

Anota la IP que muestra (ej: `192.168.1.100`)

### 3. Actualizar ApiClient.kt

Edita `ApiClient.kt` línea 16:

```kotlin
private const val BASE_URL = "http://TU_IP:3000/v1/"
```

### 4. Compilar e Instalar la App

```cmd
cd frontend
gradlew clean installDebug
```

O desde Android Studio: **Run ▶️**

### 5. Probar Registro

En la app:

1. Abre la app
2. Ve a "Registrarse"
3. Completa los datos:
   - Email: test@ejemplo.com
   - Contraseña: test1234
   - Nombre: Test User
4. Toca "Registrar"

✅ Si ves un mensaje de éxito → **¡Conexión funcionando!**
❌ Si ves "Network request failed" → Ver solución de problemas abajo

## 🐛 Solución de Problemas

### Error: "Network request failed"

**Verificar 1: Backend corriendo**

```cmd
docker ps
```

Deberías ver 3 contenedores: `nutrition_db`, `nutrition_api`, `nutrition_nginx`

**Verificar 2: IP correcta en ApiClient.kt**

```cmd
ipconfig
```

Compara con la IP en `ApiClient.kt`

**Verificar 3: Móvil y PC en la misma WiFi**

- Verifica que ambos estén conectados a la misma red

**Verificar 4: Firewall**

```cmd
# Windows - Añadir regla
netsh advfirewall firewall add rule name="Nutrition API" dir=in action=allow protocol=TCP localport=3000
```

**Verificar 5: Prueba desde navegador del móvil**
Abre el navegador en tu móvil y visita:

```
http://TU_IP:3000/health
```

Si carga → El backend está accesible
Si no carga → Problema de red/firewall

### Error: "Unable to connect" en emulador

Si usas el emulador, **DEBES** usar:

```kotlin
private const val BASE_URL = "http://10.0.2.2:3000/v1/"
```

**NO** uses `localhost` ni `127.0.0.1` en el emulador.

### Ver logs de red

En Android Studio:

1. Abre **Logcat**
2. Busca el filtro: `OkHttp`
3. Verás todas las peticiones HTTP y respuestas

## 📱 Flujo Completo de Prueba

### 1. Registro de Usuario

```kotlin
// La app enviará:
POST http://TU_IP:3000/v1/auth/register
{
  "email": "test@ejemplo.com",
  "password": "test1234",
  "name": "Test User"
}

// Respuesta esperada:
{
  "message": "Usuario registrado exitosamente",
  "user": { ... },
  "token": "eyJhbGciOiJIUzI1NiIs..."
}
```

### 2. Login

```kotlin
POST http://TU_IP:3000/v1/auth/login
{
  "email": "test@ejemplo.com",
  "password": "test1234"
}
```

### 3. Análisis de Comida

```kotlin
POST http://TU_IP:3000/v1/meals/analyze
Content-Type: multipart/form-data
Authorization: Bearer TOKEN

image: [archivo de imagen]
```

Respuesta esperada:

```json
{
  "mealId": "uuid",
  "detectedFoods": [
    {
      "name": "Arroz blanco",
      "confidence": 0.95,
      "nutrition": {
        "calories": 130,
        "protein": 2.7,
        "carbs": 28,
        "fat": 0.3
      }
    }
  ],
  "totalNutrition": { ... }
}
```

## 🔐 Gestión del Token

El token JWT se guarda automáticamente en SharedPreferences después del login:

```kotlin
// El AuthInterceptor lo añade automáticamente a cada request
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

No necesitas hacer nada manualmente, está todo configurado.

## 📊 Checklist de Verificación

Antes de probar, verifica:

- [ ] Backend corriendo (`docker ps` muestra 3 contenedores)
- [ ] Backend responde (`curl http://localhost:3000/health`)
- [ ] IP correcta en `ApiClient.kt` (para dispositivo físico)
- [ ] Móvil y PC en la misma WiFi
- [ ] Firewall permite puerto 3000
- [ ] App recompilada después de cambiar `ApiClient.kt`

## 🎯 Pasos Siguientes

Una vez conectado:

1. **Probar todos los flujos:**

   - ✅ Registro
   - ✅ Login
   - ✅ Capturar foto
   - ✅ Analizar comida
   - ✅ Ver dashboard
   - ✅ Historial de comidas

2. **Verificar sincronización:**

   - Los datos deben guardarse en el backend
   - Verifica en la base de datos PostgreSQL

3. **Preparar para producción:**
   - Cambiar `BASE_URL` a tu dominio HTTPS
   - Quitar `usesCleartextTraffic` del Manifest
   - Habilitar ProGuard

## 💡 Tips

### Ver qué se está enviando:

En Logcat busca:

```
OkHttp --> POST http://...
OkHttp --> Body: {"email":"test@ejemplo.com",...}
OkHttp <-- 200 OK
```

### Reiniciar solo la app (sin recompilar):

En Android Studio: **Shift + F5** (Run)

### Limpiar y reconstruir:

```cmd
cd frontend
gradlew clean build
```

## 📚 Archivos Modificados

✅ **ApiClient.kt** - Cliente Retrofit (DEBES modificar BASE_URL)
✅ **AppModule.kt** - Inyección de dependencias con Retrofit
✅ **AuthInterceptor.kt** - Ya existía, sin cambios
✅ **NutritionApiService.kt** - Ya existía, sin cambios
✅ **AndroidManifest.xml** - Ya tenía `usesCleartextTraffic="true"`

## 🆘 Soporte

Si algo no funciona:

1. Revisa los logs del backend:

   ```cmd
   docker-compose logs -f api
   ```

2. Revisa Logcat en Android Studio:

   - Filtra por: `OkHttp`

3. Verifica conectividad:

   ```cmd
   # Desde PC
   curl http://localhost:3000/health

   # Desde móvil (navegador)
   http://TU_IP:3000/health
   ```

## ❓ Solución de Problemas Comunes

### Error: "failed to connect to /192.168.1.101 (port 3000)... after 30000ms"

Este error indica que el móvil no puede "ver" a tu PC.

**Causas probables:**

1. **Redes Diferentes (MÁS PROBABLE):**

   - Tu PC tiene IP `192.168.1.x` (WiFi Casa).
   - Tu Móvil tiene IP `10.x.x.x` (Datos 4G/5G, VPN activada, o WiFi Invitados).
   - **Solución:** Conecta el móvil al **mismo WiFi** que el PC. Desactiva Datos Móviles y VPN.

2. **Firewall de Windows:**
   - Windows bloquea conexiones entrantes al puerto 3000 por defecto en redes Privadas/Públicas.
   - **Solución:** Ejecuta el script `open-firewall.bat` que hemos creado en la raíz del proyecto.

---

**✅ La app ahora está completamente conectada al backend con Groq (IA gratuita)!**
