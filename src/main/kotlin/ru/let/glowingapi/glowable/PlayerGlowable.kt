package ru.let.glowingapi.glowable

import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Player
import ru.let.glowingapi.Glowable

class PlayerGlowable(val player: Player) : Glowable {
    override fun getIds(): Set<Int> {
        return setOf(player.entityId)
    }

    override fun getStartPackets(): List<Packet<*>> {
        val targetId = player.entityId

        val flagsAccessor = EntityDataAccessor(0, EntityDataSerializers.BYTE)
        val glowingValue = SynchedEntityData.DataValue.create(flagsAccessor, 0x40.toByte())
        val packet = ClientboundSetEntityDataPacket(targetId, listOf(glowingValue))

        return listOf(packet)
    }

    override fun getTempPackets(): List<Packet<*>> {
        return listOf()
    }

    override fun getEndPackets(): List<Packet<*>> {
        val targetId = player.entityId

        val targetNms = (player as CraftPlayer).handle
        val currentFlags = targetNms.entityData.get(EntityDataAccessor(0, EntityDataSerializers.BYTE))

        val cleanFlags = (currentFlags.toInt() and 0x40.inv()).toByte()

        val flagsAccessor = EntityDataAccessor(0, EntityDataSerializers.BYTE)
        val packet = ClientboundSetEntityDataPacket(
            targetId, listOf(
                SynchedEntityData.DataValue.create(flagsAccessor, cleanFlags)
            )
        )

        return listOf(packet)
    }
}