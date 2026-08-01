package ru.let.glowingapi.net

import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelOutboundHandlerAdapter
import io.netty.channel.ChannelPromise
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Player
import java.lang.reflect.Field
import java.util.concurrent.ConcurrentHashMap

class NettyInjector {

    companion object {
        private const val HANDLER_NAME = "glowing_packet_modifier"
    }

    private val methodField: Field by lazy {
        ClientboundSetPlayerTeamPacket::class.java.getDeclaredField("method").apply {
            isAccessible = true
        }
    }

    val ClientboundSetPlayerTeamPacket.packetMethod: Int
        get() = try {
            methodField.getInt(this)
        } catch (e: Exception) {
            -1
        }

    private val glowingTargets = ConcurrentHashMap<Channel, MutableSet<Int>>()
    private val glowingTeams = ConcurrentHashMap<Channel, MutableMap<String, String>>()

    fun registerTarget(observer: Player, targetEntityId: Int, targetName: String? = null, teamName: String? = null) {
        val channel = getChannel(observer)

        glowingTargets.computeIfAbsent(channel) { ConcurrentHashMap.newKeySet() }.add(targetEntityId)

        if (targetName != null && teamName != null) {
            glowingTeams.computeIfAbsent(channel) { ConcurrentHashMap() }[targetName] = teamName
        }

        if (channel.pipeline().get(HANDLER_NAME) == null) {
            injectChannel(channel)
        }
    }

    fun unregisterTarget(observer: Player, targetEntityId: Int, targetName: String? = null) {
        val channel = getChannel(observer)

        glowingTargets[channel]?.remove(targetEntityId)
        if (targetName != null) {
            glowingTeams[channel]?.remove(targetName)
        }

        if (glowingTargets[channel].isNullOrEmpty()) {
            uninject(observer)
        }
    }

    fun uninject(observer: Player) {
        val channel = getChannel(observer)
        glowingTargets.remove(channel)
        glowingTeams.remove(channel)

        if (channel.pipeline().get(HANDLER_NAME) != null) {
            channel.pipeline().remove(HANDLER_NAME)
        }
    }

    private fun injectChannel(channel: Channel) {
        channel.pipeline().addBefore("packet_handler", HANDLER_NAME, object : ChannelOutboundHandlerAdapter() {
            override fun write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise) {

                if (msg is ClientboundSetEntityDataPacket) {
                    val targets = glowingTargets[channel]

                    if (targets != null && targets.contains(msg.id)) {
                        val items = msg.packedItems
                        val flagsItem = items.find { it.id == 0 }

                        if (flagsItem != null && flagsItem.value is Byte) {
                            val currentFlags = flagsItem.value as Byte
                            val newFlags = (currentFlags.toInt() or 0x40).toByte()

                            val flagsAccessor = EntityDataAccessor(0, EntityDataSerializers.BYTE)
                            val newItems = items.map { item ->
                                if (item.id == 0) SynchedEntityData.DataValue.create(flagsAccessor, newFlags)
                                else item
                            }

                            val modifiedPacket = ClientboundSetEntityDataPacket(msg.id, newItems)
                            super.write(ctx, modifiedPacket, promise)
                            return
                        }
                    }
                }

                if (msg is ClientboundSetPlayerTeamPacket) {
                    val trackedTeams = glowingTeams[channel]

                    if (!trackedTeams.isNullOrEmpty()) {
                        val playersInPacket = msg.players

                        val hasTrackedPlayer = playersInPacket.any { trackedTeams.containsKey(it) }

                        if (hasTrackedPlayer) {
                            val method = msg.packetMethod

                            if (method == 4 || method == 1) {
                                promise.setSuccess()
                                return
                            }
                        }
                    }
                }

                super.write(ctx, msg, promise)
            }
        })
    }

    private fun getChannel(player: Player): Channel {
        return (player as CraftPlayer).handle.connection.connection.channel
    }
}