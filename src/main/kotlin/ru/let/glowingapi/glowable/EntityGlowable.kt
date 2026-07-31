package ru.let.glowingapi.glowable

import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import org.bukkit.craftbukkit.entity.CraftLivingEntity
import org.bukkit.entity.LivingEntity
import ru.let.glowingapi.Glowable

open class EntityGlowable(val entity: LivingEntity) : Glowable {
    override fun getId(): String {
        return entity.entityId.toString()
    }
    
    override fun getEntityIds(): Set<Int> {
        return setOf(entity.entityId)
    }

    override fun getStartPackets(): List<Packet<*>> {
        val targetId = entity.entityId

        val flagsAccessor = EntityDataAccessor(0, EntityDataSerializers.BYTE)
        val glowingValue = SynchedEntityData.DataValue.create(flagsAccessor, 0x40.toByte())
        val packet = ClientboundSetEntityDataPacket(targetId, listOf(glowingValue))

        return listOf(packet)
    }

    override fun getTempPackets(): List<Packet<*>> {
        return getStartPackets()
    }

    override fun getEndPackets(): List<Packet<*>> {
        val targetId = entity.entityId

        val targetNms = (entity as CraftLivingEntity).handle
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