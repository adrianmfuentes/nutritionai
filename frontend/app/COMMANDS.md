# 🛠️ Comandos Útiles

Referencia rápida de comandos para desarrollo y debugging.

## 🚀 Inicio Rápido

```bash
# Instalación completa
install.bat

# Obtener IP local
get-local-ip.bat

# Iniciar todo
cd backend && docker-compose up -d --build
```

## 🐳 Docker

### Gestión de Contenedores

```bash
# Iniciar servicios
docker-compose up -d

# Iniciar y reconstruir
docker-compose up -d --build

# Ver contenedores corriendo
docker ps

# Ver todos los contenedores
docker ps -a

# Detener servicios
docker-compose down

# Detener y eliminar volúmenes (BORRA DATOS)
docker-compose down -v

# Reiniciar un servicio específico
docker-compose restart api
docker-compose restart postgres
docker-compose restart nginx
```

### Logs

```bash
# Ver logs de todos los servicios
docker-compose logs

# Ver logs en tiempo real
docker-compose logs -f

# Ver logs de un servicio específico
docker-compose logs api
docker-compose logs -f api
docker-compose logs postgres

# Ver últimas 100 líneas
docker-compose logs --tail=100 api

# Ver logs con timestamps
docker-compose logs -t api
```

### Debugging

```bash
# Entrar al contenedor de la API
docker exec -it nutrition_api sh

# Entrar a PostgreSQL
docker exec -it nutrition_db psql -U nutrition_user -d nutrition_ai

# Ver uso de recursos
docker stats

# Inspeccionar contenedor
docker inspect nutrition_api

# Ver redes
docker network ls
docker network inspect nutrition_network
```

## 📦 NPM (Backend)

```bash
cd backend

# Instalar dependencias
npm install

# Instalar una dependencia específica
npm install groq-sdk

# Modo desarrollo (hot reload)
npm run dev

# Compilar TypeScript
npm run build

# Ejecutar tests
npm test

# Linting
npm run lint

# Ver dependencias instaladas
npm list

# Ver dependencia específica
npm list groq-sdk
```

## 🗄️ Base de Datos

### Desde Docker

```bash
# Conectar a PostgreSQL
docker exec -it nutrition_db psql -U nutrition_user -d nutrition_ai

# Backup de la base de datos
docker exec nutrition_db pg_dump -U nutrition_user nutrition_ai > backup.sql

# Restaurar backup
docker exec -i nutrition_db psql -U nutrition_user -d nutrition_ai < backup.sql
```

### Queries SQL Útiles

```sql
-- Ver todas las tablas
\dt

-- Ver estructura de una tabla
\d users
\d meals
\d detected_foods

-- Contar usuarios
SELECT COUNT(*) FROM users;

-- Ver últimas comidas
SELECT * FROM meals ORDER BY created_at DESC LIMIT 10;

-- Ver resumen de hoy
SELECT * FROM daily_nutrition_summary
WHERE user_id = 'USER_ID'
  AND date = CURRENT_DATE;

-- Limpiar todas las comidas de prueba
DELETE FROM meals WHERE user_id = 'USER_ID';

-- Salir
\q
```

## 🌐 Testing API

### cURL

```bash
# Health check
curl http://localhost:3000/health

# Registro
curl -X POST http://localhost:3000/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@ejemplo.com",
    "password": "test1234",
    "name": "Test User"
  }'

# Login
curl -X POST http://localhost:3000/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@ejemplo.com",
    "password": "test1234"
  }'

# Obtener perfil (necesitas el token)
curl http://localhost:3000/v1/profile \
  -H "Authorization: Bearer TU_TOKEN_AQUI"

# Analizar imagen
curl -X POST http://localhost:3000/v1/meals/analyze \
  -H "Authorization: Bearer TU_TOKEN_AQUI" \
  -F "image=@ruta/a/tu/imagen.jpg"
```

### Desde el Móvil

```bash
# Verificar conectividad desde navegador del móvil
http://TU_IP:3000/health

# Ejemplo con IP real
http://192.168.1.100:3000/health
```

## 🔍 Debugging

