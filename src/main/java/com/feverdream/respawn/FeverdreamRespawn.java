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
import java.util.Random;
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
        PROTOCOL_VERSION::equals,  // Must match client version exactly
        PROTOCOL_VERSION::equals   // Must match server version exactly
    );
    
    // Track player deaths
    private static final Map<UUID, Boolean> playerDeathMap = new HashMap<>();
    private static final Random random = new Random();
    
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
            LOGGER.info("[FEVERDREAM] Player {} died - marked for redirect", event.getEntity().getName().getString());
        }
    }
    
    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        // Check if death mode is enabled
        if (!Config.ENABLE_REDIRECT.get() || !Config.DEATH_MODE_ENABLED.get()) {
            LOGGER.info("[FEVERDREAM] Player respawned but death mode disabled (redirect={}, deathMode={})", 
                Config.ENABLE_REDIRECT.get(), Config.DEATH_MODE_ENABLED.get());
            return;
        }
        
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            UUID playerUUID = serverPlayer.getUUID();
            
            // Check if this respawn was from a death
            if (playerDeathMap.getOrDefault(playerUUID, false)) {
                LOGGER.info("[FEVERDREAM] Player {} respawned after death, sending redirect packet", 
                    serverPlayer.getName().getString());
                
                // Send packet to client mod to trigger server redirect
                sendRedirectPacket(serverPlayer);
                
                // Clear death flag
                playerDeathMap.put(playerUUID, false);
            } else {
                LOGGER.info("[FEVERDREAM] Player {} respawned but wasn't marked as dead", 
                    serverPlayer.getName().getString());
            }
        }
    }
    
    @SubscribeEvent
    public void onPlayerTick(net.minecraftforge.event.TickEvent.PlayerTickEvent event) {
        // Check if random mode is enabled
        if (!Config.ENABLE_REDIRECT.get() || !Config.RANDOM_MODE_ENABLED.get()) {
            return;
        }
        
        // Only check on server side
        if (event.side.isClient()) {
            return;
        }
        
        // Only check during the start phase to avoid double-checking
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.START) {
            return;
        }
        
        if (event.player instanceof ServerPlayer serverPlayer) {
            // Only trigger when player is sleeping
            if (!serverPlayer.isSleeping()) {
                return;
            }
            
            // Check random chance while sleeping
            double chance = Config.RANDOM_CHANCE.get();
            if (random.nextDouble() < chance) {
                LOGGER.info("Random redirect triggered for sleeping player {}", 
                    serverPlayer.getName().getString());
                sendRedirectPacket(serverPlayer);
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
            // Get button index and convert to secret button ID (6-11)
            int buttonIndex = Config.BUTTON_INDEX.get();
            int secretButtonId = buttonIndex + 6; // 0->6, 1->7, ..., 5->11
            
            // Get prefix (death or sleep)
            String prefix = Config.REDIRECT_PREFIX.get();
            
            // Format: "prefix:buttonId" (e.g., "death:6" or "sleep:7")
            String serverName = prefix + ":" + secretButtonId;
            
            RedirectPacket packet = new RedirectPacket(serverName);
            
            // Send packet to specific player
            NETWORK.sendTo(packet, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
            
            LOGGER.info("[FEVERDREAM] Sent redirect packet to player {} with data: {}", 
                player.getName().getString(), serverName);
        } catch (Exception e) {
            LOGGER.error("Failed to send redirect packet to player {}", 
                player.getName().getString(), e);
        }
    }
}
