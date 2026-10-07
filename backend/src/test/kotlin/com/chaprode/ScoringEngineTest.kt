package com.chaprode

import com.chaprode.services.ScoringEngine
import kotlin.test.Test
import kotlin.test.assertEquals

class ScoringEngineTest {

    @Test
    fun `marcador exacto otorga 3 puntos`() {
        assertEquals(3, ScoringEngine.calculatePoints(predLocal = 2, predVisitante = 1, realLocal = 2, realVisitante = 1))
        assertEquals(3, ScoringEngine.calculatePoints(predLocal = 0, predVisitante = 0, realLocal = 0, realVisitante = 0))
        assertEquals(3, ScoringEngine.calculatePoints(predLocal = 4, predVisitante = 3, realLocal = 4, realVisitante = 3))
    }

    @Test
    fun `acertar ganador local sin marcador exacto otorga 1 punto`() {
        assertEquals(1, ScoringEngine.calculatePoints(predLocal = 1, predVisitante = 0, realLocal = 2, realVisitante = 0))
        assertEquals(1, ScoringEngine.calculatePoints(predLocal = 3, predVisitante = 1, realLocal = 2, realVisitante = 1))
        assertEquals(1, ScoringEngine.calculatePoints(predLocal = 4, predVisitante = 2, realLocal = 1, realVisitante = 0))
    }

    @Test
    fun `acertar ganador visitante sin marcador exacto otorga 1 punto`() {
        assertEquals(1, ScoringEngine.calculatePoints(predLocal = 0, predVisitante = 2, realLocal = 1, realVisitante = 3))
        assertEquals(1, ScoringEngine.calculatePoints(predLocal = 1, predVisitante = 3, realLocal = 0, realVisitante = 1))
    }

    @Test
    fun `acertar empate con distinta cantidad de goles otorga 1 punto`() {
        assertEquals(1, ScoringEngine.calculatePoints(predLocal = 1, predVisitante = 1, realLocal = 0, realVisitante = 0))
        assertEquals(1, ScoringEngine.calculatePoints(predLocal = 2, predVisitante = 2, realLocal = 3, realVisitante = 3))
    }

    @Test
    fun `no acertar tendencia ni resultado otorga 0 puntos`() {
        // Predijo local ganó visitante
        assertEquals(0, ScoringEngine.calculatePoints(predLocal = 2, predVisitante = 1, realLocal = 0, realVisitante = 1))
        // Predijo empate ganó local
        assertEquals(0, ScoringEngine.calculatePoints(predLocal = 1, predVisitante = 1, realLocal = 2, realVisitante = 0))
        // Predijo empate ganó visitante
        assertEquals(0, ScoringEngine.calculatePoints(predLocal = 0, predVisitante = 0, realLocal = 1, realVisitante = 2))
        // Predijo visitante empataron
        assertEquals(0, ScoringEngine.calculatePoints(predLocal = 0, predVisitante = 1, realLocal = 1, realVisitante = 1))
    }
}
