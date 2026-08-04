package ru.let.glowingapi

import net.minecraft.world.scores.PlayerTeam
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import ru.let.glowingapi.event.TaskListener
import ru.let.glowingapi.exts.getNmsConnection
import ru.let.glowingapi.glowable.PlayerGlowable

class GlowingTask(val plugin: GlowingApiPlugin) {
    val listener: TaskListener = TaskListener(this)
    lateinit var team: PlayerTeam

    private val observers: MutableSet<Player> = mutableSetOf()
    private val targets: MutableSet<Glowable> = mutableSetOf()

    private lateinit var bukkitTask: BukkitTask

    fun getObservers(): Set<Player> {
        return observers.toSet()
    }

    fun getTargets(): Set<Glowable> {
        return targets.toSet()
    }


    fun addObserver(observer: Player): GlowingTask = apply {
        observers.add(observer)
    }

    fun removeObserver(observerName: String): GlowingTask = apply {
        val observer = observers.firstOrNull { it.name == observerName } ?: return@apply
        observers.remove(observer)

        if (!observer.isOnline) return@apply

        targets.forEach { target ->
            val targetName = (target as? PlayerGlowable)?.getId()

            target.getEntityIds().forEach { entityId ->
                plugin.injector.unregisterTarget(observer, entityId, targetName)
            }

            target.getEndPackets().forEach { packet ->
                observer.getNmsConnection().send(packet)
            }
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
        if (removed.isEmpty()) return@apply

        targets.removeAll(removed.toSet())

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
                    target.getTempPackets().forEach { packet ->
                        observer.getNmsConnection().send(packet)
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

            target.getStartPackets().forEach { packet ->
                observer.getNmsConnection().send(packet)
            }
        }
    }

    fun desyncObserver(observer: Player) {
        plugin.injector.uninject(observer)

        if (observer.isOnline) {
            targets.forEach { target ->
                target.getEndPackets().forEach { packet ->
                    observer.getNmsConnection().send(packet)
                }
            }
        }
    }
}