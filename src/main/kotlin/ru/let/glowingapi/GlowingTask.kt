package ru.let.glowingapi

import net.minecraft.world.scores.PlayerTeam
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask
import ru.let.glowingapi.event.TaskListener
import ru.let.glowingapi.glowable.PlayerGlowable
import ru.let.glowingapi.net.NettyInjector

class GlowingTask(val plugin: GlowingApiPlugin) {
    val observers: MutableSet<Player> = mutableSetOf()
    val targets: MutableSet<Glowable> = mutableSetOf()

    lateinit var team: PlayerTeam

    private lateinit var bukkitTask: BukkitTask

    val listener: TaskListener = TaskListener(this)

    fun addObserver(observer: Player): GlowingTask = apply {
        observers.add(observer)
    }

    fun removeObserver(observerName: String): GlowingTask = apply {
        val observer = observers.firstOrNull { it.name == observerName }?.let {
            observers.remove(it)
            if (it.isOnline) desyncObserver(it)
        }
    }

    fun addTarget(target: Glowable): GlowingTask = apply {
        targets.add(target)
    }

    fun removeTarget(id: String): GlowingTask = apply {
        val removed = targets.filter { it.getId() == id }
        targets.removeAll(removed.toSet())

        observers.forEach { observer ->
            removed.forEach { target ->
                target.getEntityIds().forEach { entityId ->
                    val targetName = (target as? PlayerGlowable)?.getId()
                    plugin.injector.unregisterTarget(observer, entityId, targetName)
                }
            }
        }
    }

    fun removeTarget(entityId: Int): GlowingTask = apply {
        val removed = targets.filter { it.getEntityIds().contains(entityId) }
        targets.removeAll(removed)

        observers.forEach { observer ->
            removed.forEach { target ->
                val targetName = (target as? PlayerGlowable)?.getId()
                plugin.injector.unregisterTarget(observer, entityId, targetName)
            }
        }
    }

    fun start() {
        plugin.listener.subscribe(this)

        observers.forEach { observer ->
            resyncObserver(observer)
        }

        bukkitTask = plugin.server.scheduler.runTaskTimer(plugin, Runnable {
            observers.forEach { observer ->
                targets.forEach { target ->
                    val observerConnection = (observer as CraftPlayer).handle.connection
                    target.getTempPackets().forEach { packet ->
                        observerConnection.send(packet)
                    }
                }
            }
        }, 0L, 100L)
    }

    fun end() {
        plugin.listener.unsubscribe(this)

        bukkitTask.cancel()

        observers.forEach { observer ->
            desyncObserver(observer)
        }
    }

    fun resync() {
        observers.forEach { observer ->
            resyncObserver(observer)
        }
    }

    private fun resyncObserver(observer: Player) {
        if (!observer.isOnline) return

        targets.forEach { target ->
            val targetName = (target as? PlayerGlowable)?.getId()

            target.getEntityIds().forEach { id ->
                plugin.injector.registerTarget(observer, id, targetName, team.name)
            }

            val observerConnection = (observer as CraftPlayer).handle.connection
            target.getStartPackets().forEach { packet ->
                observerConnection.send(packet)
            }
        }
    }

    private fun desyncObserver(observer: Player) {
        plugin.injector.uninject(observer)

        if (observer.isOnline) {
            targets.forEach { target ->
                val observerConnection = (observer as CraftPlayer).handle.connection
                target.getEndPackets().forEach { packet ->
                    observerConnection.send(packet)
                }
            }
        }
    }
}