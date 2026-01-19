# 🎉 Cambios Realizados - Groq (IA Gratuita) + Pruebas Locales

## ✅ Resumen de Cambios

Se ha actualizado completamente el backend para usar **Groq (100% GRATIS)** en lugar de Anthropic Claude, y se ha configurado para **pruebas locales desde tu móvil**.

---

## 🔄 Archivos Modificados

### 1. **Backend - Dependencias**

📄 `backend/package.json`

- ❌ Removido: `@anthropic-ai/sdk`
- ✅ Agregado: `groq-sdk` v0.7.0

### 2. **Backend - Servicio de Visión IA**

📄 `backend/src/services/vision.service.ts`

- Cambiado de Anthropic Claude API a Groq API
- Modelo: `llama-3.2-90b-vision-preview`
- Misma funcionalidad: análisis de imágenes de comida
- 100% compatible con el código existente

### 3. **Backend - Configuración**

📄 `backend/src/config/env.ts`

- Cambiado `anthropicApiKey` → `groqApiKey`
- Actualizado para leer `GROQ_API_KEY`

### 4. **Backend - Variables de Entorno**

📄 `backend/.env.example` y `backend/.env`

- ❌ `ANTHROPIC_API_KEY` removida
- ✅ `GROQ_API_KEY` agregada
- Incluye link para obtener API key gratis: https://console.groq.com/

### 5. **Backend - Docker Compose**

📄 `backend/docker-compose.yml`

- Actualizado env var: `GROQ_API_KEY`
- **Puertos expuestos en todas las interfaces (0.0.0.0)** para acceso desde red local:
  - PostgreSQL: `0.0.0.0:5432:5432`
  - API: `0.0.0.0:3000:3000`
  - Nginx: `0.0.0.0:80:80` y `0.0.0.0:443:443`

---

## 📄 Archivos Nuevos Creados

### 1. **Guía de Pruebas Locales**

📄 `backend/LOCAL_TESTING.md`

- Guía completa paso a paso
- Cómo obtener API key de Groq (gratis)
- Cómo obtener la IP local de tu PC
- Configuración de firewall
- Configuración del frontend Android
- Solución de problemas comunes
- Checklist de pruebas

### 2. **Script para Obtener IP Local - Linux/Mac**

📄 `get-local-ip.sh`

- Detecta automáticamente tu IP local
- Verifica si el backend está corriendo
- Muestra instrucciones para configurar Android
- Muestra contenedores Docker activos

### 3. **Script para Obtener IP Local - Windows**

📄 `get-local-ip.bat`

- Versión para Windows del script anterior
- Misma funcionalidad adaptada a cmd/PowerShell

### 4. **Guía Rápida Windows**

📄 `QUICKSTART_WINDOWS.md`

- Guía específica para usuarios de Windows
- Paso a paso en menos de 15 minutos
- Capturas de pantalla sugeridas
- Solución de problemas comunes en Windows
- Comandos específicos para cmd

### 5. **README Principal Actualizado**

📄 `README.md`

- Actualizado para reflejar el uso de Groq
- Nueva sección de pruebas locales
- Links a toda la documentación
- Menciones de que Groq es 100% gratis

---

## 🚀 Cómo Empezar

### Paso 1: Obtener API Key de Groq (GRATIS)

```
1. Ve a: https://console.groq.com/
2. Crea una cuenta
3. Genera una API Key
4. Copia la key (empieza con gsk_...)
```

### Paso 2: Configurar Backend

```bash
cd backend
nano .env  # Pega tu GROQ_API_KEY
```

### Paso 3: Obtener tu IP Local

**Windows:**

```cmd
get-local-ip.bat
```

**Linux/Mac:**

```bash
chmod +x get-local-ip.sh
./get-local-ip.sh
```

### Paso 4: Iniciar Backend

```bash
cd backend
docker-compose up -d --build
```

### Paso 5: Configurar App Android

```kotlin
// En: frontend/app/src/main/.../ApiConfig.kt
private const val BASE_URL = "http://TU_IP_AQUI:3000/v1/"
// Ejemplo: "http://192.168.1.100:3000/v1/"
```

### Paso 6: Compilar e Instalar

```bash
cd frontend
./gradlew installDebug
```