### Ver Variables de Entorno

```bash
# En el contenedor
docker exec nutrition_api env | grep GROQ

# Verificar .env
cat backend/.env
```

### Verificar Puertos

```bash
# Windows
netstat -an | findstr 3000
netstat -an | findstr 5432

# Linux/Mac
lsof -i :3000
lsof -i :5432
```

### Verificar Firewall

```bash
# Windows - Ver reglas
netsh advfirewall firewall show rule name=all | findstr 3000

# Añadir regla temporal
netsh advfirewall set allprofiles state off  # CUIDADO: Desactiva firewall
```

### Red Local

```bash
# Ver tu IP
ipconfig  # Windows
ip addr show  # Linux
ifconfig  # Mac

# Ping a tu PC desde el móvil
ping TU_IP

# Ver si el puerto está abierto
telnet TU_IP 3000
```

## 🧹 Limpieza

### Docker

```bash
# Limpiar contenedores detenidos
docker container prune -f

# Limpiar imágenes sin usar
docker image prune -a -f

# Limpiar volúmenes sin usar
docker volume prune -f

# Limpiar todo (CUIDADO)
docker system prune -a -f --volumes
```

### Node Modules

```bash
cd backend

# Eliminar node_modules y reinstalar
rm -rf node_modules package-lock.json
npm install

# Windows
rmdir /s /q node_modules
del package-lock.json
npm install
```

### Logs

```bash
cd backend

# Limpiar logs
rm -rf logs/*

# Windows
del /q logs\*
```

## 📱 Android

### Gradle

```bash
cd frontend

# Compilar
./gradlew build

# Instalar en dispositivo
./gradlew installDebug

# Limpiar build
./gradlew clean

# Ver tareas disponibles
./gradlew tasks
```

### ADB

```bash
# Ver dispositivos conectados
adb devices

# Instalar APK
adb install app/build/outputs/apk/debug/app-debug.apk

# Ver logs de la app
adb logcat | grep -i nutrition

# Desinstalar app
adb uninstall com.tu.paquete

# Ver almacenamiento
adb shell ls /data/data/com.tu.paquete
```

## 🔄 Workflow Completo

### Primera Instalación

```bash
# 1. Clonar repo (si aplica)
git clone <repo-url>
cd nutrition-app

# 2. Instalar dependencias
install.bat

# 3. Configurar .env
nano backend/.env

# 4. Iniciar backend
cd backend
docker-compose up -d --build

# 5. Obtener IP
cd ..
get-local-ip.bat

# 6. Configurar Android
# Editar ApiConfig.kt con la IP

# 7. Compilar app
cd frontend
./gradlew installDebug
```

### Desarrollo Diario

```bash
# Iniciar backend
cd backend
docker-compose up -d

# Ver logs
docker-compose logs -f api

# Hacer cambios en código...

# Reiniciar solo API
docker-compose restart api

# O reconstruir si cambios grandes
docker-compose up -d --build api
```

### Debugging de Problemas

```bash
# 1. Verificar servicios
docker ps

# 2. Ver logs
docker-compose logs api

# 3. Verificar salud
curl http://localhost:3000/health

# 4. Verificar desde móvil
# http://TU_IP:3000/health en navegador

# 5. Ver variables de entorno
docker exec nutrition_api env

# 6. Entrar al contenedor
docker exec -it nutrition_api sh
# > cat .env
# > ls -la
# > exit

# 7. Reiniciar todo
docker-compose down
docker-compose up -d --build
```

## 📊 Monitoreo

```bash
# Ver uso de CPU/RAM
docker stats

# Ver logs específicos
docker-compose logs --tail=50 -f api

# Ver conexiones de red
docker network inspect nutrition_network

# Ver volúmenes
docker volume ls
docker volume inspect nutrition_backend_postgres_data
```

## 🚀 Deployment

```bash
# Build optimizado para producción
docker-compose -f docker-compose.prod.yml build

# Subir a producción
# Ver DEPLOYMENT.md
```

---

**💡 Tip:** Guarda estos comandos en tu favoritos para acceso rápido.
