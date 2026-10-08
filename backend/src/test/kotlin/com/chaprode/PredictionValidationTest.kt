package com.chaprode

import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes

class PredictionValidationTest {

    // Regla Oficial Actualizada: Los pronósticos cierran 5 minutos antes del inicio del partido
    private fun isPredictionAllowed(matchTime: Instant, now: Instant, matchState: String): Boolean {
        if (matchState != "PENDIENTE") return false
        val cutoff = matchTime.minus(5.minutes)
        return now < cutoff
    }

    @Test
    fun `pronostico permitido cuando faltan mas de 5 minutos`() {
        val matchTime = Instant.parse("2026-06-11T20:00:00Z")
        // 10 minutos antes (19:50) -> permitido
        val tenMinBefore = Instant.parse("2026-06-11T19:50:00Z")
        // 6 minutos antes (19:54) -> permitido
        val sixMinBefore = Instant.parse("2026-06-11T19:54:00Z")

        assertTrue(isPredictionAllowed(matchTime, tenMinBefore, "PENDIENTE"), "Debe permitirse a 10 minutos del partido")
        assertTrue(isPredictionAllowed(matchTime, sixMinBefore, "PENDIENTE"), "Debe permitirse a 6 minutos del partido")
    }

    @Test
    fun `pronostico bloqueado exactamente a 5 minutos o menos del partido`() {
        val matchTime = Instant.parse("2026-06-11T20:00:00Z")
        // Exactamente 5 minutos antes (19:55) -> bloqueado
        val exactly5MinBefore = Instant.parse("2026-06-11T19:55:00Z")
        // 3 minutos antes (19:57) -> bloqueado
        val threeMinBefore = Instant.parse("2026-06-11T19:57:00Z")
        // Durante el partido (20:30) -> bloqueado
        val duringMatch = Instant.parse("2026-06-11T20:30:00Z")

        assertFalse(isPredictionAllowed(matchTime, exactly5MinBefore, "PENDIENTE"), "Debe bloquearse a las 19:55")
        assertFalse(isPredictionAllowed(matchTime, threeMinBefore, "PENDIENTE"), "Debe bloquearse a las 19:57")
        assertFalse(isPredictionAllowed(matchTime, duringMatch, "PENDIENTE"), "Debe bloquearse durante el partido")
    }

    @Test
    fun `pronostico bloqueado si el partido no esta en estado PENDIENTE`() {
        val matchTime = Instant.parse("2026-06-11T20:00:00Z")
        val currentTime = Instant.parse("2026-06-11T18:00:00Z") // 2 horas antes

        assertFalse(isPredictionAllowed(matchTime, currentTime, "EN_JUEGO"))
        assertFalse(isPredictionAllowed(matchTime, currentTime, "FINALIZADO"))
    }
}
