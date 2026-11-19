package com.chaprode

import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes

class PredictionValidationTest {

    // Regla Oficial Actualizada: Los pronósticos cierran 15 minutos antes del inicio del partido
    // Se puede pronosticar con cualquier tiempo de anticipación (días, semanas), pero faltando 15 minutos se bloquea
    private fun isPredictionAllowed(matchTime: Instant, now: Instant, matchState: String): Boolean {
        if (matchState != "PENDIENTE") return false
        val cutoff = matchTime.minus(15.minutes)
        return now < cutoff
    }

    @Test
    fun `pronostico permitido cuando faltan mas de 15 minutos o con dias de anticipacion`() {
        val matchTime = Instant.parse("2026-06-11T20:00:00Z")
        // 7 días antes -> permitido
        val sevenDaysBefore = Instant.parse("2026-06-04T20:00:00Z")
        // 2 horas antes (18:00) -> permitido
        val twoHoursBefore = Instant.parse("2026-06-11T18:00:00Z")
        // 20 minutos antes (19:40) -> permitido
        val twentyMinBefore = Instant.parse("2026-06-11T19:40:00Z")
        // 16 minutos antes (19:44) -> permitido
        val sixteenMinBefore = Instant.parse("2026-06-11T19:44:00Z")

        assertTrue(isPredictionAllowed(matchTime, sevenDaysBefore, "PENDIENTE"), "Debe permitirse con 7 días de anticipación")
        assertTrue(isPredictionAllowed(matchTime, twoHoursBefore, "PENDIENTE"), "Debe permitirse con 2 horas de anticipación")
        assertTrue(isPredictionAllowed(matchTime, twentyMinBefore, "PENDIENTE"), "Debe permitirse a 20 minutos del partido")
        assertTrue(isPredictionAllowed(matchTime, sixteenMinBefore, "PENDIENTE"), "Debe permitirse a 16 minutos del partido")
    }

    @Test
    fun `pronostico bloqueado exactamente a 15 minutos o menos del partido`() {
        val matchTime = Instant.parse("2026-06-11T20:00:00Z")
        // Exactamente 15 minutos antes (19:45) -> bloqueado
        val exactly15MinBefore = Instant.parse("2026-06-11T19:45:00Z")
        // 10 minutos antes (19:50) -> bloqueado
        val tenMinBefore = Instant.parse("2026-06-11T19:50:00Z")
        // 5 minutos antes (19:55) -> bloqueado
        val fiveMinBefore = Instant.parse("2026-06-11T19:55:00Z")
        // Durante el partido (20:30) -> bloqueado
        val duringMatch = Instant.parse("2026-06-11T20:30:00Z")

        assertFalse(isPredictionAllowed(matchTime, exactly15MinBefore, "PENDIENTE"), "Debe bloquearse a las 19:45 (15 min antes)")
        assertFalse(isPredictionAllowed(matchTime, tenMinBefore, "PENDIENTE"), "Debe bloquearse a las 19:50")
        assertFalse(isPredictionAllowed(matchTime, fiveMinBefore, "PENDIENTE"), "Debe bloquearse a las 19:55")
        assertFalse(isPredictionAllowed(matchTime, duringMatch, "PENDIENTE"), "Debe bloquearse durante el partido")
    }

    @Test
    fun `pronostico bloqueado si el partido no esta en estado PENDIENTE`() {
        val matchTime = Instant.parse("2026-06-11T20:00:00Z")
        val currentTime = Instant.parse("2026-06-11T18:00:00Z") // 2 horas antes

        assertFalse(isPredictionAllowed(matchTime, currentTime, "EN_JUEGO"))
        assertFalse(isPredictionAllowed(matchTime, currentTime, "FINALIZADO"))
    }

    // Reglas de Administración de Resultados
    private fun isManualResultSettlementAllowed(estado: String, codigoExterno: String?): Boolean {
        // Regla 1: No se pueden cargar resultados en partidos pendientes
        if (estado == "PENDIENTE") return false
        // Regla 2: No se pueden modificar resultados de partidos finalizados traídos por la API
        if (estado == "FINALIZADO" && !codigoExterno.isNullOrBlank()) return false
        return true
    }

    @Test
    fun `carga de resultado bloqueada si el partido esta en estado PENDIENTE`() {
        assertFalse(isManualResultSettlementAllowed("PENDIENTE", null), "No debe permitirse cargar resultado a partido manual PENDIENTE")
        assertFalse(isManualResultSettlementAllowed("PENDIENTE", "FD-12345"), "No debe permitirse cargar resultado a partido de la API PENDIENTE")
    }

    @Test
    fun `modificacion de resultado bloqueada si el partido es FINALIZADO y proviene de la API`() {
        assertFalse(isManualResultSettlementAllowed("FINALIZADO", "FD-99999"), "Debe bloquearse modificacion de partido finalizado de la API")
    }

    @Test
    fun `carga de resultado permitida cuando el partido esta EN JUEGO`() {
        assertTrue(isManualResultSettlementAllowed("EN_JUEGO", null), "Debe permitirse cargar resultado cuando esta EN_JUEGO")
        assertTrue(isManualResultSettlementAllowed("EN_JUEGO", "FD-55555"), "Debe permitirse cargar resultado de partido en juego")
    }
}
