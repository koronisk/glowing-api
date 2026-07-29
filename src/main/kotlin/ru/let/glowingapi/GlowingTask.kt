package ru.let.glowingapi

import org.bukkit.Bukkit
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import ru.let.glowingapi.net.NettyInjector

class GlowingTask {
    private val injector = NettyInjector()

    private val observers: MutableList<Player> = mutableListOf()
    private val targets: MutableList<Glowable> = mutableListOf()

    private lateinit var bukkitTask: BukkitTask

    fun addObserver(observer: Player) {
        observers.add(observer)
    }

    fun addTarget(target: Glowable) {
        targets.add(target)
    }

    fun start() {
        observers.forEach { observer ->
            targets.forEach { target ->
                target.getIds().forEach { id -> injector.inject(observer, id) }

                val observerConnection = (observer as CraftPlayer).handle.connection
                target.getStartPackets().forEach { packet ->
                    observerConnection.send(packet)
                }
            }
        }

        bukkitTask = Bukkit.getScheduler().runTaskTimer(GlowingApiPlugin.plugin, Runnable {
            observers.forEach { observer ->
                targets.forEach { target ->
                    val observerConnection = (observer as CraftPlayer).handle.connection
                    target.getTempPackets().forEach { packet ->
                        observerConnection.send(packet)
                    }
                }
            }
        }, 0L, 20L)
    }

    fun end() {
        bukkitTask.cancel()

        observers.forEach { observer ->
            injector.uninject(observer)

            targets.forEach { target ->
                val observerConnection = (observer as CraftPlayer).handle.connection
                target.getEndPackets().forEach { packet ->
                    observerConnection.send(packet)
                }
            }
        }
    }
}