package ru.let.glowingapi.net

import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelOutboundHandlerAdapter
import io.netty.channel.ChannelPromise
import io.netty.channel.ChannelId
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Player

class NettyInjector {

    companion object {
        private const val HANDLER_NAME = "glowing_packet_modifier"
    }

    fun inject(observer: Player, targetEntityId: Int) {
        val channel = getChannel(observer)

        if (channel.pipeline().get(HANDLER_NAME) != null) return

        channel.pipeline().addBefore("packet_handler", HANDLER_NAME, object : ChannelOutboundHandlerAdapter() {
            override fun write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise) {
                if (msg is ClientboundSetEntityDataPacket && msg.id == targetEntityId) {
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

                        val modifiedPacket = ClientboundSetEntityDataPacket(targetEntityId, newItems)
                        super.write(ctx, modifiedPacket, promise)
                        return
                    }
                }
                super.write(ctx, msg, promise)
            }
        })
    }

    fun uninject(observer: Player) {
        val channel = getChannel(observer)
        if (channel.pipeline().get(HANDLER_NAME) != null) {
            channel.pipeline().remove(HANDLER_NAME)
        }
    }

    private fun getChannel(player: Player): Channel {
        return (player as CraftPlayer).handle.connection.connection.channel
    }
}