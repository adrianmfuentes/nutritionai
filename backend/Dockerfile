# ============================================
# ETAPA 1: Builder
# ============================================
FROM node:20-alpine AS builder

# Instalar dependencias necesarias para compilar módulos nativos en Alpine/ARM
RUN apk add --no-cache libc6-compat python3 make g++

WORKDIR /app

# Habilitar corepack
RUN corepack enable && corepack prepare pnpm@latest --activate

# Copiar archivos de definición de dependencias
COPY package.json pnpm-lock.yaml ./

# Instalamos todas las dependencias
RUN pnpm install --prefer-offline

COPY . .

RUN pnpm run build

# ============================================
# ETAPA 2: Production
# ============================================
FROM node:20-alpine AS runner

WORKDIR /app

# Instalar curl para el healthcheck y libc6-compat para compatibilidad ARM
RUN apk add --no-cache curl libc6-compat

# Habilitar pnpm en la etapa de producción
RUN corepack enable && corepack prepare pnpm@latest --activate

# Configurar entorno de producción
ENV NODE_ENV=production

# Copiar archivos de dependencias
COPY package.json pnpm-lock.yaml ./

# Instalar dependencias de producción
RUN pnpm install --prod --prefer-offline && \
    pnpm store prune

# Copiar el build desde la etapa anterior
COPY --from=builder /app/dist ./dist

# Crear carpetas necesarias y gestionar permisos para el usuario 'node'
RUN mkdir -p /app/uploads /app/logs && \
    chown -R node:node /app && \
    rm -rf /root/.npm /root/.pnpm-store

# Usar el usuario no-privilegiado que viene por defecto en la imagen de Node
USER node

EXPOSE 3000

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:3000/health || exit 1

# Ejecutar la aplicación
CMD ["node", "dist/index.js"]