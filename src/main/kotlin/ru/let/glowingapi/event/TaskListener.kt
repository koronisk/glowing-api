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

        if (task.getObservers().any { it.name == player.name }) {
            task.removeObserver(player.name)
            task.addObserver(player)
        }

        if (task.getTargets().any { it.getId() == player.name }) {
            task.removeTarget(player.name)
                .addTarget(PlayerGlowable(player, task.team))
        }

        task.plugin.server.scheduler.runTaskLater(task.plugin, Runnable {
            if (player.isOnline) {
                task.resync()
            }
        }, 5L)
    }

    fun onPlayerQuit(e: PlayerQuitEvent) {
        task.desyncObserver(e.player)
    }

    fun onEntityDeath(e: EntityDeathEvent) {
        task.removeTarget(e.entity.entityId)
    }
}