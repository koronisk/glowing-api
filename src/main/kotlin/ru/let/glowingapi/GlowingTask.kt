package ru.let.glowingapi

import org.bukkit.Bukkit
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import ru.let.glowingapi.event.TaskListener
import ru.let.glowingapi.net.NettyInjector

class GlowingTask {
    val injector = NettyInjector()

    val observers: MutableList<Player> = mutableListOf()
    val targets: MutableList<Glowable> = mutableListOf()

    private lateinit var bukkitTask: BukkitTask

    val listener: TaskListener = TaskListener(this)

    fun with(action: (GlowingTask) -> Unit): GlowingTask = apply {
        action(this)
    }

    fun addObserver(observer: Player): GlowingTask = apply {
        observers.add(observer)
    }

    fun removeObserver(observerName: String): GlowingTask = apply {
        val observer = observers.first { it.name == observerName }
        observers.remove(observer)
        if (observer.isOnline) desyncObserver(observer)
    }

    fun addTarget(target: Glowable): GlowingTask = apply {
        targets.add(target)
    }

    fun removeTarget(id: String): GlowingTask = apply {
        targets.removeAll { it.getId() == id }
    }

    fun removeTarget(entityId: Int): GlowingTask = apply {
        targets.removeAll { it.getEntityIds().contains(entityId) }
    }

    fun start() {
        GlowingApiPlugin.listener.subscribe(this)

        observers.forEach { observer ->
            resyncObserver(observer)
        }

        bukkitTask = GlowingApiPlugin.plugin.server.scheduler.runTaskTimer(GlowingApiPlugin.plugin, Runnable {
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
        GlowingApiPlugin.listener.unsubscribe(this)

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
        injector.uninject(observer)

        targets.forEach { target ->
            target.getEntityIds().forEach { id -> injector.inject(observer, id) }

            val observerConnection = (observer as CraftPlayer).handle.connection
            target.getStartPackets().forEach { packet ->
                observerConnection.send(packet)
            }
        }
    }

    private fun desyncObserver(observer: Player) {
        injector.uninject(observer)

        targets.forEach { target ->
            val observerConnection = (observer as CraftPlayer).handle.connection
            target.getEndPackets().forEach { packet ->
                observerConnection.send(packet)
            }
        }
    }
}