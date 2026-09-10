package com.bleachmod.common;
import com.bleachmod.common.quest.QuestRegistry;
import com.bleachmod.common.evolution.FormRegistry;
import net.minecraft.server.MinecraftServer;
import java.io.IOException;
/** Prepare both definitions before publishing on the server thread. */
public final class RegistryReload {
    private RegistryReload() {}
    public static void reload(MinecraftServer server) throws IOException {
        var forms=FormRegistry.prepare(server);
        var quests=QuestRegistry.prepare(server);
        for (var quest : quests.quests().values()) {
            for (var objective : quest.getObjectives()) {
                if (objective instanceof com.bleachmod.common.quest.objectives.ItemObjective item) {
                    var id = com.bleachmod.common.util.ResourceIds.parse(item.getItemId());
                    if (!net.minecraftforge.registries.ForgeRegistries.ITEMS.containsKey(id)) throw new IOException(quest.getQuestKey() + ": unknown item " + id);
                } else if (objective instanceof com.bleachmod.common.quest.objectives.KillObjective kill && !kill.getEntityId().startsWith("#")) {
                    var id = com.bleachmod.common.util.ResourceIds.parse(kill.getEntityId());
                    if (!net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.containsKey(id)) throw new IOException(quest.getQuestKey() + ": unknown entity " + id);
                }
            }
            for (var reward : quest.getRewards()) {
                var json = reward.toJson();
                if ("ITEM".equals(json.get("type").getAsString())) {
                    var id = com.bleachmod.common.util.ResourceIds.parse(json.get("item").getAsString());
                    if (!net.minecraftforge.registries.ForgeRegistries.ITEMS.containsKey(id) || "minecraft:air".equals(id.toString()))
                        throw new IOException(quest.getQuestKey() + ": unknown reward item " + id);
                }
            }
        }
        FormRegistry.installServer(forms);
        QuestRegistry.installServer(quests);
    }
}
