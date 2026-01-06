# Guía de Deployment en Oracle Ampere A1

## Requisitos

- Oracle Cloud Account
- Instancia Oracle Ampere A1 (ARM64)
- Ubuntu 22.04 LTS o superior
- Dominio configurado (opcional pero recomendado)

## Paso 1: Configurar Instancia Oracle Cloud

### 1.1 Crear Instancia

1. Accede a Oracle Cloud Console
2. Compute → Instances → Create Instance
3. Selecciona:
   - Shape: VM.Standard.A1.Flex (2-4 OCPUs, 12-24GB RAM)
   - OS: Ubuntu 22.04 ARM64
   - Boot Volume: 50GB mínimo

### 1.2 Configurar Firewall

```bash
# En Oracle Cloud Console:
# Networking → Virtual Cloud Networks → Security Lists

# Abrir puertos:
# - 22 (SSH)
# - 80 (HTTP)
# - 443 (HTTPS)
# - 3000 (API - opcional, solo para testing)
# - 5432 (PostgreSQL - solo si necesitas acceso externo)
```

### 1.3 Configurar UFW en la instancia

```bash
sudo ufw allow 22
sudo ufw allow 80
sudo ufw allow 443
sudo ufw enable
```

## Paso 2: Instalar Docker

```bash
# Actualizar sistema
sudo apt update && sudo apt upgrade -y

# Instalar Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Añadir usuario al grupo docker
sudo usermod -aG docker $USER

# Instalar Docker Compose
sudo apt install docker-compose -y

# Verificar instalación
docker --version
docker-compose --version
```

## Paso 3: Clonar y Configurar Proyecto

```bash
# Crear directorio para el proyecto
mkdir -p ~/apps
cd ~/apps

# Clonar repositorio
git clone <tu-repositorio> nutrition-app
cd nutrition-app/backend

# Copiar y editar variables de entorno
cp .env.example .env
nano .env
```

### Configuración de .env

```env
NODE_ENV=production
PORT=3000

# Database - USA CONTRASEÑAS SEGURAS
DB_HOST=postgres
DB_PORT=5432
DB_NAME=nutrition_ai
DB_USER=nutrition_user
DB_PASSWORD=TU_PASSWORD_SUPER_SEGURO_AQUI_12345

# Authentication - GENERA UN SECRET SEGURO
JWT_SECRET=TU_JWT_SECRET_SUPER_SEGURO_AQUI_67890
JWT_EXPIRES_IN=7d

# AI Service
ANTHROPIC_API_KEY=sk-ant-tu-api-key-de-anthropic-aqui

# Storage
UPLOAD_PATH=/app/uploads
MAX_FILE_SIZE=10485760

# Rate Limiting
RATE_LIMIT_WINDOW_MS=900000
RATE_LIMIT_MAX_REQUESTS=100

# CORS - Cambia por tu dominio del frontend
CORS_ORIGIN=https://tu-app-frontend.com

# Logging
LOG_LEVEL=info
LOG_PATH=/app/logs
```

## Paso 4: Configurar SSL/TLS

### Opción A: Let's Encrypt (Recomendado para producción)

```bash
# Instalar Certbot
sudo apt install certbot -y

# Detener Nginx si está corriendo
docker-compose stop nginx

# Obtener certificado
sudo certbot certonly --standalone -d tu-dominio.com -d www.tu-dominio.com

# Copiar certificados
sudo cp /etc/letsencrypt/live/tu-dominio.com/fullchain.pem nginx/ssl/cert.pem
sudo cp /etc/letsencrypt/live/tu-dominio.com/privkey.pem nginx/ssl/key.pem
sudo chown $USER:$USER nginx/ssl/*.pem

# Configurar renovación automática
sudo crontab -e
# Añadir: 0 0 1 * * certbot renew --quiet && docker-compose restart nginx
```

### Opción B: Self-Signed (Solo desarrollo)

```bash
chmod +x generate-ssl.sh
./generate-ssl.sh
```

## Paso 5: Actualizar Configuración Nginx

```bash
nano nginx/nginx.conf

# Actualizar server_name con tu dominio
server_name tu-dominio.com www.tu-dominio.com;
```

