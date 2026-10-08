package com.chaprode.services

import com.chaprode.dto.*
import com.chaprode.providers.FootballDataProvider
import com.chaprode.providers.JsonSeedFootballDataProvider
import com.chaprode.repositories.MatchRepository
import com.chaprode.repositories.TournamentRepository
import kotlinx.datetime.Instant
import java.util.*

class TournamentService(
    private val tournamentRepository: TournamentRepository = TournamentRepository(),
    private val matchRepository: MatchRepository = MatchRepository(),
    private val dataProvider: FootballDataProvider = JsonSeedFootballDataProvider(tournamentRepository, matchRepository)
) {

    suspend fun getActiveTournaments(): List<TournamentDto> {
        return tournamentRepository.getAllActiveTournaments()
    }

    suspend fun resolveTournamentUuid(identifier: String?): UUID {
        val trimmed = identifier?.trim()
        if (trimmed.isNullOrBlank() || 
            trimmed.equals("activo", ignoreCase = true) || 
            trimmed.equals("default", ignoreCase = true) || 
            trimmed.equals("current", ignoreCase = true)) {
            val active = tournamentRepository.getAllActiveTournaments().firstOrNull()
                ?: throw IllegalArgumentException("No hay torneos activos disponibles.")
            return UUID.fromString(active.id)
        }

        // 1. Si es un UUID válido y existe en la base de datos
        val parsedUuid = try {
            UUID.fromString(trimmed)
        } catch (e: Exception) {
            null
        }

        if (parsedUuid != null) {
            val exists = tournamentRepository.getTournamentById(parsedUuid)
            if (exists != null) {
                return parsedUuid
            }
        }

        // 2. Buscar por código externo (ej: "WC2026")
        val byCode = tournamentRepository.findTournamentByCodigoExterno(trimmed)
        if (byCode != null) {
            return UUID.fromString(byCode.id)
        }

        // 3. Fallback inteligente: si se pasó un UUID previo que ya no existe (por ejemplo, re-seed del servidor),
        // pero existe un torneo activo en la base de datos, utilizar el torneo activo para evitar que la UI quede en blanco.
        val fallback = tournamentRepository.getAllActiveTournaments().firstOrNull()
        if (fallback != null) {
            return UUID.fromString(fallback.id)
        }

        throw IllegalArgumentException("Torneo no encontrado para '$identifier'.")
    }

    suspend fun getTournamentById(id: String): TournamentDto {
        val uuid = resolveTournamentUuid(id)
        return tournamentRepository.getTournamentById(uuid)
            ?: throw IllegalArgumentException("Torneo no encontrado.")
    }

    suspend fun getMatchesByTournament(torneoId: String): List<MatchDto> {
        val trimmed = torneoId.trim()
        if (trimmed.equals("todos", ignoreCase = true) || trimmed.equals("all", ignoreCase = true)) {
            return matchRepository.getAllMatches()
        }
        val uuid = resolveTournamentUuid(torneoId)
        return matchRepository.getMatchesByTournament(uuid)
    }

    suspend fun getAllMatches(): List<MatchDto> {
        return matchRepository.getAllMatches()
    }

    suspend fun getAllTeams(): List<TeamDto> {
        return tournamentRepository.getAllTeams()
    }

    suspend fun seedData(): Boolean {
        return dataProvider.seedInitialData()
    }

    suspend fun createTournament(request: CreateTournamentRequest): TournamentDto {
        if (request.nombre.isBlank()) {
            throw IllegalArgumentException("El nombre del torneo es obligatorio.")
        }
        val fInicio = request.fechaInicio?.let { Instant.parse(it) }
        val fFin = request.fechaFin?.let { Instant.parse(it) }

        return tournamentRepository.createTournament(
            nombre = request.nombre,
            codigoExterno = request.codigoExterno,
            logoUrl = request.logoUrl,
            fechaInicio = fInicio,
            fechaFin = fFin
        )
    }

    suspend fun createTeam(request: CreateTeamRequest): TeamDto {
        if (request.nombre.isBlank()) {
            throw IllegalArgumentException("El nombre del equipo es obligatorio.")
        }
        return tournamentRepository.createTeam(
            nombre = request.nombre,
            codigoExterno = request.codigoExterno,
            urlBandera = request.urlBandera,
            codigoIso = request.codigoIso
        )
    }

    suspend fun createMatch(request: CreateMatchRequest): String {
        val torneoId = UUID.fromString(request.torneoId)
        val localId = UUID.fromString(request.equipoLocalId)
        val visitanteId = UUID.fromString(request.equipoVisitanteId)
        val fecha = Instant.parse(request.fechaPartido)

        if (localId == visitanteId) {
            throw IllegalArgumentException("El equipo local y visitante no pueden ser el mismo.")
        }

        val id = matchRepository.createMatch(
            torneoId = torneoId,
            equipoLocalId = localId,
            equipoVisitanteId = visitanteId,
            fechaPartido = fecha,
            codigoExterno = request.codigoExterno
        )
        return id.toString()
    }
}
