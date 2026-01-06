@echo off
echo.
echo ============================================
echo   INSTALACION - Nutrition AI con Groq
echo ============================================
echo.
echo Este script instalara las dependencias
echo y verificara la configuracion.
echo.
pause

echo.
echo [1/5] Verificando Docker...
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Docker no esta instalado
    echo.
    echo Por favor instala Docker Desktop desde:
    echo https://www.docker.com/products/docker-desktop
    pause
    exit /b 1
)
echo ✅ Docker instalado

echo.
echo [2/5] Instalando dependencias del backend...
cd backend
if not exist "node_modules\" (
    npm install
    if %errorlevel% neq 0 (
        echo ❌ Error al instalar dependencias
        pause
        exit /b 1
    )
)
echo ✅ Dependencias instaladas

echo.
echo [3/5] Verificando archivo .env...
if not exist ".env" (
    echo ⚠️  Archivo .env no encontrado
    echo Copiando .env.example a .env...
    copy .env.example .env >nul
    echo.
    echo 📝 IMPORTANTE: Necesitas editar backend\.env
    echo    y agregar tu GROQ_API_KEY
    echo.
    echo    Obten una key gratis en: https://console.groq.com/
    echo.
) else (
    findstr /C:"GROQ_API_KEY=your_groq_api_key_here" .env >nul
    if %errorlevel% equ 0 (
        echo ⚠️  ADVERTENCIA: GROQ_API_KEY aun no esta configurada
        echo.
        echo    Edita backend\.env y cambia:
        echo    GROQ_API_KEY=gsk_tu_key_real_aqui
        echo.
        echo    Obten una key gratis en: https://console.groq.com/
        echo.
    ) else (
        echo ✅ Archivo .env configurado
    )
)

echo.
echo [4/5] Verificando estructura de directorios...
if not exist "uploads\" mkdir uploads
if not exist "logs\" mkdir logs
echo ✅ Directorios creados

cd ..

echo.
echo [5/5] Obteniendo tu IP local...
for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr /c:"IPv4" ^| findstr /v "127.0.0.1"') do (
    set IP=%%a
    goto :found
)

:found
set IP=%IP:~1%

echo.
echo ============================================
echo   ✅ INSTALACION COMPLETADA
echo ============================================
echo.
echo 📋 PROXIMOS PASOS:
echo.
echo 1. Obtén tu API key de Groq (GRATIS):
echo    https://console.groq.com/
echo.
echo 2. Edita: backend\.env
echo    Cambia GROQ_API_KEY=gsk_tu_key_real_aqui
echo.
echo 3. Inicia el backend:
echo    cd backend
echo    docker-compose up -d --build
echo.
echo 4. Configura la app Android con tu IP:
if not "%IP%"=="" (
    echo    Tu IP local es: %IP%
    echo    En ApiConfig.kt usa: http://%IP%:3000/v1/
) else (
    echo    Ejecuta: get-local-ip.bat
)
echo.
echo 📖 Guía completa: QUICKSTART_WINDOWS.md
echo.
pause
