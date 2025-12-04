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
    public static final ForgeConfigSpec.BooleanValue DEATH_MODE_ENABLED;
    public static final ForgeConfigSpec.BooleanValue RANDOM_MODE_ENABLED;
    public static final ForgeConfigSpec.DoubleValue RANDOM_CHANCE;
    
    static {
        BUILDER.push("Feverdream Respawn Configuration");
        
        TARGET_SERVER = BUILDER
            .comment("The name/address of the server to redirect players to on respawn")
            .define("targetServer", "feverdream");
        
        ENABLE_REDIRECT = BUILDER
            .comment("Master switch - enable or disable all redirect features")
            .define("enableRedirect", true);
        
        SHOW_MESSAGES = BUILDER
            .comment("Show chat messages when redirecting players")
            .define("showMessages", true);
        
        DEATH_MODE_ENABLED = BUILDER
            .comment("Enable death mode: redirect players on respawn after death")
            .define("deathModeEnabled", true);
        
        RANDOM_MODE_ENABLED = BUILDER
            .comment("Enable random mode: randomly redirect players while playing")
            .define("randomModeEnabled", false);
        
        RANDOM_CHANCE = BUILDER
            .comment("Chance per tick for random redirect (0.0 to 1.0). Recommended: 0.0001 = ~0.5% chance per second")
            .defineInRange("randomChance", 0.0001, 0.0, 1.0);
        
        BUILDER.pop();
        SPEC = BUILDER.build();
    }
    
    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SPEC, "feverdream-respawn.toml");
    }
}
