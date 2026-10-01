package com.chaprode.models

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object UsuariosTable : UUIDTable("usuarios") {
    val username = varchar("username", 50).uniqueIndex()
    val email = varchar("email", 100).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val rol = varchar("rol", 20).default("USER")
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
}

object TorneosTable : UUIDTable("torneos") {
    val nombre = varchar("nombre", 100)
    val codigoExterno = varchar("codigo_externo", 50).nullable()
    val logoUrl = text("logo_url").nullable()
    val activo = bool("activo").default(true)
    val fechaInicio = timestamp("fecha_inicio").nullable()
    val fechaFin = timestamp("fecha_fin").nullable()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
}

object EquiposTable : UUIDTable("equipos") {
    val nombre = varchar("nombre", 100)
    val codigoExterno = varchar("codigo_externo", 50).nullable()
    val urlBandera = text("url_bandera").nullable()
    val codigoIso = varchar("codigo_iso", 10).nullable()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
}

object TorneoEquiposTable : UUIDTable("torneo_equipos") {
    val torneoId = reference("torneo_id", TorneosTable)
    val equipoId = reference("equipo_id", EquiposTable)

    init {
        uniqueIndex(torneoId, equipoId)
    }
}

object PartidosTable : UUIDTable("partidos") {
    val torneoId = reference("torneo_id", TorneosTable)
    val equipoLocalId = reference("equipo_local_id", EquiposTable)
    val equipoVisitanteId = reference("equipo_visitante_id", EquiposTable)
    val fechaPartido = timestamp("fecha_partido")
    val golesLocal = integer("goles_local").nullable()
    val golesVisitante = integer("goles_visitante").nullable()
    val estado = varchar("estado", 20).default("PENDIENTE")
    val codigoExterno = varchar("codigo_externo", 50).nullable()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
}

object PronosticosTable : UUIDTable("pronosticos") {
    val usuarioId = reference("usuario_id", UsuariosTable)
    val partidoId = reference("partido_id", PartidosTable)
    val golesLocalPredicho = integer("goles_local_predicho")
    val golesVisitantePredicho = integer("goles_visitante_predicho")
    val puntosGanados = integer("puntos_ganados").default(0)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)

    init {
        uniqueIndex(usuarioId, partidoId)
    }
}

object LigasPrivadasTable : UUIDTable("ligas_privadas") {
    val nombre = varchar("nombre", 100)
    val codigoAcceso = varchar("codigo_acceso", 10).uniqueIndex()
    val creadorId = reference("creador_id", UsuariosTable)
    val torneoId = reference("torneo_id", TorneosTable)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
}

object MiembrosLigaTable : UUIDTable("miembros_liga") {
    val ligaId = reference("liga_id", LigasPrivadasTable)
    val usuarioId = reference("usuario_id", UsuariosTable)
    val fechaIngreso = timestamp("fecha_ingreso").defaultExpression(CurrentTimestamp)

    init {
        uniqueIndex(ligaId, usuarioId)
    }
}
