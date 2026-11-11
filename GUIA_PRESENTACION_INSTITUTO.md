# 🏆 ChaProde — Guía Maestra de Presentación y Demostración en Vivo

Documento de referencia para la defensa del proyecto ante el docente y la demostración práctica en tiempo real desde la computadora del instituto.

---

## 📌 PARTE 1: La Pregunta del Profesor: *"¿Cómo está estructurado el proyecto?"*

### 🎙️ El "Pitch" Técnico de 1 Minuto (Para decir en voz alta con seguridad)
> *"ChaProde es una plataforma cliente-servidor para pronósticos deportivos estructurada bajo una arquitectura **Full-Stack desacoplada en un Monorepo**. Cuenta con una aplicación móvil nativa en **Android con Jetpack Compose y Kotlin**, un backend asíncrono de alto rendimiento en **Ktor con Exposed ORM**, una base de datos relacional en **PostgreSQL 17**, un portal de administración web en **React + Nginx**, y toda la infraestructura orquestada con **Docker Compose** y un túnel seguro **HTTPS de Cloudflare** que permite operar en tiempo real sobre redes móviles 4G/5G o Wi-Fi sin abrir puertos de red."*

---

### 📂 Desglose por Capas y Módulos

```text
ChaProde/
├── mobile/            # Cliente móvil nativo Android (Kotlin 2.1 + Jetpack Compose)
├── backend/           # API REST empresarial asíncrona (Kotlin + Ktor Framework + Netty)
├── admin-web/         # Portal web de administración y monitoreo (React + Vite + Nginx)
├── database/          # Esquema relacional SQL y backups exportables de datos
└── docker-compose.yml # Orquestador de contenedores (PostgreSQL, Backend, Web, Tunnel, Adminer)
```

#### 1. Capa Móvil (`mobile/`) — Patrón MVI / MVVM y Clean Architecture
* **UI Declarativa (Jetpack Compose):** 100% libre de XML. La interfaz se construye de forma reactiva a partir de funciones componibles y un sistema de diseño atómico dark mode (`Color.kt`, `Theme.kt`, `ChaProdeButton`, `ScoreStepper`, etc.).
* **Gestión de Estado (ViewModels):** Cada pantalla tiene su ViewModel que procesa eventos (`UiEvent`) y expone un único estado inmutable reactivo (`UiState`) a través de **`StateFlow`** con corrutinas de Kotlin.
* **Capa de Datos (Repositories):** Los repositorios (`AuthRepository`, `PredictionRepository`, `RankingRepository`, `LeagueRepository`) actúan como *Single Source of Truth*, manejando peticiones HTTP con la API REST y persistiendo sesiones locales en `SessionManager`.

#### 2. Capa Backend (`backend/`) — Arquitectura en Capas
* **Rutas / Controladores (`routes/`):** Endpoints REST versionados (`/api/auth`, `/api/pronosticos`, `/api/leaderboard`, `/api/ligas`, `/api/torneos`, `/api/health`).
* **Servicios de Negocio (`services/`):**
  * **`ScoringEngine`:** Motor de cálculo algorítmico (3 pts por acierto exacto de marcador, 1 pt por tendencia de ganador/empate, 0 pts por error).
  * **`PredictionService`:** Valida que ningún pronóstico se envíe o altere después del tiempo límite antes de cada partido.
  * **`MatchSchedulerService`:** Tarea en segundo plano (Daemon) que cada 15 minutos sincroniza resultados deportivos sin bloquear el servidor.
* **Persistencia (`repositories/` y `models/`):** Mapeo relacional con **JetBrains Exposed ORM** y pool de conexiones concurrentes optimizado con **HikariCP**.
* **Seguridad (`security/`):** Contraseñas saladas con **BCrypt** (costo 12) y tokens **JWT** (HMAC-256) con expiración a las 72 horas.

---

## 🛡️ PARTE 2: Aclaración Clave sobre Docker
> [!NOTE]
> **¿Si levanto Docker en la computadora del instituto, me tira abajo o borra lo de mi casa?**  
> **NO, en absoluto.**  
> Cada máquina que corre Docker vive en su propio entorno local aislado. Lo que hagas en el instituto **no tiene forma de modificar, apagar ni borrar nada de tu PC de casa**.
> Al levantar Docker en el instituto, tendrás un entorno limpio e independiente para hacer tu demostración.

---

## 🚀 PARTE 3: Paso a Paso para la Demostración en el Instituto

### 🎒 Lo que debes llevar listo:
1. Tu carpeta de proyecto `ChaProde` (en un pendrive o subida a tu GitHub).
2. El archivo de base de datos listo: `database/backup_datos.sql` (ya generado en tu proyecto).
3. Tu celular con el APK instalado.

---

### Paso 1: Levantar todos los servicios con Docker
Abre una terminal (PowerShell o CMD) en la carpeta `ChaProde` de la computadora del instituto y ejecuta:
```bash
docker compose up -d
```
Esto levantará automáticamente 5 contenedores:
* `chaprode-postgres`: Base de datos.
* `chaprode-backend`: Servidor de la API.
* `chaprode-admin-web`: Portal web administrativo (puertos 80 y 3000).
* `chaprode-adminer`: Visor visual web de la base de datos (puerto 8085).
* `chaprode-tunnel`: Túnel seguro de Cloudflare hacia internet.

---

