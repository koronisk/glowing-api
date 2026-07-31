package ru.let.glowingapi

import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import ru.let.glowingapi.glowable.EntityGlowable
import ru.let.glowingapi.glowable.PlayerGlowable

class GlowingTaskBuilder private constructor(val task: GlowingTask) {
    companion object {
        fun setup(action: (GlowingTaskBuilder).() -> Unit): GlowingTask {
            val task = GlowingTask()
            val builder = GlowingTaskBuilder(task)

            action(builder)

            return task
        }
    }

    fun addObserver(player: Player) {
        task.addObserver(player)
    }

    fun addObservers(players: Collection<Player>) {
        players.forEach { task.addObserver(it) }
    }
    

    fun addPlayerTarget(player: Player) {
        task.addTarget(PlayerGlowable(player))
    }

    fun addPlayerTargets(players: Collection<Player>) {
        players.forEach { task.addTarget(PlayerGlowable(it)) }
    }


    fun addEntityTarget(entity: LivingEntity) {
        task.addTarget(EntityGlowable(entity))
    }

    fun addEntityTargets(entities: Collection<LivingEntity>) {
        entities.forEach { task.addTarget(EntityGlowable(it)) }
    }
}