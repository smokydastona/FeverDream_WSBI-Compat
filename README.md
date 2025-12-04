# Feverdream Respawn - Server-Side Forge Mod

A server-side only Forge mod for Minecraft 1.20.1 that automatically redirects players to another server when they respawn after death.

## Features
- 100% server-side - players don't need to install anything except WaystoneButtonInjector
- Detects player deaths and respawns automatically
- Sends network packets to WaystoneButtonInjector client mod to trigger server redirect
- Configurable target server name

## Requirements
- Minecraft 1.20.1
- Forge 47.2.0+
- Players must have WaystoneButtonInjector mod installed on client

## Installation
1. Build the mod with `gradlew build`
2. Place the generated JAR from `build/libs/` into your server's `mods` folder
3. Restart the server

## How It Works
1. Mod tracks when players die using Forge events
2. When a player respawns, the mod detects it
3. Sends a custom network packet to the player's WaystoneButtonInjector client mod
4. Client mod receives packet and redirects to the configured server

## Integration with WaystoneButtonInjector
This mod sends packets on the channel `feverdreamrespawn:main` with a simple string payload containing the target server name (default: "feverdream").

Your WaystoneButtonInjector mod needs to:
1. Listen for packets on channel `feverdreamrespawn:main`
2. Read the target server name from the packet
3. Trigger the server connection redirect

## Building
```bash
gradlew build
```

The compiled JAR will be in `build/libs/`

## Configuration
To change the target server, edit the `RedirectPacket` constructor in `RedirectPacket.java` and change the default server name from "feverdream" to your server name.
