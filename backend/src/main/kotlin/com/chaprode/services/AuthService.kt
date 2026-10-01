package com.chaprode.services

import com.chaprode.dto.*
import com.chaprode.models.UsuariosTable
import com.chaprode.repositories.UserRepository
import com.chaprode.security.JwtConfig
import com.chaprode.security.SecurityUtils
import java.util.*

class AuthService(
    private val userRepository: UserRepository = UserRepository()
) {

    suspend fun register(request: RegisterRequest): AuthResponse {
        val username = request.username.trim()
        val email = request.email.trim().lowercase()
        val password = request.password

        // Validaciones
        if (username.length < 3) {
            throw IllegalArgumentException("El nombre de usuario debe tener al menos 3 caracteres.")
        }
        if (!email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$"))) {
            throw IllegalArgumentException("El formato del correo electrónico no es válido.")
        }
        if (password.length < 6) {
            throw IllegalArgumentException("La contraseña debe tener al menos 6 caracteres.")
        }

        // Comprobación de unicidad
        if (userRepository.findByUsername(username) != null) {
            throw IllegalArgumentException("El nombre de usuario '$username' ya se encuentra en uso.")
        }
        if (userRepository.findByEmail(email) != null) {
            throw IllegalArgumentException("El correo electrónico '$email' ya está registrado.")
        }

        // Hashing y creación
        val passwordHash = SecurityUtils.hashPassword(password)
        val userDto = userRepository.createUser(
            username = username,
            email = email,
            passwordHash = passwordHash,
            rol = request.rol
        )

        // Generar Token JWT
        val token = JwtConfig.generateToken(
            userId = userDto.id,
            username = userDto.username,
            rol = userDto.rol
        )

        return AuthResponse(token = token, user = userDto)
    }

    suspend fun login(request: LoginRequest): AuthResponse {
        val identifier = request.usernameOrEmail.trim()
        val password = request.password

        if (identifier.isBlank() || password.isBlank()) {
            throw IllegalArgumentException("Debes ingresar tu usuario/correo y contraseña.")
        }

        val row = userRepository.findByUsernameOrEmail(identifier)
            ?: throw IllegalArgumentException("Credenciales incorrectas. Verifica tu usuario y contraseña.")

        val storedHash = row[UsuariosTable.passwordHash]
        val isValid = SecurityUtils.verifyPassword(password, storedHash)

        if (!isValid) {
            throw IllegalArgumentException("Credenciales incorrectas. Verifica tu usuario y contraseña.")
        }

        val userDto = UserDto(
            id = row[UsuariosTable.id].value.toString(),
            username = row[UsuariosTable.username],
            email = row[UsuariosTable.email],
            rol = row[UsuariosTable.rol],
            createdAt = row[UsuariosTable.createdAt].toString()
        )

        val token = JwtConfig.generateToken(
            userId = userDto.id,
            username = userDto.username,
            rol = userDto.rol
        )

        return AuthResponse(token = token, user = userDto)
    }

    suspend fun getMe(userId: String): UserDto {
        val uuid = try {
            UUID.fromString(userId)
        } catch (e: Exception) {
            throw IllegalArgumentException("Identificador de usuario inválido.")
        }

        return userRepository.findById(uuid)
            ?: throw IllegalArgumentException("Usuario no encontrado.")
    }
}
