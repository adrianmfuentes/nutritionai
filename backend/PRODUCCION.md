# Guía de Despliegue a Producción (Oracle Cloud Ampere / Ubuntu / Portainer)

Esta guía detalla los pasos para desplegar el backend de Nutrition App en un servidor de producción.

## 1. Requisitos Previos

- Servidor Oracle Ampere (ARM64) con Ubuntu 22.04 o superior.
- **Docker** y **Docker Compose** instalados.
- **Portainer** instalado (opcional, pero facilita la gestión).
- Dominio configurado (DNS A record) apuntando a la IP de tu servidor.
- Puertos abiertos en Oracle Cloud Security List y en el firewall de Ubuntu (`ufw`):
  - 80 (HTTP)
  - 443 (HTTPS)
  - 3000 (Opcional, si das acceso directo a API, pero mejor cerrado)

## 2. Preparación de Archivos

Sube la carpeta `backend/` completa a tu servidor (puedes usar Git o SCP).

```bash
git clone https://github.com/tu-usuario/nutrition-app.git
cd nutrition-app/backend
```

## 3. Configuración

### 3.1 Variables de Entorno (.env)

Crea un archivo `.env` basado en `.env.example`.

```bash
cp .env.example .env
nano .env
```

Asegúrate de establecer credenciales seguras para `DB_PASSWORD` y `JWT_SECRET`. En producción, `NODE_ENV` debe ser `production`.

### 3.2 Configuración de Nginx

Edita `nginx/nginx.conf` y reemplaza `tu-dominio.com` por tu dominio real.

```bash
nano nginx/nginx.conf
```

Busca las líneas:

```nginx
server_name tu-dominio.com www.tu-dominio.com;
...
ssl_certificate /etc/letsencrypt/live/tu-dominio.com/fullchain.pem;
ssl_certificate_key /etc/letsencrypt/live/tu-dominio.com/privkey.pem;
```

### 3.3 Script de Certificados SSL

Edita `init-letsencrypt.sh` para poner tu dominio y tu email.

```bash
nano init-letsencrypt.sh
```

Modifica:

```bash
domains=(tu-dominio.com www.tu-dominio.com)
email="tu-email@ejemplo.com"
```

## 4. Generación de Certificados (Solo primera vez)

Para que Nginx arranque correctamente con SSL, necesitamos generar los certificados. Hemos preparado un script que automatiza el proceso (crea certificados dummy, arranca nginx, solicita certificados reales con Certbot).

```bash
chmod +x init-letsencrypt.sh
./init-letsencrypt.sh
```

Si todo sale bien, verás un mensaje de éxito y Nginx se recargará.

## 5. Despliegue con Docker Compose (Sin Portainer)

Para levantar todo el stack en producción:

```bash
docker-compose -f docker-compose.prod.yml up -d --build
```

## 6. Despliegue con Portainer

Si prefieres usar Portainer:

1.  Asegúrate de haber generado los certificados primero en el servidor (Paso 4), ya que Portainer necesitará que los volúmenes de certificados existan.
2.  Crea un nuevo **Stack**.
3.  Copia el contenido de `docker-compose.prod.yml`.
4.  Define las variables de entorno en la sección "Environment variables" de Portainer (copiando el contenido de tu `.env`).
5.  **Importante**: Debido a que Portainer a veces maneja las rutas relativas (`./nginx/...`) de forma distinta dependiendo de cómo se despliegue (Git vs Upload), se recomienda usar **Rutas Absolutas** en el docker-compose si tienes problemas, o asegurarte de que el stack se despliega desde el directorio correcto.
    - _Recomendación_: Usa la opción "Repository" en Portainer apuntando a tu Git, y configura "Compose path" a `backend/docker-compose.prod.yml`.
    - Nota: Si usas Git en Portainer, el paso 4 (init-letsencrypt) es más complejo porque los volúmenes son efímeros si no se configuran como bind mounts persistentes en el host.
    - **Estrategia recomendada**: Ejecuta el paso 4 manualmente en la consola del servidor para poblar `./nginx/certbot`. Luego, en Portainer, asegúrate de que el stack monte esos directorios locales.

## 7. Mantenimiento

- **Logs**: `docker-compose -f docker-compose.prod.yml logs -f`
- **Backups**: El script `deploy.sh` incluye comandos de backup para la base de datos.
- **Actualizaciones**:
  1.  `git pull`
  2.  `docker-compose -f docker-compose.prod.yml build`
  3.  `docker-compose -f docker-compose.prod.yml up -d`
