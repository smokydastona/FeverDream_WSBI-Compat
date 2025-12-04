package com.feverdream.respawn;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class Config {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;
    
    public static final ForgeConfigSpec.ConfigValue<String> TARGET_SERVER;
    public static final ForgeConfigSpec.BooleanValue ENABLE_REDIRECT;
    public static final ForgeConfigSpec.BooleanValue SHOW_MESSAGES;
    
    static {
        BUILDER.push("Feverdream Respawn Configuration");
        
        TARGET_SERVER = BUILDER
            .comment("The name/address of the server to redirect players to on respawn")
            .define("targetServer", "feverdream");
        
        ENABLE_REDIRECT = BUILDER
            .comment("Enable or disable the respawn redirect feature")
            .define("enableRedirect", true);
        
        SHOW_MESSAGES = BUILDER
            .comment("Show chat messages when redirecting players")
            .define("showMessages", true);
        
        BUILDER.pop();
        SPEC = BUILDER.build();
    }
    
    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SPEC, "feverdream-respawn.toml");
    }
}
