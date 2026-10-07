package com.chaprode.services

object ScoringEngine {

    /**
     * Calcula los puntos ganados para un pronóstico comparado con el resultado real.
     * Reglas oficiales de ChaProde:
     * - 3 Puntos: Acierto exacto del marcador (ej: pred 2-1, real 2-1).
     * - 1 Punto: Acierto de tendencia (ganador local, ganador visitante o empate acertado).
     * - 0 Puntos: No acertó ni el marcador ni la tendencia.
     */
    fun calculatePoints(
        predLocal: Int,
        predVisitante: Int,
        realLocal: Int,
        realVisitante: Int
    ): Int {
        // 1. Acierto exacto del resultado
        if (predLocal == realLocal && predVisitante == realVisitante) {
            return 3
        }

        // 2. Acierto del ganador o empate (tendencia)
        val predDiff = predLocal - predVisitante
        val realDiff = realLocal - realVisitante

        val acertoGanadorLocal = (predDiff > 0) && (realDiff > 0)
        val acertoGanadorVisitante = (predDiff < 0) && (realDiff < 0)
        val acertoEmpate = (predDiff == 0) && (realDiff == 0)

        if (acertoGanadorLocal || acertoGanadorVisitante || acertoEmpate) {
            return 1
        }

        // 3. No acertó
        return 0
    }
}
