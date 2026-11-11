package com.chaprode.mobile.model

/**
 * Modelo de una Liga Privada creada por los usuarios.
 *
 * @param id UUID de la liga.
 * @param nombre Nombre descriptivo de la liga (ej: "Amigos de la Oficina").
 * @param codigoAcceso Código alfanumérico único para invitar participantes.
 * @param creadorId UUID del usuario administrador que creó la liga.
 * @param creadorUsername Nombre de usuario del administrador.
 * @param torneoId ID del torneo oficial sobre el cual compite la liga.
 * @param torneoNombre Nombre descriptivo del torneo asociado.
 * @param totalMiembros Cantidad de personas unidas a la liga.
 * @param createdAt Fecha de creación de la liga.
 */
data class LeagueItem(
    val id: String,
    val nombre: String,
    val codigoAcceso: String,
    val creadorId: String,
    val creadorUsername: String,
    val torneoId: String,
    val torneoNombre: String,
    val totalMiembros: Int,
    val createdAt: String
)

/**
 * Detalle completo de una liga privada, incluyendo su metadata y la tabla interna de posiciones.
 */
data class LeagueDetailItem(
    val liga: LeagueItem,
    val ranking: List<RankingUserItem>
)
