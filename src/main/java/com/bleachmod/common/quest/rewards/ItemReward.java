package com.bleachmod.common.quest.rewards;

import com.bleachmod.common.data.PlayerData;
import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import com.bleachmod.common.util.ResourceIds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemReward extends QuestReward {
    private final String itemId;
    private final int count;

    public ItemReward(String itemId, int count) {
        this.itemId = itemId;
        this.count = Math.max(1, count);
    }

    @Override
    public void give(ServerPlayer player, PlayerData data) {
        Item item = ForgeRegistries.ITEMS.getValue(ResourceIds.parse(itemId));
        if (item == null || item == net.minecraft.world.item.Items.AIR) {
            throw new IllegalArgumentException("Unknown reward item: " + itemId);
        }
        ItemStack stack = new ItemStack(item, count);
        if (!player.getInventory().add(stack)) {
            ItemEntity dropped = player.drop(stack, false);
            if (dropped != null) {
                dropped.setNoPickUpDelay();
            }
        }
    }

    @Override
    public Component describe() {
        return Component.translatable("bleachmod.reward.item", count, itemId);
    }

    @Override
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", "ITEM");
        json.addProperty("item", itemId);
        json.addProperty("count", count);
        return json;
    }
}
