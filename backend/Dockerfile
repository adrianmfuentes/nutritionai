# Build stage
FROM node:20-alpine AS builder

WORKDIR /app
RUN npm install -g pnpm

# Instalar dependencias
COPY package.json pnpm-lock.yaml ./
RUN pnpm install --frozen-lockfile --prefer-offline

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
RUN npm install -g pnpm && pnpm store prune

# Copiar dependencias y build
COPY package.json pnpm-lock.yaml ./
RUN pnpm install --prod --frozen-lockfile --prefer-offline --omit=dev && pnpm store prune && rm -rf /root/.npm /root/.pnpm-store

COPY --from=builder /app/dist ./dist

RUN mkdir -p /app/uploads /app/logs && \
    chown -R node:node /app && \
    rm -rf /app/.git /app/test /app/tests /app/.github

# Usar usuario no-root
USER node

EXPOSE 3000

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD node -e "require('http').get('http://localhost:3000/health', (r) => {process.exit(r.statusCode === 200 ? 0 : 1)})"

CMD ["node", "dist/index.js"]