## Paso 6: Deploy

```bash
# Hacer ejecutable el script de deploy
chmod +x deploy.sh

# Ejecutar deployment
./deploy.sh
```

## Paso 7: Verificar Deployment

```bash
# Ver logs
docker-compose logs -f api

# Verificar servicios
docker-compose ps

# Test de health endpoint
curl http://localhost:3000/health

# Test desde exterior (reemplaza tu-dominio.com)
curl https://tu-dominio.com/health
```

## Paso 8: Configurar Backups Automáticos

```bash
# Crear script de backup
nano ~/backup-db.sh
```

```bash
#!/bin/bash
BACKUP_DIR=~/backups
mkdir -p $BACKUP_DIR
DATE=$(date +%Y%m%d_%H%M%S)

cd ~/apps/nutrition-app/backend
docker-compose exec -T postgres pg_dump -U nutrition_user nutrition_ai > $BACKUP_DIR/nutrition_ai_$DATE.sql

# Mantener solo últimos 7 días
find $BACKUP_DIR -name "*.sql" -mtime +7 -delete
```

```bash
# Hacer ejecutable
chmod +x ~/backup-db.sh

# Configurar cron para backup diario a las 2 AM
crontab -e
# Añadir: 0 2 * * * /home/ubuntu/backup-db.sh
```

## Paso 9: Monitoreo

### Ver logs en tiempo real

```bash
# Logs de API
docker-compose logs -f api

# Logs de PostgreSQL
docker-compose logs -f postgres

# Logs de Nginx
docker-compose logs -f nginx

# Logs del sistema
tail -f backend/logs/combined.log
```

### Monitorear recursos

```bash
# CPU y memoria
docker stats

# Espacio en disco
df -h

# Servicios activos
docker-compose ps
```

## Paso 10: Mantenimiento

### Actualizar aplicación

```bash
cd ~/apps/nutrition-app/backend

# Hacer backup antes de actualizar
./backup-db.sh

# Pull cambios
git pull

# Rebuild y restart
docker-compose down
docker-compose up -d --build
```

### Limpiar recursos

```bash
# Limpiar imágenes no usadas
docker image prune -a -f

# Limpiar volúmenes no usados
docker volume prune -f

# Limpiar todo (CUIDADO: elimina datos)
docker system prune -a --volumes -f
```

## Troubleshooting

### Puerto 80/443 bloqueado

```bash
# Verificar reglas iptables
sudo iptables -L

# Limpiar si es necesario
sudo iptables -F
```

### Error de conexión a base de datos

```bash
# Verificar que PostgreSQL está corriendo
docker-compose ps postgres

# Ver logs
docker-compose logs postgres

# Reiniciar servicio
docker-compose restart postgres
```

### Error de permisos en uploads

```bash
# Corregir permisos
sudo chown -R 1000:1000 uploads/
chmod -R 755 uploads/
```

### Memoria insuficiente

```bash
# Verificar uso de memoria
free -h

# Ver procesos
top

# Configurar swap si es necesario
sudo fallocate -l 4G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
```

## Seguridad Adicional

### Configurar fail2ban

```bash
sudo apt install fail2ban -y
sudo systemctl enable fail2ban
sudo systemctl start fail2ban
```

### Actualizar sistema regularmente

```bash
# Configurar actualizaciones automáticas
sudo apt install unattended-upgrades -y
sudo dpkg-reconfigure --priority=low unattended-upgrades
```

### Cambiar puerto SSH (opcional)

```bash
sudo nano /etc/ssh/sshd_config
# Cambiar Port 22 a Port 2222
sudo systemctl restart sshd

# No olvides actualizar firewall
sudo ufw allow 2222
```

## Notas Finales

1. ✅ Siempre usa contraseñas seguras
2. ✅ Configura backups automáticos
3. ✅ Usa HTTPS en producción
4. ✅ Monitorea logs regularmente
5. ✅ Mantén el sistema actualizado
6. ✅ Configura alertas para errores críticos
7. ✅ Documenta cualquier cambio en la configuración

## Soporte

Para problemas o preguntas, revisa:

- Logs: `docker-compose logs`
- Documentación API: `API.md`
- README: `README.md`
