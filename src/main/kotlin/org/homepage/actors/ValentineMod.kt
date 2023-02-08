package org.homepage.actors

import org.homepage.IncomingValentine
import org.homepage.SpaceGlobalUserId
import space.jetbrains.api.runtime.SpaceAppInstance

sealed class ValentineMod(
    val spaceServerInstance: SpaceAppInstance,
    val id: Long,
    val userId: SpaceGlobalUserId,
) {
    class Created(
        spaceServerInstance: SpaceAppInstance,
        userId: SpaceGlobalUserId,
        var valentine: IncomingValentine
    ) : ValentineMod(spaceServerInstance, valentine.id, userId)

    class Read(
        spaceServerInstance: SpaceAppInstance,
        id: Long,
        userId: SpaceGlobalUserId,
    ) : ValentineMod(spaceServerInstance, id, userId)
}

