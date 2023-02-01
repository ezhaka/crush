package org.homepage.services

import kotlinx.serialization.Serializable

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