package ru.let.glowingapi.event

import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import ru.let.glowingapi.GlowingApiPlugin
import ru.let.glowingapi.GlowingTask
import ru.let.glowingapi.glowable.EntityGlowable
import ru.let.glowingapi.glowable.PlayerGlowable

class TaskListener(val task: GlowingTask) {

    fun onPlayerJoin(e: PlayerJoinEvent) {
        val player = e.player

        if (task.observers.any { it.name == player.name }) {
            task.observers.removeIf { it.name == player.name }
            task.addObserver(player)
        }

        if (task.targets.any { it.getId() == player.name }) {
            task.removeTarget(player.name)
                .addTarget(PlayerGlowable(player, task.team))
        }

        GlowingApiPlugin.plugin.server.scheduler.runTaskLater(GlowingApiPlugin.plugin, Runnable {
            if (player.isOnline) {
                task.resync()
            }
        }, 5L)
    }

    fun onPlayerQuit(e: PlayerQuitEvent) {
        val player = e.player

        if (task.observers.any { it.name == player.name }) {
            task.injector.uninject(player)
        }
    }

    fun onEntityDeath(e: EntityDeathEvent) {
        val entity = e.entity
        
        if (task.targets.any { it.getId() == entity.entityId.toString() }) {
            task.removeTarget(entity.entityId)
                .addTarget(EntityGlowable(entity, task.team))
        }
    }
}