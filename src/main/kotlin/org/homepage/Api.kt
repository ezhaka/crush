package org.homepage

import kotlinx.serialization.Serializable

@Serializable
class IncomingValentine(
    val id: Long,
    val message: String,
    val type: Int,
    val read: Boolean
)

@Serializable
class Profile(
    val id: String,
    val firstName: String,
    val lastName: String
)

@Serializable
class ProfileListResponse(
    val data: List<Profile>
)

@Serializable
class ValentineListResponse(
    val data: List<IncomingValentine>
)