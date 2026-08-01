package ru.let.glowingapi.glowable

import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.scores.PlayerTeam
import org.bukkit.craftbukkit.entity.CraftLivingEntity
import org.bukkit.entity.LivingEntity
import ru.let.glowingapi.Glowable

open class EntityGlowable(val entity: LivingEntity, val team: PlayerTeam) : Glowable {
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
        val packets = mutableListOf<Packet<*>>()
        
        packets.addAll(getStartPackets())
        
        team.players.add(entity.scoreboardEntryName)
        val createTeamPacket = ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true)
        packets.add(createTeamPacket)
        
        return packets
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