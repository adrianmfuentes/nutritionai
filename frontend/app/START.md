# ⚡ INICIO RÁPIDO

¿Primera vez? Sigue estos pasos para tener tu app funcionando en **15 minutos**.

## 🎯 Objetivo

Probar tu app Android + Backend con IA (Groq) localmente en tu móvil **ANTES** de subir a producción.

## 📦 Lo que Necesitas

- ✅ Windows 10/11
- ✅ Docker Desktop
- ✅ Android Studio
- ✅ Un móvil Android
- ✅ PC y móvil en la misma WiFi

## 🚀 5 Pasos Rápidos

### 1️⃣ Instalar Dependencias

```cmd
install.bat
```

Este script:

- Verifica Docker
- Instala dependencias npm
- Crea directorios necesarios
- Muestra tu IP local

### 2️⃣ Obtener API Key (GRATIS)

1. Ve a: https://console.groq.com/
2. Crea cuenta → API Keys → Create API Key
3. Copia la key (empieza con `gsk_...`)

### 3️⃣ Configurar Backend

Edita `backend\.env`:

```env
GROQ_API_KEY=gsk_pega_aqui_tu_key_real
```

### 4️⃣ Iniciar Backend

```cmd
cd backend
docker-compose up -d --build
```

Espera 1-2 minutos. Verifica:

```cmd
curl http://localhost:3000/health
```

### 5️⃣ Configurar y Compilar App

1. Obtén tu IP:

   ```cmd
   get-local-ip.bat
   ```

2. En Android Studio, edita `ApiConfig.kt`:

   ```kotlin
   private const val BASE_URL = "http://TU_IP:3000/v1/"
   // Ejemplo: "http://192.168.1.100:3000/v1/"
   ```

3. Compila:
   ```cmd
   cd frontend
   gradlew installDebug
   ```

## 🎉 ¡Listo!

Ahora en tu móvil:

1. Abre la app
2. Regístrate
3. Toma foto de comida
4. Mira el análisis con IA

## 📚 Documentación Completa

- **[QUICKSTART_WINDOWS.md](QUICKSTART_WINDOWS.md)** - Guía completa para Windows
- **[backend/LOCAL_TESTING.md](backend/LOCAL_TESTING.md)** - Guía de pruebas locales
- **[CHANGELOG_GROQ.md](CHANGELOG_GROQ.md)** - Cambios realizados

## ❌ ¿Problemas?

### "Network request failed"

```cmd
# 1. Verifica backend
docker ps

# 2. Verifica firewall
# Windows → Firewall → Regla puerto 3000

# 3. Prueba desde navegador del móvil
# http://TU_IP:3000/health
```

### "Error al analizar imagen"

```cmd
# 1. Verifica API key en .env
# 2. Reinicia backend
docker-compose restart api

# 3. Ve los logs
docker-compose logs -f api
```

### Backend no inicia

```cmd
# Limpia y reconstruye
docker-compose down -v
docker-compose up -d --build
```

## 💡 Tips

**Ver logs en tiempo real:**

```cmd
docker-compose logs -f api
```

**Reiniciar solo el backend:**

```cmd
docker-compose restart api
```

**Detener todo:**

```cmd
docker-compose down
```

## 🎯 Después de Probar Localmente

Cuando todo funcione, despliega a producción:

- **[backend/DEPLOYMENT.md](backend/DEPLOYMENT.md)** - Despliegue en Oracle Cloud

---

**¿Dudas?** Lee [QUICKSTART_WINDOWS.md](QUICKSTART_WINDOWS.md) para más detalles.
