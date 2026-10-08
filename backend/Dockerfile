# ==========================================
# 1. ETAPA DE CONSTRUCCIÓN (BUILDER)
# ==========================================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Instalar bash para compatibilidad con gradlew
RUN apk add --no-cache bash

# Copiar archivos de configuración de Gradle para caché
COPY gradle/ gradle/
COPY gradlew build.gradle.kts settings.gradle.kts gradle.properties ./
RUN chmod +x gradlew

# Pre-descargar dependencias
RUN ./gradlew dependencies --no-daemon || true

# Copiar código fuente y recursos
COPY src/ src/

# Generar la distribución de producción
RUN ./gradlew installDist --no-daemon -x test

# ==========================================
# 2. ETAPA DE EJECUCIÓN (RUNTIME)
# ==========================================
FROM eclipse-temurin:21-jre-alpine AS runner

WORKDIR /app

# Instalar curl para healthchecks
RUN apk add --no-cache curl

# Crear usuario no privilegiado
RUN addgroup -S chaprode && adduser -S chaprode -G chaprode

# Copiar aplicación construida
COPY --from=builder --chown=chaprode:chaprode /app/build/install/chaprode-backend ./

USER chaprode

ENV PORT=8080

EXPOSE 8080

HEALTHCHECK --interval=10s --timeout=5s --start-period=20s --retries=5 \
  CMD wget -qO- http://localhost:8080/health || exit 1

ENTRYPOINT ["./bin/chaprode-backend"]
