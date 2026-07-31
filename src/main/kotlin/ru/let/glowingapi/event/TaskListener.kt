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
        if (task.observers.any { it.player == e.player }) {
            task.removeObserver(e.player.name)
                .addObserver(e.player)
        }

        if (task.targets.any { it.getId() == e.player.name }) {
            task.removeTarget(e.player.name)
                .addTarget(PlayerGlowable(e.player))
        }

        GlowingApiPlugin.plugin.server.scheduler.runTaskLater(GlowingApiPlugin.plugin, Runnable {
            task.resync()
        }, 5L)
    }

    fun onPlayerQuit(e: PlayerQuitEvent) {
        if (task.observers.any { it.player == e.player })
            task.injector.uninject(e.player)
    }
    
    fun onEntityDeath(e: EntityDeathEvent) {
        if (task.targets.any { it.getId() == e.entity.entityId.toString() }) {
            task.removeTarget(e.entity.entityId)
                .addTarget(EntityGlowable(e.entity))
        }
    }
}