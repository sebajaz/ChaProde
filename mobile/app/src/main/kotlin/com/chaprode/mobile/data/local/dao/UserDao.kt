package com.chaprode.mobile.data.local.dao

import com.chaprode.mobile.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * Interface DAO preparada para Room.
 * Define las operaciones CRUD locales sobre la tabla de usuarios.
 */
interface UserDao {
    /** Inserta o reemplaza los datos del usuario en la base de datos local Room. */
    suspend fun insertUser(user: UserEntity)
    
    /** Recupera un usuario por su identificador UUID. */
    suspend fun getUserById(id: String): UserEntity?
    
    /** Observa los cambios del usuario activo en forma de flujo reactivo. */
    fun getCurrentUserFlow(): Flow<UserEntity?>
    
    /** Elimina los datos del usuario de la sesión local (logout). */
    suspend fun deleteUser()
}
