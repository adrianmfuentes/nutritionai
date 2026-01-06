# Build stage
FROM node:20-alpine AS builder

WORKDIR /app
RUN npm install -g pnpm

# Instalar dependencias
COPY package.json pnpm-lock.yaml ./
RUN pnpm install --frozen-lockfile

# Copiar código fuente
COPY . .

# Build TypeScript
RUN pnpm run build

# ============================================
# Production Stage
# ============================================
FROM node:20-alpine

WORKDIR /app

# Instalar curl para healthcheck
RUN apk add --no-cache curl
RUN npm install -g pnpm

# Copiar dependencias y build
COPY package.json pnpm-lock.yaml ./
RUN pnpm install --prod --frozen-lockfile

COPY --from=builder /app/dist ./dist

# Crear directorio para uploads
RUN mkdir -p /app/uploads /app/logs && \
    chown -R node:node /app

# Usar usuario no-root
USER node

EXPOSE 3000

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD node -e "require('http').get('http://localhost:3000/health', (r) => {process.exit(r.statusCode === 200 ? 0 : 1)})"

CMD ["node", "dist/index.js"]
