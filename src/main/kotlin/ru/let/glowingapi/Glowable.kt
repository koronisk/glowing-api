package ru.let.glowingapi

import net.minecraft.network.protocol.Packet

interface Glowable {
    fun getIds(): Set<Int>
    
    fun getStartPackets(): List<Packet<*>>
    fun getTempPackets(): List<Packet<*>>
    fun getEndPackets(): List<Packet<*>>
}