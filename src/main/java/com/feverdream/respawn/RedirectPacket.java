package com.feverdream.respawn;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RedirectPacket {
    // You can add data here if you want to specify different servers
    // For now, it's a simple trigger packet
    private String targetServer;
    
    public RedirectPacket() {
        this.targetServer = "feverdream"; // Default server name
    }
    
    public RedirectPacket(String targetServer) {
        this.targetServer = targetServer;
    }
    
    // Encode packet data to buffer
    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(targetServer);
    }
    
    // Decode packet data from buffer
    public static RedirectPacket decode(FriendlyByteBuf buf) {
        return new RedirectPacket(buf.readUtf());
    }
    
    // Handle packet on client side (handled by WaystoneButtonInjector)
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // Client-side handling will be done by WaystoneButtonInjector mod
            // This method won't actually run on the server-only mod
        });
        context.setPacketHandled(true);
    }
    
    public String getTargetServer() {
        return targetServer;
    }
}
