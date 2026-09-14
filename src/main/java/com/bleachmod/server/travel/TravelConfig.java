package com.bleachmod.server.travel;

import com.bleachmod.common.data.TravelData;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import java.io.IOException;
import java.nio.file.*;

/** Per-world settings, published only after complete validation. */
public record TravelConfig(boolean enabled, float entryCost, int cooldownTicks, TravelData.Location arrivalOverride) {
    public TravelConfig {
        if (!Float.isFinite(entryCost) || entryCost < 0 || entryCost > 1_000_000 || cooldownTicks < 0 || cooldownTicks > 72_000)
            throw new IllegalArgumentException("Invalid travel cost/cooldown");
        if (arrivalOverride != null && !arrivalOverride.dimension().equals(SoulSociety.ID))
            throw new IllegalArgumentException("Arrival must be in Soul Society");
    }
    public static TravelConfig load(MinecraftServer server) throws IOException {
        Path path = server.getWorldPath(LevelResource.ROOT).resolve("bleachmod/travel/settings.json");
        var gson = new GsonBuilder().setPrettyPrinting().create();
        if (!Files.exists(path)) {
            Files.createDirectories(path.getParent());
            Files.writeString(path, gson.toJson(new TravelConfig(true, 20, 100, null)));
        }
        if (Files.size(path) > 16_384) throw new IOException("Travel settings too large");
        try (var reader = Files.newBufferedReader(path)) {
            var json = com.google.gson.JsonParser.parseReader(reader).getAsJsonObject();
            if (!json.has("enabled") || !json.has("entryCost") || !json.has("cooldownTicks")) throw new IOException("Missing travel settings");
            return gson.fromJson(json, TravelConfig.class);
        } catch (RuntimeException e) { throw new IOException("Invalid travel settings", e); }
    }
}
