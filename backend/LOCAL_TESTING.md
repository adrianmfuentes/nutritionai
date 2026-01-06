# 📱 Guía de Pruebas Locales - Móvil + Backend

Esta guía te ayudará a probar tu aplicación Android con el backend corriendo localmente en tu PC, antes de subir a producción.

## 📋 Requisitos Previos

- ✅ PC y móvil conectados a la **misma red WiFi**
- ✅ Docker Desktop instalado y corriendo
- ✅ Android Studio para compilar la app
- ✅ API Key de Groq (gratis en https://console.groq.com/)

## 🔧 Paso 1: Configurar API Key de Groq

### 1.1 Obtener API Key Gratuita

1. Ve a https://console.groq.com/
2. Crea una cuenta (gratis)
3. Ve a "API Keys"
4. Crea una nueva API Key
5. Copia la key (empieza con `gsk_...`)

### 1.2 Configurar el Backend

Edita el archivo `.env` en la carpeta `backend/`:

```env
# AI Service - Groq (GRATIS)
GROQ_API_KEY=gsk_tu_api_key_real_aqui
```

## 🚀 Paso 2: Levantar el Backend

### 2.1 Abrir Terminal en la Carpeta Backend

```bash
cd backend
```

### 2.2 Instalar Dependencias (primera vez)

```bash
npm install
```

### 2.3 Levantar con Docker Compose

```bash
docker-compose up -d --build
```

Verás algo como:

```
✔ Container nutrition_db     Started
✔ Container nutrition_api    Started
✔ Container nutrition_nginx  Started
```

### 2.4 Verificar que Funciona

```bash
curl http://localhost:3000/health
```

Respuesta esperada:

```json
{
  "status": "ok",
  "timestamp": "2026-01-06T..."
}
```

## 🌐 Paso 3: Obtener la IP de tu PC

### En Windows:

```bash
ipconfig
```

Busca la sección **"Adaptador de LAN inalámbrica Wi-Fi"**:

```
Dirección IPv4: 192.168.1.100  <-- Esta es tu IP
```

### En Linux/Mac:

```bash
ip addr show
# o
ifconfig
```

Busca la interfaz WiFi (usualmente `wlan0` o `en0`):

```
inet 192.168.1.100  <-- Esta es tu IP
```

## 📡 Paso 4: Probar desde el Móvil

### 4.1 Verificar Conectividad

Desde el navegador de tu móvil, visita:

```
http://TU_IP_AQUI:3000/health
```

Ejemplo:

```
http://192.168.1.100:3000/health
```

✅ Si ves `{"status":"ok"}` → ¡Conexión exitosa!
❌ Si no carga → Revisa firewall o que estén en la misma red

### 4.2 Configurar Firewall (si es necesario)

**Windows Defender Firewall:**

1. Busca "Firewall de Windows Defender"
2. Clic en "Configuración avanzada"
3. "Reglas de entrada" → "Nueva regla"
4. Tipo: Puerto
5. TCP - Puerto específico: `3000`
6. Permitir la conexión
7. Aplicar a todas las redes
8. Nombre: "Nutrition API Local"

**Linux (ufw):**

```bash
sudo ufw allow 3000/tcp
```

**Mac:**

```bash
# Por defecto debería funcionar
# Si no, verifica en Preferencias del Sistema > Seguridad > Firewall
```

## 📱 Paso 5: Configurar la App Android

### 5.1 Actualizar la URL del API

Edita el archivo en tu proyecto Android:

```
frontend/app/src/main/java/com/tu/paquete/ApiConfig.kt
```

Busca la línea con `BASE_URL` y cámbiala:

```kotlin
object ApiConfig {
    // Para pruebas locales - CAMBIA ESTA IP
    private const val BASE_URL = "http://192.168.1.100:3000/v1/"

    // Para producción (después)
    // private const val BASE_URL = "https://tu-dominio.com/v1/"

    fun getRetrofitInstance(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
```

**⚠️ IMPORTANTE:** Reemplaza `192.168.1.100` con la IP real de tu PC.

### 5.2 Permitir HTTP en Android

Edita `frontend/app/src/main/AndroidManifest.xml`:

```xml
<application
    android:usesCleartextTraffic="true"
    ...>
```

### 5.3 Compilar e Instalar en tu Móvil

```bash
cd frontend
./gradlew installDebug
```

O desde Android Studio:

1. Conecta tu móvil por USB (con depuración activada)
2. Click en "Run" (▶️)

## 🧪 Paso 6: Probar el Flujo Completo

### 6.1 Registrar un Usuario

Desde la app Android:

1. Abre la app
2. Toca "Registrarse"
3. Completa:
   - **Email:** test@ejemplo.com
   - **Contraseña:** test1234
   - **Nombre:** Usuario Test

### 6.2 Iniciar Sesión

1. Email: test@ejemplo.com
2. Contraseña: test1234

### 6.3 Analizar una Comida

1. Toca el botón de cámara
2. Toma una foto de comida o selecciona de galería
3. Espera el análisis (5-10 segundos)
4. Verifica que detecte los alimentos

### 6.4 Ver el Dashboard

1. Ve a la pestaña "Resumen"
2. Verifica que muestre:
   - Calorías del día
   - Macronutrientes
   - Progreso hacia objetivos

## 🔍 Solución de Problemas

### ❌ Error: "Network request failed"

**Causa:** La app no puede conectar al backend

**Solución:**

1. Verifica que el backend esté corriendo: `docker ps`
2. Verifica la IP en ApiConfig.kt
3. Asegúrate de que móvil y PC estén en la misma WiFi
4. Prueba desde el navegador del móvil primero

### ❌ Error: "Unable to resolve host"

**Causa:** IP incorrecta o problemas de DNS

**Solución:**

1. Vuelve a obtener tu IP: `ipconfig` (Windows)
2. Actualiza `BASE_URL` en ApiConfig.kt
3. Reinstala la app

### ❌ Error: "Connection timeout"

**Causa:** Firewall bloqueando puerto 3000

**Solución:**

1. Añade regla de firewall (ver sección 4.2)
2. Temporalmente, desactiva el firewall para probar

### ❌ Error al analizar imagen: "Error al analizar la imagen de comida"

**Causa:** API Key de Groq inválida o faltante

**Solución:**

1. Verifica que `GROQ_API_KEY` esté en `.env`
2. Verifica que la API key sea correcta
3. Reinicia el backend: `docker-compose restart api`

### ❌ Backend no levanta: "Error: Cannot find module 'groq-sdk'"

**Causa:** Dependencias no instaladas

**Solución:**

```bash
cd backend
npm install
docker-compose down
docker-compose up -d --build
```

## 📊 Verificar Logs del Backend

### Ver logs en tiempo real:

```bash
docker-compose logs -f api
```

### Ver errores específicos:

```bash
docker-compose logs api | grep -i error
```

### Ver logs de base de datos:

```bash
docker-compose logs postgres
```

## 🎯 Pruebas Manuales Recomendadas

### ✅ Checklist de Funcionalidades:

- [ ] Registro de usuario nuevo
- [ ] Login con credenciales correctas
- [ ] Login falla con credenciales incorrectas
- [ ] Captura de foto de comida
- [ ] Análisis de imagen con IA (Groq)
- [ ] Visualización de alimentos detectados
- [ ] Guardar comida analizada
- [ ] Ver lista de comidas del día
- [ ] Ver resumen nutricional diario
- [ ] Ver gráficos semanales
- [ ] Actualizar objetivos nutricionales
- [ ] Editar perfil
- [ ] Eliminar comida
- [ ] Logout

### 🧪 Casos de Prueba:

**Test 1: Foto con múltiples alimentos**

- Toma foto de un plato mixto (ej: arroz, pollo, ensalada)
- Verifica que detecte todos los componentes
- Verifica que los valores nutricionales sean razonables

**Test 2: Diferentes tipos de comida**

- Desayuno (huevos, pan, fruta)
- Almuerzo (plato principal)
- Cena (algo ligero)
- Snack (fruta, frutos secos)

**Test 3: Progreso durante el día**

- Añade 3-4 comidas en el día
- Verifica que el total se actualice
- Verifica el porcentaje de objetivos

## 📈 Monitoreo de Rendimiento

### Verificar uso de recursos:

```bash
docker stats
```

### Verificar conectividad de red:

Desde el móvil, prueba:

```
http://TU_IP:3000/health
http://TU_IP:3000/v1/auth/test  # Si existe endpoint de test
```

## 🔄 Reiniciar Todo (si es necesario)

```bash
# Detener todo
docker-compose down

# Eliminar volúmenes (borra datos)
docker-compose down -v

# Limpiar imágenes antiguas
docker system prune -a

# Volver a levantar
docker-compose up -d --build
```

## 📝 Notas Importantes

1. **IP Dinámica:** Tu IP local puede cambiar. Si dejas de conectar, verifica tu IP nuevamente.

2. **No usar localhost:** Desde Android, `localhost` apunta al propio móvil, no a tu PC.

3. **HTTP vs HTTPS:** En local usamos HTTP. En producción DEBES usar HTTPS.

4. **Firewall:** Algunos antivirus bloquean conexiones entrantes. Temporalmente desactiva para probar.

5. **Datos de Prueba:** Usa datos ficticios. Esta es solo una prueba local.

## ✅ Lista de Verificación Final

Antes de subir a producción, verifica:

- [ ] Backend funciona sin errores
- [ ] Análisis de imágenes con Groq funciona correctamente
- [ ] Registro y login funcionan
- [ ] Todas las pantallas de la app cargan
- [ ] Los cálculos nutricionales son precisos
- [ ] No hay crashes en la app
- [ ] La interfaz es intuitiva
- [ ] Los tiempos de respuesta son aceptables
- [ ] Las imágenes se suben y optimizan correctamente

## 🚀 Próximo Paso: Producción

Una vez que todo funcione localmente, sigue la guía de producción:

👉 Ver [DEPLOYMENT.md](./DEPLOYMENT.md) para desplegar en Oracle Cloud

## 💡 Tips Adicionales

**Desarrollo más rápido:**

```bash
# Modo desarrollo - recarga automática
cd backend
npm run dev
```

**Ver base de datos:**
Usa un cliente PostgreSQL como pgAdmin o DBeaver:

- Host: `TU_IP`
- Puerto: `5432`
- Base de datos: `nutrition_ai`
- Usuario: `nutrition_user`
- Contraseña: (la de tu `.env`)

**Probar endpoints con cURL desde PC:**

```bash
# Registro
curl -X POST http://localhost:3000/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@ejemplo.com","password":"test1234","name":"Test"}'

# Login
curl -X POST http://localhost:3000/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@ejemplo.com","password":"test1234"}'
```

---

¿Problemas? Revisa los logs: `docker-compose logs -f`

¡Buena suerte con las pruebas! 🎉
