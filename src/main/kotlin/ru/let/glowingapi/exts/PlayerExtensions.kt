package ru.let.glowingapi.exts

import net.minecraft.server.network.ServerGamePacketListenerImpl
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Player

fun Player.getNmsConnection(): ServerGamePacketListenerImpl {
    return (this as CraftPlayer).handle.connection
}