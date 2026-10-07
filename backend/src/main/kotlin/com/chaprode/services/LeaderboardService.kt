package com.chaprode.services

import com.chaprode.dto.LeaderboardEntryDto
import com.chaprode.dto.TournamentLeaderboardResponse
import com.chaprode.repositories.LeaderboardRepository
import com.chaprode.repositories.TournamentRepository
import java.util.*

class LeaderboardService(
    private val leaderboardRepository: LeaderboardRepository = LeaderboardRepository(),
    private val tournamentRepository: TournamentRepository = TournamentRepository()
) {

    suspend fun getTournamentLeaderboard(torneoId: String): TournamentLeaderboardResponse {
        val uuid = try {
            UUID.fromString(torneoId)
        } catch (e: Exception) {
            throw IllegalArgumentException("Identificador de torneo inválido.")
        }

        val torneo = tournamentRepository.getTournamentById(uuid)
            ?: throw IllegalArgumentException("El torneo especificado no existe.")

        val ranking = leaderboardRepository.getLeaderboardByTournament(uuid)

        return TournamentLeaderboardResponse(
            torneoId = torneo.id,
            torneoNombre = torneo.nombre,
            ranking = ranking
        )
    }

    suspend fun getGlobalLeaderboard(): List<LeaderboardEntryDto> {
        return leaderboardRepository.getGlobalLeaderboard()
    }
}
