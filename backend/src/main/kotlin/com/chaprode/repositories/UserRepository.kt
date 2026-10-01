package com.chaprode.repositories

import com.chaprode.config.DatabaseFactory.dbQuery
import com.chaprode.dto.UserDto
import com.chaprode.models.UsuariosTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.*

class UserRepository {

    private fun ResultRow.toUserDto(): UserDto = UserDto(
        id = this[UsuariosTable.id].value.toString(),
        username = this[UsuariosTable.username],
        email = this[UsuariosTable.email],
        rol = this[UsuariosTable.rol],
        createdAt = this[UsuariosTable.createdAt].toString()
    )

    suspend fun findById(id: UUID): UserDto? = dbQuery {
        UsuariosTable
            .selectAll()
            .where { UsuariosTable.id eq id }
            .map { it.toUserDto() }
            .singleOrNull()
    }

    suspend fun findByUsername(username: String): ResultRow? = dbQuery {
        UsuariosTable
            .selectAll()
            .where { UsuariosTable.username.lowerCase() eq username.lowercase().trim() }
            .singleOrNull()
    }

    suspend fun findByEmail(email: String): ResultRow? = dbQuery {
        UsuariosTable
            .selectAll()
            .where { UsuariosTable.email.lowerCase() eq email.lowercase().trim() }
            .singleOrNull()
    }

    suspend fun findByUsernameOrEmail(identifier: String): ResultRow? = dbQuery {
        val cleanIdentifier = identifier.lowercase().trim()
        UsuariosTable
            .selectAll()
            .where {
                (UsuariosTable.username.lowerCase() eq cleanIdentifier) or
                (UsuariosTable.email.lowerCase() eq cleanIdentifier)
            }
            .singleOrNull()
    }

    suspend fun createUser(
        username: String,
        email: String,
        passwordHash: String,
        rol: String = "USER"
    ): UserDto = dbQuery {
        val insertedId = UsuariosTable.insertAndGetId {
            it[UsuariosTable.username] = username.trim()
            it[UsuariosTable.email] = email.lowercase().trim()
            it[UsuariosTable.passwordHash] = passwordHash
            it[UsuariosTable.rol] = if (rol.uppercase() == "ADMIN") "ADMIN" else "USER"
        }

        UsuariosTable
            .selectAll()
            .where { UsuariosTable.id eq insertedId }
            .map { it.toUserDto() }
            .single()
    }
}
