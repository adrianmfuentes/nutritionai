# 🚀 Guía de Inicio Rápido - Windows

Esta guía te ayudará a configurar y probar la aplicación en **menos de 15 minutos**.

## ✅ Checklist de Requisitos

Antes de empezar, asegúrate de tener:

- [ ] Windows 10/11
- [ ] Docker Desktop instalado y corriendo
- [ ] Android Studio (para compilar la app)
- [ ] Un móvil Android (para probar)
- [ ] PC y móvil en la **misma WiFi**

## 📋 Paso a Paso

### 1️⃣ Obtener API Key de Groq (GRATIS)

1. Abre tu navegador
2. Ve a: https://console.groq.com/
3. Crea una cuenta (email + contraseña)
4. Ve a "API Keys" en el menú
5. Click en "Create API Key"
6. **Copia la key** (empieza con `gsk_...`)

### 2️⃣ Configurar el Backend

1. Abre la carpeta del proyecto:

   ```
   d:\34644\Documents\GitHub\nutrition-app\backend
   ```

2. Edita el archivo `.env` (con Notepad o VS Code):

   ```env
   GROQ_API_KEY=gsk_pega_aqui_tu_key_real
   ```

3. Guarda el archivo

### 3️⃣ Obtener tu IP Local

Opción A - **Automático** (Recomendado):

```cmd
# Desde la carpeta raíz del proyecto
get-local-ip.bat
```

Opción B - **Manual**:

```cmd
ipconfig
```

Busca "Adaptador de LAN inalámbrica Wi-Fi" y anota la **IPv4** (ej: `192.168.1.100`)

### 4️⃣ Iniciar el Backend

```cmd
cd backend
docker-compose up -d --build
```

Espera 1-2 minutos. Verás:

```
✔ Container nutrition_db     Started
✔ Container nutrition_api    Started
✔ Container nutrition_nginx  Started
```

Verificar que funciona:

```cmd
curl http://localhost:3000/health
```

Deberías ver: `{"status":"ok",...}`

### 5️⃣ Configurar la App Android

1. Abre Android Studio
2. Abre el proyecto `frontend/`
3. Busca el archivo `ApiConfig.kt`
4. Cambia esta línea:
   ```kotlin
   private const val BASE_URL = "http://TU_IP_AQUI:3000/v1/"
   ```
   Por ejemplo:
   ```kotlin
   private const val BASE_URL = "http://192.168.1.100:3000/v1/"
   ```
5. Guarda el archivo

### 6️⃣ Compilar e Instalar la App

En Android Studio:

1. Conecta tu móvil por USB
2. Activa "Depuración USB" en tu móvil
3. Click en el botón ▶️ (Run)
4. Espera que compile e instale

O desde terminal:

```cmd
cd frontend
gradlew installDebug
```

### 7️⃣ Probar la Aplicación

#### Primera Prueba - Registro:

1. Abre la app en tu móvil
2. Toca "Registrarse"
3. Completa:
   - Email: `test@ejemplo.com`
   - Contraseña: `test1234`
   - Nombre: `Test User`
4. Toca "Registrar"

#### Segunda Prueba - Análisis de Comida:

1. Toca el botón de cámara 📷
2. Toma una foto de comida (o selecciona una de galería)
3. Espera 5-10 segundos
4. Verifica que detecte los alimentos
5. Revisa los valores nutricionales

#### Tercera Prueba - Dashboard:

1. Ve a la pestaña "Resumen"
2. Verifica que muestre:
   - Calorías consumidas
   - Macronutrientes (proteínas, carbos, grasas)
   - Progreso hacia objetivos

## ❌ Solución de Problemas

### Error: "Network request failed"

**Problema:** La app no puede conectar al backend

**Solución:**

1. Verifica que el backend esté corriendo:

   ```cmd
   docker ps
   ```

   Deberías ver 3 contenedores corriendo

2. Verifica la IP en `ApiConfig.kt`

3. Prueba desde el navegador del móvil:

   ```
   http://TU_IP:3000/health
   ```

4. Si no carga, configura el firewall:
   - Windows → Busca "Firewall"
   - "Configuración avanzada"
   - "Reglas de entrada" → "Nueva regla"
   - Tipo: Puerto
   - TCP, puerto: 3000
   - Permitir la conexión

### Error: Backend no inicia

**Problema:** Docker no puede iniciar los contenedores

**Solución:**

1. Verifica que Docker Desktop esté corriendo
2. Reinicia Docker Desktop
3. Limpia y vuelve a construir:
   ```cmd
   cd backend
   docker-compose down -v
   docker-compose up -d --build
   ```

### Error: "Error al analizar la imagen"

**Problema:** API Key de Groq inválida

**Solución:**

1. Verifica que copiaste bien la API key en `.env`
2. Verifica que no tenga espacios extras
3. Reinicia el contenedor:
   ```cmd
   docker-compose restart api
   ```

### La app no se instala en el móvil

**Problema:** Configuración de Android

**Solución:**

1. Activa "Depuración USB" en tu móvil:

   - Ajustes → Acerca del teléfono
   - Toca 7 veces en "Número de compilación"
   - Vuelve atrás → Opciones de desarrollo
   - Activa "Depuración USB"

2. Acepta la conexión USB en el móvil

## 📊 Verificar Todo Funciona

Ejecuta estos comandos para verificar:

```cmd
# 1. Ver contenedores corriendo
docker ps

# 2. Ver logs del backend
docker-compose logs -f api

# 3. Probar endpoint de salud
curl http://localhost:3000/health

# 4. Ver tu IP
ipconfig
```

## 🎯 Próximos Pasos

Una vez que todo funcione:

1. **Prueba más funcionalidades:**

   - Añade varias comidas en un día
   - Revisa el resumen semanal
   - Actualiza tus objetivos nutricionales
   - Edita tu perfil

2. **Lee la documentación completa:**

   - [LOCAL_TESTING.md](backend/LOCAL_TESTING.md) - Guía completa de pruebas
   - [API.md](backend/API.md) - Todos los endpoints disponibles

3. **Cuando estés listo para producción:**
   - [DEPLOYMENT.md](backend/DEPLOYMENT.md) - Despliegue en Oracle Cloud

## 💡 Tips Útiles

- **Ver logs en tiempo real:**

  ```cmd
  docker-compose logs -f api
  ```

- **Reiniciar solo el backend:**

  ```cmd
  docker-compose restart api
  ```

- **Detener todo:**

  ```cmd
  docker-compose down
  ```

- **Empezar de cero (borra todos los datos):**
  ```cmd
  docker-compose down -v
  docker-compose up -d --build
  ```

## 📞 ¿Necesitas Ayuda?

Si algo no funciona:

1. Revisa los logs: `docker-compose logs api`
2. Verifica tu IP: `ipconfig`
3. Prueba desde el navegador del móvil: `http://TU_IP:3000/health`
4. Lee [LOCAL_TESTING.md](backend/LOCAL_TESTING.md) para más detalles

---

¡Disfruta probando tu app! 🎉
