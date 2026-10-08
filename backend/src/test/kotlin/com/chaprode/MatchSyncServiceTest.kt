package com.chaprode

import com.chaprode.providers.ExternalMatchScore
import com.chaprode.providers.FootballDataSimulator
import com.chaprode.providers.SportsDataProvider
import com.chaprode.services.ScoringEngine
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MatchSyncServiceTest {

    @Test
    fun `FootballDataSimulator devuelve partidos precargados correctamente`() = runBlocking {
        val simulator = FootballDataSimulator()
        val results = simulator.fetchMatchResults()

        assertTrue(results.isNotEmpty(), "El simulador debe contener partidos")
        
        val argMex = results.firstOrNull { it.codigoExterno == "MATCH-01" }
        assertEquals("ARG", argMex?.localCodigo)
        assertEquals("MEX", argMex?.visitanteCodigo)
        assertEquals(2, argMex?.golesLocal)
        assertEquals(1, argMex?.golesVisitante)
        assertEquals("FINALIZADO", argMex?.estado)
    }

    @Test
    fun `FootballDataSimulator permite configurar y actualizar marcadores en memoria`() = runBlocking {
        val simulator = FootballDataSimulator()
        simulator.setSimulatedMatch(
            ExternalMatchScore(
                codigoExterno = "CUSTOM-99",
                localCodigo = "ARG",
                visitanteCodigo = "BRA",
                golesLocal = 3,
                golesVisitante = 1,
                estado = "FINALIZADO"
            )
        )

        val results = simulator.fetchMatchResults()
        val customMatch = results.firstOrNull { it.codigoExterno == "CUSTOM-99" }
        
        assertEquals(3, customMatch?.golesLocal)
        assertEquals(1, customMatch?.golesVisitante)
        assertEquals("FINALIZADO", customMatch?.estado)
    }

    @Test
    fun `simulacion de resultados liquida puntos coherentes con el motor de scoring`() = runBlocking {
        // Simular que el partido terminó 2 - 1
        val realLocal = 2
        val realVisitante = 1

        // Caso 1: Pronóstico exacto (2 - 1)
        val ptsExacto = ScoringEngine.calculatePoints(predLocal = 2, predVisitante = 1, realLocal = realLocal, realVisitante = realVisitante)
        assertEquals(3, ptsExacto)

        // Caso 2: Pronóstico con ganador acertado pero no exacto (1 - 0)
        val ptsGanador = ScoringEngine.calculatePoints(predLocal = 1, predVisitante = 0, realLocal = realLocal, realVisitante = realVisitante)
        assertEquals(1, ptsGanador)

        // Caso 3: Pronóstico errado (0 - 2)
        val ptsErrado = ScoringEngine.calculatePoints(predLocal = 0, predVisitante = 2, realLocal = realLocal, realVisitante = realVisitante)
        assertEquals(0, ptsErrado)
    }

    @Test
    fun `SportsDataProvider custom responde con lista de marcadores para sincronizador`() = runBlocking {
        val customProvider = object : SportsDataProvider {
            override suspend fun fetchMatchResults(competitionCode: String?): List<ExternalMatchScore> {
                return listOf(
                    ExternalMatchScore(
                        codigoExterno = "M-TEST",
                        localCodigo = "ARG",
                        visitanteCodigo = "MEX",
                        golesLocal = 2,
                        golesVisitante = 0,
                        estado = "FINALIZADO"
                    )
                )
            }
        }

        val results = customProvider.fetchMatchResults()
        assertEquals(1, results.size)
        assertEquals("M-TEST", results[0].codigoExterno)
        assertEquals(2, results[0].golesLocal)
        assertEquals(0, results[0].golesVisitante)
    }
}
