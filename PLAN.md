# 🏆 ChaProde — Plan de Implementación Integral

## 📌 1. Resumen Ejecutivo y Arquitectura

**ChaProde** es una plataforma Full-Stack de pronósticos deportivos ("prode") diseñada para ser modular, escalable y mantenible.

### 🛠️ Stack Tecnológico
* **Backend:** Kotlin 2.1+ con **Ktor** (motor Netty, Coroutines asíncronas).
* **Persistencia:** PostgreSQL 18 + **Exposed ORM** + **HikariCP** (Connection Pool).
* **Seguridad:** JWT (JSON Web Tokens) con hashing de contraseñas vía **BCrypt**.
* **Estrategia de Datos:** **Híbrida (Provider Pattern)** con:
  * Sincronización en vivo vía **`football-data.org`** (API REST externa).
  * Dataset local de respaldo (**JSON/SQL Seeds**) para funcionamiento 100% offline y testing.
  * Carga y sobreescritura manual desde el Panel de Administración.
* **Frontend Móvil:** **Android Nativo con Jetpack Compose** + Material 3 + Coil (imágenes/escudos) + DataStore.
* **Panel Web Admin:** **React 18 + TypeScript + Vite + TailwindCSS**.

---

## 🎯 2. Desglose de Fases de Implementación

* **Fase 0:** Diseño, Arquitectura y Preparación del Entorno (Base de datos PostgreSQL, Backend Kotlin con Gradle, Health Check).
* **Fase 1:** MVP de Usuarios (Registro, Login JWT, Roles, almacenamiento seguro en móvil).
* **Fase 2:** Torneos, Equipos y Fixture (Estrategia híbrida de datos, visualización en móvil).
* **Fase 3:** Pronósticos y Regla de Bloqueo 15 Minutos (Validación estricta en servidor).
* **Fase 4:** Resultados y Motor de Puntuación (Liquidación automática de 3, 1 y 0 puntos).
* **Fase 5:** Ranking Global (Tabla de posiciones acumulada con podio visual).
* **Fase 6:** Ligas Privadas (Código de acceso único de 6 caracteres y ranking privado).
* **Fase 7:** Panel Web Administrativo Completo (Backoffice en React).
* **Fase 8:** Seguridad, RBAC y Endurecimiento.
* **Fase 9:** Optimización e Índices SQL en PostgreSQL.
* **Fase 10:** Testing Automatizado y Demo Final.
