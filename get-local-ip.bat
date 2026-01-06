@echo off
REM Script para obtener la IP local y verificar el backend en Windows

echo.
echo 🔍 Detectando IP local para pruebas desde móvil...
echo.

for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr /c:"IPv4" ^| findstr /v "127.0.0.1"') do (
    set IP=%%a
    goto :found
)

:found
set IP=%IP:~1%

if "%IP%"=="" (
    echo ❌ No se pudo detectar la IP automáticamente
    echo.
    echo Ejecuta: ipconfig
    echo Busca "Adaptador de LAN inalámbrica Wi-Fi"
    echo Y anota la "Dirección IPv4"
    pause
    exit /b 1
)

echo ✅ Tu IP local es: %IP%
echo.
echo 📱 Configuración para Android:
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
echo Edita: frontend/app/src/main/java/.../ApiConfig.kt
echo.
echo Cambia BASE_URL a:
echo   private const val BASE_URL = "http://%IP%:3000/v1/"
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
echo.

REM Verificar si el backend está corriendo
echo 🔍 Verificando backend...
curl -s http://localhost:3000/health >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Backend está corriendo en http://localhost:3000
    echo.
    echo 📱 Prueba desde tu móvil:
    echo   Abre el navegador y visita: http://%IP%:3000/health
    echo.
) else (
    echo ❌ Backend NO está corriendo
    echo.
    echo Para iniciar el backend:
    echo   cd backend
    echo   docker-compose up -d
    echo.
)

REM Mostrar contenedores de Docker
echo 🐳 Contenedores Docker:
docker ps --filter name=nutrition --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" 2>nul
if %errorlevel% neq 0 (
    echo Docker no está corriendo o no hay contenedores
)
echo.

REM Instrucciones finales
echo 📋 Checklist de pruebas locales:
echo   1. ✓ Obtén una API key de Groq: https://console.groq.com/
echo   2. ✓ Edita backend\.env con tu GROQ_API_KEY
echo   3. ✓ Inicia backend: cd backend ^&^& docker-compose up -d
echo   4. ✓ Actualiza ApiConfig.kt con la IP: %IP%
echo   5. ✓ Compila la app: cd frontend ^&^& gradlew installDebug
echo   6. ✓ Prueba el registro y análisis de comida
echo.
echo 📖 Guía completa: backend\LOCAL_TESTING.md
echo.
pause
