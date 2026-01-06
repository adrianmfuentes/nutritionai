#!/bin/bash
# generate-ssl.sh - Generar certificados SSL self-signed para desarrollo

echo "🔐 Generando certificados SSL self-signed..."

# Crear directorio si no existe
mkdir -p nginx/ssl

# Generar certificado self-signed
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout nginx/ssl/key.pem \
  -out nginx/ssl/cert.pem \
  -subj "/C=ES/ST=State/L=City/O=Organization/CN=localhost"

echo "✅ Certificados generados en nginx/ssl/"
echo ""
echo "⚠️  IMPORTANTE: Estos son certificados self-signed solo para desarrollo."
echo "   Para producción, usa Let's Encrypt o un certificado válido."
echo ""
echo "Para Let's Encrypt, ejecuta:"
echo "  certbot certonly --webroot -w /var/www/certbot -d tu-dominio.com"
