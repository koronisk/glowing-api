package ru.let.glowingapi

import net.minecraft.world.scores.TeamColor
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChatEvent
import org.bukkit.plugin.java.JavaPlugin
import ru.let.glowingapi.event.GlobalListener
import ru.let.glowingapi.net.NettyInjector

class GlowingApiPlugin : JavaPlugin(), Listener {
    val injector: NettyInjector = NettyInjector()
    val listener: GlobalListener = GlobalListener()

    override fun onEnable() {
        server.pluginManager.registerEvents(listener, this)
        server.pluginManager.registerEvents(this, this)
    }

    @EventHandler
    fun onChat(e: PlayerChatEvent) {
        val players = server.onlinePlayers
        val entities = e.player.world.entities.filterIsInstance<LivingEntity>()

        val multiTask = GlowingMultiTask(this).setup {
            addTask {
                color = TeamColor.GREEN
                observers.add(e.player)

                playerTargets.addAll(players)
            }

            addTask {
                color = TeamColor.YELLOW
                observers.add(e.player)

                entityTargets.addAll(entities)
            }
        }

        multiTask.start()
    }

    fun initTask(action: (GlowingTaskBuilder).() -> Unit): GlowingTask {
        val builder = GlowingTaskBuilder.setup {
            action(this)
        }

        return builder.getTask(this)
    }
} 