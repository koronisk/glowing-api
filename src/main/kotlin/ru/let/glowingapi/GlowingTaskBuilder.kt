package ru.let.glowingapi

import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Scoreboard
import net.minecraft.world.scores.TeamColor
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import ru.let.glowingapi.glowable.EntityGlowable
import ru.let.glowingapi.glowable.PlayerGlowable
import java.util.Optional
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class GlowingTaskBuilder private constructor() {
    companion object {
        fun setup(action: (GlowingTaskBuilder).() -> Unit): GlowingTaskBuilder {
            val builder = GlowingTaskBuilder()

            action(builder)

            return builder
        }
    }

    var color: TeamColor = TeamColor.WHITE
    val observers: MutableSet<Player> = mutableSetOf()
    val playerTargets: MutableSet<Player> = mutableSetOf()
    val entityTargets: MutableSet<LivingEntity> = mutableSetOf()

    @OptIn(ExperimentalUuidApi::class)
    fun getTask(plugin: GlowingApiPlugin): GlowingTask {
        val task = GlowingTask(plugin)

        val scoreboard = Scoreboard()
        val teamName = Uuid.random().toString()
        val team = PlayerTeam(scoreboard, teamName)
        team.color = Optional.of(color)
        task.team = team

        observers.forEach { task.addObserver(it) }
        playerTargets.forEach { task.addTarget(PlayerGlowable(it, task.team)) }
        entityTargets.forEach { if (it !is Player) task.addTarget(EntityGlowable(it, task.team)) }

        return task
    }
}