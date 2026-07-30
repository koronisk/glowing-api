package ru.let.glowingapi

import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChatEvent
import org.bukkit.plugin.java.JavaPlugin
import ru.let.glowingapi.event.GlobalListener
import ru.let.glowingapi.glowable.PlayerGlowable

class GlowingApiPlugin : JavaPlugin(), Listener {
    companion object {
        lateinit var plugin: JavaPlugin
        lateinit var listener: GlobalListener
    }

    override fun onEnable() {
        plugin = this
        listener = GlobalListener()

        server.pluginManager.registerEvents(listener, this)
        server.pluginManager.registerEvents(this, this)
    }

    @EventHandler
    fun onChat(event: PlayerChatEvent) {
        val task = GlowingTask().with { task ->
            server.onlinePlayers.forEach { player ->
                task.addTarget(PlayerGlowable(player))
                task.addObserver(player)
            }
        }

        task.start()
    }
} 