---

## 📚 Documentación Actualizada

Toda la documentación ha sido actualizada:

1. **[QUICKSTART_WINDOWS.md](QUICKSTART_WINDOWS.md)** ⭐ **EMPIEZA AQUÍ** (Windows)
2. **[backend/LOCAL_TESTING.md](backend/LOCAL_TESTING.md)** ⭐ Guía completa de pruebas locales
3. [README.md](README.md) - Visión general del proyecto
4. [backend/README.md](backend/README.md) - Documentación del backend
5. [backend/API.md](backend/API.md) - Referencia de API
6. [backend/DEPLOYMENT.md](backend/DEPLOYMENT.md) - Despliegue en producción
7. [backend/ANDROID_INTEGRATION.md](backend/ANDROID_INTEGRATION.md) - Integración Android
8. [backend/TESTING.md](backend/TESTING.md) - Testing con cURL

---

## 🔧 Cambios Técnicos Detallados

### API de Groq vs Anthropic

**Antes (Anthropic):**

```typescript
import Anthropic from "@anthropic-ai/sdk";

const client = new Anthropic({
  apiKey: process.env.ANTHROPIC_API_KEY,
});

const message = await client.messages.create({
  model: "claude-3-5-sonnet-20241022",
  // ...
});
```

**Ahora (Groq):**

```typescript
import Groq from "groq-sdk";

const client = new Groq({
  apiKey: config.ai.groqApiKey,
});

const completion = await client.chat.completions.create({
  model: "llama-3.2-90b-vision-preview",
  // ...
});
```

### Configuración de Red

**Antes:**

```yaml
ports:
  - "3000:3000" # Solo accesible desde localhost
```

**Ahora:**

```yaml
ports:
  - "0.0.0.0:3000:3000" # Accesible desde la red local
```

---

## ✅ Ventajas de Groq

1. **100% Gratuito** - Sin costos, sin tarjeta de crédito
2. **Rápido** - Inferencia ultra-rápida
3. **Modelos Potentes** - LLaMA 3.2 90B Vision
4. **API Compatible** - Similar a OpenAI
5. **Generoso** - Rate limits altos para desarrollo

---

## 🧪 Verificación Rápida

Ejecuta estos comandos para verificar todo:

```bash
# 1. Verificar que Groq SDK está instalado
cd backend
npm list groq-sdk

# 2. Verificar configuración
cat .env | grep GROQ

# 3. Iniciar backend
docker-compose up -d

# 4. Verificar salud
curl http://localhost:3000/health

# 5. Ver logs
docker-compose logs -f api
```

---

## 🎯 Próximos Pasos

1. ✅ Obtén tu API key de Groq
2. ✅ Configura el `.env`
3. ✅ Obtén tu IP local
4. ✅ Inicia el backend
5. ✅ Configura la app Android
6. ✅ ¡Prueba en tu móvil!

Una vez que todo funcione localmente:

- Lee [backend/LOCAL_TESTING.md](backend/LOCAL_TESTING.md) para pruebas avanzadas
- Lee [backend/DEPLOYMENT.md](backend/DEPLOYMENT.md) para subir a producción

---

## 💡 Notas Importantes

- **Groq es gratis** pero tiene rate limits. Para producción con mucho tráfico, considera escalar.
- **HTTP en local** está bien, pero en producción **siempre usa HTTPS**.
- Tu **IP local puede cambiar**. Si dejas de conectar, verifica tu IP nuevamente.
- **No uses `localhost`** en Android - siempre usa la IP real de tu PC.

---

## 🆘 Solución de Problemas

### "Network request failed"

- Verifica que backend esté corriendo: `docker ps`
- Verifica la IP en `ApiConfig.kt`
- Prueba desde navegador del móvil: `http://TU_IP:3000/health`

### "Error al analizar imagen"

- Verifica que `GROQ_API_KEY` esté en `.env`
- Reinicia el backend: `docker-compose restart api`
- Revisa logs: `docker-compose logs api`

### Backend no inicia

- Verifica Docker Desktop esté corriendo
- Limpia y reconstruye: `docker-compose down -v && docker-compose up -d --build`

---

¡Todo listo para probar tu app con IA gratuita! 🚀