### Paso 2: Importar la base de datos completa con tus datos
En la misma terminal, ejecuta:
```bash
docker exec -i chaprode-postgres psql -U postgres -d chaprode_db < database/backup_datos.sql
```
*(Listo: con este único comando, la computadora del instituto tendrá exactamente los mismos usuarios, torneos, partidos y pronósticos que tienes en tu casa).*

---

### Paso 3: Abrir el Visor de Base de Datos (Adminer) en pantalla grande
Abre el navegador (Google Chrome o Edge) e ingresa a:
👉 **`http://localhost:8085`**

Completa el formulario de inicio de sesión con estos datos:
| Campo | Valor a colocar |
| :--- | :--- |
| **Sistema** | `PostgreSQL` |
| **Servidor** | `db` |
| **Usuario** | `postgres` |
| **Contraseña** | `prodepw` |
| **Base de datos** | `chaprode_db` |

Presiona **"Entrar"**. Verás a la izquierda el listado de tablas (`usuarios`, `partidos`, `pronosticos`, `ligas_privadas`, etc.).

---

### Paso 4: Conectar tu celular a la computadora del instituto
Tienes 2 opciones muy fáciles:

#### Opción Directa (Por Túnel Cloudflare - Recomendada para evitar problemas de Wi-Fi):
1. En la terminal de la PC del instituto, escribe:
   ```bash
   docker logs chaprode-tunnel
   ```
2. Busca la línea que dice `Your quick Tunnel has been created! Visit it at: https://xxxx.trycloudflare.com`.
3. En la app móvil de tu celular, en la pantalla de login, toca el texto abajo de todo:  
   `⚙️ Servidor: ...`
4. Pegas la URL de Cloudflare que te dio la terminal y tocas **Guardar**.

---

## 🎬 PARTE 4: El Guion de la Demostración en Tiempo Real frente al Profesor

1. **Mostrar el estado inicial de la Base de Datos:**
   - En **Adminer** (`http://localhost:8085`), haces clic en la tabla **`pronosticos`** y luego en **"Seleccionar datos"**.
   - Muéstrale al profesor la lista de pronósticos existentes (con goles locales, visitantes y fechas).
2. **Hacer el cambio en vivo desde tu celular:**
   - Abres la app ChaProde en tu teléfono.
   - Inicias sesión con tu usuario.
   - Vas a la pestaña **Fixture**.
   - Eliges un partido pendiente (ej: *Argentina vs Francia*).
   - Usas los botones de goles `+` / `-` y pones un marcador llamativo (ejemplo: **3 - 2**).
   - Presionas el botón **"Guardar Pronóstico"**.
   - Verás el mensaje verde de éxito en el teléfono: *"¡Pronóstico guardado!"*.
3. **El impacto en vivo:**
   - Vuelves a la pantalla de la PC y en Adminer presionas **F5** (o el botón *Seleccionar*).
   - 👉 **Señálale al profesor cómo apareció instantáneamente la nueva fila en PostgreSQL** con el UUID del partido, los goles predichos `(3, 2)` y la fecha y hora de la transacción en el servidor.
4. **(Opcional) Mostrar el Panel Web de Administración:**
   - Abre otra pestaña en el navegador en `http://localhost:3000` para mostrarle el panel web interactivo donde también se reflejan las métricas globales del prode.

---

## 🧠 PARTE 5: Cheat Sheet: Respuestas a Preguntas Trampa del Docente

| Pregunta probable del profesor | Tu respuesta técnica ideal |
| :--- | :--- |
| **¿Cómo evitan que un usuario modifique el pronóstico cuando el partido ya empezó?** | *"La validación se realiza de forma estricta en el Backend (`PredictionService`) utilizando el reloj UTC del servidor. Si la hora actual es mayor o igual a la hora de corte (15 min antes del partido), la API rechaza la transacción con un error 400 Bad Request, impidiendo cualquier alteración sin importar la hora del teléfono del usuario."* |
| **¿Por qué la contraseña no se guarda en texto plano?** | *"Utilizamos BCrypt con factor de costo 12 (`SecurityUtils`). BCrypt incorpora sal aleatoria automática (salt) y un algoritmo iterativo computacionalmente costoso que resiste ataques de diccionario y tablas arcoíris."* |
| **¿Cómo calculan los puntos de cada participante?** | *"Mediante un motor de reglas desacoplado (`ScoringEngine`). Cuando el servicio de sincronización (`MatchSyncService`) marca un partido como 'FINALIZADO' con su resultado oficial, se evalúan los pronósticos: 3 puntos por acierto exacto del marcador y 1 punto si se acertó la tendencia (ganador o empate)."* |
| **¿Por qué eligieron Ktor en lugar de Spring Boot?** | *"Ktor es un framework nativo de Kotlin, sumamente liviano y basado en corrutinas. A diferencia de Spring Boot, inicia en menos de 2 segundos, tiene una huella de memoria RAM mínima y no utiliza reflexión excesiva, lo que lo hace ideal para arquitecturas contenerizadas en microservicios."* |
| **¿Por qué Jetpack Compose en lugar de XML en Android?** | *"Compose es el estándar moderno de Android para UI declarativa. Reduce el código en un 40%, elimina la sincronización manual de vistas (`findViewById`), maneja la recomposición inteligente y se enlaza directamente con flujos reactivos `StateFlow`."* |
