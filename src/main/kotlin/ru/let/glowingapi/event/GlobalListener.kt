package ru.let.glowingapi.event

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
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
    fun onPlayerJoin(event: PlayerJoinEvent) {
        subscribedTasks.forEach { task ->
            task.listener.onPlayerJoin(event)
        }
    }
    
    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        subscribedTasks.forEach { task ->
            task.listener.onPlayerQuit(event)
        }
    }
}