package com.feverdream.respawn;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod(FeverdreamRespawn.MODID)
public class FeverdreamRespawn {
    public static final String MODID = "feverdreamrespawn";
    private static final Logger LOGGER = LoggerFactory.getLogger(FeverdreamRespawn.class);
    
    // Network channel to communicate with client mod
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel NETWORK = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(MODID, "main"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );
    
    // Track player deaths
    private static final Map<UUID, Boolean> playerDeathMap = new HashMap<>();
    
    public FeverdreamRespawn() {
        MinecraftForge.EVENT_BUS.register(this);
        
        // Register config
        Config.register();
        
        // Register packet
        NETWORK.registerMessage(0, RedirectPacket.class, 
            RedirectPacket::encode, 
            RedirectPacket::decode, 
            RedirectPacket::handle);
        
        LOGGER.info("Feverdream Respawn mod initialized");
    }
    
    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            // Mark player as having died
            playerDeathMap.put(event.getEntity().getUUID(), true);
            LOGGER.debug("Player {} died", event.getEntity().getName().getString());
        }
    }
    
    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            UUID playerUUID = serverPlayer.getUUID();
            
            // Check if this respawn was from a death and redirect is enabled
            if (Config.ENABLE_REDIRECT.get() && playerDeathMap.getOrDefault(playerUUID, false)) {
                LOGGER.info("Player {} respawned after death, sending redirect packet", 
                    serverPlayer.getName().getString());
                
                // Send packet to client mod to trigger server redirect
                sendRedirectPacket(serverPlayer);
                
                // Clear death flag
                playerDeathMap.put(playerUUID, false);
            }
        }
    }
    
    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        // Clean up tracking data when player leaves
        playerDeathMap.remove(event.getEntity().getUUID());
    }
    
    private void sendRedirectPacket(ServerPlayer player) {
        try {
            // Create redirect packet with configured server name
            String targetServer = Config.TARGET_SERVER.get();
            RedirectPacket packet = new RedirectPacket(targetServer);
            
            // Send packet to specific player
            NETWORK.sendTo(packet, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
            
            LOGGER.info("Sent redirect packet to player {} for server {}", 
                player.getName().getString(), targetServer);
        } catch (Exception e) {
            LOGGER.error("Failed to send redirect packet to player {}", 
                player.getName().getString(), e);
        }
    }
}
