package com.chaprode

import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes

class PredictionValidationTest {

    // Regla: Los pronósticos cierran 15 minutos antes del inicio del partido
    private fun isPredictionAllowed(matchTime: Instant, now: Instant, matchState: String): Boolean {
        if (matchState != "PENDIENTE") return false
        val cutoff = matchTime.minus(15.minutes)
        return now < cutoff
    }

    @Test
    fun `pronostico permitido cuando faltan mas de 15 minutos`() {
        val matchTime = Instant.parse("2026-06-11T20:00:00Z")
        // 20 minutos antes
        val currentTime = Instant.parse("2026-06-11T19:40:00Z")

        val allowed = isPredictionAllowed(matchTime, currentTime, "PENDIENTE")
        assertTrue(allowed, "El pronóstico debería permitirse cuando faltan 20 minutos")
    }

    @Test
    fun `pronostico bloqueado exactamente a 15 minutos o menos del partido`() {
        val matchTime = Instant.parse("2026-06-11T20:00:00Z")
        // Exactamente 15 minutos antes
        val exactly15MinBefore = Instant.parse("2026-06-11T19:45:00Z")
        // 10 minutos antes
        val tenMinBefore = Instant.parse("2026-06-11T19:50:00Z")
        // Durante el partido
        val duringMatch = Instant.parse("2026-06-11T20:30:00Z")

        assertFalse(isPredictionAllowed(matchTime, exactly15MinBefore, "PENDIENTE"), "Debe bloquearse a las 19:45")
        assertFalse(isPredictionAllowed(matchTime, tenMinBefore, "PENDIENTE"), "Debe bloquearse a las 19:50")
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
