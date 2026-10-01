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

    suspend fun getTournamentById(id: String): TournamentDto {
        val uuid = try {
            UUID.fromString(id)
        } catch (e: Exception) {
            throw IllegalArgumentException("Identificador de torneo inválido.")
        }
        return tournamentRepository.getTournamentById(uuid)
            ?: throw IllegalArgumentException("Torneo no encontrado.")
    }

    suspend fun getMatchesByTournament(torneoId: String): List<MatchDto> {
        val uuid = try {
            UUID.fromString(torneoId)
        } catch (e: Exception) {
            throw IllegalArgumentException("Identificador de torneo inválido.")
        }
        return matchRepository.getMatchesByTournament(uuid)
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
