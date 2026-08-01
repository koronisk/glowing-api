package ru.let.glowingapi

import net.minecraft.world.scores.TeamColor
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChatEvent
import org.bukkit.plugin.java.JavaPlugin
import ru.let.glowingapi.event.GlobalListener
import ru.let.glowingapi.glowable.EntityGlowable
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
    fun onChat(e: PlayerChatEvent) {
        val players = server.onlinePlayers
        val entities = e.player.world.entities.filterIsInstance<LivingEntity>()
        
        val task = GlowingTaskBuilder.setup {
            color = TeamColor.GREEN
            observers.addAll(players)

            playerTargets.addAll(players)
            entityTargets.addAll(entities)
        }

        task.start()
    }
} 