package com.chaprode.mobile.data.local.dao

import com.chaprode.mobile.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * Interface DAO preparada para Room.
 * Define las operaciones CRUD locales sobre la tabla de usuarios.
 */
interface UserDao {
    suspend fun insertUser(user: UserEntity)
    suspend fun getUserById(id: String): UserEntity?
    fun getCurrentUserFlow(): Flow<UserEntity?>
    suspend fun deleteUser()
}
