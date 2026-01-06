#!/bin/bash
# deploy.sh - Script de deployment para Oracle Ampere

set -e

echo "🚀 Iniciando deployment de Nutrition AI Backend..."

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Verificar que estamos en el directorio correcto
if [ ! -f "docker-compose.yml" ]; then
    echo -e "${RED}❌ Error: docker-compose.yml no encontrado${NC}"
    echo "Por favor ejecuta este script desde el directorio backend/"
    exit 1
fi

# Verificar que existe .env
if [ ! -f ".env" ]; then
    echo -e "${YELLOW}⚠️  Archivo .env no encontrado${NC}"
    echo "Creando desde .env.example..."
    cp .env.example .env
    echo -e "${YELLOW}⚠️  Por favor edita .env con tus credenciales antes de continuar${NC}"
    exit 1
fi

# Verificar Docker
if ! command -v docker &> /dev/null; then
    echo -e "${RED}❌ Docker no está instalado${NC}"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo -e "${RED}❌ Docker Compose no está instalado${NC}"
    exit 1
fi

# Hacer backup de la base de datos si existe
if docker ps | grep -q nutrition_db; then
    echo -e "${YELLOW}📦 Haciendo backup de base de datos...${NC}"
    docker-compose exec -T postgres pg_dump -U ${DB_USER:-nutrition_user} ${DB_NAME:-nutrition_ai} > backup_$(date +%Y%m%d_%H%M%S).sql
    echo -e "${GREEN}✅ Backup completado${NC}"
fi

# Detener servicios existentes
echo "🛑 Deteniendo servicios existentes..."
docker-compose down

# Limpiar imágenes antiguas (opcional)
echo "🧹 Limpiando imágenes antiguas..."
docker image prune -f

# Construir imágenes
echo "🔨 Construyendo imágenes..."
docker-compose build --no-cache

# Levantar servicios
echo "🚀 Levantando servicios..."
docker-compose up -d

# Esperar a que los servicios estén listos
echo "⏳ Esperando a que los servicios estén listos..."
sleep 10

# Verificar salud de los servicios
echo "🏥 Verificando salud de los servicios..."

# Verificar PostgreSQL
if docker-compose ps postgres | grep -q "Up"; then
    echo -e "${GREEN}✅ PostgreSQL está corriendo${NC}"
else
    echo -e "${RED}❌ PostgreSQL no está corriendo${NC}"
    docker-compose logs postgres
    exit 1
fi

# Verificar API
if docker-compose ps api | grep -q "Up"; then
    echo -e "${GREEN}✅ API está corriendo${NC}"
else
    echo -e "${RED}❌ API no está corriendo${NC}"
    docker-compose logs api
    exit 1
fi

# Verificar Nginx
if docker-compose ps nginx | grep -q "Up"; then
    echo -e "${GREEN}✅ Nginx está corriendo${NC}"
else
    echo -e "${RED}❌ Nginx no está corriendo${NC}"
    docker-compose logs nginx
    exit 1
fi

# Test de health endpoint
echo "🔍 Verificando health endpoint..."
sleep 5
if curl -f http://localhost:3000/health > /dev/null 2>&1; then
    echo -e "${GREEN}✅ Health check exitoso${NC}"
else
    echo -e "${RED}❌ Health check falló${NC}"
    docker-compose logs api
    exit 1
fi

# Mostrar información
echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}✨ Deployment completado exitosamente ✨${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "📊 Estado de servicios:"
docker-compose ps
echo ""
echo "🔗 Endpoints disponibles:"
echo "   - Health: http://localhost:3000/health"
echo "   - API: http://localhost:3000/v1/"
echo "   - Nginx: http://localhost/"
echo ""
echo "📝 Ver logs:"
echo "   docker-compose logs -f api"
echo "   docker-compose logs -f postgres"
echo "   docker-compose logs -f nginx"
echo ""
echo -e "${YELLOW}⚠️  Recuerda configurar SSL/TLS para producción${NC}"
echo ""
