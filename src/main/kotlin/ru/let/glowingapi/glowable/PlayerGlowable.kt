package ru.let.glowingapi.glowable

import org.bukkit.entity.Player

class PlayerGlowable(val player: Player) : EntityGlowable(player) {
    override fun getId(): String {
        return player.name
    }
    
    override fun getEntityIds(): Set<Int> {
        return setOf(player.entityId)
    }
}