package ru.let.glowingapi.event

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import ru.let.glowingapi.GlowingTask

class GlobalListener : Listener {
    private val subscribedTasks = mutableSetOf<GlowingTask>() 
    
    fun subscribe(task: GlowingTask) {
        subscribedTasks.add(task)
    }
    
    fun unsubscribe(task: GlowingTask) {
        subscribedTasks.remove(task)
    }
    
    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        subscribedTasks.forEach { task ->
            task.listener.onPlayerJoin(e)
        }
    }
    
    @EventHandler
    fun onPlayerQuit(e: PlayerQuitEvent) {
        subscribedTasks.forEach { task ->
            task.listener.onPlayerQuit(e)
        }
    }

    @EventHandler
    fun onEntityDeath(e: EntityDeathEvent) {
        subscribedTasks.forEach { task ->
            task.listener.onEntityDeath(e)
        }
    }
}