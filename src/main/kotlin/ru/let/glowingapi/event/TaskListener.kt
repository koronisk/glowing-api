package ru.let.glowingapi.event

import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import ru.let.glowingapi.GlowingApiPlugin
import ru.let.glowingapi.GlowingTask
import ru.let.glowingapi.glowable.PlayerGlowable

class TaskListener(val task: GlowingTask) {
    fun onPlayerJoin(event: PlayerJoinEvent) {
        if (task.observers.any { it.player == event.player }) {
            task.removeObserver(event.player.name)
                .addObserver(event.player)
        }

        if (task.targets.any { it.getId() == event.player.name }) {
            task.removeTarget(event.player.name)
                .addTarget(PlayerGlowable(event.player))
        }

        GlowingApiPlugin.plugin.server.scheduler.runTaskLater(GlowingApiPlugin.plugin, Runnable {
            task.resync()
        }, 5L)
    }

    fun onPlayerQuit(event: PlayerQuitEvent) {
        if (task.observers.any { it.player == event.player })
            task.injector.uninject(event.player)
    }
}