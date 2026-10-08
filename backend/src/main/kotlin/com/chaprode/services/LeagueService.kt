package com.chaprode.services

import com.chaprode.dto.*
import com.chaprode.repositories.LeagueRepository
import java.security.SecureRandom
import java.util.*

class LeagueService(
    private val leagueRepository: LeagueRepository = LeagueRepository()
) {

    private val random = SecureRandom()
    private val allowedChars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789" // Excluimos 0, O, 1, I para evitar confusiones visuales

    suspend fun createLeague(userId: String, request: CreateLeagueRequest): LeagueDto {
        val userUuid = try {
            UUID.fromString(userId)
        } catch (e: Exception) {
            throw IllegalArgumentException("Identificador de usuario inválido.")
        }

        val torneoUuid = try {
            UUID.fromString(request.torneoId)
        } catch (e: Exception) {
            throw IllegalArgumentException("Identificador de torneo inválido.")
        }

        val leagueName = request.nombre.trim()
        if (leagueName.length < 3 || leagueName.length > 50) {
            throw IllegalArgumentException("El nombre de la liga debe tener entre 3 y 50 caracteres.")
        }

        // Generar un código único de 6 caracteres
        var uniqueCode = generateAccessCode()
        var attempts = 0
        while (!leagueRepository.isCodeAvailable(uniqueCode) && attempts < 10) {
            uniqueCode = generateAccessCode()
            attempts++
        }

        return leagueRepository.createLeague(
            nombre = leagueName,
            creadorId = userUuid,
            torneoId = torneoUuid,
            codigoAcceso = uniqueCode
        )
    }

    suspend fun joinLeague(userId: String, request: JoinLeagueRequest): LeagueDto {
        val userUuid = try {
            UUID.fromString(userId)
        } catch (e: Exception) {
            throw IllegalArgumentException("Identificador de usuario inválido.")
        }

        val code = request.codigoAcceso.trim().uppercase()
        if (code.length < 4 || code.length > 10) {
            throw IllegalArgumentException("El código de acceso no tiene un formato válido.")
        }

        return leagueRepository.joinLeague(userUuid, code)
    }

    suspend fun getMyLeagues(userId: String): List<LeagueDto> {
        val userUuid = UUID.fromString(userId)
        return leagueRepository.getLeaguesByUser(userUuid)
    }

    suspend fun getLeagueDetail(leagueId: String): LeagueDetailDto {
        val uuid = UUID.fromString(leagueId)
        val league = leagueRepository.getLeagueById(uuid)
            ?: throw IllegalArgumentException("La liga seleccionada no existe.")

        val ranking = leagueRepository.getLeagueLeaderboard(uuid)

        return LeagueDetailDto(
            liga = league,
            ranking = ranking
        )
    }

    private fun generateAccessCode(length: Int = 6): String {
        val sb = StringBuilder(length)
        for (i in 0 until length) {
            sb.append(allowedChars[random.nextInt(allowedChars.length)])
        }
        return sb.toString()
    }
}
