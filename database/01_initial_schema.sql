-- ==========================================================
-- ChaProde - Esquema Inicial de Base de Datos (PostgreSQL 18)
-- ==========================================================

-- Extensión para generar UUIDs nativos si se requiere
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. TABLA: Usuarios
CREATE TABLE IF NOT EXISTS usuarios (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    rol VARCHAR(20) NOT NULL DEFAULT 'USER' CHECK (rol IN ('USER', 'ADMIN')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. TABLA: Torneos
CREATE TABLE IF NOT EXISTS torneos (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nombre VARCHAR(100) NOT NULL,
    codigo_externo VARCHAR(50),
    logo_url TEXT,
    activo BOOLEAN NOT NULL DEFAULT true,
    fecha_inicio TIMESTAMP WITH TIME ZONE,
    fecha_fin TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 3. TABLA: Equipos
CREATE TABLE IF NOT EXISTS equipos (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nombre VARCHAR(100) NOT NULL,
    codigo_externo VARCHAR(50),
    url_bandera TEXT,
    codigo_iso VARCHAR(10),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 4. TABLA: Torneo_Equipos (Relación N:M para escalabilidad multi-torneo)
CREATE TABLE IF NOT EXISTS torneo_equipos (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    torneo_id UUID NOT NULL REFERENCES torneos(id) ON DELETE CASCADE,
    equipo_id UUID NOT NULL REFERENCES equipos(id) ON DELETE CASCADE,
    CONSTRAINT uq_torneo_equipo UNIQUE (torneo_id, equipo_id)
);

-- 5. TABLA: Partidos
CREATE TABLE IF NOT EXISTS partidos (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    torneo_id UUID NOT NULL REFERENCES torneos(id) ON DELETE CASCADE,
    equipo_local_id UUID NOT NULL REFERENCES equipos(id) ON DELETE RESTRICT,
    equipo_visitante_id UUID NOT NULL REFERENCES equipos(id) ON DELETE RESTRICT,
    fecha_partido TIMESTAMP WITH TIME ZONE NOT NULL,
    goles_local INT DEFAULT NULL,
    goles_visitante INT DEFAULT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE' CHECK (estado IN ('PENDIENTE', 'EN_JUEGO', 'FINALIZADO')),
    codigo_externo VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 6. TABLA: Pronósticos (Core del Prode)
CREATE TABLE IF NOT EXISTS pronosticos (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    usuario_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    partido_id UUID NOT NULL REFERENCES partidos(id) ON DELETE CASCADE,
    goles_local_predicho INT NOT NULL CHECK (goles_local_predicho >= 0),
    goles_visitante_predicho INT NOT NULL CHECK (goles_visitante_predicho >= 0),
    puntos_ganados INT DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_usuario_partido UNIQUE (usuario_id, partido_id)
);

-- 7. TABLA: Ligas Privadas
CREATE TABLE IF NOT EXISTS ligas_privadas (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nombre VARCHAR(100) NOT NULL,
    codigo_acceso VARCHAR(10) NOT NULL UNIQUE,
    creador_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    torneo_id UUID NOT NULL REFERENCES torneos(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 8. TABLA: Miembros de Liga Privada
CREATE TABLE IF NOT EXISTS miembros_liga (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    liga_id UUID NOT NULL REFERENCES ligas_privadas(id) ON DELETE CASCADE,
    usuario_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    fecha_ingreso TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_liga_usuario UNIQUE (liga_id, usuario_id)
);

-- ==========================================================
-- Índices para Rendimiento y Consultas de Ranking
-- ==========================================================
CREATE INDEX IF NOT EXISTS idx_pronosticos_usuario ON pronosticos(usuario_id);
CREATE INDEX IF NOT EXISTS idx_pronosticos_partido ON pronosticos(partido_id);
CREATE INDEX IF NOT EXISTS idx_partidos_torneo_fecha ON partidos(torneo_id, fecha_partido);
CREATE INDEX IF NOT EXISTS idx_miembros_liga_usuario ON miembros_liga(usuario_id);
CREATE INDEX IF NOT EXISTS idx_miembros_liga_liga ON miembros_liga(liga_id);
