package com.feverdream.respawn;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RedirectPacket {
    private int buttonIndex;
    
    public RedirectPacket(int buttonIndex) {
        this.buttonIndex = buttonIndex;
    }
    
    // Encode packet data to buffer
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(buttonIndex);
    }
    
    // Decode packet data from buffer
    public static RedirectPacket decode(FriendlyByteBuf buf) {
        return new RedirectPacket(buf.readInt());
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
    
    public int getButtonIndex() {
        return buttonIndex;
    }
}
