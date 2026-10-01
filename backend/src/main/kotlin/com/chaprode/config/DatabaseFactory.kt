package com.chaprode.config

import com.chaprode.models.*
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

object DatabaseFactory {
    private val log = LoggerFactory.getLogger(DatabaseFactory::class.java)
    private var dataSource: HikariDataSource? = null

    fun init(config: ApplicationConfig) {
        val jdbcUrl = config.propertyOrNull("database.jdbcUrl")?.getString()
            ?: "jdbc:postgresql://localhost:5432/chaprode_db"
        val driverClassName = config.propertyOrNull("database.driverClassName")?.getString()
            ?: "org.postgresql.Driver"
        val user = config.propertyOrNull("database.user")?.getString() ?: "postgres"
        val password = config.propertyOrNull("database.password")?.getString() ?: "postgres"
        val maxPoolSize = config.propertyOrNull("database.maxPoolSize")?.getString()?.toIntOrNull() ?: 10

        log.info("Conectando a la base de datos PostgreSQL en {}", jdbcUrl)

        val hikariConfig = HikariConfig().apply {
            this.jdbcUrl = jdbcUrl
            this.driverClassName = driverClassName
            this.username = user
            this.password = password
            this.maximumPoolSize = maxPoolSize
            this.isAutoCommit = false
            this.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            this.validate()
        }

        val ds = HikariDataSource(hikariConfig)
        dataSource = ds
        val db = Database.connect(ds)

        transaction(db) {
            log.info("Verificando y sincronizando esquema de tablas con Exposed...")
            SchemaUtils.create(
                UsuariosTable,
                TorneosTable,
                EquiposTable,
                TorneoEquiposTable,
                PartidosTable,
                PronosticosTable,
                LigasPrivadasTable,
                MiembrosLigaTable
            )
            log.info("Tablas sincronizadas correctamente.")
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    fun checkHealth(): Boolean {
        return try {
            transaction {
                exec("SELECT 1") { rs -> rs.next() }
            } ?: false
        } catch (e: Exception) {
            log.error("Error comprobando salud de la base de datos", e)
            false
        }
    }
}
