package com.chaprode.providers

import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

class FootballDataSimulator : SportsDataProvider {

    private val log = LoggerFactory.getLogger(FootballDataSimulator::class.java)

    // Almacenamiento en memoria de marcadores simulados
    private val simulatedScores = ConcurrentHashMap<String, ExternalMatchScore>()

    init {
        resetToDefaultSimulation()
    }

    fun resetToDefaultSimulation() {
        simulatedScores.clear()
        // Carga de marcadores simulados realistas para los partidos iniciales del Mundial 2026
        setSimulatedMatch(
            ExternalMatchScore(
                codigoExterno = "MATCH-01",
                localCodigo = "ARG",
                visitanteCodigo = "MEX",
                golesLocal = 2,
                golesVisitante = 1,
                estado = "FINALIZADO"
            )
        )
        setSimulatedMatch(
            ExternalMatchScore(
                codigoExterno = "MATCH-02",
                localCodigo = "BRA",
                visitanteCodigo = "URU",
                golesLocal = 3,
                golesVisitante = 0,
                estado = "FINALIZADO"
            )
        )
        setSimulatedMatch(
            ExternalMatchScore(
                codigoExterno = "MATCH-03",
                localCodigo = "ESP",
                visitanteCodigo = "GER",
                golesLocal = 1,
                golesVisitante = 1,
                estado = "FINALIZADO"
            )
        )
        setSimulatedMatch(
            ExternalMatchScore(
                codigoExterno = "MATCH-04",
                localCodigo = "FRA",
                visitanteCodigo = "USA",
                golesLocal = 1,
                golesVisitante = 0,
                estado = "EN_JUEGO"
            )
        )
        setSimulatedMatch(
            ExternalMatchScore(
                codigoExterno = "MATCH-05",
                localCodigo = "ARG",
                visitanteCodigo = "BRA",
                golesLocal = null,
                golesVisitante = null,
                estado = "PENDIENTE"
            )
        )
    }

    fun setSimulatedMatch(score: ExternalMatchScore) {
        val key = score.codigoExterno ?: "${score.localCodigo}_${score.visitanteCodigo}"
        simulatedScores[key] = score
        log.info("Marcador simulado configurado para [{}]: {} {} - {} {} [{}]",
            key, score.localCodigo, score.golesLocal ?: "-", score.golesVisitante ?: "-", score.visitanteCodigo, score.estado
        )
    }

    override suspend fun fetchMatchResults(competitionCode: String?): List<ExternalMatchScore> {
        log.info("FootballDataSimulator: retornando {} marcadores simulados.", simulatedScores.size)
        return simulatedScores.values.toList()
    }
}
