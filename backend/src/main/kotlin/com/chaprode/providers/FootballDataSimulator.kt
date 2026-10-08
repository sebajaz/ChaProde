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
        // Partidos del Mundial 2026
        setSimulatedMatch(
            ExternalMatchScore(
                codigoExterno = "MATCH-01",
                localCodigo = "ARG",
                visitanteCodigo = "MEX",
                golesLocal = 2,
                golesVisitante = 1,
                estado = "FINALIZADO",
                competitionCode = "WC2026",
                competitionName = "Copa Mundial FIFA 2026",
                localNombre = "Argentina",
                visitanteNombre = "México"
            )
        )
        setSimulatedMatch(
            ExternalMatchScore(
                codigoExterno = "MATCH-02",
                localCodigo = "BRA",
                visitanteCodigo = "URU",
                golesLocal = 3,
                golesVisitante = 0,
                estado = "FINALIZADO",
                competitionCode = "WC2026",
                competitionName = "Copa Mundial FIFA 2026",
                localNombre = "Brasil",
                visitanteNombre = "Uruguay"
            )
        )
        setSimulatedMatch(
            ExternalMatchScore(
                codigoExterno = "MATCH-03",
                localCodigo = "ESP",
                visitanteCodigo = "GER",
                golesLocal = 1,
                golesVisitante = 1,
                estado = "FINALIZADO",
                competitionCode = "WC2026",
                competitionName = "Copa Mundial FIFA 2026",
                localNombre = "España",
                visitanteNombre = "Alemania"
            )
        )
        setSimulatedMatch(
            ExternalMatchScore(
                codigoExterno = "MATCH-04",
                localCodigo = "FRA",
                visitanteCodigo = "USA",
                golesLocal = 1,
                golesVisitante = 0,
                estado = "EN_JUEGO",
                competitionCode = "WC2026",
                competitionName = "Copa Mundial FIFA 2026",
                localNombre = "Francia",
                visitanteNombre = "Estados Unidos"
            )
        )
        setSimulatedMatch(
            ExternalMatchScore(
                codigoExterno = "MATCH-05",
                localCodigo = "ARG",
                visitanteCodigo = "BRA",
                golesLocal = null,
                golesVisitante = null,
                estado = "PENDIENTE",
                competitionCode = "WC2026",
                competitionName = "Copa Mundial FIFA 2026",
                localNombre = "Argentina",
                visitanteNombre = "Brasil"
            )
        )

        // UEFA Champions League
        setSimulatedMatch(
            ExternalMatchScore(
                codigoExterno = "MATCH-CL-01",
                localCodigo = "RMA",
                visitanteCodigo = "MCI",
                golesLocal = 3,
                golesVisitante = 2,
                estado = "FINALIZADO",
                competitionCode = "CL",
                competitionName = "UEFA Champions League",
                localNombre = "Real Madrid",
                visitanteNombre = "Manchester City"
            )
        )
        setSimulatedMatch(
            ExternalMatchScore(
                codigoExterno = "MATCH-CL-02",
                localCodigo = "BAY",
                visitanteCodigo = "PSG",
                golesLocal = 2,
                golesVisitante = 1,
                estado = "FINALIZADO",
                competitionCode = "CL",
                competitionName = "UEFA Champions League",
                localNombre = "Bayern Múnich",
                visitanteNombre = "Paris Saint-Germain"
            )
        )
        setSimulatedMatch(
            ExternalMatchScore(
                codigoExterno = "MATCH-CL-03",
                localCodigo = "ARS",
                visitanteCodigo = "BAR",
                golesLocal = 1,
                golesVisitante = 1,
                estado = "EN_JUEGO",
                competitionCode = "CL",
                competitionName = "UEFA Champions League",
                localNombre = "Arsenal",
                visitanteNombre = "FC Barcelona"
            )
        )

        // Premier League
        setSimulatedMatch(
            ExternalMatchScore(
                codigoExterno = "MATCH-PL-01",
                localCodigo = "LIV",
                visitanteCodigo = "CHE",
                golesLocal = 2,
                golesVisitante = 0,
                estado = "FINALIZADO",
                competitionCode = "PL",
                competitionName = "Premier League",
                localNombre = "Liverpool",
                visitanteNombre = "Chelsea"
            )
        )
        setSimulatedMatch(
            ExternalMatchScore(
                codigoExterno = "MATCH-PL-02",
                localCodigo = "ARS",
                visitanteCodigo = "MUN",
                golesLocal = 3,
                golesVisitante = 1,
                estado = "FINALIZADO",
                competitionCode = "PL",
                competitionName = "Premier League",
                localNombre = "Arsenal",
                visitanteNombre = "Manchester United"
            )
        )

        // La Liga
        setSimulatedMatch(
            ExternalMatchScore(
                codigoExterno = "MATCH-PD-01",
                localCodigo = "RMA",
                visitanteCodigo = "BAR",
                golesLocal = 2,
                golesVisitante = 1,
                estado = "FINALIZADO",
                competitionCode = "PD",
                competitionName = "La Liga",
                localNombre = "Real Madrid",
                visitanteNombre = "FC Barcelona"
            )
        )

        // Serie A
        setSimulatedMatch(
            ExternalMatchScore(
                codigoExterno = "MATCH-SA-01",
                localCodigo = "INT",
                visitanteCodigo = "MIL",
                golesLocal = 1,
                golesVisitante = 0,
                estado = "FINALIZADO",
                competitionCode = "SA",
                competitionName = "Serie A",
                localNombre = "Inter de Milán",
                visitanteNombre = "AC Milan"
            )
        )

        // Copa Libertadores
        setSimulatedMatch(
            ExternalMatchScore(
                codigoExterno = "MATCH-LIB-01",
                localCodigo = "BOC",
                visitanteCodigo = "RIV",
                golesLocal = 2,
                golesVisitante = 2,
                estado = "FINALIZADO",
                competitionCode = "CLI",
                competitionName = "Copa Libertadores",
                localNombre = "Boca Juniors",
                visitanteNombre = "River Plate"
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

    override suspend fun fetchWorldwideMatches(dateFrom: String?, dateTo: String?): List<ExternalMatchScore> {
        log.info("FootballDataSimulator: retornando {} marcadores simulados mundiales.", simulatedScores.size)
        return simulatedScores.values.toList()
    }
}
