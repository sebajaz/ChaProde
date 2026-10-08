# ==========================================
# 1. ETAPA DE CONSTRUCCIÓN (BUILDER)
# ==========================================
FROM node:22-alpine AS builder

WORKDIR /app

# Copiar manifiestos e instalar dependencias
COPY package.json package-lock.json ./
RUN npm ci

# Copiar configuración y código fuente
COPY index.html vite.config.ts tailwind.config.js postcss.config.js tsconfig.json tsconfig.node.json ./
COPY src/ src/
COPY public/ public/

# Compilación de producción
RUN npm run build

# ==========================================
# 2. ETAPA DE EJECUCIÓN (NGINX RUNTIME)
# ==========================================
FROM nginx:1.27-alpine AS runner

# Configuración personalizada de Nginx con proxy inverso
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copiar archivos estáticos compilados de React
COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 80

HEALTHCHECK --interval=15s --timeout=5s --start-period=5s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost/ || exit 1

CMD ["nginx", "-g", "daemon off;"]
