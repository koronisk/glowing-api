package ru.let.glowingapi.glowable

import net.minecraft.world.scores.PlayerTeam
import org.bukkit.entity.Player

class PlayerGlowable(val player: Player, team: PlayerTeam) : EntityGlowable(player, team) {
    override fun getId(): String {
        return player.name
    }
    
    override fun getEntityIds(): Set<Int> {
        return setOf(player.entityId)
    }
}