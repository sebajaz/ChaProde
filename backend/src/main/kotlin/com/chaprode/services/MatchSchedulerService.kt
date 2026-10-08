package com.chaprode.services

import kotlinx.coroutines.*
import org.slf4j.LoggerFactory

class MatchSchedulerService(
    private val matchSyncService: MatchSyncService,
    private val intervalMinutes: Long = 15,
    private val enabled: Boolean = true
) {

    private val log = LoggerFactory.getLogger(MatchSchedulerService::class.java)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var job: Job? = null

    fun start() {
        if (!enabled) {
            log.info("MatchSchedulerService está deshabilitado por configuración (autoSyncEnabled = false).")
            return
        }

        log.info("Iniciando MatchSchedulerService: sincronización automática programada cada {} minutos.", intervalMinutes)

        job = scope.launch {
            // Espera inicial breve de 30 segundos tras arrancar el backend para no saturar el inicio
            delay(30_000L)

            while (isActive) {
                try {
                    log.info("MatchSchedulerService: Ejecutando verificación automática de partidos con API...")
                    val summary = matchSyncService.syncMatches()
                    if (summary.partidosFinalizados > 0 || summary.partidosProcesados > 0) {
                        log.info("MatchSchedulerService éxito: {}", summary.mensaje)
                    } else {
                        log.debug("MatchSchedulerService: Sin cambios en partidos pendientes.")
                    }
                } catch (e: CancellationException) {
                    log.info("MatchSchedulerService cancelado.")
                    break
                } catch (e: Exception) {
                    log.error("Error durante sincronización programada en MatchSchedulerService: {}", e.message, e)
                }

                // Esperar el intervalo configurado
                delay(intervalMinutes * 60 * 1000L)
            }
        }
    }

    fun stop() {
        log.info("Deteniendo MatchSchedulerService...")
        job?.cancel()
    }
}
