package ru.let.glowingapi

import org.bukkit.plugin.java.JavaPlugin
import ru.let.glowingapi.event.GlobalListener
import ru.let.glowingapi.net.NettyInjector

class GlowingApiPlugin : JavaPlugin() {
    val injector: NettyInjector = NettyInjector()
    val listener: GlobalListener = GlobalListener()

    override fun onEnable() {
        server.pluginManager.registerEvents(listener, this)
    }

    fun initTask(action: (GlowingTaskBuilder).() -> Unit): GlowingTask {
        val builder = GlowingTaskBuilder.setup {
            action(this)
        }

        return builder.getTask(this)
    }
} 