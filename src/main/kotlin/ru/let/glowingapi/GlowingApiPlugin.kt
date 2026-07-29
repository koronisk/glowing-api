package ru.let.glowingapi

import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerChatEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import ru.let.glowingapi.glowable.PlayerGlowable
import ru.let.glowingapi.net.NettyInjector

class GlowingApiPlugin : JavaPlugin(), Listener {
    companion object {
        lateinit var plugin: JavaPlugin
    }
    
    override fun onEnable() {
        plugin = this
        Bukkit.getPluginManager().registerEvents(this, this)
    }
    
    @EventHandler
    fun onChat(event: PlayerChatEvent) {
        val task = GlowingTask()
        
        task.addTarget(PlayerGlowable(event.player))
        
        Bukkit.getOnlinePlayers().forEach { 
            task.addObserver(it)
        }
        
        task.start()
    }
} 