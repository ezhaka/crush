package org.homepage.services

import kotlinx.serialization.Serializable

@Serializable
class IncomingValentine(
    val id: Long,
    val message: String,
)

@Serializable
class IncomingValentineListResponse(
    val data: List<IncomingValentine>
)